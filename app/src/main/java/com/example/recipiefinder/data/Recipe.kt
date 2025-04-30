package com.example.recipiefinder

import java.io.Serializable

data class Recipe(
    val name: String,
    val ingredients: List<String>,
    val instructions: List<String>
) : Serializable
