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

    //private val id = id
    //private val kivalaszthato = false, private var torolhato:kotlin.Boolean = false

    init {
        if (color == Color.WHITE) this.image = Image(getResource("/pieceWhite.png"))
        else if (color == Color.BLACK) this.image = Image(getResource("/pieceBlack.png"))
        //else //TODO: valami hibaüzenet szerű
    }

    //public fun getPieceImage() : Image {return image}
}