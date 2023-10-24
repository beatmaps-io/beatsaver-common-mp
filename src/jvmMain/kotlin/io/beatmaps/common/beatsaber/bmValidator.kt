package io.beatmaps.common.beatsaber

import kotlinx.serialization.SerialName
import org.valiktor.Constraint
import org.valiktor.ConstraintViolation
import org.valiktor.ConstraintViolationException
import org.valiktor.i18n.ConstraintViolationMessage
import org.valiktor.i18n.MessageBundle
import org.valiktor.i18n.interpolate
import java.util.Locale
import kotlin.reflect.KProperty1

inline fun <E> validate(obj: E, block: BMValidator<E>.(E) -> Unit): E {
    val validator = BMValidator(obj).apply { block(obj) }
    if (validator.constraintViolations.isNotEmpty()) {
        throw ConstraintViolationException(validator.constraintViolations)
    }
    return obj
}

class BMValidator<E>(private val obj: E) {
    val constraintViolations = mutableSetOf<ConstraintViolation>()

    @JvmName("validate")
    fun <T> validate(property: KProperty1<E, T?>): BMProperty<T?> = BMProperty(obj, property)

    @JvmName("validateIterable")
    fun <T> validate(property: KProperty1<E, Iterable<T>?>): BMProperty<Iterable<T>?> = BMProperty(obj, property)

    @JvmName("validateArray")
    fun <T> validate(property: KProperty1<E, Array<T>?>): BMProperty<Array<T>?> = BMProperty(obj, property)

    open inner class BMProperty<T>(val obj: E, val property: KProperty1<E, T?>) {
        fun validate(constraint: (T?) -> Constraint, isValid: (T?) -> Boolean): BMProperty<T> {
            val value = this.property.get(this.obj)
            if (!isValid(value)) {
                this@BMValidator.constraintViolations += BMConstraintViolation(
                    propertyInfo = listOf(this.property.toInfo()),
                    value = value,
                    constraint = constraint(value)
                )
            }
            return this
        }

        fun validate(constraint: Constraint, isValid: (T?) -> Boolean): BMProperty<T> =
            validate({ constraint }, isValid)

        fun addConstraintViolations(constraintViolations: Iterable<ConstraintViolation>) {
            this@BMValidator.constraintViolations += constraintViolations
        }
    }
}

data class BMConstraintViolationMessage(
    val propertyInfo: List<BMPropertyInfo>,
    override val value: Any? = null,
    override val constraint: Constraint,
    override val message: String
) : ConstraintViolation by BMConstraintViolation(propertyInfo, value, constraint), ConstraintViolationMessage {
    override val property = propertyInfo.joinToString(".") {
        val name = it.descriptor?.let { d -> "${it.name}($d)" } ?: it.name
        it.index?.let { idx -> "$name[$idx]" } ?: name
    }
}

data class BMConstraintViolation(
    val propertyInfo: List<BMPropertyInfo>,
    override val value: Any? = null,
    override val constraint: Constraint
) : ConstraintViolation {
    override val property = propertyInfo.joinToString(".") {
        val name = it.descriptor?.let { d -> "${it.name}($d)" } ?: it.name
        it.index?.let { idx -> "$name[$idx]" } ?: name
    }

    fun addParent(property: BMPropertyInfo) = copy(
        propertyInfo = listOf(property) + propertyInfo
    )

    fun toMessage(baseName: String = constraint.messageBundle, locale: Locale = Locale.getDefault()): ConstraintViolationMessage =
        BMConstraintViolationMessage(
            propertyInfo = propertyInfo,
            value = value,
            constraint = constraint,
            message = interpolate(
                MessageBundle(
                    baseName = baseName,
                    locale = locale,
                    fallbackBaseName = constraint.messageBundle,
                    fallbackLocale = Locale.getDefault()),
                constraint.messageKey,
                constraint.messageParams))
}

fun <E, T> KProperty1<E, T>.toInfo(index: Int? = null): BMPropertyInfo {
    val serialName = annotations.filterIsInstance<SerialName>()
    val validationName = annotations.filterIsInstance<ValidationName>()

    return BMPropertyInfo(
        validationName.firstOrNull()?.value ?: serialName.firstOrNull()?.value ?: name,
        if (validationName.isEmpty() && serialName.isNotEmpty()) name else null,
        index
    )
}
