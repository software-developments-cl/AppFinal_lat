package com.example.deflatam_pruebafinal.utilidades

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Formatear fecha en formato dd/mm/yyyy
fun FormatoFecha(fecha: Date): String {
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))
    return formato.format(fecha)
}


