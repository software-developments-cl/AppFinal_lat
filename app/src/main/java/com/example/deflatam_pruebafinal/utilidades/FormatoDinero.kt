package com.example.deflatam_pruebafinal.utilidades

import java.text.NumberFormat
import java.util.Locale

object FormatoDinero {

    fun enteroAStringDineroChileno(monto: Int): String {
        // Locale para Chile (español, Chile)
        val localeChileno = Locale("es", "CL")
        val formatoMoneda = NumberFormat.getCurrencyInstance(localeChileno)
        
        // El NumberFormat para CLP por defecto no muestra fracciones de moneda.
        // Si en algún caso se necesitara forzarlo para este u otro Locale:
        // formatoMoneda.maximumFractionDigits = 0
        // formatoMoneda.minimumFractionDigits = 0

        return formatoMoneda.format(monto)
    }
}
