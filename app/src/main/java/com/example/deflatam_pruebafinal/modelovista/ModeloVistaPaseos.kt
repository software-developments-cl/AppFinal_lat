package com.example.deflatam_pruebafinal.modelovista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deflatam_pruebafinal.datos.EntidadPaseoMascota
import com.example.deflatam_pruebafinal.repositorio.RepositorioPaseosMascotas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    init {
        // Cuando se crea el ViewModel, cargar todos los datos
        cargarDatos()
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
    fun agregarPaseo() {
        viewModelScope.launch {
            val horas = _duracionHoras.value.toDoubleOrNull() ?: 0.0
            val tarifa = _tarifaPorHora.value.toDoubleOrNull() ?: 0.0
            val total = horas * tarifa

            // Crear el nuevo paseo
            val nuevoPaseo = EntidadPaseoMascota(
                nombreMascota = _nombreMascota.value,
                tipoMascota = _tipoMascota.value,
                nombreCliente = _nombreCliente.value,
                duracionHoras = horas,
                tarifaPorHora = tarifa,
                montoTotal = total,
                estaPagado = false, // Nuevo paseo siempre empieza como "no pagado"
                fecha = Date(), // Fecha actual
                notas = _notas.value
            )

            // Guardarlo en la base de datos
            repositorio.agregarPaseo(nuevoPaseo)
            // Limpiar el formulario
            limpiarFormulario()
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
    fun eliminarPaseo(paseo: EntidadPaseoMascota) {
        viewModelScope.launch {
            repositorio.eliminarPaseo(paseo)
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
    }

    // Verificar si el formulario tiene todos los datos necesarios
    fun formularioEsValido(): Boolean {
        return _nombreMascota.value.isNotBlank() &&
                _nombreCliente.value.isNotBlank() &&
                _duracionHoras.value.toDoubleOrNull() != null &&
                _tarifaPorHora.value.toDoubleOrNull() != null
    }
}
