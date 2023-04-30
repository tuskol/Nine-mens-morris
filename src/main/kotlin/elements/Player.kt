package elements

import javafx.scene.paint.Color

class Player(val color: Color, val name: String) {
    //val playerColor : Color get() = color
    //val name = color.toString()

    var piecesList = mutableListOf<Piece>()

    var canFly: Boolean = false

    var takenSteps = 0
    var millsPlaced = 0

    //piecesList[Color.WHITE] = mutableListOf()

}