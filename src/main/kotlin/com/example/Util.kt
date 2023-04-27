package com.example

import frames.GameFrame

fun getResource(filename: String): String {
    return GameFrame::class.java.getResource(filename).toString()
}
