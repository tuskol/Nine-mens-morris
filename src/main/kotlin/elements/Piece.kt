package elements

import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.paint.Color
import utils.getResource


class Piece(private val id: Int, private val color: Color) : ImageView() {
    val getColor: Color
        get() = color

    var parentField: Field? = null

    init {
        when (color) {
            Color.WHITE -> this.image = Image(getResource("/pieceWhite.png"))
            Color.BLACK -> this.image = Image(getResource("/pieceBlack.png"))
            else -> println("WARNING! There is no image for this color!!!")
        }
    }
}