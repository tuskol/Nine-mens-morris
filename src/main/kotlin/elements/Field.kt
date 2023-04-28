package elements

import com.example.getResource
import javafx.scene.image.Image
import javafx.scene.image.ImageView

class Field(layer:Int, horizontal:Int, vertical:Int) : ImageView() {
    private val layer: Int get() = layer
    private val horizontal:Int get() = horizontal
    private val vertical:Int get() = vertical

    init {
        this.image = Image(getResource("/field.png"))
    }



}