package com.suyash.mockcivilaviationexam.domain.usecase

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.suyash.mockcivilaviationexam.data.local.repository.FlightEntryRepository
import com.suyash.mockcivilaviationexam.data.local.repository.UserProfileRepository
import com.suyash.mockcivilaviationexam.domain.logbook.FlightTimeCalculator
import com.suyash.mockcivilaviationexam.domain.model.FlightEntry
import com.suyash.mockcivilaviationexam.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
class PdfExportUseCase(
    private val flightEntryRepository: FlightEntryRepository,
    private val userProfileRepository: UserProfileRepository
) {

    suspend fun exportLogbookToPdf(
        context: Context,
        userId: String,
        startDate: String? = null,
        endDate: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // A pilot who has not filled in their profile should still be able
            // to export; fall back to whatever the signed-in account gives us
            // rather than failing the export outright.
            val userProfile = userProfileRepository.getUserProfileSync(userId)
                ?: defaultProfile(userId)

            val flights = if (startDate != null && endDate != null) {
                flightEntryRepository.getFlightsByDateRange(userId, startDate, endDate)
            } else {
                // For PDF export, we need to collect all flights from the Flow
                flightEntryRepository.getAllFlights(userId).first()
            }.sortedBy { it.date }

            val fileName = generateFileName(userProfile)
            val file = createPdfFile(context, fileName)
            
            generatePdfDocument(flights, userProfile, file)
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun defaultProfile(userId: String): UserProfile {
        val account = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        return UserProfile(
            id = userId,
            name = account?.displayName?.takeIf { it.isNotBlank() } ?: "Pilot",
            email = account?.email ?: ""
        )
    }

    private fun generateFileName(userProfile: UserProfile): String {
        val name = userProfile.name.replace(" ", "_")
        val timestamp = System.currentTimeMillis()
        return "Pilot_Logbook_${name}_$timestamp.pdf"
    }

    private fun createPdfFile(context: Context, fileName: String): File {
        val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Logbooks")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return File(directory, fileName)
    }

    private suspend fun generatePdfDocument(
        flights: List<FlightEntry>,
        userProfile: UserProfile,
        file: File
    ) {
        val document = PdfDocument()
        val pageWidth = 842 // A4 landscape width in points
        val pageHeight = 595 // A4 landscape height in points
        
        var currentPage = 1
        var yPosition = 50f
        val lineHeight = 20f
        val flightsPerPage = 25

        flights.chunked(flightsPerPage).forEachIndexed { pageIndex, pageFlights ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            
            // Draw page header
            yPosition = drawPageHeader(canvas, userProfile, currentPage, pageWidth)
            
            // Draw table header
            yPosition = drawTableHeader(canvas, yPosition, pageWidth)
            
            // Draw flight entries
            pageFlights.forEach { flight ->
                yPosition = drawFlightEntry(canvas, flight, yPosition, pageWidth, lineHeight)
            }
            
            // Draw page footer
            drawPageFooter(canvas, currentPage, pageHeight, pageWidth)
            
            document.finishPage(page)
            currentPage++
            yPosition = 50f
        }

        // Write document to file
        FileOutputStream(file).use { outputStream ->
            document.writeTo(outputStream)
        }
        document.close()
    }

    private fun drawPageHeader(
        canvas: android.graphics.Canvas,
        userProfile: UserProfile,
        pageNumber: Int,
        pageWidth: Int
    ): Float {
        val paint = Paint().apply {
            textSize = 16f
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
        }

        var y = 30f
        
        // Main title
        canvas.drawText(
            "KENYA CIVIL AVIATION AUTHORITY - PILOT LOGBOOK",
            (pageWidth / 2 - 200).toFloat(),
            y,
            paint
        )
        
        y += 25f
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        
        // Pilot information
        canvas.drawText("Pilot Name: ${userProfile.name}", 50f, y, paint)
        canvas.drawText("License Number: ${userProfile.licenseNumber ?: "N/A"}", 300f, y, paint)
        canvas.drawText("Page: $pageNumber", (pageWidth - 100).toFloat(), y, paint)
        
        y += 15f
        
        canvas.drawText("Certificate Type: ${userProfile.certificateType?.name ?: "N/A"}", 50f, y, paint)
        canvas.drawText("Export Date: ${java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", 300f, y, paint)
        
        return y + 20f
    }

    private fun drawTableHeader(
        canvas: android.graphics.Canvas,
        startY: Float,
        pageWidth: Int
    ): Float {
        val paint = Paint().apply {
            textSize = 10f
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
        }

        var y = startY
        
        // Draw header background
        val headerPaint = Paint().apply {
            color = android.graphics.Color.LTGRAY
        }
        canvas.drawRect(30f, y - 15f, (pageWidth - 30).toFloat(), y + 5f, headerPaint)
        
        // Column positions
        val columns = listOf(
            Pair(COL_DATE, "Date"),
            Pair(COL_DEP, "Dep"),
            Pair(COL_ARR, "Arr"),
            Pair(COL_REG, "Reg"),
            Pair(COL_TYPE, "Type"),
            Pair(COL_OFF, "Off"),
            Pair(COL_ON, "On"),
            Pair(COL_BLOCK, "Block"),
            Pair(COL_AIR, "Air"),
            Pair(COL_LDG, "Ldg"),
            Pair(COL_TOTAL, "Total"),
            Pair(COL_PIC, "PIC"),
            Pair(COL_DUAL, "Dual"),
            Pair(COL_NIGHT, "Night"),
            Pair(COL_IFR, "IFR"),
            Pair(COL_XC, "XC"),
            Pair(COL_SIM, "Sim"),
            Pair(COL_INSTRUCTOR, "Instructor"),
            Pair(COL_REMARKS, "Remarks")
        )
        
        columns.forEach { (x, text) ->
            canvas.drawText(text, x, y, paint)
        }
        
        // Draw line under header
        canvas.drawLine(30f, y + 5f, (pageWidth - 30).toFloat(), y + 5f, paint)
        
        return y + 15f
    }

    private fun drawFlightEntry(
        canvas: android.graphics.Canvas,
        flight: FlightEntry,
        startY: Float,
        pageWidth: Int,
        lineHeight: Float
    ): Float {
        val paint = Paint().apply {
            textSize = 9f
            color = android.graphics.Color.BLACK
        }

        val y = startY + lineHeight
        
        // Format flight data
        val date = flight.date.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
        val offBlocks = FlightTimeCalculator.format(flight.offBlockTime).ifEmpty { "-" }
        val onBlocks = FlightTimeCalculator.format(flight.onBlockTime).ifEmpty { "-" }
        val blockTime =
            if (flight.blockTime > 0) FlightTimeCalculator.formatHoursMinutes(flight.blockTime) else "-"
        val airTime =
            if (flight.airTime > 0) FlightTimeCalculator.formatHoursMinutes(flight.airTime) else "-"
        val landings =
            if (flight.totalLandings > 0) "${flight.dayLandings}/${flight.nightLandings}" else "-"
        val totalTime = FlightTimeCalculator.formatDecimal(flight.totalFlightTime)
        val picTime = FlightTimeCalculator.formatDecimal(flight.picTime)
        val dualTime = FlightTimeCalculator.formatDecimal(flight.dualTime)
        val nightTime = FlightTimeCalculator.formatDecimal(flight.nightTime)
        val ifrTime = FlightTimeCalculator.formatDecimal(flight.ifrTime)
        val xcTime = FlightTimeCalculator.formatDecimal(flight.crossCountryTime)
        val simTime =
            if (flight.simulatorTime > 0) "${FlightTimeCalculator.formatDecimal(flight.simulatorTime)}*" else "-"
        val instructor = flight.instructorName?.let { "$it (${flight.instructorLicenseNumber})" } ?: "-"
        val remarks = flight.remarks.take(20) + if (flight.remarks.length > 20) "..." else ""

        // Draw flight data
        canvas.drawText(date, COL_DATE, y, paint)
        canvas.drawText(flight.departureAerodrome.take(5), COL_DEP, y, paint)
        canvas.drawText(flight.arrivalAerodrome.take(5), COL_ARR, y, paint)
        canvas.drawText(flight.aircraftRegistration.take(8), COL_REG, y, paint)
        canvas.drawText(flight.aircraftType.take(6), COL_TYPE, y, paint)
        canvas.drawText(offBlocks, COL_OFF, y, paint)
        canvas.drawText(onBlocks, COL_ON, y, paint)
        canvas.drawText(blockTime, COL_BLOCK, y, paint)
        canvas.drawText(airTime, COL_AIR, y, paint)
        canvas.drawText(landings, COL_LDG, y, paint)
        canvas.drawText(totalTime, COL_TOTAL, y, paint)
        canvas.drawText(picTime, COL_PIC, y, paint)
        canvas.drawText(dualTime, COL_DUAL, y, paint)
        canvas.drawText(nightTime, COL_NIGHT, y, paint)
        canvas.drawText(ifrTime, COL_IFR, y, paint)
        canvas.drawText(xcTime, COL_XC, y, paint)
        canvas.drawText(simTime, COL_SIM, y, paint)
        canvas.drawText(instructor.take(14), COL_INSTRUCTOR, y, paint)
        canvas.drawText(remarks, COL_REMARKS, y, paint)

        return y
    }

    /**
     * Column x-positions for the A4-landscape logbook table (842pt wide, 30pt
     * margins). Shared by the header and the rows so the two cannot drift apart.
     */
    private companion object {
        const val COL_DATE = 36f
        const val COL_DEP = 82f
        const val COL_ARR = 116f
        const val COL_REG = 150f
        const val COL_TYPE = 200f
        const val COL_OFF = 240f
        const val COL_ON = 270f
        const val COL_BLOCK = 300f
        const val COL_AIR = 334f
        const val COL_LDG = 366f
        const val COL_TOTAL = 396f
        const val COL_PIC = 430f
        const val COL_DUAL = 460f
        const val COL_NIGHT = 492f
        const val COL_IFR = 526f
        const val COL_XC = 552f
        const val COL_SIM = 580f
        const val COL_INSTRUCTOR = 614f
        const val COL_REMARKS = 700f
    }

    private fun drawPageFooter(
        canvas: android.graphics.Canvas,
        pageNumber: Int,
        pageHeight: Int,
        pageWidth: Int
    ) {
        val paint = Paint().apply {
            textSize = 10f
            color = android.graphics.Color.GRAY
        }

        val y = (pageHeight - 20).toFloat()

        canvas.drawText(
            "* Simulator/Synthetic Flight Training Device",
            50f,
            y - 27f,
            paint
        )

        canvas.drawText(
            "This logbook is generated in compliance with aviation regulations",
            50f,
            y - 14f,
            paint
        )

        // Exported logbooks get shared with instructors and examiners, so the
        // footer carries an attribution line back to the app. Kept short so it
        // cannot run into the page number on the right.
        canvas.drawText(
            "Generated with Aviation Exam Pro - free on Google Play",
            50f,
            y,
            paint
        )

        canvas.drawText(
            "Page $pageNumber",
            (pageWidth - 80).toFloat(),
            y,
            paint
        )
    }

    suspend fun getExportDirectory(context: Context): String {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Logbooks").absolutePath
    }

    suspend fun exportFlightSummary(
        context: Context,
        userId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val summary = flightEntryRepository.getLogbookSummary(userId)
            val userProfile = userProfileRepository.getUserProfileSync(userId)
                ?: throw IllegalStateException("User profile not found")

            val fileName = "Flight_Summary_${userProfile.name.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val file = createPdfFile(context, fileName)
            
            generateSummaryPdf(summary, userProfile, file)
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun generateSummaryPdf(
        summary: com.suyash.mockcivilaviationexam.domain.model.LogbookSummary,
        userProfile: UserProfile,
        file: File
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 portrait
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        val titlePaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
        }
        
        val headerPaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
        }
        
        val bodyPaint = Paint().apply {
            textSize = 12f
            color = android.graphics.Color.BLACK
        }

        var y = 50f
        
        // Title
        canvas.drawText("FLIGHT HOURS SUMMARY", 200f, y, titlePaint)
        y += 40f
        
        // Pilot information
        canvas.drawText("Pilot: ${userProfile.name}", 50f, y, headerPaint)
        y += 20f
        canvas.drawText("License: ${userProfile.licenseNumber ?: "N/A"}", 50f, y, bodyPaint)
        y += 15f
        canvas.drawText("Certificate: ${userProfile.certificateType?.name ?: "N/A"}", 50f, y, bodyPaint)
        y += 30f
        
        // Flight time breakdown
        canvas.drawText("FLIGHT TIME BREAKDOWN", 50f, y, headerPaint)
        y += 25f
        
        val summaryItems = listOf(
            "Total Flight Time" to "${summary.totalTime} hours",
            "Pilot-in-Command (PIC)" to "${summary.picTime} hours",
            "Dual Instruction Received" to "${summary.dualTime} hours",
            "Co-Pilot Time" to "${summary.coPilotTime} hours",
            "Flight Instructor Time" to "${summary.instructorTime} hours",
            "Cross Country Time" to "${summary.crossCountryTime} hours",
            "Night Flight Time" to "${summary.nightTime} hours",
            "IFR Flight Time" to "${summary.ifrTime} hours",
            "VFR Flight Time" to "${summary.vfrTime} hours",
            "Simulator/Synthetic Training" to "${summary.simulatorTime} hours",
            "Total Landings" to "${summary.totalLandings}",
            "Night Landings" to "${summary.nightLandings}"
        )
        
        summaryItems.forEach { (label, value) ->
            canvas.drawText("$label:", 50f, y, bodyPaint)
            canvas.drawText(value, 350f, y, bodyPaint)
            y += 20f
        }
        
        y += 30f
        canvas.drawText("Generated: ${java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}", 50f, y, bodyPaint)
        y += 15f
        canvas.drawText("Compliant with aviation regulations", 50f, y, bodyPaint)

        document.finishPage(page)
        
        FileOutputStream(file).use { outputStream ->
            document.writeTo(outputStream)
        }
        document.close()
    }
}