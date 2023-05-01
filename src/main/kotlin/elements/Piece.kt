package elements

import com.example.getResource
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.paint.Color


class Piece(private val id: Int, private val color: Color) : ImageView() {
    val getColor: Color
        get() = color
    val getId: Int
        get() = id

    var parentField: Field? = null

    init {
        if (color == Color.WHITE) this.image = Image(getResource("/pieceWhite.png"))
        else if (color == Color.BLACK) this.image = Image(getResource("/pieceBlack.png"))
        else println("WARNING! There is no image for this color!!!")
    }
}