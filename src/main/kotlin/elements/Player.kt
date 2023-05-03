package elements

import javafx.scene.paint.Color

class Player(val color: Color, val name: String) {
    var piecesList = mutableListOf<Piece>()

    var canFly: Boolean = false

    //For statistics
    var takenSteps = 0
    var millsPlaced = 0
}