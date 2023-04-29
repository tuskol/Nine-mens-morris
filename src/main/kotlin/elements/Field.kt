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

    public fun isNeighbour(f: Field) :Boolean {
        //Check in the same layer
        if (f.getLayer == layer){
            //Check horizontally
            if (f.getVertical == vertical) {
                if (f.getHorizontal+1 == horizontal ||
                    f.getHorizontal-1 == horizontal){
                    return true
                }
            }
            //Check vertically
            if (f.getHorizontal == horizontal) {
                if (f.getVertical+1 == vertical ||
                    f.getVertical-1 == vertical){
                    return true
                }
            }
        }
        //Check in layer connections
        if (horizontal == 2 || vertical == 2){
            if ((f.getHorizontal == horizontal && f.getVertical == vertical) &&
                (f.getLayer+1 == layer || f.getLayer-1 == layer)){
                return true
            }
        }
        return false
    }
}