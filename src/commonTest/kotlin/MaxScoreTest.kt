import io.beatmaps.common.beatsaber.MultiplierEventType
import io.beatmaps.common.beatsaber.ScoreMultiplierCounter
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MaxScoreTest {
    @Test
    fun testFirstTick() {
        val test = ScoreMultiplierCounter()
        val actual = test.processMultiplierEvent(MultiplierEventType.Positive)

        val expected = ScoreMultiplierCounter(1, 1, 2)
        assertEquals(expected, actual)

        val actual2 = actual.processMultiplierEvent(MultiplierEventType.Positive)
        val expected2 = ScoreMultiplierCounter(2, 0, 4)
        assertEquals(expected2, actual2)
    }

    @Test
    fun testPositiveMultiplierAtLimit() {
        val test = ScoreMultiplierCounter(4, 7, 8)
        val actual = test.processMultiplierEvent(MultiplierEventType.Positive)

        val expected = ScoreMultiplierCounter(8, 0, 16)
        assertEquals(expected, actual)
    }

    @Test
    fun testPositiveMultiplierBelowLimit() {
        val test = ScoreMultiplierCounter(4, 6, 8)
        val actual = test.processMultiplierEvent(MultiplierEventType.Positive)

        val expected = ScoreMultiplierCounter(4, 7, 8)
        assertEquals(expected, actual)
    }

    @Test
    fun testPositiveMultiplierOverflow() {
        val test = ScoreMultiplierCounter(4, 8, 8)
        val actual = test.processMultiplierEvent(MultiplierEventType.Positive)

        val expected = ScoreMultiplierCounter(8, 0, 16)
        assertEquals(expected, actual)
    }

    @Test
    fun testNegativeMultiplierWithProgress() {
        val test = ScoreMultiplierCounter(4, 7, 8)
        val actual = test.processMultiplierEvent(MultiplierEventType.Negative)

        val expected = ScoreMultiplierCounter(4, 0, 8)
        assertEquals(expected, actual)
    }

    @Test
    fun testNegativeMultiplierWithoutProgress() {
        val test = ScoreMultiplierCounter(4, 0, 8)
        val actual = test.processMultiplierEvent(MultiplierEventType.Negative)

        val expected = ScoreMultiplierCounter(2, 0, 4)
        assertEquals(expected, actual)
    }
}
