package com.example.deflatam_pruebafinal.utilidades

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.deflatam_pruebafinal.datos.EntidadPaseoMascota
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

/** objeto para generar pdf de paseos de mascotas */
object GeneradorPdf {

    fun generarPdfPaseos(context: Context, paseos: List<EntidadPaseoMascota>, nombreArchivo: String): File? {
        // Usar el directorio específico de la app para documentos
        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (documentsDir == null) {
            Log.e("GeneradorPdf", "Directorio de documentos no disponible.")
            return null
        }
        val pdfFile = File(documentsDir, nombreArchivo)

        try {
            val outputStream = FileOutputStream(pdfFile)
            val writer = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(writer)
            val document = Document(pdfDocument)

            // Título del documento
            document.add(Paragraph("Reporte de Paseos de Mascotas")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(18f)
                .setMarginBottom(20f))

            // Crear tabla
            // Ajuste en el tamaño de columnas para mejor visualización
            val table = Table(UnitValue.createPercentArray(floatArrayOf(2.5f, 1.5f, 2.5f, 1f, 1.5f, 1.5f, 1f, 1.5f, 2.5f)))
            table.setWidth(UnitValue.createPercentValue(100f))

            // Encabezados de la tabla
            table.addHeaderCell(Paragraph("Mascota").setBold())
            table.addHeaderCell(Paragraph("Tipo").setBold())
            table.addHeaderCell(Paragraph("Cliente").setBold())
            table.addHeaderCell(Paragraph("Horas").setBold().setFontSize(10f))
            table.addHeaderCell(Paragraph("Tarifa/h").setBold().setFontSize(10f))
            table.addHeaderCell(Paragraph("Total").setBold().setFontSize(10f))
            table.addHeaderCell(Paragraph("Pagado").setBold().setFontSize(10f))
            table.addHeaderCell(Paragraph("Fecha").setBold().setFontSize(10f))
            table.addHeaderCell(Paragraph("Notas").setBold())

            // Formateador de fecha
            val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault()) // Formato más corto para fecha

            // Llenar tabla con datos
            for (paseo in paseos) {
                table.addCell(Paragraph(paseo.nombreMascota).setFontSize(10f))
                table.addCell(Paragraph(paseo.tipoMascota).setFontSize(10f))
                table.addCell(Paragraph(paseo.nombreCliente).setFontSize(10f))
                table.addCell(Paragraph(String.format(Locale.US, "%.1f", paseo.duracionHoras)).setFontSize(10f))
                table.addCell(Paragraph(FormatoDinero.enteroAStringDineroChileno(paseo.tarifaPorHora.toInt())).setFontSize(10f))
                table.addCell(Paragraph(FormatoDinero.enteroAStringDineroChileno(paseo.montoTotal.toInt())).setFontSize(10f))
                table.addCell(Paragraph(if (paseo.estaPagado) "Sí" else "No").setFontSize(10f))
                table.addCell(Paragraph(sdf.format(paseo.fecha)).setFontSize(10f))
                table.addCell(Paragraph(paseo.notas).setFontSize(10f))
            }

            document.add(table)
            document.close()
            outputStream.close()

            Log.d("GeneradorPdf", "PDF generado exitosamente en ${pdfFile.absolutePath}")
            return pdfFile
        } catch (e: Exception) {
            Log.e("GeneradorPdf", "Error al generar PDF", e)
            e.printStackTrace()
            return null
        }
    }

    fun abrirPdf(context: Context, file: File) {
        val authority = "${context.packageName}.provider" // Debe coincidir con el AndroidManifest
        val uri: Uri
        try {
            uri = FileProvider.getUriForFile(context, authority, file)
        } catch (e: IllegalArgumentException) {
            Log.e("GeneradorPdf", "Error al obtener URI para el archivo: ${e.message}")
            Toast.makeText(context, "Error al obtener URI del PDF. Verifica la configuración del FileProvider.", Toast.LENGTH_LONG).show()
            return
        }


        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY) // Opcional: para no guardar el visor de PDF en el historial
        }

        try {
            context.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            Log.e("GeneradorPdf", "No se encontró una aplicación para abrir PDFs", e)
            Toast.makeText(context, "No se encontró una aplicación para abrir PDFs.", Toast.LENGTH_SHORT).show()
        }
    }
}
