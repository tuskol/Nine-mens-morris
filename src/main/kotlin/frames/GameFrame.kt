package frames

import com.example.getResource
import elements.Field
import elements.Piece
import elements.Player
import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.effect.DropShadow
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Stage

//TODO: Az minta dolgokat kiszedni (mint a nap, meg a többi)
class GameFrame : Application() {

    companion object {
        private const val WIDTH_GAME = 600
        private const val WIDTH_UI = 256
        private const val HEIGHT = 600
        private const val SPACING = 15.0
    }

    private lateinit var mainScene: Scene
    private lateinit var graphicsContext: GraphicsContext

    //UI Elements
    private lateinit var timePassedLabel: Label
    private lateinit var instructionTextArea: TextArea

    //Game Elements
    private val fields: MutableList<MutableList<Field>> = mutableListOf()
    private var previouslySelectedPiece: Piece? = null
    private val players = mutableMapOf<Color, Player>()
    private var currentPlayerTurn: Color = Color.WHITE //Starer Player
    private var phase1Placing: Boolean = true

    //Resources
    private lateinit var backgroundImage: Image
    private lateinit var fieldImage: Image
    //private lateinit var sun: Image

    private val centerPoints: Array<Double> = Array(2) { 0.0 }

    private var sunX = WIDTH_GAME / 2
    private var sunY = HEIGHT / 2

    private var lastFrameTime: Long = System.nanoTime()

    // use a set so duplicates are not possible
    private val currentlyActiveKeys = mutableSetOf<KeyCode>()
    private var startTime = 0L

    override fun start(mainStage: Stage) {
    mainStage.title = "Kotlin HW - Nine Men's Morris"

    val root = HBox()
    val gameGroup = Group()
    val uiGroup = Group()

    mainScene = Scene(root, (WIDTH_GAME + WIDTH_UI).toDouble(), HEIGHT.toDouble() )
    mainStage.isResizable = false
    mainStage.scene = mainScene

    root.children.addAll(gameGroup, uiGroup)

    val canvas = Canvas(WIDTH_GAME.toDouble(), HEIGHT.toDouble())
    gameGroup.children.add(canvas)

    prepareActionHandlers()

    graphicsContext = canvas.graphicsContext2D

    //piecesList[Color.WHITE] = mutableListOf()
    //piecesList[Color.BLACK] = mutableListOf()

    players[Color.WHITE] = Player(Color.WHITE)
    players[Color.BLACK] = Player(Color.BLACK)

    loadBoard(gameGroup)
    loadUI(uiGroup)

    // Main loop
    object : AnimationTimer() {
        override fun handle(currentNanoTime: Long) {
            //tickAndRender(currentNanoTime)

            if (startTime == 0L) {
                startTime = currentNanoTime
            }
            val elapsedTime = (currentNanoTime - startTime) / 1_000_000_000.0
            val elapsedMinutes = elapsedTime / 60
            val elapsedSeconds = elapsedTime % 60
            timePassedLabel.text = "Elapsed time: " + String.format("%02.0f:%02.0f", elapsedMinutes, elapsedSeconds)
        }
    }.start()
    mainStage.show()
}


    private fun prepareActionHandlers() {
        mainScene.onKeyPressed = EventHandler { event ->
            currentlyActiveKeys.add(event.code)
        }
        mainScene.onKeyReleased = EventHandler { event ->
            currentlyActiveKeys.remove(event.code)
        }
    }

    private fun loadBoard(parent: Group) {
        // prefixed with / to indicate that the files are
        // in the root of the "resources" folder
        backgroundImage = Image(getResource("/space.png"))
        // draw background
        graphicsContext.drawImage(backgroundImage, 0.0, 0.0)

        //draw piece holders
        val pHolder = Image(getResource("/pieceHolder.png"))
        graphicsContext.drawImage(pHolder, (WIDTH_GAME-pHolder.width)/2.0 , 0.0)
        graphicsContext.drawImage(pHolder, (WIDTH_GAME-pHolder.width)/2.0, (HEIGHT-pHolder.height))


        fieldImage = Image(getResource("/field.png"))

        centerPoints[0] = (WIDTH_GAME - fieldImage.width) / 2.0
        centerPoints[1] = (HEIGHT - fieldImage.height) / 2.0


        val whitePiecePic = Image(getResource("/pieceWhite.png"))
        //TODO: valami figyelmeztetés ha nem ugynakkorák a képek



        //Drawing the lines and fields

        //draws the left-side horizontal line
        parent.children.add(drawCrossLines(false,true))
        //draws the upper-side vertical line
        parent.children.add(drawCrossLines(false,false))
        //draws the right-side horizontal line
        parent.children.add(drawCrossLines(true,true))
        //draws the lower-side vertical line
        parent.children.add(drawCrossLines(true,false))
        var i = 1
        while (i <= 3){
            //Drawing the line between the fields in their layers
            //draws the upper horizontal line
            parent.children.add(makeLines(arrayOf(false, false), arrayOf(true, false), i))
            //draws the lower horizontal line
            parent.children.add(makeLines(arrayOf(false, true), arrayOf(true, true), i))
            //draws the left vertical line
            parent.children.add(makeLines(arrayOf(false, false), arrayOf(false, true), i))
            //draws the right vertical line
            parent.children.add(makeLines(arrayOf(true, false), arrayOf(true, true), i))

            //Making the fields
            val newFieldLayer: MutableList<Field> = mutableListOf()
            fields.add(newFieldLayer)

            //Left-middle horizontal line
            fields.last().add(makeFields(i, arrayOf(false, true), arrayOf(true, false)))
            //Right-middle horizontal line
            fields.last().add(makeFields(i, arrayOf(true, false), arrayOf(true, false)))
            //Upper-middle vertical line
            fields.last().add(makeFields(i, arrayOf(true, false), arrayOf(false, true)))
            //Lower-middle vertical line
            fields.last().add(makeFields(i, arrayOf(false, true), arrayOf(false, true)))
            //Left-Upper diagonal line
            fields.last().add(makeFields(i, arrayOf(false, false)))
            //Right-Upper diagonal line
            fields.last().add(makeFields(i, arrayOf(true, false)))
            //Left-Lower diagonal line
            fields.last().add(makeFields(i, arrayOf(false, true)))
            //Right-Lower diagonal line
            fields.last().add(makeFields(i, arrayOf(true, true)))

            parent.children.addAll(fields.last())
            i++
        }

        //Initializing and drawing the pieces
        i = 0
        var x = (WIDTH_GAME-pHolder.width)/2.0 + 15.0
        while (i < 9){
            var p = initPiece(i, Color.WHITE, arrayOf(x, 10.0))

            players[Color.WHITE]?.piecesList?.add(p)
            parent.children.add(p)
            p = initPiece(i, Color.BLACK, arrayOf(x, (HEIGHT- whitePiecePic.height) - 8.0))
            parent.children.add(p)

            x += whitePiecePic.width + 15
            i++
        }
    }

    private fun makeLines(startPoint:Array<Boolean>,
                          endPoint:Array<Boolean>,
                          layer:Int,
                          color: Color = Color.GRAY,
                          lineWidth:Double = 20.0) : Line{

        val smx = centerPoints[0] + (if (startPoint[0]) 1 else -1) *
                (fieldImage.width + SPACING) * layer + (fieldImage.width) / 2.0
        val smy = centerPoints[1] + (if (startPoint[1]) 1 else -1) *
                (fieldImage.height + SPACING) * layer + (fieldImage.height) / 2.0

        val emx = centerPoints[0] + (if (endPoint[0]) 1 else -1) *
                (fieldImage.width + SPACING) * layer + (fieldImage.width) / 2.0
        val emy = centerPoints[1] + (if (endPoint[1]) 1 else -1) *
                (fieldImage.height + SPACING) * layer + (fieldImage.height) / 2.0

        val line = Line(smx, smy, emx, emy)
        line.stroke = color
        line.strokeWidth = lineWidth
        line.toBack()
        return line
    }

    private fun drawCrossLines(op: Boolean,
                               horizontal: Boolean,
                               color: Color = Color.GRAY,
                               lineWidth:Double = 20.0) : Line {

        val smx = centerPoints[0] + ((if (op) 1 else -1) *
                (if (horizontal) 1 else 0) * (fieldImage.width + SPACING) * 3) + ((fieldImage.width) / 2.0)
        val smy = centerPoints[1] + (if (op) 1 else -1) *
                (if (horizontal) 0 else 1)*(fieldImage.height + SPACING) * 3 + (fieldImage.height) / 2.0

        val emx = centerPoints[0] + (if (op) 1 else -1) *
                (if (horizontal) 1 else 0)*(fieldImage.width + SPACING) * 1 + (fieldImage.width) / 2.0
        val emy = centerPoints[1] + (if (op) 1 else -1) *
                (if (horizontal) 0 else 1)*(fieldImage.height + SPACING) * 1 + (fieldImage.height) / 2.0

        val line = Line(smx, smy, emx, emy)
        line.stroke = color
        line.strokeWidth = lineWidth
        line.toBack()
        return line
    }

    /**
     * @param op Operator that moves the elements horizontally and vertically.
     * + true means +
     * + false mean -
     * @param k This parameter allows the elements to move horizontally and vertically.
     *          This helps to place them to the middle of their line
     * @return an initialized Field
     */
    private fun makeFields(layer:Int,
                           op:Array<Boolean>,
                           k:Array<Boolean> = arrayOf(true, true), ) : Field{
        val hv = filedIdxToCoord(op, k)
        val f = Field(layer, hv[0], hv[1])

        f.setOnMouseClicked {
            clickOnField(f)
        }

        f.layoutX = centerPoints[0] + (if (op[0]) 1 else -1) *
                ((if (k[0]) 1 else 0))*(fieldImage.width + SPACING) * layer
        f.layoutY = centerPoints[1] + (if (op[1]) 1 else -1) *
                ((if (k[1]) 1 else 0))*(fieldImage.height + SPACING) * layer

        return f
    }
    private fun initPiece(id:Int,
                          color: Color,
                          coordinates:Array<Double>) : Piece{
        val p = Piece(id, color)

        p.setOnMouseClicked {
            clickOnPiece(p)
        }
        p.layoutX = coordinates[0]
        p.layoutY = coordinates[1]

        return p
    }

    private fun loadUI(parent: Group){
        val uiMainVBox = VBox(10.0)
        VBox.setVgrow(uiMainVBox, Priority.ALWAYS)
        uiMainVBox.minWidth = WIDTH_UI.toDouble()
        uiMainVBox.alignment = Pos.CENTER

        val uiTitleLabel = initLabel("Nine Men's Morris", 25.0, "Arial", FontWeight.BOLD)

        val playersNHBox = HBox((WIDTH_UI/3).toDouble())
        HBox.setHgrow(playersNHBox, Priority.ALWAYS)
        playersNHBox.minWidth = WIDTH_UI.toDouble()
        playersNHBox.minHeight = 50.0
        playersNHBox.alignment = Pos.CENTER

        playersNHBox.style = "-fx-background-color:#D3D3D3"
        val playerWhiteText = initLabel("WHITE", 15.0, fontWeight=FontWeight.BOLD)
        val playerBlackText = initLabel("BLACK", 15.0, fontWeight=FontWeight.BOLD)
        playerWhiteText.textFill = Color.WHITE
        playerBlackText.textFill = Color.BLACK


        instructionTextArea = TextArea("Instruction panel\n" +
                                "Here comes all the instructions")
        instructionTextArea.maxWidth = WIDTH_UI.toDouble() - 10.0
        instructionTextArea.isEditable = false
        instructionTextArea.isWrapText = true
        //instructionTextArea.alignment = Pos.CENTER

        timePassedLabel = initLabel("Elapsed time: ..")

        uiMainVBox.children.addAll(uiTitleLabel, playersNHBox, instructionTextArea)
        uiMainVBox.children.add(timePassedLabel)
        playersNHBox.children.addAll(playerWhiteText, playerBlackText)

        parent.children.add(uiMainVBox)
    }

    private  fun initLabel(text:String,
                           size:Double=15.0,
                           fontFamily:String="System",
                           fontWeight:FontWeight=FontWeight.NORMAL) : Label {
        val label = Label(text)
        label.font = Font.font(fontFamily, fontWeight, size)
        return label
    }


    private fun placePieceOnField(field: Field, piece: Piece?){
        piece?.layoutX = field.layoutX + (field.image.width - (piece?.image?.width ?: 0.0)) / 2.0
        piece?.layoutY = field.layoutY + (field.image.height - (piece?.image?.height ?: 0.0)) / 2.0
    }

    private fun selOrUnselPiece(piece: Piece?, addEff:Boolean){
        if (addEff){
            previouslySelectedPiece?.effect = null
            piece?.effect = DropShadow(10.0, Color.YELLOW)
            previouslySelectedPiece = piece
        }
        else{
            piece?.effect = null
            previouslySelectedPiece = null
        }
    }
    private fun filedIdxToCoord(op: Array<Boolean>, k: Array<Boolean>) :Array<Int>{
        val x: Int
        val y: Int

        if (k[0] != k[1]){
            if (k[0]){
                y = 2
                x = when (op[0]){
                    false -> 1
                    true -> 3
                }
            }
            else{
                x = 2
                y = when (op[1]){
                    false -> 1
                    true -> 3
                }
            }
        }
        else{
            x = when (op[0]){
                false -> 1
                true -> 3
            }
            y = when (op[1]){
                false -> 1
                true -> 3
            }
        }
        return arrayOf(x, y)
    }
    private fun setInstructionText(newInstruction: String){
        instructionTextArea.text = newInstruction
    }
    private fun changePlayerTurn(){
        if (currentPlayerTurn == Color.WHITE) currentPlayerTurn = Color.BLACK
        else currentPlayerTurn = Color.WHITE
    }


    private fun clickOnPiece(p: Piece){
        if (previouslySelectedPiece != p){
            selOrUnselPiece(p, true)
        }
        else{
            selOrUnselPiece(p, false)
        }


        //EZZ AZ ELLENŐRZÉSE HOGY KI VAN SORON
        /*
        if (p.getColor == currentPlayerTurn){
            if (previouslySelectedPiece != p){
                selOrUnselPiece(p, true)
            }
            else{
                selOrUnselPiece(p, false)
            }
        }
        else {
            setInstructionText("NONO!")
        }*/
    }
    private fun clickOnField(f: Field){
        //TODO
        //Hierarchiája az ellenőrzésnek:
        //Kiválasztott bábe van-e --> (Mező szomszédos) --> Üres-e

        /*
        if (previouslySelectedPiece != null){

            if (f.pieceStored == null){
                f.pieceStored = previouslySelectedPiece
                previouslySelectedPiece?.parentField?.pieceStored = null
                previouslySelectedPiece?.parentField = f
                placePieceOnField(f, previouslySelectedPiece)
                selOrUnselPiece(previouslySelectedPiece, false)
                changePlayerTurn()
            }
            else {
                println(f.pieceStored?.getColor.toString())

                setInstructionText("Ide nem rakhatsz asdasd")
            }
        }
         */

        for (fsl in fields){
            for (fs in fsl){
                if (f.isNeighbour(fs)){
                    fs?.effect = DropShadow(10.0, Color.YELLOW)
                }
                else {
                    fs?.effect = null
                }
            }
        }
        f?.effect = DropShadow(10.0, Color.RED)

    }








    private fun tickAndRender(currentNanoTime: Long) {
        // the time elapsed since the last frame, in nanoseconds
        // can be used for physics calculation, etc
        val elapsedNanos = currentNanoTime - lastFrameTime
        lastFrameTime = currentNanoTime

        // clear canvas
        graphicsContext.clearRect(0.0, 0.0, WIDTH_GAME.toDouble(), HEIGHT.toDouble())

        // draw background
        graphicsContext.drawImage(backgroundImage, 0.0, 0.0)

        // perform world updates
        updateSunPosition()

        // draw sun
        //graphicsContext.drawImage(sun, sunX.toDouble(), sunY.toDouble())


        // display crude fps counter
        val elapsedMs = elapsedNanos / 1_000_000
        if (elapsedMs != 0L) {
            graphicsContext.fill = Color.WHITE
            graphicsContext.fillText("${1000 / elapsedMs} fps", 10.0, 10.0)
        }
    }

    private fun updateSunPosition() {
        if (currentlyActiveKeys.contains(KeyCode.LEFT)) {
            sunX--
        }
        if (currentlyActiveKeys.contains(KeyCode.RIGHT)) {
            sunX++
        }
        if (currentlyActiveKeys.contains(KeyCode.UP)) {
            sunY--
        }
        if (currentlyActiveKeys.contains(KeyCode.DOWN)) {
            sunY++
        }
    }
}
