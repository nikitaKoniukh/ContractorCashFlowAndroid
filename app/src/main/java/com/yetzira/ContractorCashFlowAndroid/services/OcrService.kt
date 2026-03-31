package com.yetzira.ContractorCashFlowAndroid.services

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

/**
 * OCR service that uses ML Kit Text Recognition to extract
 * amount, date, and description from receipt images.
 */
object OcrService {

    data class ScannedReceiptData(
        val amount: Double?,
        val date: Long?,
        val description: String,
        val rawText: String
    )

    /**
     * Parse a receipt image from [uri] and extract structured data.
     */
    suspend fun parseFromUri(context: Context, uri: Uri): ScannedReceiptData {
        val inputImage = InputImage.fromFilePath(context, uri)
        return processImage(inputImage)
    }

    /**
     * Parse a receipt image from [bitmap] and extract structured data.
     */
    suspend fun parseFromBitmap(bitmap: Bitmap): ScannedReceiptData {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        return processImage(inputImage)
    }

    private suspend fun processImage(image: InputImage): ScannedReceiptData {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            val result = recognizer.process(image).await()
            val lines = result.textBlocks.flatMap { it.lines }.map { it.text }
            val rawText = lines.joinToString("\n")

            ScannedReceiptData(
                amount = extractTotalAmount(lines),
                date = extractDate(lines),
                description = bestDescription(lines),
                rawText = rawText
            )
        } catch (e: Exception) {
            ScannedReceiptData(
                amount = null,
                date = null,
                description = "",
                rawText = ""
            )
        } finally {
            recognizer.close()
        }
    }

    // ── Amount extraction (5-strategy waterfall, mirrors iOS) ──

    private val totalKeywords = listOf(
        "total due", "amount due", "total to pay", "balance due",
        "grand total", "total", "amount payable", "subtotal",
        // Hebrew
        "סה\"כ לתשלום", "לתשלום", "סכום לתשלום", "סה\"כ",
        "סה״כ לתשלום", "סה״כ",
        // Russian
        "итого к оплате", "итого", "к оплате", "сумма"
    )

    private val currencyPattern = Pattern.compile("[₪$€£₽¥]\\s*([\\d,]+\\.?\\d*)")
    private val decimalPattern = Pattern.compile("(\\d{1,3}(?:[,.]\\d{3})*(?:[.,]\\d{1,2}))")
    private val simpleDecimalPattern = Pattern.compile("(\\d+\\.\\d{1,2})")
    private val integerPattern = Pattern.compile("\\b(\\d{1,5})\\b")

    private fun extractTotalAmount(lines: List<String>): Double? {
        // Strategy 1: Total keyword + decimal on same line
        for (line in lines) {
            val lower = line.lowercase()
            if (totalKeywords.any { lower.contains(it) }) {
                val amount = extractDecimalFromLine(line)
                if (amount != null && amount > 0.0) return amount
            }
        }

        // Strategy 2: Total keyword line, look ahead 1-3 lines for decimal
        for (i in lines.indices) {
            val lower = lines[i].lowercase()
            if (totalKeywords.any { lower.contains(it) }) {
                for (j in 1..minOf(3, lines.size - i - 1)) {
                    val amount = extractDecimalFromLine(lines[i + j])
                    if (amount != null && amount > 0.0) return amount
                }
            }
        }

        // Strategy 3: Currency symbol + decimal
        for (line in lines) {
            val matcher = currencyPattern.matcher(line)
            if (matcher.find()) {
                val raw = matcher.group(1)?.replace(",", "") ?: continue
                val amount = raw.toDoubleOrNull()
                if (amount != null && amount > 0.0) return amount
            }
        }

        // Strategy 4: Any X.XX decimal pattern (largest value)
        val allDecimals = lines.flatMap { line ->
            val matches = mutableListOf<Double>()
            val matcher = simpleDecimalPattern.matcher(line)
            while (matcher.find()) {
                matcher.group(1)?.replace(",", "")?.toDoubleOrNull()?.let { matches.add(it) }
            }
            matches
        }
        if (allDecimals.isNotEmpty()) return allDecimals.maxOrNull()

        // Strategy 5: Last resort — integer < 10,000
        val allIntegers = lines.flatMap { line ->
            val matches = mutableListOf<Double>()
            val matcher = integerPattern.matcher(line)
            while (matcher.find()) {
                val value = matcher.group(1)?.toDoubleOrNull()
                if (value != null && value in 1.0..10000.0) matches.add(value)
            }
            matches
        }
        return allIntegers.maxOrNull()
    }

    private fun extractDecimalFromLine(line: String): Double? {
        val matcher = decimalPattern.matcher(line)
        val amounts = mutableListOf<Double>()
        while (matcher.find()) {
            val raw = matcher.group(1)?.replace(",", "") ?: continue
            raw.toDoubleOrNull()?.let { amounts.add(it) }
        }
        if (amounts.isNotEmpty()) return amounts.maxOrNull()

        // Fallback to simple pattern
        val simpleMatcher = simpleDecimalPattern.matcher(line)
        while (simpleMatcher.find()) {
            simpleMatcher.group(1)?.toDoubleOrNull()?.let { return it }
        }
        return null
    }

    // ── Date extraction (12 date formats) ──

    private val dateFormats = listOf(
        "MM/dd/yyyy", "dd/MM/yyyy", "yyyy-MM-dd",
        "MMM dd yyyy", "dd MMM yyyy", "MMMM dd yyyy",
        "MM-dd-yyyy", "dd-MM-yyyy", "d/M/yyyy",
        "M/d/yyyy", "dd.MM.yyyy", "d.M.yyyy"
    )

    private val datePattern = Pattern.compile(
        "(\\d{1,4}[/\\-.]\\d{1,2}[/\\-.]\\d{2,4})" +
            "|(\\d{1,2}\\s+\\w{3,}\\s+\\d{4})" +
            "|(\\w{3,}\\s+\\d{1,2}\\s+\\d{4})"
    )

    private fun extractDate(lines: List<String>): Long? {
        for (line in lines) {
            val matcher = datePattern.matcher(line)
            while (matcher.find()) {
                val dateStr = matcher.group()?.trim() ?: continue
                for (format in dateFormats) {
                    try {
                        val sdf = SimpleDateFormat(format, Locale.US)
                        sdf.isLenient = false
                        val date = sdf.parse(dateStr)
                        if (date != null) return date.time
                    } catch (_: Exception) { }
                }
            }
        }
        return null
    }

    // ── Description extraction ──

    private fun bestDescription(lines: List<String>): String {
        // Find the longest non-numeric line under 60 chars
        return lines
            .filter { it.length in 3..60 }
            .filter { line ->
                val digitRatio = line.count { it.isDigit() }.toDouble() / line.length
                digitRatio < 0.5 // Mostly text, not numbers
            }
            .maxByOrNull { it.length }
            ?: lines.firstOrNull { it.length > 2 }
            ?: ""
    }

    /**
     * Suggest an expense category based on OCR description keywords.
     */
    fun suggestCategory(description: String): String {
        val lower = description.lowercase()
        return when {
            lower.containsAny("labor", "worker", "wages", "salary", "עבודה", "שכר", "работа", "зарплата") -> "LABOR"
            lower.containsAny("material", "lumber", "supply", "supplies", "cement", "wood", "חומרים", "материалы") -> "MATERIALS"
            lower.containsAny("equipment", "rental", "tool", "machine", "ציוד", "оборудование", "инструмент") -> "EQUIPMENT"
            else -> "MISC"
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean =
        keywords.any { this.contains(it) }
}

