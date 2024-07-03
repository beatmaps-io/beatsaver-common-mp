package io.beatmaps.common.beatsaber

import io.beatmaps.common.OptionalProperty
import org.valiktor.ConstraintViolation
import org.valiktor.DefaultConstraintViolation
import org.valiktor.constraints.Between
import org.valiktor.constraints.Equals
import org.valiktor.constraints.GreaterOrEqual
import org.valiktor.constraints.In
import org.valiktor.constraints.Less
import org.valiktor.constraints.LessOrEqual
import org.valiktor.constraints.Matches
import org.valiktor.constraints.NotBlank
import org.valiktor.constraints.NotEmpty
import org.valiktor.constraints.NotNull
import kotlin.reflect.KProperty1

interface Validatable<T : Validatable<T>> {
    fun validate(validator: BMValidator<T>): BMValidator<T>
}

fun <E, Q, T : OptionalProperty<Q>> BMValidator<E>.BMProperty<T?>.existsBefore(ver: Version, requiredVersion: Version): BMValidator<E>.BMProperty<T?> =
    this.validate(NodePresent) { it == null || ver >= requiredVersion || it !is OptionalProperty.NotPresent }

fun <E, Q, T : OptionalProperty<Q>> BMValidator<E>.BMProperty<T?>.notExistsBefore(ver: Version, requiredVersion: Version): BMValidator<E>.BMProperty<T?> =
    this.validate(NodeNotPresent) { it == null || ver >= requiredVersion || it is OptionalProperty.NotPresent }

fun <E, Q, T : OptionalProperty<Q>> BMValidator<E>.BMProperty<T?>.onlyExistsBefore(ver: Version, requiredVersion: Version) =
    existsBefore(ver, requiredVersion).notExistsAfter(ver, requiredVersion)

fun <E, Q, T : OptionalProperty<Q>> BMValidator<E>.BMProperty<T?>.existsBetween(ver: Version, startVersion: Version, endVersion: Version): BMValidator<E>.BMProperty<T?> =
    this.validate(NodePresent) { it == null || ver < startVersion || ver >= endVersion || it !is OptionalProperty.NotPresent }

fun <E, Q, T : OptionalProperty<Q>> BMValidator<E>.BMProperty<T?>.existsAfter(ver: Version, requiredVersion: Version): BMValidator<E>.BMProperty<T?> =
    this.validate(NodePresent) { it == null || ver < requiredVersion || it !is OptionalProperty.NotPresent }

fun <E, Q, T : OptionalProperty<Q>> BMValidator<E>.BMProperty<T?>.notExistsAfter(ver: Version, requiredVersion: Version): BMValidator<E>.BMProperty<T?> =
    this.validate(NodeNotPresent) { it == null || ver < requiredVersion || it is OptionalProperty.NotPresent }

fun <E, Q, T : OptionalProperty<Q>> BMValidator<E>.BMProperty<T?>.onlyExistsAfter(ver: Version, requiredVersion: Version) =
    existsAfter(ver, requiredVersion).notExistsBefore(ver, requiredVersion)

fun <E, Q, T : OptionalProperty<Q>> BMValidator<E>.BMProperty<T?>.exists(): BMValidator<E>.BMProperty<T?> =
    this.validate(NodePresent) { it == null || it !is OptionalProperty.NotPresent }

fun <E, Q, T : OptionalProperty<Q>> BMValidator<E>.BMProperty<T?>.notExists(): BMValidator<E>.BMProperty<T?> =
    this.validate(NodeNotPresent) { it == null || it is OptionalProperty.NotPresent }

fun <E, Q, T : OptionalProperty<Q>> BMValidator<E>.BMProperty<T?>.correctType(): BMValidator<E>.BMProperty<T?> =
    this.validate(CorrectType) { it == null || it !is OptionalProperty.WrongType }

fun <E, Q, T : OptionalProperty<Q>> BMValidator<E>.BMProperty<T?>.optionalNotNull(): BMValidator<E>.BMProperty<T?> =
    this.validate(NotNull) { it != null && it.validate { q -> q != null } }

fun <E, Q : Any, T : OptionalProperty<Iterable<OptionalProperty<Q?>>?>> BMValidator<E>.BMProperty<T?>.isNotEmpty(): BMValidator<E>.BMProperty<T?> =
    this.validate(NotEmpty) { it != null && it.validate { q -> q == null || q.count() > 0 } }

fun <E, T : OptionalProperty<Float?>> BMValidator<E>.BMProperty<T?>.isZero(): BMValidator<E>.BMProperty<T?> =
    this.validate(Equals(0f)) { it != null && it.validate { q -> q == null || q == 0f } }

@JvmName("flotIsPositiveOrZero")
fun <E, T : OptionalProperty<Float?>> BMValidator<E>.BMProperty<T?>.isPositiveOrZero(): BMValidator<E>.BMProperty<T?> =
    isGreaterThanOrEqualTo(0f)

@JvmName("intIsPositiveOrZero")
fun <E, T : OptionalProperty<Int?>> BMValidator<E>.BMProperty<T?>.isPositiveOrZero(): BMValidator<E>.BMProperty<T?> =
    isGreaterThanOrEqualTo(0)

fun <E, Q : Comparable<Q>, T : OptionalProperty<Q?>> BMValidator<E>.BMProperty<T?>.isGreaterThanOrEqualTo(value: Q): BMValidator<E>.BMProperty<T?> =
    this.validate(GreaterOrEqual(value)) { it != null && it.validate { q -> q == null || q >= value } }

fun <E, Q : Comparable<Q>, T : OptionalProperty<Q?>> BMValidator<E>.BMProperty<T?>.isLessThan(value: Q): BMValidator<E>.BMProperty<T?> =
    this.validate(Less(value)) { it != null && it.validate { q -> q == null || q < value } }

fun <E, Q : Comparable<Q>, T : OptionalProperty<Q?>> BMValidator<E>.BMProperty<T?>.isLessThanOrEqualTo(value: Q): BMValidator<E>.BMProperty<T?> =
    this.validate(LessOrEqual(value)) { it != null && it.validate { q -> q == null || q <= value } }

fun <E, T : OptionalProperty<String?>> BMValidator<E>.BMProperty<T?>.isNotBlank(): BMValidator<E>.BMProperty<T?> =
    this.validate(NotBlank) { it != null && it.validate { q -> q == null || q.isNotBlank() } }

inline fun <E, reified Q : Comparable<Q>, T : OptionalProperty<Q?>> BMValidator<E>.BMProperty<T?>.isBetween(start: Q, end: Q): BMValidator<E>.BMProperty<T?> =
    this.validate(Between(start, end)) {
        it == null || it.validate { q ->
            q == null || q in start.rangeTo(end)
        }
    }

fun <E, T : OptionalProperty<String?>> BMValidator<E>.BMProperty<T?>.matches(regex: Regex): BMValidator<E>.BMProperty<T?> =
    this.validate(Matches(regex)) { it == null || it.validate { q -> q == null || q.matches(regex) } }

fun <E, P, Q : Iterable<P>, T : OptionalProperty<P?>> BMValidator<E>.BMProperty<T?>.isIn(values: Q): BMValidator<E>.BMProperty<T?> =
    this.validate(In(values)) {
        it == null || it.validate { q ->
            values.contains(q)
        }
    }

fun <E, Q : Any, T : OptionalProperty<Q?>> BMValidator<E>.BMProperty<T?>.isIn(vararg values: Q?): BMValidator<E>.BMProperty<T?> =
    this.validate(In(values.toSet())) {
        it == null || it.validate { q ->
            values.contains(q)
        }
    }

fun <E, Q : Any, T : OptionalProperty<Iterable<OptionalProperty<Q?>>?>> BMValidator<E>.BMProperty<T?>.validateWith(
    block: (BMValidator<Q>) -> Unit,
    wrongTypesAllowed: Boolean = false,
    nullsAllowed: Boolean = false
) = this.validateForEach(wrongTypesAllowed, nullsAllowed) {
    block(this)
}

fun <E, Q : Any, T : OptionalProperty<Iterable<OptionalProperty<Q?>>?>> BMValidator<E>.BMProperty<T?>.validateEach(
    wrongTypesAllowed: Boolean = false,
    nullsAllowed: Boolean = false
) = validateForEach(wrongTypesAllowed, nullsAllowed) {
    // Required
}

@JvmName("validateEachAuto")
fun <E, Q : Validatable<Q>, T : OptionalProperty<Iterable<OptionalProperty<Q?>>?>> BMValidator<E>.BMProperty<T?>.validateEach(
    wrongTypesAllowed: Boolean = false,
    nullsAllowed: Boolean = false
) = validateForEach(wrongTypesAllowed, nullsAllowed) {
    it.validate(this)
}

fun <E, Q : Any, T : OptionalProperty<Iterable<OptionalProperty<Q?>>?>> BMValidator<E>.BMProperty<T?>.validateForEach(
    wrongTypesAllowed: Boolean = false,
    nullsAllowed: Boolean = false,
    block: BMValidator<Q>.(Q) -> Unit
): BMValidator<E>.BMProperty<T?> {
    this.property.get(this.obj)?.validate { q ->
        q?.forEachIndexed { index, value ->
            if (!wrongTypesAllowed && value is OptionalProperty.WrongType) {
                addConstraintViolations(
                    listOf(
                        BMConstraintViolation(
                            propertyInfo = listOf(this.property.toInfo(index)),
                            value = value,
                            constraint = CorrectType
                        )
                    )
                )
            }

            if (!nullsAllowed && value is OptionalProperty.Present<*> && value.value == null) {
                addConstraintViolations(
                    listOf(
                        BMConstraintViolation(
                            propertyInfo = listOf(this.property.toInfo(index)),
                            value = value,
                            constraint = NotNull
                        )
                    )
                )
            }

            val r = value.orNull() ?: return@forEachIndexed

            addConstraintViolations(
                BMValidator(r).apply { block(r) }.constraintViolations.map { it.addParent(this.property, index) }
            )
        }

        true
    }
    return this
}

fun <E, Q : Validatable<Q>, T : OptionalProperty<Q?>> BMValidator<E>.BMProperty<T?>.validate() =
    validateOptional { it.validate(this) }

inline fun <E, Q : Any, T : OptionalProperty<Q?>> BMValidator<E>.BMProperty<T?>.validateOptional(block: BMValidator<Q>.(Q) -> Unit): BMValidator<E>.BMProperty<T?> {
    val value = this.property.get(this.obj)?.orNull()
    if (value != null) {
        this.addConstraintViolations(
            BMValidator(value).apply { block(value) }.constraintViolations.map { it.addParent(this.property) }
        )
    }
    return this
}

fun <T, V> ConstraintViolation.addParent(property: KProperty1<T, V>, index: Int? = null) = addParent(property.toInfo(index))

fun ConstraintViolation.addParent(property: BMPropertyInfo) = when (this) {
    is BMConstraintViolation -> this.addParent(property)
    is DefaultConstraintViolation -> this.addParent(property)
    else -> throw IllegalArgumentException("Unknown constraint type")
}

fun DefaultConstraintViolation.addParent(property: BMPropertyInfo) = DefaultConstraintViolation(
    property = "${property.name}.${this.property}",
    value = this.value,
    constraint = this.constraint
)
