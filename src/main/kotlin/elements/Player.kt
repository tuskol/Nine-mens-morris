package elements

import javafx.scene.paint.Color

class Player(private val color:Color) {
    val playerColor : Color get() = color

    val name = color.toString()

}