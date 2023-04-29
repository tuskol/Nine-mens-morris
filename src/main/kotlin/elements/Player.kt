package elements

import javafx.scene.paint.Color

class Player(private val color:Color) {
    val playerColor : Color get() = color
    val name = color.toString()

    var piecesList = mutableListOf<Piece>()

    var canFly: Boolean = false

    //piecesList[Color.WHITE] = mutableListOf()

}