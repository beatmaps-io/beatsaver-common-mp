package io.beatmaps.common.beatsaber

import io.beatmaps.common.OptionalProperty
import org.valiktor.DefaultConstraintViolation
import org.valiktor.Validator
import org.valiktor.constraints.Between
import org.valiktor.constraints.In
import org.valiktor.constraints.Matches
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

inline fun <E, reified Q : Comparable<Q>, T : OptionalProperty<Q?>> Validator<E>.Property<T?>.isBetween(start: Q, end: Q): Validator<E>.Property<T?> =
    this.validate(Between(start, end)) {
        it == null || it.validate { q ->
            q == null || q in start.rangeTo(end)
        }
    }

fun <E, T: OptionalProperty<String?>> Validator<E>.Property<T?>.matches(regex: Regex): Validator<E>.Property<T?> =
        this.validate(Matches(regex)) { it == null || it.validate { q -> q == null || q.matches(regex) } }

fun <E, T : OptionalProperty<Int?>> Validator<E>.Property<T?>.isIn(vararg values: Int?): Validator<E>.Property<T?> =
    this.validate(In(values.toSet())) {
        it == null || it.validate { q ->
            values.contains(q)
        }
    }

fun <E, Q, T : OptionalProperty<Iterable<Q>?>> Validator<E>.Property<T?>.validateForEach(
    block: Validator<Q>.(Q) -> Unit
): Validator<E>.Property<T?> {
    this.property.get(this.obj)?.validate { q ->
        q?.forEachIndexed { index, value ->
            this.addConstraintViolations(
                Validator(value).apply { block(value) }.constraintViolations.map {
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
