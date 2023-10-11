package io.beatmaps.common.beatsaber

import io.beatmaps.common.OptionalProperty
import org.valiktor.DefaultConstraintViolation
import org.valiktor.Validator
import org.valiktor.constraints.Between
import org.valiktor.constraints.Equals
import org.valiktor.constraints.GreaterOrEqual
import org.valiktor.constraints.In
import org.valiktor.constraints.Less
import org.valiktor.constraints.Matches
import org.valiktor.constraints.NotBlank
import org.valiktor.constraints.NotEmpty
import org.valiktor.constraints.NotNull

fun <E, Q, T : OptionalProperty<Q>> Validator<E>.Property<T?>.existsBefore(ver: Version, requiredVersion: Version): Validator<E>.Property<T?> =
    this.validate(NodePresent) { it == null || ver >= requiredVersion || it !is OptionalProperty.NotPresent }

fun <E, Q, T : OptionalProperty<Q>> Validator<E>.Property<T?>.notExistsBefore(ver: Version, requiredVersion: Version): Validator<E>.Property<T?> =
    this.validate(NodeNotPresent) { it == null || ver >= requiredVersion || it is OptionalProperty.NotPresent }

fun <E, Q, T : OptionalProperty<Q>> Validator<E>.Property<T?>.onlyExistsBefore(ver: Version, requiredVersion: Version) =
    existsBefore(ver, requiredVersion).notExistsAfter(ver, requiredVersion)

fun <E, Q, T : OptionalProperty<Q>> Validator<E>.Property<T?>.existsBetween(ver: Version, startVersion: Version, endVersion: Version): Validator<E>.Property<T?> =
    this.validate(NodePresent) { it == null || ver < startVersion || ver >= endVersion || it !is OptionalProperty.NotPresent }

fun <E, Q, T : OptionalProperty<Q>> Validator<E>.Property<T?>.existsAfter(ver: Version, requiredVersion: Version): Validator<E>.Property<T?> =
    this.validate(NodePresent) { it == null || ver < requiredVersion || it !is OptionalProperty.NotPresent }

fun <E, Q, T : OptionalProperty<Q>> Validator<E>.Property<T?>.notExistsAfter(ver: Version, requiredVersion: Version): Validator<E>.Property<T?> =
    this.validate(NodeNotPresent) { it == null || ver < requiredVersion || it is OptionalProperty.NotPresent }

fun <E, Q, T : OptionalProperty<Q>> Validator<E>.Property<T?>.onlyExistsAfter(ver: Version, requiredVersion: Version) =
    existsAfter(ver, requiredVersion).notExistsBefore(ver, requiredVersion)

fun <E, Q, T : OptionalProperty<Q>> Validator<E>.Property<T?>.exists(): Validator<E>.Property<T?> =
    this.validate(NodePresent) { it == null || it !is OptionalProperty.NotPresent }

fun <E, Q, T : OptionalProperty<Q>> Validator<E>.Property<T?>.notExists(): Validator<E>.Property<T?> =
    this.validate(NodeNotPresent) { it == null || it is OptionalProperty.NotPresent }

fun <E, Q, T : OptionalProperty<Q>> Validator<E>.Property<T?>.correctType(): Validator<E>.Property<T?> =
    this.validate(CorrectType) { it == null || it !is OptionalProperty.WrongType }

fun <E, Q, T : OptionalProperty<Q>> Validator<E>.Property<T?>.optionalNotNull(): Validator<E>.Property<T?> =
    this.validate(NotNull) { it != null && it.validate { q -> q != null } }

fun <E, Q : Any, T : OptionalProperty<Iterable<OptionalProperty<Q?>>?>> Validator<E>.Property<T?>.isNotEmpty(): Validator<E>.Property<T?> =
    this.validate(NotEmpty) { it != null && it.validate { q -> q == null || q.count() > 0 } }

fun <E, T : OptionalProperty<Float?>> Validator<E>.Property<T?>.isZero(): Validator<E>.Property<T?> =
    this.validate(Equals(0f)) { it != null && it.validate { q -> q == null || q == 0f } }

fun <E, T : OptionalProperty<Float?>> Validator<E>.Property<T?>.isPositiveOrZero(): Validator<E>.Property<T?> =
    isGreaterThanOrEqualTo(0f)

fun <E, Q : Comparable<Q>, T : OptionalProperty<Q?>> Validator<E>.Property<T?>.isGreaterThanOrEqualTo(value: Q): Validator<E>.Property<T?> =
    this.validate(GreaterOrEqual(value)) { it != null && it.validate { q -> q == null || q >= value } }

fun <E, Q : Comparable<Q>, T : OptionalProperty<Q?>> Validator<E>.Property<T?>.isLessThan(value: Q): Validator<E>.Property<T?> =
    this.validate(Less(value)) { it != null && it.validate { q -> q == null || q < value } }

fun <E, T : OptionalProperty<String?>> Validator<E>.Property<T?>.isNotBlank(): Validator<E>.Property<T?> =
    this.validate(NotBlank) { it != null && it.validate { q -> q == null || q.isNotBlank() } }

inline fun <E, reified Q : Comparable<Q>, T : OptionalProperty<Q?>> Validator<E>.Property<T?>.isBetween(start: Q, end: Q): Validator<E>.Property<T?> =
    this.validate(Between(start, end)) {
        it == null || it.validate { q ->
            q == null || q in start.rangeTo(end)
        }
    }

fun <E, T : OptionalProperty<String?>> Validator<E>.Property<T?>.matches(regex: Regex): Validator<E>.Property<T?> =
    this.validate(Matches(regex)) { it == null || it.validate { q -> q == null || q.matches(regex) } }

fun <E, P, Q : Iterable<P>, T : OptionalProperty<P?>> Validator<E>.Property<T?>.isIn(values: Q): Validator<E>.Property<T?> =
    this.validate(In(values)) {
        it == null || it.validate { q ->
            values.contains(q)
        }
    }

fun <E, Q : Any, T : OptionalProperty<Q?>> Validator<E>.Property<T?>.isIn(vararg values: Q?): Validator<E>.Property<T?> =
    this.validate(In(values.toSet())) {
        it == null || it.validate { q ->
            values.contains(q)
        }
    }

fun <E, Q : Any, T : OptionalProperty<Iterable<OptionalProperty<Q?>>?>> Validator<E>.Property<T?>.validateWith(
    block: (Validator<Q>) -> Unit,
    wrongTypesAllowed: Boolean = false,
    nullsAllowed: Boolean = false
) = this.validateForEach(wrongTypesAllowed, nullsAllowed) {
   block(this)
}

fun <E, Q : Any, T : OptionalProperty<Iterable<OptionalProperty<Q?>>?>> Validator<E>.Property<T?>.validateEach(
    wrongTypesAllowed: Boolean = false,
    nullsAllowed: Boolean = false
) = validateForEach(wrongTypesAllowed, nullsAllowed) {
    // Required
}

fun <E, Q : Any, T : OptionalProperty<Iterable<OptionalProperty<Q?>>?>> Validator<E>.Property<T?>.validateForEach(
    wrongTypesAllowed: Boolean = false,
    nullsAllowed: Boolean = false,
    block: Validator<Q>.(Q) -> Unit
): Validator<E>.Property<T?> {
    this.property.get(this.obj)?.validate { q ->
        q?.forEachIndexed { index, value ->
            if (!wrongTypesAllowed && value is OptionalProperty.WrongType) {
                addConstraintViolations(
                    listOf(
                        DefaultConstraintViolation(
                            property = "${this.property.name}[$index]",
                            value = value,
                            constraint = CorrectType
                        )
                    )
                )
            }

            if (!nullsAllowed && value is OptionalProperty.Present<*> && value.value == null) {
                addConstraintViolations(
                    listOf(
                        DefaultConstraintViolation(
                            property = "${this.property.name}[$index]",
                            value = value,
                            constraint = NotNull
                        )
                    )
                )
            }

            val r = value.orNull() ?: return@forEachIndexed

            addConstraintViolations(
                Validator(r).apply { block(r) }.constraintViolations.map {
                    DefaultConstraintViolation(
                        property = "${this.property.name}[$index].${it.property}",
                        value = it.value,
                        constraint = it.constraint
                    )
                }
            )
        }

        true
    }
    return this
}

inline fun <E, Q : Any, T : OptionalProperty<Q?>> Validator<E>.Property<T?>.validateOptional(block: Validator<Q>.(Q) -> Unit): Validator<E>.Property<T?> {
    val value = this.property.get(this.obj)?.orNull()
    if (value != null) {
        this.addConstraintViolations(
            Validator(value).apply { block(value) }.constraintViolations.map {
                DefaultConstraintViolation(
                    property = "${this.property.name}.${it.property}",
                    value = it.value,
                    constraint = it.constraint
                )
            }
        )
    }
    return this
}
