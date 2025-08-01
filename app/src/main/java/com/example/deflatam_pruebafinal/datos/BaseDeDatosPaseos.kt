package com.example.deflatam_pruebafinal.datos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/** Configuración principal de nuestra base de datos
 * @param entities Las tablas que tenemos
 * @param version La versión de la base de datos
 * @param exportSchema No exportar esquema por ahora*/
@Database(
    entities = [EntidadPaseoMascota::class], // Las tablas que tenemos
    version = 1, // Versión de la base de datos
    exportSchema = false // No exportar esquema por ahora
)
@TypeConverters(ConvertidoresDeTipo::class) // Usar nuestro convertidor de fechas
abstract class BaseDeDatosPaseos : RoomDatabase() {

    /** Función para acceder a nuestro DAO */
    abstract fun accesoDatosPaseos(): AccesoDatosPaseos

    companion object {

        /** Instancia de la base de datos */
        @Volatile
        private var INSTANCIA: BaseDeDatosPaseos? = null

        /** Función para obtener la instancia de la base de datos (patrón Singleton) */
        fun obtenerBaseDeDatos(context: Context): BaseDeDatosPaseos {
            // Si ya existe la instancia, la devolvemos
            return INSTANCIA ?: synchronized(this) {
                // Si no existe, se crea
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    BaseDeDatosPaseos::class.java,
                    "base_datos_paseos" // Nombre del archivo de la base de datos
                ).build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}