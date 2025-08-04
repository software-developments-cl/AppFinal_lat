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
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

// El ViewModel es como el "cerebro" de la aplicación
// Maneja toda la lógica y conecta la interfaz con los datos
class ModeloVistaPaseos(private val repositorio: RepositorioPaseosMascotas) : ViewModel() {

    // Estados para mostrar información en la interfaz
    private val _paseos = MutableStateFlow<List<EntidadPaseoMascota>>(emptyList())
    val paseos: StateFlow<List<EntidadPaseoMascota>> = _paseos.asStateFlow()

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
    
    // Necesitarás una forma de establecer la fecha y hora del paseo desde la UI
    private val _fechaHoraPaseo = MutableStateFlow<Date>(Date()) // Inicializa con la fecha actual, actualiza desde la UI
    val fechaHoraPaseo: StateFlow<Date> = _fechaHoraPaseo.asStateFlow()

    init {
        // Cuando se crea el ViewModel, cargar todos los datos
        cargarDatos()
    }
    
    fun actualizarFechaHoraPaseo(date: Date) {
        _fechaHoraPaseo.value = date
    }

    // Función para cargar todos los datos desde la base de datos
    private fun cargarDatos() {
        // Cargar lista de paseos
        viewModelScope.launch {
            repositorio.obtenerTodosLosPaseos().collect { listaPaseos ->
                _paseos.value = listaPaseos
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
        // Permitir solo números y limitar a 9 caracteres
        val duracionFiltrada = duracion.filter { it.isDigit() }
        if (duracionFiltrada.length <= 9) {
            _duracionHoras.value = duracionFiltrada
        } else {
            // Opcional: si excede, se trunca a 9. O podrías no hacer nada
            // y mantener el valor anterior o el valor truncado.
             _duracionHoras.value = duracionFiltrada.take(9)
        }
    }

    fun actualizarTarifaPorHora(tarifa: String) {
        // Permitir solo números y limitar a 9 caracteres
        val tarifaFiltrada = tarifa.filter { it.isDigit() }
        if (tarifaFiltrada.length <= 9) {
            _tarifaPorHora.value = tarifaFiltrada
        } else {
            // Opcional: si excede, se trunca a 9.
            _tarifaPorHora.value = tarifaFiltrada.take(9)
        }
    }

    fun actualizarNotas(notas: String) {
        _notas.value = notas
    }

    // Calcular automáticamente cuánto cobrar (horas × tarifa por hora)
    fun calcularMontoTotal(): Double {
        val horas = _duracionHoras.value.toDoubleOrNull() ?: 0.0
        val tarifa = _tarifaPorHora.value.toDoubleOrNull() ?: 0.0
        return horas * tarifa
    }

    // Agregar un nuevo paseo a la base de datos
    fun agregarPaseo(context: Context) {
        viewModelScope.launch {
            val horas = _duracionHoras.value.toDoubleOrNull() ?: 0.0
            val tarifa = _tarifaPorHora.value.toDoubleOrNull() ?: 0.0
            val total = horas * tarifa

            // Crear el nuevo paseo
            // IMPORTANTE: Asegúrate que _fechaHoraPaseo.value contenga la FECHA Y HORA FUTURA del paseo
            // y no solo la fecha actual al momento de la creación.
            val nuevoPaseo = EntidadPaseoMascota(
                nombreMascota = _nombreMascota.value,
                tipoMascota = _tipoMascota.value,
                nombreCliente = _nombreCliente.value,
                duracionHoras = horas,
                tarifaPorHora = tarifa,
                montoTotal = total,
                estaPagado = false, // Nuevo paseo siempre empieza como "no pagado"
                fecha = _fechaHoraPaseo.value, // USA LA FECHA Y HORA SELECCIONADA PARA EL PASEO
                notas = _notas.value
            )

            // Guardarlo en la base de datos
            val paseoGuardado = repositorio.agregarPaseo(nuevoPaseo) // Asumiendo que retorna el objeto con ID o actualiza el ID.

            // Si el ID es 0 o no es único, las alarmas se sobrescribirán.
            if (paseoGuardado.id != 0L) { // o la forma que tengas para verificar que el ID es válido
                 programarRecordatorioPaseo(context, paseoGuardado)
            } else {
                // Manejar el caso donde el ID no es válido (opcional, pero recomendado)
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

        // Usar paseo.id para el requestCode del PendingIntent para asegurar unicidad
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            paseo.id.toInt(), // requestCode debe ser único para cada alarma
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calcular tiempo de recordatorio: 1 minuto después de la creación del paseo
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis() // Hora actual
        calendar.add(Calendar.MINUTE, 1) // Añadir 1 minuto
        val triggerTime = calendar.timeInMillis

        // Asegurarse de que el recordatorio sea en el futuro (siempre debería serlo en este caso)
        if (triggerTime > System.currentTimeMillis()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        Log.d("ModeloVistaPaseos", "Alarma programada para el paseo ID: ${paseo.id} con triggerTime: $triggerTime (1 min después de creación)")
                    } else {
                        // Opcional: Informar al usuario o recurrir a una alarma no exacta.
                        Log.w("ModeloVistaPaseos", "No se pueden programar alarmas exactas.")
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    Log.d("ModeloVistaPaseos", "Alarma programada para el paseo ID: ${paseo.id} con triggerTime: $triggerTime (1 min después de creación)")
                }
            } catch (e: SecurityException) {
                Log.e("ModeloVistaPaseos", "SecurityException al programar alarma: ${e.message}")
                // Manejar la excepción, por ejemplo, informando al usuario.
            }
        } else {
             // Este caso ahora es mucho menos probable, a menos que el reloj del sistema salte hacia atrás.
             Log.w("ModeloVistaPaseos", "El tiempo de recordatorio ya pasó (1 min después de creación), no se programará.")
        }
    }


    // Cambiar el estado de pago de un paseo (pagado ↔ pendiente)
    fun cambiarEstadoPago(paseo: EntidadPaseoMascota) {
        viewModelScope.launch {
            // Crear una copia del paseo con el estado opuesto
            val paseoActualizado = paseo.copy(estaPagado = !paseo.estaPagado)
            repositorio.actualizarPaseo(paseoActualizado)
        }
    }

    // Eliminar un paseo de la base de datos
    fun eliminarPaseo(paseo: EntidadPaseoMascota, context: Context) {
        viewModelScope.launch {
            // Antes de eliminar, cancelar cualquier recordatorio programado para este paseo
            cancelarRecordatorioPaseo(context, paseo)
            repositorio.eliminarPaseo(paseo)
        }
    }
    
    // Función para cancelar un recordatorio programado
    private fun cancelarRecordatorioPaseo(context: Context, paseo: EntidadPaseoMascota) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.EXTRA_PASEO_ID, paseo.id)
            putExtra(NotificationReceiver.EXTRA_MASCOTA_NOMBRE, paseo.nombreMascota)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            paseo.id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // FLAG_NO_CREATE para verificar si existe y cancelarlo
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("ModeloVistaPaseos", "Recordatorio cancelado para el paseo ID: ${paseo.id}")
        }
    }


    // Limpiar todos los campos del formulario
    private fun limpiarFormulario() {
        _nombreMascota.value = ""
        _tipoMascota.value = "Perro"
        _nombreCliente.value = ""
        _duracionHoras.value = ""
        _tarifaPorHora.value = ""
        _notas.value = ""
        _fechaHoraPaseo.value = Date() // Resetear la fecha también
    }

    // Verificar si el formulario tiene todos los datos necesarios
    fun formularioEsValido(): Boolean {
        return _nombreMascota.value.isNotBlank() &&
                _nombreCliente.value.isNotBlank() &&
                _duracionHoras.value.toDoubleOrNull() != null &&
                _tarifaPorHora.value.toDoubleOrNull() != null
    }
}
