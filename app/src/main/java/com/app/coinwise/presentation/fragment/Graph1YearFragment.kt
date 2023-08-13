package com.app.coinwise.presentation.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.app.coinwise.R
import com.app.coinwise.data.local.Value
import com.app.coinwise.presentation.viewmodel.Graph1YearViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

class Graph1YearFragment : Fragment(), OnChartValueSelectedListener {

    private lateinit var swipeToRefresh: SwipeRefreshLayout
    private lateinit var lineChartBitcoin: LineChart
    private lateinit var textViewData: TextView
    private lateinit var textViewOpen: TextView
    private lateinit var textViewMax: TextView
    private lateinit var textViewAverage: TextView
    private lateinit var textViewClose: TextView
    private lateinit var textViewMin: TextView
    private lateinit var textViewChange: TextView
    private lateinit var percentageTextView :TextView
    private lateinit var greenArrowImageView :ImageView
    private lateinit var redArrowImageView :ImageView


    private val viewModel : Graph1YearViewModel by lazy {
        Graph1YearViewModel.create(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_graph1_year, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeToRefresh = view.findViewById(R.id.swipeToRefresh)
        refreshApp()

        lineChartBitcoin = view.findViewById(R.id.line_chart_bitcoin_1year)
        textViewData = view.findViewById(R.id.text_view_dados_1year)
        textViewOpen = view.findViewById(R.id.text_view_open_1year)
        textViewMax = view.findViewById(R.id.text_view_maximo_1year)
        textViewAverage = view.findViewById(R.id.text_view_media_1year)
        textViewClose = view.findViewById(R.id.text_view_close_1year)
        textViewMin = view.findViewById(R.id.text_view_minimo_1year)
        textViewChange = view.findViewById(R.id.text_view_diferenca_1year)
        percentageTextView = view.findViewById<TextView>(R.id.tv_percentage)
        greenArrowImageView =view.findViewById<ImageView>(R.id.img_up)
        redArrowImageView =view.findViewById<ImageView>(R.id.img_down)
        setUpLineCharts()




    }

    override fun onStart() {
        super.onStart()

        if (isNetworkAvailable(requireContext())) {
            viewModel.refreshChartItem()
            viewModel.chartItem.observe(this) { chartItem ->
                if(chartItem != null) {
                    val itemChartValue = chartItem.values
                    val lastValueIndex = itemChartValue.size - 1

                    var yesterdayPrice = 0.0

                    if (lastValueIndex >= 1) {
                        yesterdayPrice = itemChartValue[lastValueIndex - 1].y
                    }
                    updateLineChart(chartItem.values, yesterdayPrice)
                }
            }
        } else {
            viewModel.chartItem.observe(this) { chartItem ->
                val itemChartValue = chartItem.values
                val lastValueIndex = itemChartValue.size - 1

                var yesterdayPrice = 0.0

                if (lastValueIndex >= 1) {
                    yesterdayPrice = itemChartValue[lastValueIndex - 1].y
                }
                updateLineChart(chartItem.values, yesterdayPrice)
            }
        }
    }


    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }


    @SuppressLint("SetTextI18n")
    private fun updateLineChart(bitcoinList: List<Value>, yesterdayPrice: Double) {
        val entries = bitcoinList.mapIndexed { _, value ->
            Entry(value.x.toFloat(), value.y.toFloat())
        }

        // Log só pra checar o valor que está sendo passado nos objetos Entry(x,y)
        entries.forEachIndexed { index, entry ->
            Log.d("Entry Debug", "Entry $index - x: ${entry.x}, y: ${entry.y}")
        }

        val highestValue = entries.maxByOrNull { it.y }?.y ?: 0f
        val lowestValue = entries.minByOrNull { it.y }?.y ?: 0f
        val averageValue = entries.map { it.y }.average()
        val formattedAverageValue = String.format("%.2f", averageValue)
        val firstEntryYValue = entries.firstOrNull()?.y ?: 0f
        val lastEntryYValue = entries.lastOrNull()?.y ?: 0f
        val change = lastEntryYValue - firstEntryYValue
        val formattedChange = String.format("%.2f", change)

        textViewMax.text = "Max: US$ $highestValue"
        textViewAverage.text = "Average: US$ $formattedAverageValue"
        textViewOpen.text = "Open: US$ $firstEntryYValue"
        textViewClose.text = "Close: US$ $lastEntryYValue"
        textViewMin.text = "Min: US$ $lowestValue"
        textViewChange.text = "Change: US$ $formattedChange"

        val lineDataSet = LineDataSet(entries, "Bitcoin Price")
        lineDataSet.color = resources.getColor(R.color.green_500)
        lineDataSet.circleRadius = 1f
        lineDataSet.setDrawFilled(true)
        lineDataSet.fillColor = resources.getColor((R.color.green_500))
        lineDataSet.fillAlpha = 30
        lineDataSet.setDrawCircles(false)
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet.highLightColor = resources.getColor(R.color.black)
        lineDataSet.lineWidth = 0.5f
        lineDataSet.valueTextSize = 0f
        lineDataSet.valueTextColor = Color.TRANSPARENT

        val dataSets: ArrayList<ILineDataSet> = ArrayList()
        dataSets.add(lineDataSet)

        val lineData = LineData(dataSets)
        lineChartBitcoin.data = lineData

        val xAxis = lineChartBitcoin.xAxis
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return viewModel.convertUnixTimestampToDateFormat(value.toInt())
            }
        }

        val todayValue = bitcoinList.lastOrNull()
        val todayPrice = todayValue?.y ?: 0.0
        val percentageChange = ((todayPrice - yesterdayPrice) / yesterdayPrice) * 100.0


        if (percentageChange > 0) {
            greenArrowImageView.visibility = View.VISIBLE
            redArrowImageView.visibility = View.GONE
        } else if (percentageChange < 0) {
            greenArrowImageView.visibility = View.GONE
            redArrowImageView.visibility = View.VISIBLE
        } else {
            greenArrowImageView.visibility = View.GONE
            redArrowImageView.visibility = View.GONE
        }

        percentageTextView.text = String.format("%.2f%%", percentageChange)
        percentageTextView.visibility = View.VISIBLE

        lineChartBitcoin.invalidate()
    }

    private fun setUpLineCharts() {
        // 1 year chart
        lineChartBitcoin.setTouchEnabled(true)
        lineChartBitcoin.setPinchZoom(false)
        lineChartBitcoin.setBackgroundColor(resources.getColor(R.color.white))
        lineChartBitcoin.animateX(1000)
        lineChartBitcoin.description.isEnabled = false
        lineChartBitcoin.isDragEnabled = false
        lineChartBitcoin.setScaleEnabled(false)
        lineChartBitcoin.isDoubleTapToZoomEnabled = false
        lineChartBitcoin.setPinchZoom(false)
        lineChartBitcoin.setOnChartValueSelectedListener(this)
        lineChartBitcoin.legend.isEnabled = false

        val xAxis = lineChartBitcoin.xAxis
        xAxis.textColor = resources.getColor(R.color.black)
        xAxis.textSize = 12f
        xAxis.labelCount = 3
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        val yAxisLeft = lineChartBitcoin.axisLeft
        yAxisLeft.setDrawLabels(true)
        yAxisLeft.setDrawAxisLine(false)
        yAxisLeft.textColor = resources.getColor(R.color.black)
        yAxisLeft.textSize = 12f
        yAxisLeft.axisLineColor = resources.getColor(R.color.black)


        val yAxisRight = lineChartBitcoin.axisRight
        yAxisRight.isEnabled = false
    }

    private fun refreshApp() {

        swipeToRefresh.setOnRefreshListener {

            if (isNetworkAvailable(requireContext())) {
                viewModel.refreshChartItem()
                viewModel.chartItem.observe(requireActivity()) { chartItem ->
                    if(chartItem != null) {
                        val itemChartValue = chartItem.values
                        val lastValueIndex = itemChartValue.size - 1

                        var yesterdayPrice = 0.0

                        if (lastValueIndex >= 1) {
                            yesterdayPrice = itemChartValue[lastValueIndex - 1].y
                        }

                        updateLineChart(chartItem.values, yesterdayPrice)
                        Toast.makeText(requireActivity(), "Pagina atualizada", Toast.LENGTH_SHORT).show()
                        swipeToRefresh.isRefreshing = false
                    }

                }
            } else {
                viewModel.chartItem.observe(requireActivity()) { chartItem ->
                    val itemChartValue = chartItem.values
                    val lastValueIndex = itemChartValue.size - 1

                    var yesterdayPrice = 0.0

                    if (lastValueIndex >= 1) {
                        yesterdayPrice = itemChartValue[lastValueIndex - 1].y
                    }
                    updateLineChart(chartItem.values, yesterdayPrice)
                    Toast.makeText(requireActivity(), "Sem Internet, Dados apresentadados localmente.", Toast.LENGTH_SHORT).show()
                    swipeToRefresh.isRefreshing = false
                }
            }


        }
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e != null) {
            val xValue = e.x.toInt()
            val yValue = e.y
            val xValueFormatted = viewModel.convertUnixTimestampToDateFormat(xValue)
            textViewData.text = "$xValueFormatted - US$ $yValue"

        }
    }

    override fun onNothingSelected() {
        textViewData.text = "Clique no gráfico para exibir os valores"
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            Graph1YearFragment()
    }
}