package frames

import elements.Field
import elements.Piece
import elements.Player
import javafx.animation.AnimationTimer
import javafx.animation.TranslateTransition
import javafx.application.Application
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.*
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
import javafx.util.Duration
import utils.createHeaderLabel
import utils.getResource

//TODO: Az minta dolgokat kiszedni (mint a nap, meg a többi)
//TODO: Értelmes utasítások leírása
class GameFrame(private val players: MutableList<Player>) : Application() {
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
    private val playerNameText = mutableListOf<Label>()
    private val playerStatTexts: MutableList<MutableMap<String, Label>> = mutableListOf()
    private lateinit var pauseButton: Button

    //Game Elements
    private var gameInProgress: Boolean = true
    private val fields: MutableList<MutableList<Field>> = mutableListOf()
    private var previouslySelectedPiece: Piece? = null
    //private val players = mutableListOf<Player>()
    private var currentPlayerTurn = 0 //Starer Player id
    private var phase1Placing: Boolean = true
    private var countPiecesOnField = 0
    private var phaseRemoving: Boolean = false
    private var removablePieces = mutableListOf<Piece>()

    //Resources
    private lateinit var backgroundImage: Image
    private lateinit var fieldImage: Image
    private lateinit var whitePiecePic: Image

    private val centerPoints: Array<Double> = Array(2) { 0.0 }

    // use a set so duplicates are not possible
    private val currentlyActiveKeys = mutableSetOf<KeyCode>()
    private var startTime = 0L
    private var pauseStartTime = 0L
    private var timeInPaused = 0L
    private var timeElapsed = 0L

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

        graphicsContext = canvas.graphicsContext2D

        loadBoard(gameGroup)
        loadUI(uiGroup, mainStage)

        // Main loop
        object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                if (gameInProgress) {
                    if (startTime == 0L) {
                        startTime = currentNanoTime
                    }
                    val elapsedTime = (currentNanoTime - startTime + timeElapsed) / 1_000_000_000.0
                    val elapsedMinutes = elapsedTime / 60
                    val elapsedSeconds = elapsedTime % 60
                    timePassedLabel.text = "Elapsed time: " + String.format("%02.0f:%02.0f", elapsedMinutes, elapsedSeconds)
                    pauseStartTime = 0L
                } else {
                    if (pauseStartTime == 0L) {
                        pauseStartTime = currentNanoTime
                    }
                }
            }
        }.start()
        mainStage.show()
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


        whitePiecePic = Image(getResource("/pieceWhite.png"))


        //Drawing the lines and fields
        //draws the left-side horizontal line
        parent.children.add(drawCrossLines(op=false, horizontal=true))
        //draws the upper-side vertical line
        parent.children.add(drawCrossLines(op=false, horizontal=false))
        //draws the right-side horizontal line
        parent.children.add(drawCrossLines(op=true, horizontal=true))
        //draws the lower-side vertical line
        parent.children.add(drawCrossLines(op=true, horizontal=false))

        for (i in 1..3){
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
        }

        //Initializing and drawing the pieces
        var x = (WIDTH_GAME-pHolder.width)/2.0 + 15.0
        for (i in 0..8){
            var p = initPiece(i, players[0].color, arrayOf(x, 10.0))

            players[0].piecesList.add(p)
            parent.children.add(p)
            p = initPiece(i, players[1].color, arrayOf(x, (HEIGHT- whitePiecePic.height) - 8.0))
            players[1].piecesList.add(p)
            parent.children.add(p)

            x += whitePiecePic.width + 15
        }
    }

    private fun makeLines(startPoint:Array<Boolean>,
                          endPoint:Array<Boolean>,
                          layer:Int,
                          color: Color = Color.GRAY,
                          lineWidth:Double = 20.0) : Line{
        val calcStartPoints = calcLayoutPoints(layer, startPoint)
        val calcEndPoints = calcLayoutPoints(layer, endPoint)

        val line = Line(calcStartPoints[0], calcStartPoints[1], calcEndPoints[0], calcEndPoints[1])
        line.stroke = color
        line.strokeWidth = lineWidth
        line.toBack()
        return line
    }
    private fun drawCrossLines(op: Boolean,
                               horizontal: Boolean,
                               color: Color = Color.GRAY,
                               lineWidth:Double = 20.0) : Line {
        val ops = arrayOf(op, op)
        val k = arrayOf(horizontal, !horizontal)

        val calcStartPoints = calcLayoutPoints(3, ops, k)
        val calcEndPoints = calcLayoutPoints(1, ops, k)

        val line = Line(calcStartPoints[0], calcStartPoints[1], calcEndPoints[0], calcEndPoints[1])
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
        val hv = fieldIdxToCoord(op, k)
        val f = Field(layer, hv[0], hv[1])

        f.setOnMouseClicked {
            clickOnField(f)
        }

        val coord = calcLayoutPoints(layer, op, k, false)
        f.layoutX = coord[0]
        f.layoutY = coord[1]

        return f
    }
    /**
     * @param op Operator that moves the elements horizontally and vertically.
     * + true means +
     * + false mean -
     * @param k This parameter allows the elements to move horizontally and vertically.
     *          This helps to place them to the middle of their line
     * @return Calculated layout coordinates
     */
    private fun calcLayoutPoints(layer: Int,
                                 op:Array<Boolean>,
                                 k:Array<Boolean> = arrayOf(true, true),
                                 placeMiddle: Boolean = true): Array<Double>
    {
        val x = centerPoints[0] + (if (op[0]) 1 else -1) *
                ((if (k[0]) 1 else 0))*(fieldImage.width + SPACING) * layer +
                ((if (placeMiddle) 1 else 0))*(fieldImage.width) / 2.0
        val y = centerPoints[1] + (if (op[1]) 1 else -1) *
                ((if (k[1]) 1 else 0))*(fieldImage.height + SPACING) * layer +
                ((if (placeMiddle) 1 else 0))*(fieldImage.height) / 2.0
        return arrayOf(x, y)
    }
    private fun initPiece(id:Int,
                          color: Color,
                          coordinates:Array<Double>) : Piece{
        val p = Piece(id, color)

        p.setOnMouseClicked {
            clickOnPiece(p)
        }
        p.translateX = coordinates[0]
        p.translateY = coordinates[1]

        return p
    }

    private fun loadUI(parent: Group, primaryStage: Stage){
        val uiMainVBox = VBox(10.0)
        VBox.setVgrow(uiMainVBox, Priority.ALWAYS)
        uiMainVBox.minWidth = WIDTH_UI.toDouble()
        uiMainVBox.alignment = Pos.CENTER

        val uiTitleLabel = utils.initLabel("Nine Men's Morris", 25.0, "Arial", FontWeight.BOLD)

        val playersHeaderHBox = utils.initHBox((WIDTH_UI/4).toDouble(), WIDTH_UI.toDouble(), 50.0)
        playersHeaderHBox.style = "-fx-background-color:#D3D3D3"
        val playerHeaderTexts = mutableListOf<Label>()
        playerHeaderTexts.add(createHeaderLabel(players[0].color))
        playerHeaderTexts.add(createHeaderLabel(players[1].color))

        val playersNameHBox = utils.initHBox((WIDTH_UI/4).toDouble(),  WIDTH_UI.toDouble(),30.0)
        playerNameText.add(utils.initLabel(players[0].name, 12.0, fontWeight=FontWeight.BOLD))
        playerNameText.add(utils.initLabel(players[1].name, 12.0))

        val line = Line(0.0, 0.0, 205.0, 0.0)
        line.strokeWidth = 2.5
        line.stroke = Color.GRAY

        val playersStatsHBox = utils.initHBox(spacing=20.0, minHeight=30.0, minWidth=WIDTH_UI.toDouble())

        val playersPropVBox = mutableListOf<VBox>()
        val playersSVBox1 = utils.initVBox(10.0,WIDTH_UI / 4.0, 30.0)
        val playersSVBox2 = utils.initVBox(10.0,WIDTH_UI / 4.0, 30.0)
        playersPropVBox.add(playersSVBox1)
        playersPropVBox.add(playersSVBox2)

        for (i in 0..1){
            val newPlayerStat = mutableMapOf<String, Label>()
            playerStatTexts.add(newPlayerStat)

            playerStatTexts[i]["pieces"] = utils.initLabel("Number of Pieces: 9", 12.0)
            playerStatTexts[i]["steps"] = utils.initLabel("Steps taken: 0", 12.0)
            playerStatTexts[i]["mills"] = utils.initLabel("Mills placed: 0", 12.0)

            for (pst in playerStatTexts[i]) {
                playersPropVBox[i].children.addAll(pst.value)
            }
        }
        instructionTextArea = TextArea("Instruction panel\n" +
                                "Here comes all the instructions")
        instructionTextArea.maxWidth = WIDTH_UI.toDouble() - 10.0
        instructionTextArea.isEditable = false
        instructionTextArea.isWrapText = true
        //instructionTextArea.style = "-fx-control-inner-background: #D3D3D3;"
        //instructionTextArea.alignment = Pos.CENTER

        timePassedLabel = utils.initLabel("Elapsed time: ..")

        pauseButton = Button("Pause Game")
        pauseButton.setOnAction {
            if (gameInProgress){
                pauseButton.text = "Continue Game"
                gameInProgress = false

                timeElapsed += System.nanoTime() - startTime
            }
            else{
                pauseButton.text = "Pause Game"
                gameInProgress = true

                timeInPaused += System.nanoTime() - pauseStartTime
                startTime = 0L
            }
        }
        val newGameButton = Button("New Game")
        newGameButton.setOnAction {
            val confirmationDialog = Alert(Alert.AlertType.CONFIRMATION)
            confirmationDialog.title = "Start a New Game"
            confirmationDialog.headerText = "Are you sure you want to start a new game?"
            confirmationDialog.contentText = "Your current game progress will be lost."

            val okButton = ButtonType("Start New Game", ButtonBar.ButtonData.OK_DONE)
            confirmationDialog.buttonTypes.setAll(okButton, ButtonType.CANCEL)

            val result = confirmationDialog.showAndWait()
            if (result.get() == okButton) {
                //Starting the main game frame
                val newGameStage = Stage()
                val newGameFrame = NewGameFrame()
                newGameFrame.start(newGameStage)
                //Closing the starter frame
                primaryStage?.close()
            }
        }

        uiMainVBox.children.addAll(uiTitleLabel, playersHeaderHBox, playersNameHBox, line,
            playersStatsHBox, instructionTextArea, timePassedLabel, pauseButton, newGameButton)
        playersHeaderHBox.children.addAll(playerHeaderTexts)
        playersNameHBox.children.addAll(playerNameText)
        playersStatsHBox.children.addAll(playersPropVBox)

        parent.children.add(uiMainVBox)
    }

    private fun placePieceOnField(field: Field, piece: Piece?){
        //Animated transition to the new fields
        val transition = TranslateTransition(Duration.seconds(1.0), piece)
        transition.toX = field.layoutX + (field.image.width - (piece?.image?.width ?: 0.0)) / 2.0
        transition.toY = field.layoutY + (field.image.height - (piece?.image?.height ?: 0.0)) / 2.0
        transition.play()
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

    /**
     * Adds or Removes graphical effect on a field where the player can place a piece
     */
    private fun addOrDelEffectOnField(middleField: Field?, addEff: Boolean){
        for (fsl in fields){
            for (f in fsl){
                if (addEff){
                    if (isStepCorrect(middleField, f, currentPlayerTurn)){

                        f.effect = DropShadow(10.0, Color.GREEN)
                    }
                }
                else f.effect = null
            }
        }
    }
    private fun isStepCorrect(fieldFrom: Field?, fieldTo: Field, playerIdx: Int): Boolean{
        return (fieldFrom?.isNeighbour(fieldTo) == true ||
                phase1Placing ||
                players[playerIdx].canFly) &&
                fieldTo.pieceStored == null
    }
    private fun fieldIdxToCoord(op: Array<Boolean>, k: Array<Boolean>) :Array<Int>{
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
        playerNameText[currentPlayerTurn].font =
            Font.font(playerNameText[currentPlayerTurn].font.family, FontWeight.NORMAL, playerNameText[currentPlayerTurn].font.size)
        currentPlayerTurn = getOtherPlayer()
        playerNameText[currentPlayerTurn].font =
            Font.font(playerNameText[currentPlayerTurn].font.family, FontWeight.BOLD, playerNameText[currentPlayerTurn].font.size)
    }
    private fun getOtherPlayer(): Int{
        return if (currentPlayerTurn == 0) 1 else 0
    }


    private fun clickOnPiece(p: Piece){
        if (gameInProgress) {
            //Checks player of the selected piece
            if (p.getColor == players[currentPlayerTurn].color && !phaseRemoving){
                //Checks if it's selected or unselected
                if (previouslySelectedPiece != p){
                    //Prevents players to select the already placed pieces in phase 1
                    if (phase1Placing && p.parentField == null){
                        selOrUnselPiece(p, true)
                        addOrDelEffectOnField(p.parentField, true)

                        setInstructionText("Figura kiválasztva, rakd le valahova")
                    }
                    else if (!phase1Placing) {
                        addOrDelEffectOnField(previouslySelectedPiece?.parentField, false)

                        selOrUnselPiece(p, true)
                        addOrDelEffectOnField(p.parentField, true)

                        setInstructionText("Figura kiválasztva, rakd le valahova")
                    }
                    else{
                        setInstructionText("Ezt már leraktad, mit csinálsz?!")
                    }
                }
                else{
                    selOrUnselPiece(p, false)
                    addOrDelEffectOnField(p.parentField, false)
                }
            }
            //Checks the other player in the Removing Phase
            else if(p.getColor != players[currentPlayerTurn].color && phaseRemoving){
                //Checks that the selected piece is removable
                if (p in removablePieces){
                    val transition = TranslateTransition(Duration.seconds(1.0), p)
                    transition.toX = 10.0
                    transition.toY = (if (currentPlayerTurn == 0) 10.0 else (HEIGHT-10.0-p.image.height))
                    transition.setOnFinished {
                        p.parentField?.pieceStored = null
                        p.parentField = null
                        p.isVisible = false
                        players[getOtherPlayer()].piecesList.remove(p)
                    }
                    transition.play()

                    updatePlayerStat("pieces", players[getOtherPlayer()].piecesList.size, getOtherPlayer())

                    changeRemovingPhase(false)
                    changePlayerTurn()
                    setInstructionText("meg volt a kukázás te jössz sry")
                    if(players[currentPlayerTurn].piecesList.size == 3){
                        players[currentPlayerTurn].canFly = true
                        setInstructionText("Mostmár ugrálhatsz te szerencsétlen xddx")
                    }
                    checkGameEnded()
                }
            }
            else {
                setInstructionText("NONO!")
            }
        }
    }
    private fun clickOnField(f: Field) {
        //Checks that a piece is selected
        if (previouslySelectedPiece != null) {
            //Checks that the selected field is neighbour OR the players can move freely
            if ((previouslySelectedPiece?.parentField?.isNeighbour(f) == true) ||
                phase1Placing || players[currentPlayerTurn].canFly)
            {
                //Checks that the field is empty
                if (f.pieceStored == null) {
                    addOrDelEffectOnField(previouslySelectedPiece?.parentField, false)

                    f.pieceStored = previouslySelectedPiece
                    previouslySelectedPiece?.parentField?.pieceStored = null
                    previouslySelectedPiece?.parentField = f
                    placePieceOnField(f, previouslySelectedPiece)
                    selOrUnselPiece(previouslySelectedPiece, false)

                    players[currentPlayerTurn].takenSteps += 1
                    updatePlayerStat("steps", players[currentPlayerTurn].takenSteps, currentPlayerTurn)
                    setInstructionText("Figura lerakva, MÁSIK játékos jön")

                    //In phase 1 we need to count the second player's placed pieces
                    if (phase1Placing && currentPlayerTurn == 1) {
                        countPiecesOnField += 1
                        //...when it's 9, it means all pieces have been placed, so phase 1 is over
                        if (countPiecesOnField >= 9) {
                            phase1Placing = false

                            setInstructionText("Nincs több pakolás ááááá")
                        }
                    }
                    //If there's a mill, the current player can remove a piece from the other player
                    if (checkMill(f)){
                        players[currentPlayerTurn].millsPlaced += 1
                        updatePlayerStat("mills", players[currentPlayerTurn].millsPlaced, currentPlayerTurn)
                        setInstructionText("MALOM VAN GECOO")
                        changeRemovingPhase(true)
                    }
                    //If there's no new mill, the game continuous
                    else{
                        changePlayerTurn()
                        checkGameEnded()
                    }
                }
                else {
                    setInstructionText("itt már van HE")
                }
            }
            else {
                setInstructionText("Nem szomszédos, mit csinálsz xdd")
            }
        }
    }

    private fun checkMill(placeField: Field?, checkedPlayer: Int = currentPlayerTurn): Boolean{
        var pieceCounterM = 0
        //Check middle lines where the layers are connected
        if (placeField?.getHorizontal == 2 || placeField?.getVertical == 2) {
            val fieldIdx = fields[placeField.getLayer-1].indexOf(placeField)
            for (i in 0..2){
                //Checks that the current player's pieces are on the checked fields
                if (fields[i][fieldIdx].pieceStored?.getColor == players[checkedPlayer].color){
                    pieceCounterM += 1
                }
            }
        }
        //Also checking vertically and horizontally in the layers
        var pieceCounterH = 0
        var pieceCounterV = 0
        for (i in 1..3){
            for (fl in fields[(placeField?.getLayer ?: 0) -1]){
                //Checks that the current player's pieces are on the checked fields
                if (fl.pieceStored?.getColor == players[checkedPlayer].color){
                    //Check horizontally
                    if (fl.getVertical == placeField?.getVertical && fl.getHorizontal == i) {
                        pieceCounterH += 1
                    }
                    //Check vertically
                    if (fl.getHorizontal == placeField?.getHorizontal && fl.getVertical == i) {
                        pieceCounterV += 1
                    }
                }
            }
        }
        return pieceCounterM == 3 || pieceCounterH == 3 || pieceCounterV == 3
    }
    private fun changeRemovingPhase(activatePhase: Boolean){
        phaseRemoving = activatePhase

        for (p in players[getOtherPlayer()].piecesList) {
            if (activatePhase){
                //Checks that the piece is removable. It is if it's placed and not in a mill
                if (p.parentField != null &&
                    !checkMill(p.parentField, getOtherPlayer())){
                    removablePieces.add(p)
                    p.effect = DropShadow(13.0, Color.RED)
                }
            }
            else{
                p.effect = null
            }
        }
        //If there are no removable pieces, then the Removing Phase should be ended
        if(removablePieces.size == 0) {
            phaseRemoving = false
            setInstructionText("Talán legközlebb ahahhahah")
            changePlayerTurn()
        }
        //Clears the list of the removable list at the end of the phase
        if (!activatePhase) removablePieces.clear()
    }
    private fun checkGameEnded(){
        for (player in players){
            //The game ends when one player has less than 3 pieces
            if (player.piecesList.size < 3){
                gameInProgress = false
                setInstructionText("VÉGEEEE (mert nincs több bábu): " + player.name)
                pauseButton.isDisable = true
            }
            //Or if a player's all pieces are surrounded, so that player can't move
            var playerStucked = true
            for (piece in player.piecesList){
                for (fsl in fields){
                    for (f in fsl){
                        if (isStepCorrect(piece.parentField, f, players.indexOf(player))){
                            playerStucked = false
                            break
                        }
                    }
                }
            }
            if (playerStucked){
                gameInProgress = false
                setInstructionText("VÉGEEEE (mert nem tudsz lépni): " + player.name)
                pauseButton.isDisable = true
            }
        }
    }
    private fun updatePlayerStat(type:String, newValue: Int, playerIdx: Int){
        when(type){
            "pieces" -> playerStatTexts[playerIdx][type]?.text = "Number of Pieces: $newValue"
            "steps" -> playerStatTexts[playerIdx][type]?.text = "Steps taken: $newValue"
            "mills" -> playerStatTexts[playerIdx][type]?.text = "Mills placed: $newValue"
        }
    }
}
