package frames

import elements.Player
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import javafx.stage.Stage
import utils.createHeaderLabel

class NewGameFrame: Application() {
    companion object {
        private const val WIDTH = 300
        private const val HEIGHT = 270
    }
    private lateinit var scene: Scene
    override fun start(primaryStage: Stage?) {
        if (primaryStage != null) {
            primaryStage.title = "Start New Game - Nine Men's Morris"
        }

        val root = HBox()
        val uiGroup = Group()

        scene = Scene(root, WIDTH.toDouble(), HEIGHT.toDouble())
        if (primaryStage != null) {
            primaryStage.isResizable = false
        }
        if (primaryStage != null) {
            primaryStage.scene = scene
        }
        root.children.addAll(uiGroup)
        loadUI(uiGroup, primaryStage)
        primaryStage?.show()
    }

    private fun loadUI(parent: Group, primaryStage: Stage?){
        val uiMainVBox = VBox(10.0)
        VBox.setVgrow(uiMainVBox, Priority.ALWAYS)
        uiMainVBox.minWidth = WIDTH.toDouble()
        uiMainVBox.alignment = Pos.CENTER

        val uiTitleLabel = utils.initLabel("Nine Men's Morris", 25.0, "Arial", FontWeight.BOLD)

        val uiLabel1 = utils.initLabel("Let's start a new game!", 15.0)

        val playersHeaderHBox = utils.initHBox((WIDTH/4).toDouble(), 50.0, 40.0)
        playersHeaderHBox.style = "-fx-background-color:#D3D3D3"
        val playerWhiteText = createHeaderLabel(Color.WHITE)
        val playerBlackText = createHeaderLabel(Color.BLACK)

        val playersNameHBox = utils.initHBox((WIDTH/4).toDouble(), 30.0, 30.0)

        val playerWhiteNameTextArea = TextArea("Player White")
        playerWhiteNameTextArea.maxWidth = WIDTH / 3.0
        playerWhiteNameTextArea.maxHeight = 10.0
        val playerBlackNameTextArea = TextArea("Player Black")
        playerBlackNameTextArea.maxWidth = WIDTH / 3.0
        playerBlackNameTextArea.maxHeight = 10.0

        val playerStarterSelectHBox = utils.initHBox(10.0, 50.0, 40.0)
        val uiLabel2 = utils.initLabel("Choose the starter player:", 15.0)

        val players = FXCollections.observableArrayList("WHITE", "BLACK")
        val starterPlayerComboBox = ComboBox(players)
        starterPlayerComboBox.selectionModel.selectFirst()

        val startGameButton = Button("New Game")
        startGameButton.setOnAction {
            //Get players' data
            val pl1 = if (starterPlayerComboBox.selectionModel.selectedIndex == 0)
                Player(Color.WHITE, playerWhiteNameTextArea.text) else Player(Color.BLACK, playerBlackNameTextArea.text)
            val pl2 = if (starterPlayerComboBox.selectionModel.selectedIndex == 1)
                Player(Color.WHITE, playerWhiteNameTextArea.text) else Player(Color.BLACK, playerBlackNameTextArea.text)

            //Starting the main game frame
            val gameStage = Stage()
            val gameFrame = GameFrame(mutableListOf(pl1, pl2))
            gameFrame.start(gameStage)
            //Closing the starter frame
            primaryStage?.close()
        }

        uiMainVBox.children.addAll(uiTitleLabel, uiLabel1, playersHeaderHBox, playersNameHBox,
            playerStarterSelectHBox, startGameButton)
        playersHeaderHBox.children.addAll(playerWhiteText, playerBlackText)
        playersNameHBox.children.addAll(playerWhiteNameTextArea, playerBlackNameTextArea)
        playerStarterSelectHBox.children.addAll(uiLabel2, starterPlayerComboBox)

        parent.children.add(uiMainVBox)
    }
}