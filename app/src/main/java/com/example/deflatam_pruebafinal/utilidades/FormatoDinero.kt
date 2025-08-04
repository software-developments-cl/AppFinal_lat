package com.example.deflatam_pruebafinal.utilidades

import java.text.NumberFormat
import java.util.Locale

object FormatoDinero {
    fun enteroAStringDineroChileno(monto: Int): String {
        val formato = NumberFormat.getNumberInstance(Locale("es", "CL"))
        // Para evitar decimales no deseados con el formato de número,
        // y asegurar que se trate como un entero para la moneda.
        return "$${formato.format(monto.toLong())}"
    }
}
