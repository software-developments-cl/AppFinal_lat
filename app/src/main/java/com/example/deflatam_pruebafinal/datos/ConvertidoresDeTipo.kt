package com.example.deflatam_pruebafinal.datos

import androidx.room.TypeConverter
import java.util.Date

/**
 * Clase que proporciona convertidores de tipo para Room.
 * Room no sabe cómo guardar objetos Date directamente, por lo que necesitamos
 * convertirlos a un tipo primitivo (Long) que Room pueda manejar.
 */
class ConvertidoresDeTipo {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        // Convierte un Long (timestamp) de la base de datos a un objeto Date.
        return value?.let { timestamp -> Date(timestamp) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        // Convierte un objeto Date a un Long (timestamp) para guardarlo en la base de datos.
        return date?.time
    }
}