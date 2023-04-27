package frames

import com.example.getResource
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
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Stage

class GameFrame : Application() {

    companion object {
        private const val WIDTH_GAME = 512
        private const val WIDTH_UI = 256
        private const val HEIGHT = 512
    }

    private lateinit var mainScene: Scene
    private lateinit var graphicsContext: GraphicsContext

    //UI Elements
    private lateinit var timePassedLabel: Label

    //Game Elements
    private lateinit var testPiece: Image

    //Resources
    private lateinit var space: Image
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

    root.children.addAll(gameGroup, uiGroup)

    mainScene = Scene(root, (WIDTH_GAME + WIDTH_UI).toDouble(), HEIGHT.toDouble() )
    mainStage.scene = mainScene

    val canvas = Canvas(WIDTH_GAME.toDouble(), HEIGHT.toDouble())
    gameGroup.children.add(canvas)

    prepareActionHandlers()

    graphicsContext = canvas.graphicsContext2D

    loadBoard()
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

    private fun loadBoard() {
        // prefixed with / to indicate that the files are
        // in the root of the "resources" folder

        space = Image(getResource("/space.png"))
        // draw background
        graphicsContext.drawImage(space, 0.0, 0.0)

        val pHolder = Image(getResource("/pieceHolder.png"))
        graphicsContext.drawImage(pHolder, 0.0, 0.0)
        graphicsContext.drawImage(pHolder, 0.0, (HEIGHT-pHolder.height))

        //sun = Image(getResource("/sun.png"))

        testPiece = Image(getResource("/pieceWhite.png"))
        graphicsContext.drawImage(testPiece, 10.0, 10.0)


        //Teszteléshez lepakolja a korongokat
        var moreTestPiecesW = mutableListOf<Image>()
        var moreTestPiecesB = mutableListOf<Image>()
        var i = 0
        var x = 10.0
        while (i < 9){
            moreTestPiecesW.add(Image(getResource("/pieceWhite.png")))
            graphicsContext.drawImage(moreTestPiecesW[i], x, 10.0)
            moreTestPiecesB.add(Image(getResource("/pieceBlack.png")))
            graphicsContext.drawImage(moreTestPiecesB[i], x, (HEIGHT-moreTestPiecesB[i].height) - 8.0)

            x += moreTestPiecesW[i].width + 10
            i++
        }
    }

    private fun loadUI(parent: Group){
        val uiMainVBox = VBox(10.0)
        VBox.setVgrow(uiMainVBox, Priority.ALWAYS)
        uiMainVBox.minWidth = WIDTH_UI.toDouble()
        uiMainVBox.alignment = Pos.CENTER

        //val uiTitleLabel = Label("Nine Men's Morris")
        //uiTitleLabel.font = Font("Arial", 25.0)

        val uiTitleLabel = inicLabel("Nine Men's Morris", 25.0, "Arial", FontWeight.BOLD)

        val playersNHBox = HBox((WIDTH_UI/3).toDouble())
        HBox.setHgrow(playersNHBox, Priority.ALWAYS)
        playersNHBox.minWidth = WIDTH_UI.toDouble()
        playersNHBox.minHeight = 50.0
        playersNHBox.alignment = Pos.CENTER

        playersNHBox.style = "-fx-background-color:#D3D3D3"
        //playersNHBox.background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
        val playerWhiteText = inicLabel("WHITE", 15.0, fontWeight=FontWeight.BOLD)
        val playerBlackText = inicLabel("BLACK", 15.0, fontWeight=FontWeight.BOLD)
        playerWhiteText.textFill = Color.WHITE
        playerBlackText.textFill = Color.BLACK


        val instructionTextArea = TextArea("Instruction panel\n" +
                                "Here comes all the instructions")
        instructionTextArea.maxWidth = WIDTH_UI.toDouble() - 10.0
        instructionTextArea.isEditable = false
        instructionTextArea.isWrapText = true
        //instructionTextArea.alignment = Pos.CENTER

        timePassedLabel = inicLabel("Elapsed time: ..")

        uiMainVBox.children.addAll(uiTitleLabel, playersNHBox, instructionTextArea)
        uiMainVBox.children.add(timePassedLabel)
        playersNHBox.children.addAll(playerWhiteText, playerBlackText)

        parent.children.add(uiMainVBox)
    }

    private  fun inicLabel(text:String,
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
        graphicsContext.drawImage(space, 0.0, 0.0)

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
