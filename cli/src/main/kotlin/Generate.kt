import dev.mtib.expansion.image.ExpansionImage

fun main() {
    val path = "expansion_WIDTH_HEIGHT_TIME.png"

    val image = ExpansionImage(
        path = path,
        width = 2560,
        height = 1440,
        stars = 300,
        blur = 10.0,
        seed = 1L,
    )

    0.rangeTo(10).forEach {
        image.generate(it / 10.0)
    }
}