package com.dailyreceipt.presentation.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.dailyreceipt.presentation.ui.model.DailyReceipt
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.time.format.DateTimeFormatter

object PdfGenerator {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
    private val currencyFormatter = NumberFormat.getCurrencyInstance(java.util.Locale.KOREA)

    fun generateReceiptPdf(receipt: DailyReceipt): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawReceipt(canvas, receipt)

        document.finishPage(page)

        val outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val outputFile = File(outputDir, "하루의영수증_${receipt.date}.pdf")

        document.writeTo(FileOutputStream(outputFile))
        document.close()

        return outputFile
    }

    private fun drawReceipt(canvas: Canvas, receipt: DailyReceipt) {
        // Background - cream color
        canvas.drawColor(Color.parseColor("#FFFEF5"))

        val titlePaint = Paint().apply {
            color = Color.parseColor("#8B7355")
            textSize = 24f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.parseColor("#8B7355")
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.parseColor("#3D3D3D")
            textSize = 11f
            isAntiAlias = true
        }

        val subTextPaint = Paint().apply {
            color = Color.parseColor("#7D7D7D")
            textSize = 10f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#8B7355")
            strokeWidth = 1f
        }

        val dottedPaint = Paint().apply {
            color = Color.parseColor("#8B7355")
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
        }

        var yPos = 50f
        val leftMargin = 40f
        val rightMargin = 555f
        val lineHeight = 18f

        // Title
        canvas.drawText("하루의 영수증", 200f, yPos, titlePaint)
        yPos += 30f

        // Date
        canvas.drawText(receipt.date.format(dateFormatter), leftMargin, yPos, subTextPaint)
        yPos += 40f

        // Draw dotted border rectangle
        drawDottedRect(canvas, leftMargin, yPos - 20f, rightMargin, yPos + 600f, dottedPaint)
        yPos += 10f

        // Section: App Usage
        canvas.drawText("📱 앱 사용", leftMargin + 10, yPos, headerPaint)
        yPos += 20f
        receipt.appUsage.topApps.take(4).forEach { item ->
            canvas.drawText("${item.icon} ${item.appName}: ${item.minutes}분", leftMargin + 15, yPos, textPaint)
            yPos += lineHeight
        }
        yPos += 15f

        // Section: Health
        canvas.drawText("❤️ 건강", leftMargin + 10, yPos, headerPaint)
        yPos += 20f
        val h = receipt.health
        val sleepHours = h.sleepMinutes / 60
        canvas.drawText("걸음 수: ${h.steps}보 | 칼로리: ${h.calories}kcal", leftMargin + 15, yPos, textPaint)
        yPos += lineHeight
        canvas.drawText("수면: ${sleepHours}시간 | 심박수: ${h.heartRate}bpm", leftMargin + 15, yPos, textPaint)
        yPos += 25f

        // Section: Notifications
        canvas.drawText("🔔 알림", leftMargin + 10, yPos, headerPaint)
        yPos += 20f
        canvas.drawText("총 ${receipt.notifications.totalCount}개 (메신저: ${receipt.notifications.messenger}, 금융: ${receipt.notifications.banking}, 쇼핑: ${receipt.notifications.shopping})", leftMargin + 15, yPos, textPaint)
        yPos += 25f

        // Section: Finance
        canvas.drawText("💰 금융", leftMargin + 10, yPos, headerPaint)
        yPos += 20f
        canvas.drawText("총 지출: ${currencyFormatter.format(receipt.finance.totalSpent)}", leftMargin + 15, yPos, textPaint)
        yPos += lineHeight
        receipt.finance.transactions.take(4).forEach { item ->
            canvas.drawText("${item.time} ${item.description}: ${currencyFormatter.format(item.amount)}", leftMargin + 15, yPos, subTextPaint)
            yPos += lineHeight - 2
        }
        yPos += 15f

        // Section: Schedule Timeline
        canvas.drawText("📅 오늘의 일정", leftMargin + 10, yPos, headerPaint)
        yPos += 20f

        // Draw timeline line
        canvas.drawLine(leftMargin + 20, yPos, leftMargin + 20, yPos + 200, linePaint)

        receipt.schedules.take(8).forEach { item ->
            // Timeline dot
            canvas.drawCircle(leftMargin + 20, yPos + 5, 4f, linePaint)
            // Time
            canvas.drawText(item.time, leftMargin + 30, yPos + 10, textPaint)
            // Title
            canvas.drawText(item.title, leftMargin + 80, yPos + 10, textPaint)
            // Location
            if (item.location.isNotEmpty()) {
                canvas.drawText(item.location, leftMargin + 280, yPos + 10, subTextPaint)
            }
            yPos += 22f
        }

        // Footer
        yPos = 820f
        canvas.drawText("Generated by 하루의 영수증", 200f, yPos, subTextPaint)
    }

    private fun drawDottedRect(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
        val pathEffect = android.graphics.DashPathEffect(floatArrayOf(6f, 6f), 0f)
        val updatedPaint = Paint(paint).apply { this.pathEffect = pathEffect }
        canvas.drawRect(left, top, right, bottom, updatedPaint)
    }
}
