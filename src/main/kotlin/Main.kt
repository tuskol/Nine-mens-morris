//KotlinHáziFeladat - Malom
//Mérnökinfó - Kotlin - 2023/tavaszi félév
//Készítette: Tuskó László István (CGTRRV)

import frames.NewGameFrame
import javafx.application.Application

fun main(args: Array<String>) {
    println("Kotlin HW started")
    Application.launch(NewGameFrame::class.java, *args)
    println("Kotlin HW closed")
}
