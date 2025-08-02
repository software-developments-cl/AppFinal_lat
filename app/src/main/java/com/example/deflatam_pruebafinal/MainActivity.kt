package com.example.deflatam_pruebafinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deflatam_pruebafinal.datos.BaseDeDatosPaseos
import com.example.deflatam_pruebafinal.datos.EntidadPaseoMascota
import com.example.deflatam_pruebafinal.modelovista.ModeloVistaPaseos
import com.example.deflatam_pruebafinal.repositorio.RepositorioPaseosMascotas
import com.example.deflatam_pruebafinal.ui.theme.DefLatam_pruebaFinalTheme
import com.example.deflatam_pruebafinal.utilidades.FormatoDinero
import com.example.deflatam_pruebafinal.utilidades.FormatoFecha

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DefLatam_pruebaFinalTheme {
                AplicacionPaseosMascotas()
            }
        }
    }
}

// Pantalla principal de la aplicación
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AplicacionPaseosMascotas() {
    // Configurar la base de datos y el ViewModel
    val context = LocalContext.current
    val baseDeDatos = BaseDeDatosPaseos.obtenerBaseDeDatos(context)
    val repositorio = RepositorioPaseosMascotas(baseDeDatos.accesoDatosPaseos())
    val viewModel: ModeloVistaPaseos = viewModel { ModeloVistaPaseos(repositorio) }

    // Estado para mostrar/ocultar el formulario
    var mostrandoFormulario by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🐕 Control de Paseos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrandoFormulario = !mostrandoFormulario }
            ) {
                Icon(
                    imageVector = if (mostrandoFormulario) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (mostrandoFormulario) "Cerrar" else "Agregar paseo"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Mostrar estadísticas de dinero
            EstadisticasCard(viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            if (mostrandoFormulario) {
                // Mostrar formulario para agregar nuevo paseo (CON SCROLL)
                FormularioNuevoPaseo(viewModel) {
                    mostrandoFormulario = false
                }
            } else {
                // Mostrar lista de todos los paseos
                ListaDePaseos(viewModel)
            }
        }
    }
}

// Tarjeta que muestra las estadísticas de dinero
@Composable
fun EstadisticasCard(viewModel: ModeloVistaPaseos) {
    val totalGanado by viewModel.totalGanado.collectAsState()
    val totalPendiente by viewModel.totalPendiente.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Estadísticas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Dinero ya ganado (pagado)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "💰 Ganado",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = FormatoDinero(totalGanado),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF4CAF50), // Verde
                        fontWeight = FontWeight.Bold
                    )
                }

                // Dinero pendiente de cobro
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⏳ Pendiente",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = FormatoDinero(totalPendiente),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFFFF9800), // Naranja
                        fontWeight = FontWeight.Bold
                    )
                }

                // Total general
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "💵 Total",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = FormatoDinero(totalGanado + totalPendiente),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


// Formulario para agregar un nuevo paseo (CON SCROLL)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioNuevoPaseo(
    viewModel: ModeloVistaPaseos,
    onPaseoAgregado: () -> Unit
) {
    // Obtener los estados del formulario del ViewModel
    val nombreMascota by viewModel.nombreMascota.collectAsState()
    val tipoMascota by viewModel.tipoMascota.collectAsState()
    val nombreCliente by viewModel.nombreCliente.collectAsState()
    val duracionHoras by viewModel.duracionHoras.collectAsState()
    val tarifaPorHora by viewModel.tarifaPorHora.collectAsState()
    val notas by viewModel.notas.collectAsState()

    // Estado para el dropdown de tipo de mascota
    var expandedTipoMascota by remember { mutableStateOf(false) }
    val tiposMascotas = listOf("Perro", "Gato", "Conejo", "Otro")

    // ¡CLAVE! Agregar scroll al formulario
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // ¡ESTO PERMITE SCROLL!
        ) {
            Text(
                text = "➕ Nuevo Paseo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Nombre de la mascota
            OutlinedTextField(
                value = nombreMascota,
                onValueChange = viewModel::actualizarNombreMascota,
                label = { Text("🐕 Nombre de la mascota") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo: Tipo de mascota (Dropdown)
            ExposedDropdownMenuBox(
                expanded = expandedTipoMascota,
                onExpandedChange = { expandedTipoMascota = !expandedTipoMascota }
            ) {
                OutlinedTextField(
                    value = tipoMascota,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("🐾 Tipo de mascota") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipoMascota)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedTipoMascota,
                    onDismissRequest = { expandedTipoMascota = false }
                ) {
                    tiposMascotas.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                viewModel.actualizarTipoMascota(tipo)
                                expandedTipoMascota = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Campo: Nombre del cliente
            OutlinedTextField(
                value = nombreCliente,
                onValueChange = viewModel::actualizarNombreCliente,
                label = { Text("👤 Nombre del cliente") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campos: Duración y Tarifa (en la misma fila)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Duración en horas
                OutlinedTextField(
                    value = duracionHoras,
                    onValueChange = viewModel::actualizarDuracionHoras,
                    label = { Text("⏱️ Horas") },
                    modifier = Modifier.weight(1f)
                )

                // Tarifa por hora
                OutlinedTextField(
                    value = tarifaPorHora,
                    onValueChange = viewModel::actualizarTarifaPorHora,
                    label = { Text("💵 Tarifa/hora") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mostrar cálculo automático del total
            if (duracionHoras.isNotEmpty() && tarifaPorHora.isNotEmpty()) {
                val total = viewModel.calcularMontoTotal()
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "💰 Total: ${FormatoDinero(total)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Campo: Notas adicionales
            OutlinedTextField(
                value = notas,
                onValueChange = viewModel::actualizarNotas,
                label = { Text("📝 Notas (opcional)") },
                placeholder = { Text("Ej: Paseo por el parque, muy activo") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ¡BOTÓN PARA GUARDAR EL PASEO!
            Button(
                onClick = {
                    viewModel.agregarPaseo()
                    onPaseoAgregado()
                },
                enabled = viewModel.formularioEsValido(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp), // Más alto para ser más visible
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "💾 Guardar Paseo",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Espaciado adicional al final para que se vea bien
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}


// Lista de todos los paseos registrados
@Composable
fun ListaDePaseos(viewModel: ModeloVistaPaseos) {
    val paseos by viewModel.paseos.collectAsState()

    Text(
        text = "📋 Lista de Paseos",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    if (paseos.isEmpty()) {
        // Mostrar mensaje cuando no hay paseos
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🐕",
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = "No hay paseos registrados",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "¡Agrega tu primer paseo con el botón +!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        // Mostrar lista de paseos
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(paseos) { paseo ->
                TarjetaPaseo(
                    paseo = paseo,
                    onCambiarEstadoPago = { viewModel.cambiarEstadoPago(paseo) },
                    onEliminar = { viewModel.eliminarPaseo(paseo) }
                )
            }
        }
    }
}

// Tarjeta individual para cada paseo
@Composable
fun TarjetaPaseo(
    paseo: EntidadPaseoMascota,
    onCambiarEstadoPago: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (paseo.estaPagado) {
                Color(0xFFE8F5E8) // Verde claro para paseos pagados
            } else {
                Color(0xFFFFF3E0) // Naranja claro para paseos pendientes
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Nombre de la mascota con emoji según el tipo
                    Text(
                        text = "${obtenerEmojiTipo(paseo.tipoMascota)} ${paseo.nombreMascota}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Nombre del cliente
                    Text(
                        text = "👤 ${paseo.nombreCliente}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Fecha del paseo
                    Text(
                        text = "📅 ${FormatoFecha.formatearFechaHora(paseo.fecha)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Chip para mostrar/cambiar estado de pago
                AssistChip(
                    onClick = onCambiarEstadoPago,
                    label = {
                        Text(
                            text = if (paseo.estaPagado) "✅ Pagado" else "⏳ Pendiente"
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (paseo.estaPagado) {
                            Color(0xFF4CAF50) // Verde para pagado
                        } else {
                            Color(0xFFFF9800) // Naranja para pendiente
                        },
                        labelColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Información del paseo: duración, tarifa y total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⏱️ ${paseo.duracionHoras}h",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${FormatoDinero(paseo.tarifaPorHora)}/h",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "💰 ${FormatoDinero(paseo.montoTotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Mostrar notas si las hay
            if (paseo.notas.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📝 ${paseo.notas}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botón para eliminar el paseo
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onEliminar,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFD32F2F) // Rojo para eliminar
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}


// Obtener emoji según el tipo de mascota
fun obtenerEmojiTipo(tipo: String): String {
    return when (tipo) {
        "Perro" -> "🐕"
        "Gato" -> "🐱"
        "Conejo" -> "🐰"
        else -> "🐾"
    }
}
