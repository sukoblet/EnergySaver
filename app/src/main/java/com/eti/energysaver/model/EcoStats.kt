package com.eti.energysaver.model

data class EcoStats(
    val energySavings: Double = 0.0,  // Зекономлена енергія (кВт·год)
    val moneySavings: Double = 0.0,   // Заощаджені кошти (PLN)
    val co2Savings: Double = 0.0      // Зменшення викидів CO₂ (кг)
)
