package com.example.deflatam_pruebafinal.utilidades

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatoFecha{


    fun formatearFecha(fecha: Date): String {
        val formato = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "CL"))
        return formato.format(fecha)
    }

}

