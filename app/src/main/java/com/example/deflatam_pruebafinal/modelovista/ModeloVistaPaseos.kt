package com.example.deflatam_pruebafinal.modelovista

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deflatam_pruebafinal.datos.EntidadPaseoMascota
import com.example.deflatam_pruebafinal.repositorio.RepositorioPaseosMascotas
import com.example.deflatam_pruebafinal.utilidades.NotificationReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine // Asegúrate de tener esta importación
import kotlinx.coroutines.flow.SharingStarted // Asegúrate de tener esta importación
import kotlinx.coroutines.flow.stateIn // Asegúrate de tener esta importación
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

// El ViewModel es como el "cerebro" de la aplicación
// Maneja toda la lógica y conecta la interfaz con los datos
class ModeloVistaPaseos(private val repositorio: RepositorioPaseosMascotas) : ViewModel() {

    // Lista original de todos los paseos, sin filtrar
    private val _todosLosPaseos = MutableStateFlow<List<EntidadPaseoMascota>>(emptyList())

    // Término de búsqueda actual
    private val _terminoBusqueda = MutableStateFlow("")
    val terminoBusqueda: StateFlow<String> = _terminoBusqueda.asStateFlow()

    // StateFlow público de paseos filtrados
    val paseos: StateFlow<List<EntidadPaseoMascota>> =
        combine(_todosLosPaseos, _terminoBusqueda) { listaCompleta, termino ->
            if (termino.isBlank()) {
                listaCompleta
            } else {
                listaCompleta.filter {
                    it.nombreCliente.contains(termino, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    private val _totalGanado = MutableStateFlow(0.0)
    val totalGanado: StateFlow<Double> = _totalGanado.asStateFlow()

    private val _totalPendiente = MutableStateFlow(0.0)
    val totalPendiente: StateFlow<Double> = _totalPendiente.asStateFlow()

    // Estados para el formulario de nuevo paseo
    private val _nombreMascota = MutableStateFlow("")
    val nombreMascota: StateFlow<String> = _nombreMascota.asStateFlow()

    private val _tipoMascota = MutableStateFlow("Perro")
    val tipoMascota: StateFlow<String> = _tipoMascota.asStateFlow()

    private val _nombreCliente = MutableStateFlow("")
    val nombreCliente: StateFlow<String> = _nombreCliente.asStateFlow()

    private val _duracionHoras = MutableStateFlow("")
    val duracionHoras: StateFlow<String> = _duracionHoras.asStateFlow()

    private val _tarifaPorHora = MutableStateFlow("")
    val tarifaPorHora: StateFlow<String> = _tarifaPorHora.asStateFlow()

    private val _notas = MutableStateFlow("")
    val notas: StateFlow<String> = _notas.asStateFlow()

    private val _fechaHoraPaseo = MutableStateFlow<Date>(Date())
    val fechaHoraPaseo: StateFlow<Date> = _fechaHoraPaseo.asStateFlow()

    init {
        // Cuando se crea el ViewModel, cargar todos los datos
        cargarDatosCompletos()
    }

    fun actualizarTerminoBusqueda(termino: String) {
        _terminoBusqueda.value = termino
    }

    fun actualizarFechaHoraPaseo(date: Date) {
        _fechaHoraPaseo.value = date
    }

    // Función para cargar todos los datos desde la base de datos
    private fun cargarDatosCompletos() {
        // Cargar lista de todos los paseos
        viewModelScope.launch {
            repositorio.obtenerTodosLosPaseos().collect { listaPaseos ->
                _todosLosPaseos.value = listaPaseos // Actualiza la lista original completa
            }
        }

        // Cargar total ganado
        viewModelScope.launch {
            repositorio.obtenerTotalGanado().collect { total ->
                _totalGanado.value = total ?: 0.0
            }
        }

        // Cargar total pendiente
        viewModelScope.launch {
            repositorio.obtenerTotalPendiente().collect { total ->
                _totalPendiente.value = total ?: 0.0
            }
        }
    }

    // Funciones para actualizar los campos del formulario
    fun actualizarNombreMascota(nombre: String) {
        _nombreMascota.value = nombre
    }

    fun actualizarTipoMascota(tipo: String) {
        _tipoMascota.value = tipo
    }

    fun actualizarNombreCliente(nombre: String) {
        _nombreCliente.value = nombre
    }

    fun actualizarDuracionHoras(duracion: String) {
        val duracionFiltrada = duracion.filter { it.isDigit() }
        _duracionHoras.value = if (duracionFiltrada.length <= 9) duracionFiltrada else duracionFiltrada.take(9)
    }

    fun actualizarTarifaPorHora(tarifa: String) {
        val tarifaFiltrada = tarifa.filter { it.isDigit() }
        _tarifaPorHora.value = if (tarifaFiltrada.length <= 9) tarifaFiltrada else tarifaFiltrada.take(9)
    }

    fun actualizarNotas(notas: String) {
        _notas.value = notas
    }

    fun calcularMontoTotal(): Double {
        val horas = _duracionHoras.value.toDoubleOrNull() ?: 0.0
        val tarifa = _tarifaPorHora.value.toDoubleOrNull() ?: 0.0
        return horas * tarifa
    }

    fun agregarPaseo(context: Context) {
        viewModelScope.launch {
            val horas = _duracionHoras.value.toDoubleOrNull() ?: 0.0
            val tarifa = _tarifaPorHora.value.toDoubleOrNull() ?: 0.0
            val total = horas * tarifa

            val nuevoPaseo = EntidadPaseoMascota(
                nombreMascota = _nombreMascota.value,
                tipoMascota = _tipoMascota.value,
                nombreCliente = _nombreCliente.value,
                duracionHoras = horas,
                tarifaPorHora = tarifa,
                montoTotal = total,
                estaPagado = false,
                fecha = _fechaHoraPaseo.value,
                notas = _notas.value
            )

            val paseoGuardado = repositorio.agregarPaseo(nuevoPaseo)

            if (paseoGuardado.id != 0L) {
                programarRecordatorioPaseo(context, paseoGuardado)
            } else {
                Log.e("ModeloVistaPaseos", "ID de paseo no válido, no se puede programar recordatorio.")
            }
            limpiarFormulario()
        }
    }

    private fun programarRecordatorioPaseo(context: Context, paseo: EntidadPaseoMascota) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.EXTRA_PASEO_ID, paseo.id)
            putExtra(NotificationReceiver.EXTRA_MASCOTA_NOMBRE, paseo.nombreMascota)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            paseo.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis() // Hora actual
        calendar.add(Calendar.MINUTE, 1) // Añadir 1 minuto
        val triggerTime = calendar.timeInMillis
        Log.d("ModeloVistaPaseos", "Recordatorio para ${paseo.nombreMascota} (ID: ${paseo.id}) se programará para 1 minuto después de la creación.")
        Log.d("ModeloVistaPaseos", "Hora actual (millis): ${System.currentTimeMillis()}")
        Log.d("ModeloVistaPaseos", "Tiempo de disparo calculado (millis): $triggerTime")
        Log.d("ModeloVistaPaseos", "Tiempo de disparo calculado (fecha): ${Date(triggerTime)}")


        if (triggerTime > System.currentTimeMillis()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        Log.d("ModeloVistaPaseos", "Alarma programada para el paseo ID: ${paseo.id} con triggerTime: $triggerTime (1 min después de creación)")
                    } else {
                        Log.w("ModeloVistaPaseos", "No se pueden programar alarmas exactas.")
                        // Considerar una alternativa o informar al usuario
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    Log.d("ModeloVistaPaseos", "Alarma programada para el paseo ID: ${paseo.id} con triggerTime: $triggerTime (1 min después de creación)")
                }
            } catch (e: SecurityException) {
                Log.e("ModeloVistaPaseos", "SecurityException al programar alarma: ${e.message}")
            }
        } else {
             Log.w("ModeloVistaPaseos", "El tiempo de recordatorio ya pasó (1 min después de creación), no se programará. Paseo ID: ${paseo.id}, TriggerTime: $triggerTime")
        }
    }

    fun cambiarEstadoPago(paseo: EntidadPaseoMascota) {
        viewModelScope.launch {
            val paseoActualizado = paseo.copy(estaPagado = !paseo.estaPagado)
            repositorio.actualizarPaseo(paseoActualizado)
        }
    }

    fun eliminarPaseo(paseo: EntidadPaseoMascota, context: Context) {
        viewModelScope.launch {
            cancelarRecordatorioPaseo(context, paseo)
            repositorio.eliminarPaseo(paseo)
        }
    }

    private fun cancelarRecordatorioPaseo(context: Context, paseo: EntidadPaseoMascota) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.EXTRA_PASEO_ID, paseo.id)
            // No es estrictamente necesario pasar el nombre aquí, pero no hace daño
            putExtra(NotificationReceiver.EXTRA_MASCOTA_NOMBRE, paseo.nombreMascota)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            paseo.id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("ModeloVistaPaseos", "Recordatorio cancelado para el paseo ID: ${paseo.id}")
        }
    }

    private fun limpiarFormulario() {
        _nombreMascota.value = ""
        _tipoMascota.value = "Perro" // O el valor por defecto que prefieras
        _nombreCliente.value = ""
        _duracionHoras.value = ""
        _tarifaPorHora.value = ""
        _notas.value = ""
        _fechaHoraPaseo.value = Date() // Resetear la fecha también
    }

    fun formularioEsValido(): Boolean {
        return _nombreMascota.value.isNotBlank() &&
                _nombreCliente.value.isNotBlank() &&
                _duracionHoras.value.toDoubleOrNull() != null &&
                _tarifaPorHora.value.toDoubleOrNull() != null
    }
}
