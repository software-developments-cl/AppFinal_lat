package com.example.deflatam_pruebafinal.modelovista


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deflatam_pruebafinal.entidades.EntidadPaseoMascota
import com.example.deflatam_pruebafinal.repositorio.RepositorioPaseos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class ModeloVistaPaseos(
    private val repositorio: RepositorioPaseosMascotas
) : ViewModel() {

    // Estados consolidados para mejor manejo
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Estados para el formulario
    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    init {
        cargarDatos()
        setupObservers()
    }

    private fun setupObservers() {
        viewModelScope.launch {
            combine(
                repositorio.obtenerTodosLosPaseos(),
                repositorio.obtenerTotalGanado(),
                repositorio.obtenerTotalPendiente()
            ) { paseos, ganado, pendiente ->
                Triple(paseos, ganado ?: 0.0, pendiente ?: 0.0)
            }.collect { (paseos, ganado, pendiente) ->
                _uiState.update { current ->
                    current.copy(
                        paseos = paseos,
                        totalGanado = ganado,
                        totalPendiente = pendiente
                    )
                }
            }
        }
    }

    // Funciones para actualizar el formulario
    fun actualizarFormulario(
        nombreMascota: String = _formState.value.nombreMascota,
        tipoMascota: String = _formState.value.tipoMascota,
        nombreCliente: String = _formState.value.nombreCliente,
        duracionHoras: String = _formState.value.duracionHoras,
        tarifaPorHora: String = _formState.value.tarifaPorHora,
        notas: String = _formState.value.notas,
        fecha: LocalDate = _formState.value.fecha
    ) {
        _formState.update { current ->
            current.copy(
                nombreMascota = nombreMascota,
                tipoMascota = tipoMascota,
                nombreCliente = nombreCliente,
                duracionHoras = duracionHoras,
                tarifaPorHora = tarifaPorHora,
                notas = notas,
                fecha = fecha
            )
        }
    }

    fun agregarPaseo() {
        if (!formularioEsValido()) return

        viewModelScope.launch {
            val form = _formState.value
            val horas = form.duracionHoras.toDoubleOrNull() ?: 0.0
            val tarifa = form.tarifaPorHora.toDoubleOrNull() ?: 0.0
            val total = horas * tarifa

            val nuevoPaseo = EntidadPaseoMascota(
                nombreMascota = form.nombreMascota,
                tipoMascota = form.tipoMascota,
                nombreCliente = form.nombreCliente,
                duracionHoras = horas,
                tarifaPorHora = tarifa,
                montoTotal = total,
                estaPagado = false,
                fecha = Date.from(form.fecha.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                notas = form.notas
            )

            repositorio.agregarPaseo(nuevoPaseo)
            limpiarFormulario()
        }
    }

    fun cambiarEstadoPago(paseo: EntidadPaseoMascota) {
        viewModelScope.launch {
            repositorio.actualizarPaseo(paseo.copy(estaPagado = !paseo.estaPagado))
        }
    }

    fun eliminarPaseo(paseo: EntidadPaseoMascota) {
        viewModelScope.launch {
            repositorio.eliminarPaseo(paseo)
        }
    }

    private fun limpiarFormulario() {
        _formState.value = FormState(tipoMascota = "Perro")
    }

    fun formularioEsValido(): Boolean {
        val form = _formState.value
        return form.nombreMascota.isNotBlank() &&
                form.nombreCliente.isNotBlank() &&
                form.duracionHoras.toDoubleOrNull() != null &&
                form.tarifaPorHora.toDoubleOrNull() != null
    }

    // Data classes para manejar estados
    data class UiState(
        val paseos: List<EntidadPaseoMascota> = emptyList(),
        val totalGanado: Double = 0.0,
        val totalPendiente: Double = 0.0,
        val isLoading: Boolean = true,
        val error: String? = null
    )

    data class FormState(
        val nombreMascota: String = "",
        val tipoMascota: String = "Perro",
        val nombreCliente: String = "",
        val duracionHoras: String = "",
        val tarifaPorHora: String = "",
        val notas: String = "",
        val fecha: LocalDate = LocalDate.now()
    )
}

