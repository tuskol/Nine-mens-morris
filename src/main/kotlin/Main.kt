//KotlinHáziFeladat - Malom
//Mérnökinfó - Kotlin - 2023/tavaszi félév
//Készítette: Tuskó László István (CGTRRV)

import frames.GameFrame
import javafx.application.Application

fun main(args: Array<String>) {
    println("Kotlin Hazi elindult")
    Application.launch(GameFrame::class.java, *args)
    println("Kotlin Hazi bezarult")
}
