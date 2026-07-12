package com.example.stockdemo.feature.stock.presentation.report

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stockdemo.R
import com.example.stockdemo.core.ui.theme.GreenColor
import com.example.stockdemo.core.ui.theme.OrangeColor
import com.example.stockdemo.core.ui.theme.PrimaryColor
import com.example.stockdemo.core.ui.util.toast
import com.example.stockdemo.feature.stock.domain.model.StockMovementReport
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val report = state.report

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            report?.let {
                                if (it.items.isEmpty()) {
                                    context.toast(context.getString(R.string.report_empty))
                                } else {
                                    exportCsv(context, it)
                                }
                            }
                        },
                        enabled = report != null && report.items.isNotEmpty()
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.report_export))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RangeChip(state.range, ReportRange.WEEK, R.string.report_range_week, viewModel::selectRange)
                RangeChip(state.range, ReportRange.MONTH, R.string.report_range_month, viewModel::selectRange)
                RangeChip(state.range, ReportRange.QUARTER, R.string.report_range_quarter, viewModel::selectRange)
                RangeChip(state.range, ReportRange.ALL, R.string.report_range_all, viewModel::selectRange)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.report_period, state.fromLabel, state.toLabel),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            when {
                state.isLoading && report == null -> Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                    CircularProgressIndicator()
                }
                report == null || report.items.isEmpty() -> Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                    Text(stringResource(R.string.report_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> ReportTable(report)
            }
        }
    }
}

@Composable
private fun RangeChip(
    current: ReportRange,
    value: ReportRange,
    labelRes: Int,
    onSelect: (ReportRange) -> Unit
) {
    FilterChip(
        selected = current == value,
        onClick = { onSelect(value) },
        label = { Text(stringResource(labelRes)) }
    )
}

@Composable
private fun ReportTable(report: StockMovementReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TableRow(
                product = stringResource(R.string.stocktake_col_product),
                inValue = stringResource(R.string.report_col_in),
                outValue = stringResource(R.string.report_col_out),
                stockValue = stringResource(R.string.report_col_stock),
                header = true
            )
            HorizontalDivider()
            report.items.forEach { item ->
                TableRow(
                    product = item.productName,
                    inValue = item.totalIn.toString(),
                    outValue = item.totalOut.toString(),
                    stockValue = item.currentStock.toString()
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            }
            TableRow(
                product = stringResource(R.string.report_total),
                inValue = report.totalIn.toString(),
                outValue = report.totalOut.toString(),
                stockValue = report.totalStock.toString(),
                header = true
            )
        }
    }
}

@Composable
private fun TableRow(
    product: String,
    inValue: String,
    outValue: String,
    stockValue: String,
    header: Boolean = false
) {
    val weight = if (header) FontWeight.Bold else FontWeight.Normal
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(product, Modifier.weight(1f), fontSize = 13.sp, fontWeight = weight)
        Text(inValue, Modifier.width(52.dp), fontSize = 13.sp, fontWeight = weight, color = if (header) MaterialTheme.colorScheme.onSurface else GreenColor)
        Text(outValue, Modifier.width(52.dp), fontSize = 13.sp, fontWeight = weight, color = if (header) MaterialTheme.colorScheme.onSurface else OrangeColor)
        Text(stockValue, Modifier.width(56.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun exportCsv(context: Context, report: StockMovementReport) {
    val dir = File(context.cacheDir, "reports").apply { mkdirs() }
    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File(dir, "stock_report_$stamp.csv")

    val sb = StringBuilder()
    sb.append("﻿") // BOM so Excel reads UTF-8 (Vietnamese) correctly
    sb.append("Ma SP,Ten SP,DVT,Nhap,Xuat,Ton\n")
    report.items.forEach { item ->
        sb.append(csvField(item.productCode)).append(',')
            .append(csvField(item.productName)).append(',')
            .append(csvField(item.unit)).append(',')
            .append(item.totalIn).append(',')
            .append(item.totalOut).append(',')
            .append(item.currentStock).append('\n')
    }
    sb.append(csvField("TONG")).append(",,,")
        .append(report.totalIn).append(',')
        .append(report.totalOut).append(',')
        .append(report.totalStock).append('\n')

    file.writeText(sb.toString(), Charsets.UTF_8)

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.report_export)))
}

private fun csvField(value: String): String = "\"" + value.replace("\"", "\"\"") + "\""
