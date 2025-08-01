package com.example.deflatam_pruebafinal.datos

import androidx.room.Entity
import androidx.room.PrimaryKey

//Entidad mientras construyen este archivo. para que no me de error


@Entity(tableName = "paseos_mascotas")
data class EntidadPaseoMascota(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fecha: Long = System.currentTimeMillis(),
    val estaPagado: Int = 0,
    val montoTotal: Double = 0.0
)