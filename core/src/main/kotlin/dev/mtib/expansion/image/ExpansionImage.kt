package dev.mtib.expansion.image

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import java.awt.Color
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.random.Random

class ExpansionImage(
    val path: String,
    val width: Int,
    val height: Int,
    val stars: Int,
    val seed: Long = Random.nextLong(),
    val expansionRate: Double = 0.1,
    val blur: Double = 1.0
) {
    init {
        require(width > 0) { "Width must be greater than 0" }
        require(height > 0) { "Height must be greater than 0" }
        require(stars > 0) { "Stars must be greater than 0" }
    }

    fun absoluteStarPosition(index: Int, time: Double): Pair<Double, Double> {
        val random = Random(randomSeed(index, Usage.Position))
        val individualTime = time + timeOffset(index)
        return Pair(random.nextDouble(-1.0, 1.0), random.nextDouble(-1.0, 1.0))
            .let { Pair(it.first * (1.0 + individualTime * expansionRate), it.second * (1.0 + individualTime * expansionRate)) }
            .let { Pair(it.first + 1.0, it.second + 1.0) }
            .let { Pair(it.first * width / 2, it.second * height / 2) }
    }

    fun randomSeed(index: Int, usage: Usage = Usage.Unknown): Long = Random(seed + index).nextLong() + Random(seed + index * usage.value * 17).nextLong()

    enum class Usage(val value: Int) {
        Unknown(0),
        RedShift(1),
        TimeOffset(2),
        Position(3),
    }

    fun redShift(index: Int): Double {
        val random = Random(randomSeed(index, Usage.RedShift))
        return random.nextDouble(-1.0, 1.0)
    }

    fun timeOffset(index: Int): Double {
        val random = Random(randomSeed(index, Usage.TimeOffset))
        return random.nextDouble(0.0, 1.0).pow(2)
    }

    fun replacePathVars(time: Double): String = path
        .replace("TIME", time.toString())
        .replace("WIDTH", width.toString())
        .replace("HEIGHT", height.toString())
        .replace("STARS", stars.toString())
        .replace("SEED", seed.toString())
        .replace("EXPANSION_RATE", expansionRate.toString())
        .replace("BLUR", blur.toString())

    fun generate(time: Double) {
        val starColors: List<Color> = (0..<stars).map {
            val redShift = redShift(it)
            val individualTime = (time + timeOffset(it)).coerceIn(0.0, 1.0)
            val brightness = 1.0 - (individualTime * individualTime / (2.0 * (individualTime * individualTime - individualTime) + 1.0)).pow(2)

            require(brightness in 0.0..1.0) { "Brightness must be between 0 and 1" }

            val red = brightness * (1.0 + redShift / 2.0).coerceIn(0.0, 1.0)
            val green = brightness
            val blue = brightness * (1.0 - redShift / 2.0).coerceIn(0.0, 1.0)
            Color((red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())
        }
        val starPositions = List(stars) { absoluteStarPosition(it, time) }
        val image = ImmutableImage.create(width, height)
            .map { pixel ->
                val starContributions = List(starColors.size) { index ->
                    val distance = hypot(starPositions[index].first - pixel.x, starPositions[index].second - pixel.y)
                    1.0 / (1.0 + distance.pow(2) / blur)
                }
                val color = starColors
                    .zip(starContributions)
                    .fold(Triple(0.0, 0.0, 0.0)) { acc, (color, contribution) ->
                        Triple(acc.first + color.red * contribution, acc.second + color.green * contribution, acc.third + color.blue * contribution)
                    }
                    .let { (red, green, blue) ->
                        Color(
                            (red * (1.0 + time)).coerceIn(0.0, 255.0).toInt(),
                            green.coerceIn(0.0, 255.0).toInt(),
                            blue.coerceIn(0.0, 255.0).toInt()
                        )
                    }
                color
            }
        image.output(PngWriter(), replacePathVars(time))
    }
}