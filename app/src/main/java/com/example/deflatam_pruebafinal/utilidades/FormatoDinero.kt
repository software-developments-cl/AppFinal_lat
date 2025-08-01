package com.example.deflatam_pruebafinal.utilidades

import java.text.NumberFormat
import java.util.Locale

fun FormatoDinero(valor: Double): String {
    val formato = NumberFormat.getNumberInstance(Locale("es", "CL"))  // Formato chileno
    return "$${formato.format(valor)}"
}

