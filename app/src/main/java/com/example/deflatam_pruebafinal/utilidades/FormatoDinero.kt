package com.example.deflatam_pruebafinal.utilidades

import java.text.NumberFormat
import java.util.Locale

fun FormatoDinero(valor: Int): String {
    val formato = NumberFormat.getNumberInstance(Locale("es", "CL"))  // Formato chileno
    return "$${formato.format(valor)}"
}
/*
fun main() {
    val numero = 120000
    val numeroFormateado = FormatoDinero(numero)
    println(numeroFormateado)  // Salida: $120.000
}*/

