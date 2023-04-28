package elements

import com.example.getResource
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.paint.Color


class Piece(id:Int, color: Color) : ImageView() {
    private val c = color
    private val id = id
    //private val kivalaszthato = false, private var torolhato:kotlin.Boolean = false

    init {
        if (c == Color.WHITE) this.image = Image(getResource("/pieceWhite.png"))
        else if (c == Color.BLACK) this.image = Image(getResource("/pieceBlack.png"))
        //else //TODO: valami hibaüzenet szerű
    }

    //public fun getPieceImage() : Image {return image}
}