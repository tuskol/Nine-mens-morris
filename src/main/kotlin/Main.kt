//KotlinHáziFeladat - Malom
//Mérnökinfó - Kotlin - 2023/tavaszi félév
//Készítette: Tuskó László István (CGTRRV)

import com.example.Game
import javafx.application.Application

fun main(args: Array<String>) {
    println("Kotlin Hazi elindult")
    Application.launch(Game::class.java, *args)
    println("Kotlin Hazi bezarult")
}
