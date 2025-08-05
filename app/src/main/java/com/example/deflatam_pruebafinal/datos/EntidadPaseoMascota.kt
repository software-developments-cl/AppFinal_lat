package com.example.deflatam_pruebafinal.datos

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import kotlin.random.Random

//Entidad mientras construyen este archivo. para que no me de error


@Entity(tableName = "paseos_mascotas")
data class EntidadPaseoMascota(
    @PrimaryKey(autoGenerate = true)
    val id: Long = Random.nextLong(),
    val nombreMascota: String, // Nombre de la mascota (ej: "Firulais")
    val tipoMascota: String, // Tipo: Perro, Gato, Conejo, Otro
    val nombreCliente: String, // Nombre del dueño
    val duracionHoras: Double, // Cuántas horas duró el paseo
    val tarifaPorHora: Double, // Cuánto cobramos por hora
    val montoTotal: Double, // Total a cobrar (horas × tarifa)
    val estaPagado: Boolean, // ¿Ya nos pagaron? true/false
    val fecha: Date, // Cuándo fue el paseo
    val notas: String = "", // Comentarios extra
    val fotoMascotaUri: String? = null // URI de la foto de la mascota

)