package com.example.deflatam_pruebafinal.utilidades

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatoFecha {


    //Funcion para convertir un timestamp a una cadena de fecha en un formato específico.
    fun timestampAFechaString(timestamp: Long, patron: String = "dd/MM/yyyy"): String {
        val sdf = SimpleDateFormat(patron, Locale.getDefault())
        val fecha = Date(timestamp)
        return sdf.format(fecha)
    }

}
