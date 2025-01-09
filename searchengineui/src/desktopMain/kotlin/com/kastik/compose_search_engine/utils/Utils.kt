package com.kastik.compose_search_engine.utils

import java.awt.Desktop
import java.io.File

fun openFileInBrowser(filePath: String) {
    try {
        val file = File(filePath)
        if (file.exists()) {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(file.toURI()) // Convert file path to a URI and open in the browser
            } else {
                println("Browsing is not supported on this system.")
            }
        } else {
            println("File does not exist: $filePath")
        }
    } catch (e: Exception) {
        println("Error opening file in browser: ${e.message}")
    }
}



fun getPageDisplayIndices(numberOfPages: Int, selectedIndex: Int): List<Int> {
    println("Number of pages: $numberOfPages")
        if (numberOfPages <= 10) {
        return (1..numberOfPages).toList()
    }

    val pagination = mutableListOf<Int>()
    when {
        selectedIndex <= 4 -> {
            pagination.addAll((1..5))
            pagination.add(-1) // -1 = dots
            pagination.add(numberOfPages)
        }
        selectedIndex >= numberOfPages - 3 -> {
            pagination.add(1)
            pagination.add(-1) // -1 = dots
            pagination.addAll((numberOfPages - 4)..numberOfPages)
        }
        else -> {
            pagination.add(1)
            pagination.add(-1) // -1 = dots
            pagination.addAll((selectedIndex - 1)..(selectedIndex + 1))
            pagination.add(-1) // -1 = dots
            pagination.add(numberOfPages)
        }
    }
    return pagination
}