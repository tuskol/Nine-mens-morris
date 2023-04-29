package elements

import com.example.getResource
import javafx.scene.image.Image
import javafx.scene.image.ImageView

class Field(private val layer:Int,
            private val horizontal:Int,
            private val vertical: Int) : ImageView() {
    val getLayer: Int get() = layer
    val getHorizontal:Int get() = horizontal
    val getVertical:Int get() = vertical

    var pieceStored: Piece? = null

    init {
        this.image = Image(getResource("/field.png"))
    }

}