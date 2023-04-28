package frames

import com.example.getResource
import elements.Field
import elements.Piece
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
import javafx.scene.image.ImageView
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

    //Game Elements
    private val fields: MutableList<MutableList<Field>> = mutableListOf()
    private var previouslySelectedPiece: ImageView? = null
    private val piecesList = mutableMapOf<Color, MutableList<Piece>>()

    //Resources
    private lateinit var backgroundImage: Image
    private lateinit var sun: Image

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

    piecesList[Color.WHITE] = mutableListOf()
    piecesList[Color.BLACK] = mutableListOf()

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

        //draw the fields
        val fieldSpacing = 15.0

        var fieldImage = Image(getResource("/field.png"))

        var mx = (WIDTH_GAME - fieldImage.width) / 2.0
        var my = (HEIGHT - fieldImage.height) / 2.0


        val whitePiecePic = Image(getResource("/pieceWhite.png"))
        //TODO: valami figyelmeztetés ha nem ugynakkorák a képek



        //Drawing the lines and fields

        //draws the left-side horizontal line
        parent.children.add(drawCrossLines(fieldImage, arrayOf(mx, my),false,true))
        //draws the upper-side vertical line
        parent.children.add(drawCrossLines(fieldImage, arrayOf(mx, my),false,false))
        //draws the right-side horizontal line
        parent.children.add(drawCrossLines(fieldImage, arrayOf(mx, my),true,true))
        //draws the lower-side vertical line
        parent.children.add(drawCrossLines(fieldImage, arrayOf(mx, my),true,false))
        var i = 1
        while (i <= 3){
            //Drawing the line between the fields in their layers
            //draws the upper horizontal line
            parent.children.add(makeLines(fieldImage, arrayOf(mx, my),
                arrayOf(false, false), arrayOf(true, false), i))
            //draws the lower horizontal line
            parent.children.add(makeLines(fieldImage, arrayOf(mx, my),
                arrayOf(false, true), arrayOf(true, true), i))
            //draws the left vertical line
            parent.children.add(makeLines(fieldImage, arrayOf(mx, my),
                arrayOf(false, false), arrayOf(false, true), i))
            //draws the right vertical line
            parent.children.add(makeLines(fieldImage, arrayOf(mx, my),
                arrayOf(true, false), arrayOf(true, true), i))

            //Making the fields
            val newFieldLayer: MutableList<Field> = mutableListOf()
            fields.add(newFieldLayer)

            //Left-middle horizontal line
            fields.last().add(makeFields(fieldImage, arrayOf(mx, my), i, arrayOf(false, true), arrayOf(true, false)))
            //Right-middle horizontal line
            fields.last().add(makeFields(fieldImage, arrayOf(mx, my), i, arrayOf(true, false), arrayOf(true, false)))
            //Upper-middle vertical line
            fields.last().add(makeFields(fieldImage, arrayOf(mx, my), i, arrayOf(true, false), arrayOf(false, true)))
            //Lower-middle vertical line
            fields.last().add(makeFields(fieldImage, arrayOf(mx, my), i, arrayOf(false, true), arrayOf(false, true)))
            //Left-Upper diagonal line
            fields.last().add(makeFields(fieldImage, arrayOf(mx, my), i, arrayOf(false, false)))
            //Right-Upper diagonal line
            fields.last().add(makeFields(fieldImage, arrayOf(mx, my), i, arrayOf(true, false)))
            //Left-Lower diagonal line
            fields.last().add(makeFields(fieldImage, arrayOf(mx, my), i, arrayOf(false, true)))
            //Right-Lower diagonal line
            fields.last().add(makeFields(fieldImage, arrayOf(mx, my), i, arrayOf(true, true)))

            parent.children.addAll(fields.last())
            i++
        }

        //Initializing and drawing the pieces
        i = 0
        var x = (WIDTH_GAME-pHolder.width)/2.0 + 15.0
        while (i < 9){
            drawPieces(i, Color.WHITE, arrayOf(x, 10.0))
            parent.children.add(piecesList[Color.WHITE]?.last())
            drawPieces(i, Color.BLACK, arrayOf(x, (HEIGHT- whitePiecePic.height) - 8.0))
            parent.children.add(piecesList[Color.BLACK]?.last())

            x += whitePiecePic.width + 15
            i++
        }
    }

    private fun makeLines(fieldImage:Image,
                          centerPoints:Array<Double>,
                          startPoint:Array<Boolean>,
                          endPoint:Array<Boolean>,
                          layer:Int,
                          color: Color = Color.GRAY,
                          lineWidth:Double = 20.0) : Line{

        var smx = centerPoints[0] + (if (startPoint[0] === true) 1 else -1) *
                (fieldImage.width + SPACING) * layer + (fieldImage.width) / 2.0
        var smy = centerPoints[1] + (if (startPoint[1] === true) 1 else -1) *
                (fieldImage.height + SPACING) * layer + (fieldImage.height) / 2.0

        var emx = centerPoints[0] + (if (endPoint[0] === true) 1 else -1) *
                (fieldImage.width + SPACING) * layer + (fieldImage.width) / 2.0
        var emy = centerPoints[1] + (if (endPoint[1] === true) 1 else -1) *
                (fieldImage.height + SPACING) * layer + (fieldImage.height) / 2.0

        val line = Line(smx, smy, emx, emy)
        line.stroke = color
        line.strokeWidth = lineWidth
        line.toBack()
        return line
    }

    private fun drawCrossLines(fieldImage:Image,
                               centerPoints:Array<Double>,
                               op: Boolean,
                               horizontal: Boolean,
                               color: Color = Color.GRAY,
                               lineWidth:Double = 20.0) : Line {

        var smx = centerPoints[0] + (if (op) 1 else -1) *
                (if (horizontal) 1 else 0)*(fieldImage.width + SPACING) * 3 + (fieldImage.width) / 2.0
        var smy = centerPoints[1] + (if (op) 1 else -1) *
                (if (horizontal) 0 else 1)*(fieldImage.height + SPACING) * 3 + (fieldImage.height) / 2.0

        var emx = centerPoints[0] + (if (op) 1 else -1) *
                (if (horizontal) 1 else 0)*(fieldImage.width + SPACING) * 1 + (fieldImage.width) / 2.0
        var emy = centerPoints[1] + (if (op) 1 else -1) *
                (if (horizontal) 0 else 1)*(fieldImage.height + SPACING) * 1 + (fieldImage.height) / 2.0

        val line = Line(smx, smy, emx, emy)
        line.stroke = color
        line.strokeWidth = lineWidth
        line.toBack()
        return line
    }
    private fun makeFields(fieldImage:Image,
                           centerPoints:Array<Double>,
                           layer:Int,
                           op:Array<Boolean>,
                           k:Array<Boolean> = arrayOf(true, true), ) : Field{

        //TODO Mezok indexelese meg nincs megcsinalva rendesen
        var f = Field(layer,0,0)

        f.setOnMouseClicked {
            //TODO
        }

        f.layoutX = centerPoints[0] + (if (op[0]) 1 else -1) *
                ((if (k[0]) 1 else 0))*(fieldImage.width + SPACING) * layer
        f.layoutY = centerPoints[1] + (if (op[1]) 1 else -1) *
                ((if (k[1]) 1 else 0))*(fieldImage.height + SPACING) * layer

        return f
    }
    private  fun drawPieces(id:Int,
                            color: Color,
                            coordinates:Array<Double>){
        var p = Piece(id, color)

        p.setOnMouseClicked {
            if (previouslySelectedPiece != p){
                previouslySelectedPiece?.effect = null
                p.effect = DropShadow(10.0, Color.YELLOW)
                previouslySelectedPiece = p
            }
            else{
                p.effect = null
                previouslySelectedPiece = null
            }
        }
        p.layoutX = coordinates[0]
        p.layoutY = coordinates[1]

        piecesList[color]?.add(p)
    }

    private fun loadUI(parent: Group){
        val uiMainVBox = VBox(10.0)
        VBox.setVgrow(uiMainVBox, Priority.ALWAYS)
        uiMainVBox.minWidth = WIDTH_UI.toDouble()
        uiMainVBox.alignment = Pos.CENTER

        //val uiTitleLabel = Label("Nine Men's Morris")
        //uiTitleLabel.font = Font("Arial", 25.0)

        val uiTitleLabel = initLabel("Nine Men's Morris", 25.0, "Arial", FontWeight.BOLD)

        val playersNHBox = HBox((WIDTH_UI/3).toDouble())
        HBox.setHgrow(playersNHBox, Priority.ALWAYS)
        playersNHBox.minWidth = WIDTH_UI.toDouble()
        playersNHBox.minHeight = 50.0
        playersNHBox.alignment = Pos.CENTER

        playersNHBox.style = "-fx-background-color:#D3D3D3"
        //playersNHBox.background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
        val playerWhiteText = initLabel("WHITE", 15.0, fontWeight=FontWeight.BOLD)
        val playerBlackText = initLabel("BLACK", 15.0, fontWeight=FontWeight.BOLD)
        playerWhiteText.textFill = Color.WHITE
        playerBlackText.textFill = Color.BLACK


        val instructionTextArea = TextArea("Instruction panel\n" +
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
        graphicsContext.drawImage(sun, sunX.toDouble(), sunY.toDouble())


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
