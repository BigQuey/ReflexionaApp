package com.qfnm.reflexionaapp.estadisticas

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.qfnm.reflexionaapp.R
import com.qfnm.reflexionaapp.datos.AppDatabaseHelper
//IMPORTACIONES PARA EL GRAFICO DE BARRAS :X
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
class EstadisticasActivity : AppCompatActivity() {
    private lateinit var tvResumenEstadisticas: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadisticas)

        tvResumenEstadisticas = findViewById(R.id.tvResumenEstadisticas)

        val dbHelper = AppDatabaseHelper(this)
        val respuestas = dbHelper.obtenerRespuestasUltimos7Dias()

        val totalRespuestas = respuestas.size
        val diasUnicos = respuestas.map { it.fecha }.distinct()
        val totalDias = diasUnicos.size

        val totalPalabras = respuestas.sumOf {
            it.respuesta.split("\\s+".toRegex()).size
        }
        val promedioPalabras = if (totalRespuestas > 0) totalPalabras / totalRespuestas else 0

        val resumen = """
            📅 Días con respuestas: $totalDias
            ✍️ Respuestas totales: $totalRespuestas
            📝 Promedio de palabras por respuesta: $promedioPalabras
        """.trimIndent()

        tvResumenEstadisticas.text = resumen
        //PAL GRAFICO
        val barChart = findViewById<BarChart>(R.id.barChart)

        val respuestasPorDia = respuestas.groupBy { it.fecha }

        val entries = respuestasPorDia.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.size.toFloat())
        }


        val etiquetas = respuestasPorDia.keys.sorted()

        val dataSet = BarDataSet(entries, "Respuestas por Día")
        dataSet.color = getColor(R.color.green_500)
        val barData = BarData(dataSet)
        barData.barWidth = 0.9f


        barChart.data = barData
        barChart.setFitBars(true)
        barChart.description.isEnabled = false
        barChart.setDrawValueAboveBar(true)
        barChart.setScaleEnabled(false)

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(etiquetas)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        barChart.axisRight.isEnabled = false
        barChart.invalidate()
    }
}