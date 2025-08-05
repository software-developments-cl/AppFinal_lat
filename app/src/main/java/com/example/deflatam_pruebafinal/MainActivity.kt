package com.example.deflatam_pruebafinal

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.deflatam_pruebafinal.datos.BaseDeDatosPaseos
import com.example.deflatam_pruebafinal.datos.EntidadPaseoMascota
import com.example.deflatam_pruebafinal.modelovista.ModeloVistaPaseos
import com.example.deflatam_pruebafinal.repositorio.RepositorioPaseosMascotas
import com.example.deflatam_pruebafinal.ui.theme.DefLatam_pruebaFinalTheme
import com.example.deflatam_pruebafinal.utilidades.FormatoDinero
import com.example.deflatam_pruebafinal.utilidades.FormatoFecha
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Función auxiliar para crear un archivo de imagen temporal
fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        storageDir      /* directory */
    )
}

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
    // Obtener el término de búsqueda actual del ViewModel
    val terminoBusqueda by viewModel.terminoBusqueda.collectAsState()

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

            // Campo de búsqueda
            OutlinedTextField(
                value = terminoBusqueda,
                onValueChange = { viewModel.actualizarTerminoBusqueda(it) },
                label = { Text("🔍 Buscar por nombre de cliente") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (mostrandoFormulario) {
                // Mostrar formulario para agregar nuevo paseo (CON SCROLL)
                FormularioNuevoPaseo(
                    viewModel,
                    onPaseoAgregado = { mostrandoFormulario = false }
                )
            } else {
                // Mostrar lista de todos los paseos (ya se filtrará automáticamente por el ViewModel)
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💰 Ganado", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        FormatoDinero.enteroAStringDineroChileno(totalGanado.toInt()),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⏳ Pendiente", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        FormatoDinero.enteroAStringDineroChileno(totalPendiente.toInt()),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💵 Total", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        FormatoDinero.enteroAStringDineroChileno((totalGanado + totalPendiente).toInt()),
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
    onPaseoAgregado: () -> Unit,
    context: Context = LocalContext.current
) {
    val nombreMascota by viewModel.nombreMascota.collectAsState()
    val tipoMascota by viewModel.tipoMascota.collectAsState()
    val nombreCliente by viewModel.nombreCliente.collectAsState()
    val duracionHoras by viewModel.duracionHoras.collectAsState()
    val tarifaPorHora by viewModel.tarifaPorHora.collectAsState()
    val notas by viewModel.notas.collectAsState()
    val fotoMascotaUriActual by viewModel.fotoMascotaUri.collectAsState()

    var expandedTipoMascota by remember { mutableStateOf(false) }
    val tiposMascotas = listOf("Perro", "Gato", "Conejo", "Otro")

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let { uri ->
                viewModel.actualizarFotoMascotaUri(uri.toString())
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val newPhotoFile = context.createImageFile()
            tempPhotoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                newPhotoFile
            )
            tempPhotoUri?.let { uri ->
                cameraActivityLauncher.launch(uri)
            }
        } else {
            // Manejar el caso de permiso denegado, e.g., mostrar un Toast
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "➕ Nuevo Paseo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nombreMascota,
                onValueChange = viewModel::actualizarNombreMascota,
                label = { Text("🐕 Nombre de la mascota") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expandedTipoMascota,
                onExpandedChange = { expandedTipoMascota = !expandedTipoMascota }
            ) {
                OutlinedTextField(
                    value = tipoMascota,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("🐾 Tipo de mascota") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipoMascota) },
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

            OutlinedTextField(
                value = nombreCliente,
                onValueChange = viewModel::actualizarNombreCliente,
                label = { Text("👤 Nombre del cliente") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = duracionHoras,
                    onValueChange = viewModel::actualizarDuracionHoras,
                    label = { Text("⏱️ Horas") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = tarifaPorHora,
                    onValueChange = viewModel::actualizarTarifaPorHora,
                    label = { Text("💵 Tarifa/hora") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (duracionHoras.isNotEmpty() && tarifaPorHora.isNotEmpty()) {
                val total = viewModel.calcularMontoTotal()
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(
                        text = "💰 Total: ${FormatoDinero.enteroAStringDineroChileno(total.toInt())}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = notas,
                onValueChange = viewModel::actualizarNotas,
                label = { Text("📝 Notas (opcional)") },
                placeholder = { Text("Ej: Paseo por el parque, muy activo") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Vista previa de la imagen y botón para tomar foto
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                fotoMascotaUriActual?.let { uriString ->
                    AsyncImage(
                        model = Uri.parse(uriString),
                        contentDescription = "Foto de la mascota",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(onClick = {
                    when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
                        PackageManager.PERMISSION_GRANTED -> {
                            val newPhotoFile = context.createImageFile()
                            tempPhotoUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider", // Asegúrate que coincida con AndroidManifest
                                newPhotoFile
                            )
                            tempPhotoUri?.let { uri ->
                                cameraActivityLauncher.launch(uri)
                            }
                        }

                        else -> {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Tomar Foto")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tomar Foto")
                }
            }


            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.agregarPaseo(context)
                    onPaseoAgregado()
                },
                enabled = viewModel.formularioEsValido(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("✔️ Guardar Paseo", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// Lista de paseos
@Composable
fun ListaDePaseos(viewModel: ModeloVistaPaseos) {
    val paseos by viewModel.paseos.collectAsState()
    val terminoBusqueda by viewModel.terminoBusqueda.collectAsState()
    val context = LocalContext.current

    if (paseos.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Pets,
                    contentDescription = "Sin paseos",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (terminoBusqueda.isBlank()) {
                    Text(
                        "🐾 No hay paseos registrados todavía.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "¡Anímate a agregar el primero!",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        "🐾 No se encontraron paseos para $terminoBusqueda",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Intenta con otro término de búsqueda.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    } else {
        if (terminoBusqueda.isBlank()) {
            Text(
                "📋 Lista de Paseos",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {
            Text(
                "🔍 Resultados para $terminoBusqueda",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(paseos, key = { it.id }) { paseo ->
                TarjetaPaseo(
                    paseo = paseo,
                    onCambiarEstadoPago = { viewModel.cambiarEstadoPago(paseo) },
                    onEliminar = { viewModel.eliminarPaseo(paseo, context) }
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
    val errorPainter =
        rememberVectorPainter(Icons.Default.Pets) // Placeholder for image loading error

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (paseo.estaPagado) Color(0xFFE8F5E8) else Color(0xFFFFF3E0)
        )
    ) {
        Row(modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)) { // Adjusted padding
            // Foto de la Mascota
            Box(modifier = Modifier
                .size(72.dp)
                .padding(end = 8.dp), contentAlignment = Alignment.Center) { // Increased size slightly
                if (paseo.fotoMascotaUri != null) {
                    AsyncImage(
                        model = paseo.fotoMascotaUri.toUri(),
                        contentDescription = "Foto de ${paseo.nombreMascota}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        error = errorPainter
                    )
                } else {
                    Image(
                        painter = errorPainter, // Use the same placeholder
                        contentDescription = "Sin foto",
                        modifier = Modifier
                            .fillMaxSize(0.8f)
                            .clip(CircleShape), // Make placeholder slightly smaller
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Resto de los detalles del paseo
            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)) { // Added start padding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${obtenerEmojiTipo(paseo.tipoMascota)} ${paseo.nombreMascota}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "👤 ${paseo.nombreCliente}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2f3030),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "📅 ${FormatoFecha(paseo.fecha)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2f3030)
                        )
                    }
                    AssistChip(
                        onClick = onCambiarEstadoPago,
                        label = { Text(text = if (paseo.estaPagado) "✅ Pagado" else "⏳ Pendiente", maxLines = 1) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (paseo.estaPagado) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            labelColor = Color.White
                        ),
                        modifier = Modifier.padding(start = 4.dp) // Ensure chip doesn't overlap text
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("⏱️ ${paseo.duracionHoras}h", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("${FormatoDinero.enteroAStringDineroChileno(paseo.tarifaPorHora.toInt())}/h", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("💰 ${FormatoDinero.enteroAStringDineroChileno(paseo.montoTotal.toInt())}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                if (paseo.notas.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📝 ${paseo.notas}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2f3030),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onEliminar,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}

fun obtenerEmojiTipo(tipo: String): String {
    return when (tipo) {
        "Perro" -> "🐕"
        "Gato" -> "🐱"
        "Conejo" -> "🐰"
        else -> "🐾"
    }
}
