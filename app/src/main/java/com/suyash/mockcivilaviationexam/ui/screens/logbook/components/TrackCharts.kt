package com.suyash.mockcivilaviationexam.ui.screens.logbook.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suyash.mockcivilaviationexam.domain.logbook.TrackPoint
import com.suyash.mockcivilaviationexam.ui.theme.AvionicsBlue
import com.suyash.mockcivilaviationexam.ui.theme.AvionicsBlueSubtle
import com.suyash.mockcivilaviationexam.ui.theme.AviationGold
import com.suyash.mockcivilaviationexam.ui.theme.SkyBorder
import com.suyash.mockcivilaviationexam.ui.theme.TextDarkSecondary
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max

/*
 * Hand-drawn charts for a recorded flight. There is no charting library in the
 * project and these three views do not justify one: a line, a fill, a few
 * ticks. Every chart downsamples to [MAX_POINTS] first so a two-hour lesson at
 * one fix per second (7,200 points) does not cost a frame.
 */

private const val MAX_POINTS = 600
private const val TAKEOFF_REFERENCE_KT = 40.0

/** Altitude in feet against elapsed minutes, area filled under the line. */
@Composable
fun AltitudeProfileChart(
    points: List<TrackPoint>,
    modifier: Modifier = Modifier
) {
    val sampled = remember(points) { downsample(points) }
    val measurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        if (sampled.size < 2) return@Canvas
        drawTimeSeries(
            points = sampled,
            value = { it.altitudeFt },
            unit = "ft",
            measurer = measurer,
            referenceLine = null
        )
    }
}

/** Groundspeed in knots against elapsed minutes, with the 40 kt takeoff rule marked. */
@Composable
fun SpeedProfileChart(
    points: List<TrackPoint>,
    modifier: Modifier = Modifier
) {
    val sampled = remember(points) { downsample(points) }
    val measurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        if (sampled.size < 2) return@Canvas
        drawTimeSeries(
            points = sampled,
            value = { it.groundSpeedKt },
            unit = "kt",
            measurer = measurer,
            referenceLine = ReferenceLine(TAKEOFF_REFERENCE_KT, "takeoff")
        )
    }
}

/**
 * The ground track as a shape. Longitude is scaled by cos(mean latitude) so a
 * circuit looks like a circuit rather than an ellipse. Start is a filled dot,
 * end a hollow one; north is up.
 */
@Composable
fun TrackOutline(
    points: List<TrackPoint>,
    modifier: Modifier = Modifier
) {
    val sampled = remember(points) { downsample(points) }
    val measurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        if (sampled.size < 2) return@Canvas

        val meanLat = sampled.map { it.latitude }.average()
        val lonScale = cos(Math.toRadians(meanLat))
        val xs = sampled.map { it.longitude * lonScale }
        val ys = sampled.map { it.latitude }
        val minX = xs.min()
        val maxX = xs.max()
        val minY = ys.min()
        val maxY = ys.max()
        val spanX = max(maxX - minX, 1e-6)
        val spanY = max(maxY - minY, 1e-6)

        val pad = 20.dp.toPx()
        val availW = size.width - 2 * pad
        val availH = size.height - 2 * pad
        // One scale for both axes so the shape is true.
        val scale = minOf(availW / spanX, availH / spanY)
        val drawnW = spanX * scale
        val drawnH = spanY * scale
        val originX = (pad + (availW - drawnW) / 2).toFloat()
        val originY = (pad + (availH - drawnH) / 2).toFloat()

        fun toOffset(i: Int): Offset {
            val x = originX + ((xs[i] - minX) * scale).toFloat()
            // Latitude grows northwards; screen y grows downwards.
            val y = originY + ((maxY - ys[i]) * scale).toFloat()
            return Offset(x, y)
        }

        val path = Path()
        path.moveTo(toOffset(0).x, toOffset(0).y)
        for (i in 1 until sampled.size) {
            val o = toOffset(i)
            path.lineTo(o.x, o.y)
        }
        drawPath(
            path = path,
            color = AvionicsBlue,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val start = toOffset(0)
        val end = toOffset(sampled.lastIndex)
        drawCircle(color = AvionicsBlue, radius = 5.dp.toPx(), center = start)
        drawCircle(color = Color.White, radius = 5.dp.toPx(), center = end)
        drawCircle(
            color = AvionicsBlue,
            radius = 5.dp.toPx(),
            center = end,
            style = Stroke(width = 2.dp.toPx())
        )

        drawNorthArrow(measurer)
    }
}

// ---- shared drawing ---------------------------------------------------------

private data class ReferenceLine(val value: Double, val label: String)

private fun DrawScope.drawTimeSeries(
    points: List<TrackPoint>,
    value: (TrackPoint) -> Double,
    unit: String,
    measurer: TextMeasurer,
    referenceLine: ReferenceLine?
) {
    val labelStyle = TextStyle(color = TextDarkSecondary, fontSize = 10.sp)

    val leftPad = 44.dp.toPx()
    val rightPad = 8.dp.toPx()
    val topPad = 8.dp.toPx()
    val bottomPad = 20.dp.toPx()
    val plotW = size.width - leftPad - rightPad
    val plotH = size.height - topPad - bottomPad
    if (plotW <= 0f || plotH <= 0f) return

    val t0 = points.first().timestamp
    val totalMinutes = max((points.last().timestamp - t0) / 60_000.0, 1.0)

    val values = points.map(value)
    val rawMax = max(values.max(), referenceLine?.value ?: 0.0)
    val yTicks = niceTicks(0.0, rawMax)
    val yMax = yTicks.last()

    fun x(p: TrackPoint) = leftPad + ((p.timestamp - t0) / 60_000.0 / totalMinutes * plotW).toFloat()
    fun y(v: Double) = topPad + (plotH - (v / yMax * plotH)).toFloat()

    // Axes and horizontal grid
    val axisStroke = 1.dp.toPx()
    yTicks.forEach { tick ->
        val yy = y(tick)
        drawLine(SkyBorder, Offset(leftPad, yy), Offset(leftPad + plotW, yy), axisStroke)
        val text = formatTick(tick) + if (tick == yTicks.last()) " $unit" else ""
        val layout = measurer.measure(text, labelStyle)
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(leftPad - layout.size.width - 4.dp.toPx(), yy - layout.size.height / 2)
        )
    }
    drawLine(SkyBorder, Offset(leftPad, topPad), Offset(leftPad, topPad + plotH), axisStroke)

    // X ticks in elapsed minutes
    val xTicks = niceTicks(0.0, totalMinutes, targetCount = 5)
    xTicks.filter { it <= totalMinutes }.forEach { m ->
        val xx = leftPad + (m / totalMinutes * plotW).toFloat()
        drawLine(SkyBorder, Offset(xx, topPad + plotH), Offset(xx, topPad + plotH + 4.dp.toPx()), axisStroke)
        val layout = measurer.measure("${formatTick(m)} min", labelStyle)
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(xx - layout.size.width / 2, topPad + plotH + 5.dp.toPx())
        )
    }

    // Filled area
    val fill = Path()
    fill.moveTo(x(points.first()), y(0.0))
    points.forEach { p -> fill.lineTo(x(p), y(value(p))) }
    fill.lineTo(x(points.last()), y(0.0))
    fill.close()
    drawPath(fill, AvionicsBlueSubtle)

    // Line
    val line = Path()
    line.moveTo(x(points.first()), y(value(points.first())))
    for (i in 1 until points.size) line.lineTo(x(points[i]), y(value(points[i])))
    drawPath(
        line,
        AvionicsBlue,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Reference line (e.g. takeoff speed)
    if (referenceLine != null && referenceLine.value <= yMax) {
        val yy = y(referenceLine.value)
        drawLine(
            color = AviationGold,
            start = Offset(leftPad, yy),
            end = Offset(leftPad + plotW, yy),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        )
        val layout = measurer.measure(
            referenceLine.label,
            TextStyle(color = AviationGold, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        )
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(leftPad + plotW - layout.size.width - 2.dp.toPx(), yy - layout.size.height - 2.dp.toPx())
        )
    }
}

private fun DrawScope.drawNorthArrow(measurer: TextMeasurer) {
    val margin = 10.dp.toPx()
    val length = 22.dp.toPx()
    val cx = size.width - margin - 8.dp.toPx()
    val top = margin
    val bottom = top + length
    drawLine(TextDarkSecondary, Offset(cx, bottom), Offset(cx, top), 1.5.dp.toPx(), cap = StrokeCap.Round)
    val head = Path().apply {
        moveTo(cx, top)
        lineTo(cx - 4.dp.toPx(), top + 7.dp.toPx())
        lineTo(cx + 4.dp.toPx(), top + 7.dp.toPx())
        close()
    }
    drawPath(head, TextDarkSecondary)
    val layout = measurer.measure(
        "N",
        TextStyle(color = TextDarkSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    )
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(cx - layout.size.width / 2, bottom + 2.dp.toPx())
    )
}

// ---- helpers -----------------------------------------------------------------

/** Keeps at most [MAX_POINTS] samples, always including the first and last. */
private fun downsample(points: List<TrackPoint>): List<TrackPoint> {
    if (points.size <= MAX_POINTS) return points
    val step = points.size.toDouble() / MAX_POINTS
    val out = ArrayList<TrackPoint>(MAX_POINTS + 1)
    var i = 0.0
    while (i < points.size) {
        out.add(points[i.toInt()])
        i += step
    }
    if (out.last() !== points.last()) out.add(points.last())
    return out
}

/** Round-number ticks covering [min, max]; always ends at or above max. */
private fun niceTicks(min: Double, max: Double, targetCount: Int = 4): List<Double> {
    val span = max(max - min, 1.0)
    val rough = span / targetCount
    val magnitude = Math.pow(10.0, floor(Math.log10(rough)))
    val residual = rough / magnitude
    val step = when {
        residual <= 1.0 -> 1.0
        residual <= 2.0 -> 2.0
        residual <= 2.5 -> 2.5
        residual <= 5.0 -> 5.0
        else -> 10.0
    } * magnitude
    val start = floor(min / step) * step
    val end = ceil(max / step) * step
    val ticks = ArrayList<Double>()
    var v = start
    while (v <= end + step / 2) {
        ticks.add(v)
        v += step
    }
    return if (ticks.size < 2) listOf(start, start + step) else ticks
}

private fun formatTick(v: Double): String =
    if (v == floor(v)) "%,d".format(v.toLong()) else "%.1f".format(v)
