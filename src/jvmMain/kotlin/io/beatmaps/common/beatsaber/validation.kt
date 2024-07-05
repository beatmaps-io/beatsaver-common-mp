package io.beatmaps.common.beatsaber

import org.valiktor.Constraint

object CorrectType : Constraint
object NodePresent : Constraint
object NodeNotPresent : Constraint

object InFiles : Constraint
object ImageSquare : Constraint
object ImageSize : Constraint
object ImageFormat : Constraint
object AudioFormat : Constraint
object CutDirection : Constraint
object MisplacedCustomData : Constraint
data class UniqueDiff(val diff: String?) : Constraint
object MetadataLength : Constraint
object IndexedConstraint : Constraint

val Schema2_1 = Version("2.1.0")
val Schema3_1 = Version("3.1.0")
val Schema3_2 = Version("3.2.0")
val Schema3_3 = Version("3.3.0")
