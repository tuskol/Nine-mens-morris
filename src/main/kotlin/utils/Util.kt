package utils

import frames.GameFrame
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight

fun getResource(filename: String): String {
    return GameFrame::class.java.getResource(filename).toString()
}

fun initLabel(text:String,
              size:Double=15.0,
              fontFamily:String="System",
              fontWeight: FontWeight = FontWeight.NORMAL) : Label {
    val label = Label(text)
    label.font = Font.font(fontFamily, fontWeight, size)
    return label
}

fun initHBox(spacing: Double = 10.0,
             minWidth: Double = 100.0,
             minHeight: Double = 30.0): HBox {
    val nHBox = HBox(spacing)
    HBox.setHgrow(nHBox, Priority.ALWAYS)
    nHBox.minWidth = minWidth
    nHBox.minHeight = minHeight
    nHBox.alignment = Pos.CENTER
    return nHBox
}

fun initVBox(spacing: Double = 10.0,
             minWidth: Double = 100.0,
             minHeight: Double = 100.0): VBox {
    val nVBox = VBox(spacing)
    VBox.setVgrow(nVBox, Priority.ALWAYS)
    nVBox.minWidth = minWidth
    nVBox.minHeight = minHeight
    nVBox.alignment = Pos.CENTER
    return nVBox
}

fun createHeaderLabel(color: Color): Label{
    val hLabel: Label
    val text = when(color){
        Color.WHITE -> "WHITE"
        Color.BLACK -> "BLACK"
        else -> "NOTANOPTION"
    }
    hLabel = initLabel(text, 15.0, fontWeight= FontWeight.BOLD)
    hLabel.textFill = color
    return hLabel
}