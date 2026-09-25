package com.kjbilling.app.pdf.template

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.kjbilling.app.pdf.InvoiceDocumentModel
import com.kjbilling.app.util.ImageSizing

/**
 * Classic Corporate invoice layout matching mock-templates/01-classic-corporate.html:
 * Letterhead with business logo (or initials badge when none), 3.5pt solid Navy divider, 2-column meta grid,
 * solid Navy items table header with white typography, alternating zebra rows (#F5F8FC),
 * boxed totals with solid Navy Grand Total row, amount in words with teal accent bar,
 * payment details card, notes & terms, and aligned signature block.
 * Engineered to fit 5-10 items cleanly on a single A4 page with zero text clipping or overlaps.
 */
class ClassicTemplateRenderer {
    private val pageWidth = 595f
    private val pageHeight = 842f
    private val margin = 40f
    private val contentWidth = pageWidth - 2 * margin
    private val rightEdge = pageWidth - margin

    private val ink = Color.parseColor("#1D2733")
    private val muted = Color.parseColor("#5C6B7A")
    private val navy = Color.parseColor("#123A6B")
    private val hairline = Color.parseColor("#D8E0EA")
    private val zebra = Color.parseColor("#F5F8FC")
    private val accentTeal = Color.parseColor("#0F7B6C")
    private val successGreen = Color.parseColor("#16A34A")
    private val white = Color.parseColor("#FFFFFF")

    private val regularPaint = Paint().apply {
        color = ink
        textSize = 9.5f
        isAntiAlias = true
    }

    private val mutedPaint = Paint().apply {
        color = muted
        textSize = 9f
        isAntiAlias = true
    }

    private val boldPaint = Paint().apply {
        color = ink
        textSize = 9.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val navyBoldPaint = Paint().apply {
        color = navy
        textSize = 9.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val discountPaint = Paint().apply {
        color = successGreen
        textSize = 9.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val labelPaint = Paint().apply {
        color = muted
        textSize = 8.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        letterSpacing = 0.12f
        isAntiAlias = true
    }

    private val bigNamePaint = Paint().apply {
        color = navy
        textSize = 19f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val docTypePaint = Paint().apply {
        color = navy
        textSize = 20f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        letterSpacing = 0.12f
        isAntiAlias = true
    }

    private val partyNamePaint = Paint().apply {
        color = navy
        textSize = 13f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val gstinPaint = Paint().apply {
        color = accentTeal
        textSize = 9.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val smallPaint = Paint().apply {
        color = muted
        textSize = 8.5f
        isAntiAlias = true
    }

    private val wordsPaint = Paint().apply {
        color = ink
        textSize = 8.5f
        isAntiAlias = true
    }

    private val whiteHeaderPaint = Paint().apply {
        color = white
        textSize = 8.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        letterSpacing = 0.08f
        isAntiAlias = true
    }

    private val whiteGrandPaint = Paint().apply {
        color = white
        textSize = 13f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val whiteLogoPaint = Paint().apply {
        color = white
        textSize = 15f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        letterSpacing = 0.05f
        isAntiAlias = true
    }

    private val tagTextPaint = Paint().apply {
        color = white
        textSize = 8f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        letterSpacing = 0.06f
        isAntiAlias = true
    }

    private val linePaint = Paint().apply {
        color = hairline
        strokeWidth = 0.8f
    }

    private val navyBarPaint = Paint().apply {
        color = navy
        strokeWidth = 3.5f
    }

    private val navyFillPaint = Paint().apply {
        color = navy
        style = Paint.Style.FILL
    }

    private val zebraPaint = Paint().apply {
        color = zebra
        style = Paint.Style.FILL
    }

    private val tealBarPaint = Paint().apply {
        color = accentTeal
        style = Paint.Style.FILL
    }

    private val logoPaint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }

    private val cardBorderPaint = Paint().apply {
        color = hairline
        style = Paint.Style.STROKE
        strokeWidth = 0.8f
    }

    private class Columns(
        val indexR: Float,
        val nameLeft: Float,
        val nameR: Float,
        val qtyR: Float,
        val rateR: Float,
        val totalR: Float
    )

    private fun buildColumns(): Columns {
        val indexR = margin + 20f // 60f
        val nameLeft = indexR + 8f // 68f
        val nameR = 310f
        val qtyR = 370f
        val rateR = 445f
        val totalR = rightEdge // 555f
        return Columns(indexR, nameLeft, nameR, qtyR, rateR, totalR)
    }

    private fun getInitials(name: String): String {
        val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            words.isEmpty() -> "KJ"
            words.size == 1 -> words[0].take(2).uppercase()
            else -> "${words[0].first()}${words[1].first()}".uppercase()
        }
    }

    /**
     * @param logo Optional business logo; drawn aspect-fit in the letterhead in place of the initials badge.
     */
    fun render(model: InvoiceDocumentModel, logo: Bitmap? = null): PdfDocument {
        val document = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var currentY = margin

        fun drawRight(paint: Paint, text: String, rightX: Float, baseline: Float, pad: Float = 0f) {
            canvas.drawText(text, rightX - paint.measureText(text) - pad, baseline, paint)
        }

        fun drawFooter(c: Canvas, no: Int) {
            val footerStr = "This is a computer-generated invoice. | Invoice ${model.invoiceNumber} • ${model.invoiceDate} (Page $no)"
            val footerWidth = smallPaint.measureText(footerStr)
            c.drawText(footerStr, pageWidth / 2f - footerWidth / 2f, pageHeight - 20f, smallPaint)
        }

        fun checkSpace(requiredHeight: Float) {
            if (currentY + requiredHeight > pageHeight - margin) {
                drawFooter(canvas, pageNumber)
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin
            }
        }

        checkSpace(90f)

        // 1. Letterhead matching 01-classic-corporate.html
        val headerStartY = currentY
        val logoSize = 36f
        val (logoW, logoH) = if (logo != null) {
            ImageSizing.fitInside(logo.width, logo.height, LOGO_MAX_WIDTH, LOGO_MAX_HEIGHT)
        } else {
            0f to 0f
        }
        val logoSlotW = if (logo != null && logoW > 0f) {
            val logoTop = headerStartY + (LOGO_MAX_HEIGHT - logoH) / 2f
            canvas.drawBitmap(logo, null, RectF(margin, logoTop, margin + logoW, logoTop + logoH), logoPaint)
            logoW
        } else {
            val logoRect = RectF(margin, headerStartY, margin + logoSize, headerStartY + logoSize)
            canvas.drawRoundRect(logoRect, 8f, 8f, navyFillPaint)
            val initials = getInitials(model.businessName)
            val initWidth = whiteLogoPaint.measureText(initials)
            canvas.drawText(initials, margin + (logoSize - initWidth) / 2f, headerStartY + 23.5f, whiteLogoPaint)
            logoSize
        }

        val busLeft = margin + logoSlotW + 10f
        val headerLeftW = contentWidth - 210f - (logoSlotW + 10f)
        canvas.drawText(truncate(model.businessName, bigNamePaint, headerLeftW), busLeft, headerStartY + 16f, bigNamePaint)

        val contactLines = listOfNotNull(
            model.businessAddress.takeIf { it.isNotBlank() },
            buildString {
                append("Phone: ").append(model.businessPhone)
                if (!model.businessEmail.isNullOrBlank()) append("   ·   ").append(model.businessEmail)
            }
        )
        var busY = headerStartY + 16f
        contactLines.forEach { line ->
            busY += 12f
            canvas.drawText(truncate(line, mutedPaint, headerLeftW), busLeft, busY, mutedPaint)
        }

        // Right side: Document Title & Tag
        drawRight(docTypePaint, model.invoiceTitle.uppercase(), rightEdge, headerStartY + 17f)
        val tagText = if (model.businessGstin != null) "GST INVOICE" else "INVOICE"
        val tagW = tagTextPaint.measureText(tagText) + 14f
        val tagH = 15f
        val tagLeft = rightEdge - tagW
        val tagTop = headerStartY + 23f
        val tagRect = RectF(tagLeft, tagTop, rightEdge, tagTop + tagH)
        canvas.drawRoundRect(tagRect, 7.5f, 7.5f, navyFillPaint)
        canvas.drawText(tagText, tagLeft + 7f, tagTop + 10.5f, tagTextPaint)

        currentY = maxOf(busY + 12f, tagTop + tagH + 10f, headerStartY + logoH + 10f)

        // 3.5pt solid Navy rule under letterhead
        canvas.drawLine(margin, currentY, rightEdge, currentY, navyBarPaint)
        currentY += 14f

        // 2. Parties + invoice details (Clean 2-Column)
        checkSpace(65f)
        val partiesTop = currentY
        val partyColW = (contentWidth / 2f) - 10f

        // Left: Billed to
        canvas.drawText("BILLED TO", margin, partiesTop + 8f, labelPaint)
        canvas.drawText(truncate(model.customerName, partyNamePaint, partyColW), margin, partiesTop + 22f, partyNamePaint)
        var pY = partiesTop + 22f
        if (!model.customerAddress.isNullOrBlank()) {
            wrapText(model.customerAddress, mutedPaint, partyColW).take(2).forEach { line ->
                pY += 12f
                canvas.drawText(line, margin, pY, mutedPaint)
            }
        }
        if (!model.customerGstin.isNullOrBlank()) {
            pY += 13f
            canvas.drawText(truncate("GSTIN: ${model.customerGstin}", gstinPaint, partyColW), margin, pY, gstinPaint)
        }

        // Right: Invoice details table
        val metaW = 180f
        val metaLeft = rightEdge - metaW
        canvas.drawText("INVOICE DETAILS", metaLeft, partiesTop + 8f, labelPaint)
        val kvRows = buildList {
            add("Invoice No:" to model.invoiceNumber)
            add("Invoice Date:" to model.invoiceDate)
            if (!model.dueDate.isNullOrBlank()) add("Due Date:" to model.dueDate)
            if (!model.customerState.isNullOrBlank()) add("Place of Supply:" to model.customerState)
        }
        var kvY = partiesTop + 8f
        kvRows.forEach { (label, value) ->
            kvY += 13f
            canvas.drawText(truncate(label, mutedPaint, 95f), metaLeft, kvY, mutedPaint)
            drawRight(boldPaint, truncate(value, boldPaint, 80f), rightEdge, kvY)
        }
        currentY = maxOf(pY, kvY) + 16f

        // 3. Clean Items Table with Solid Navy Header & Zebra Striping
        val cols = buildColumns()

        fun rightWhiteLabel(c: Canvas, text: String, rightX: Float, y: Float) {
            c.drawText(text, rightX - whiteHeaderPaint.measureText(text) - 6f, y + 13.5f, whiteHeaderPaint)
        }

        fun drawTableHeader(c: Canvas, y: Float) {
            val headerRect = RectF(margin, y, rightEdge, y + 19f)
            c.drawRoundRect(headerRect, 2f, 2f, navyFillPaint)
            rightWhiteLabel(c, "#", cols.indexR, y)
            c.drawText("ITEM DESCRIPTION", cols.nameLeft, y + 13.5f, whiteHeaderPaint)
            rightWhiteLabel(c, "QTY", cols.qtyR, y)
            rightWhiteLabel(c, "RATE (₹)", cols.rateR, y)
            rightWhiteLabel(c, "AMOUNT (₹)", cols.totalR, y)
        }

        checkSpace(35f)
        drawTableHeader(canvas, currentY)
        currentY += 21f

        model.items.forEachIndexed { idx, item ->
            val subtitle = buildList {
                if (!item.hsnCode.isNullOrBlank()) add("HSN: ${item.hsnCode}")
                if (item.gstRate.isNotBlank() && item.gstRate != "0%") add("GST ${item.gstRate}")
                if (!item.discount.isNullOrBlank()) add("Disc: ${item.discount}")
            }.joinToString("   ·   ")

            val rowHeight = if (subtitle.isNotBlank()) 25f else 19f
            checkSpace(rowHeight + 4f)
            if (currentY <= margin + 1f) {
                drawTableHeader(canvas, currentY)
                currentY += 21f
            }

            // Zebra striping: alternate rows
            if (idx % 2 == 1) {
                val zebraRect = RectF(margin, currentY, rightEdge, currentY + rowHeight)
                canvas.drawRect(zebraRect, zebraPaint)
            }

            val rowBase = currentY + 12.5f
            val itemNameWidth = cols.nameR - cols.nameLeft

            drawRight(mutedPaint, item.index.toString(), cols.indexR, rowBase, pad = 6f)
            canvas.drawText(truncate(item.name, boldPaint, itemNameWidth), cols.nameLeft, rowBase, boldPaint)

            val qtyText = listOfNotNull(item.quantity, item.unit).joinToString(" ").trim()
            drawRight(regularPaint, qtyText, cols.qtyR, rowBase, pad = 6f)
            drawRight(regularPaint, item.rate, cols.rateR, rowBase, pad = 6f)
            drawRight(boldPaint, item.taxableAmount, cols.totalR, rowBase, pad = 6f)

            if (subtitle.isNotBlank()) {
                canvas.drawText(truncate(subtitle, smallPaint, itemNameWidth), cols.nameLeft, currentY + 22f, smallPaint)
            }

            canvas.drawLine(margin, currentY + rowHeight, rightEdge, currentY + rowHeight, linePaint)
            currentY += rowHeight
        }

        // 4. Lower section: Anchored to bottom of page for single-page invoices
        val footerHeight = 150f
        if (currentY + footerHeight > pageHeight - margin) {
            drawFooter(canvas, pageNumber)
            document.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            currentY = margin
        }

        val footerStartY = maxOf(currentY + 18f, pageHeight - margin - footerHeight)
        val totalsW = 195f
        val totalColX = rightEdge - totalsW
        val leftColW = contentWidth - totalsW - 20f

        // Right: Bordered total box with solid Navy Grand Total row
        var boxY = footerStartY
        fun drawBoxRow(label: String, value: String, isDiscount: Boolean = false) {
            val rowH = 15f
            val rowRect = RectF(totalColX, boxY, rightEdge, boxY + rowH)
            canvas.drawRect(rowRect, cardBorderPaint)
            canvas.drawText(truncate(label, mutedPaint, 100f), totalColX + 8f, boxY + 11f, mutedPaint)
            val vPaint = if (isDiscount) discountPaint else boldPaint
            drawRight(vPaint, value, rightEdge - 8f, boxY + 11f)
            boxY += rowH
        }

        drawBoxRow("Subtotal", model.subtotal)
        if (model.showDiscount) {
            drawBoxRow("Total Discount", "-${model.totalDiscount}", isDiscount = true)
        }
        if (model.showTaxSummary) {
            model.taxSummary.forEach { row ->
                val cleanLabel = if (row.label.contains(" on ")) {
                    val part = row.label.substringBefore(" on ")
                    part.replace("@", "").replace(Regex("\\s+"), " ").trim()
                } else {
                    row.label
                }
                drawBoxRow(cleanLabel, row.amount)
            }
        }

        // Solid Navy Grand Total Row
        val grandH = 22f
        val grandRect = RectF(totalColX, boxY, rightEdge, boxY + grandH)
        canvas.drawRect(grandRect, navyFillPaint)
        canvas.drawText("Grand Total", totalColX + 8f, boxY + 15f, whiteHeaderPaint)
        drawRight(whiteGrandPaint, model.grandTotal, rightEdge - 8f, boxY + 16f)

        // Right side under totals: Signature Block
        val signTop = boxY + grandH + 18f
        val signLineY = signTop + 14f
        canvas.drawLine(totalColX + 25f, signLineY, rightEdge, signLineY, linePaint)

        val signName = model.signatureName ?: model.businessName
        val signTitle = "For $signName"
        val signTitleW = boldPaint.measureText(signTitle)
        val signLineW = rightEdge - (totalColX + 25f)
        val signTitleX = (totalColX + 25f) + (signLineW - signTitleW) / 2f
        canvas.drawText(truncate(signTitle, boldPaint, signLineW), maxOf(totalColX + 25f, signTitleX), signLineY + 12f, boldPaint)

        val authText = "Authorised Signatory"
        val authW = smallPaint.measureText(authText)
        val authX = (totalColX + 25f) + (signLineW - authW) / 2f
        canvas.drawText(authText, maxOf(totalColX + 25f, authX), signLineY + 23f, smallPaint)

        // Left side: Amount in Words with Teal accent bar
        var leftY = footerStartY
        if (model.amountInWords.isNotBlank()) {
            val wordsLines = wrapText(model.amountInWords, wordsPaint, leftColW - 20f)
            val wordsH = maxOf(32f, 16f + wordsLines.size * 11f)
            val wordsRect = RectF(margin, leftY, margin + leftColW, leftY + wordsH)
            canvas.drawRect(wordsRect, zebraPaint)
            canvas.drawRect(wordsRect, cardBorderPaint)
            // Teal accent left bar
            val tealRect = RectF(margin, leftY, margin + 3.5f, leftY + wordsH)
            canvas.drawRect(tealRect, tealBarPaint)

            canvas.drawText("Amount in words:", margin + 10f, leftY + 11.5f, navyBoldPaint)
            var wY = leftY + 11.5f
            wordsLines.forEach { line ->
                wY += 11f
                canvas.drawText(line, margin + 10f, wY, wordsPaint)
            }
            leftY += wordsH + 8f
        }

        // Left side: Payment Details Card
        val payH = 32f
        val payRect = RectF(margin, leftY, margin + leftColW, leftY + payH)
        canvas.drawRoundRect(payRect, 4f, 4f, zebraPaint)
        canvas.drawRoundRect(payRect, 4f, 4f, cardBorderPaint)

        val payTitle = if (model.paymentStatus == "PAID") "Payment Status: Paid" else "Payment Details"
        canvas.drawText(payTitle, margin + 8f, leftY + 12f, navyBoldPaint)
        val payLine = buildString {
            append("Status: ").append(model.paymentStatus ?: "Pending")
            if (!model.paymentMethod.isNullOrBlank()) append(" (").append(model.paymentMethod).append(")")
            if (!model.amountPaid.isNullOrBlank() && model.paymentStatus == "PARTIALLY_PAID") {
                append("  ·  Paid: ").append(model.amountPaid)
            }
            if (!model.balanceDue.isNullOrBlank() && model.paymentStatus != "PAID") {
                append("  ·  Balance: ").append(model.balanceDue)
            }
        }
        canvas.drawText(truncate(payLine, smallPaint, leftColW - 16f), margin + 8f, leftY + 24f, smallPaint)
        leftY += payH + 8f

        // Left side: Notes & Terms
        canvas.drawText("NOTES & TERMS", margin, leftY + 8f, labelPaint)
        leftY += 8f
        if (!model.notes.isNullOrBlank()) {
            wrapText(model.notes, mutedPaint, leftColW).take(2).forEach { line ->
                leftY += 10f
                canvas.drawText(line, margin, leftY, mutedPaint)
            }
        }
        val termsText = model.terms?.takeIf { it.isNotBlank() }
            ?: "We declare that this invoice shows the actual price of the goods described and that all particulars are true and correct."
        wrapText(termsText, smallPaint, leftColW).take(2).forEach { line ->
            leftY += 9.5f
            canvas.drawText(line, margin, leftY, smallPaint)
        }

        drawFooter(canvas, pageNumber)
        document.finishPage(page)
        return document
    }

    private fun truncate(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        val ellipsis = "..."
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end) + ellipsis) > maxWidth) {
            end--
        }
        return if (end > 0) text.substring(0, end) + ellipsis else ""
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return emptyList()
        val words = text.split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = StringBuilder(candidate)
            } else {
                if (current.isNotEmpty()) {
                    lines.add(current.toString())
                    current = StringBuilder()
                }
                var remainder = word
                while (paint.measureText(remainder) > maxWidth && remainder.isNotEmpty()) {
                    var len = remainder.length
                    while (len > 1 && paint.measureText(remainder.substring(0, len)) > maxWidth) {
                        len--
                    }
                    lines.add(remainder.substring(0, len))
                    remainder = remainder.substring(len)
                }
                if (remainder.isNotEmpty()) {
                    current = StringBuilder(remainder)
                }
            }
        }
        if (current.isNotEmpty()) {
            lines.add(current.toString())
        }
        return lines
    }

    companion object {
        private const val LOGO_MAX_WIDTH = 90f
        private const val LOGO_MAX_HEIGHT = 44f
    }
}
