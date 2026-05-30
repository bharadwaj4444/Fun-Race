package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RaceScreen(
    liveRace: LiveRaceSimulationState,
    onSteer: (Float) -> Unit,
    onActivateAbility: () -> Unit,
    onQuitRace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = liveRace.racers.find { it.isPlayer } ?: return
    val track = liveRace.track

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFEF7FF))
    ) {
        // High-contrast Header / Live Dashboard HUD from Vibrant theme
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3EDF7))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "PLACEMENT",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF49454F)
                )
                Text(
                    text = when (player.currentPlacement) {
                        1 -> "1st / 4"
                        2 -> "2nd / 4"
                        3 -> "3rd / 4"
                        else -> "4th / 4"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF6750A4)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SPEEDOMETER",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF49454F)
                )
                Text(
                    text = "%.1f m/s".format(player.speed),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF6750A4)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "LAP COUNT",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF49454F)
                )
                Text(
                    text = "Lap ${player.currentLap} / ${liveRace.maxLaps}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1B20)
                )
            }
        }

        // Live Horizontal Progress Bar simulating Distance Covered
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color(0xFFE8DEF8))
        ) {
            val progressPercent = (player.z / (track.lengthMeters * liveRace.maxLaps)).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressPercent)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF6750A4), Color(0xFFFFD8E4))
                        )
                    )
            )
        }

        // Interactive 3D Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .drawBehind {
                    drawLine(
                        color = Color(0xFFCAC4D0).copy(0.4f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
        ) {
            Pseudo3DRenderer(liveRace = liveRace)

            // Stun overlay alert
            if (player.isStunned) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌩️ STUNNED / WOBBLY 🌩️", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Red, fontFamily = FontFamily.Monospace)
                        Text("TEMPORARY LOSS OF PROPULSION MASS", fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            // Numb glitch overlay alert displaying control scramble status
            if (player.isNumbed) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF00E5FF).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌀 CONTROLS SCRAMBLED 🌀", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF), fontFamily = FontFamily.Monospace)
                        Text("CENTRAL NERVOUS SYSTEM LOCK: STEERING REVERSED", fontSize = 10.sp, color = Color.White)
                    }
                }
            }

            // Countdown screen overlay
            if (liveRace.showStartCountdown) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    val count = (liveRace.countdownRemainingMs / 1000) + 1
                    Text(
                        text = if (count > 0) "$count" else "GO!",
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Black,
                        color = if (count > 1) Color(0xFFFF5722) else Color(0xFF4CAF50),
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }

        // Telemetry Science Bar + Live Racing Action Logs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(95.dp)
                .background(Color(0xFFFEF7FF))
                .drawBehind {
                    drawLine(
                        color = Color(0xFFCAC4D0).copy(0.5f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left block: Real-time Physics variables reflecting Scaling Rules
            Card(
                modifier = Modifier
                    .weight(0.42f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)),
                border = BorderStroke(1.dp, Color(0xFFD0BCFF))
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Text(
                        "PHYSICS TELEMETRY",
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF21005D),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Scale size: ${player.scaleFactor}x", fontSize = 9.sp, color = Color(0xFF21005D), fontFamily = FontFamily.Monospace)
                    Text("Mass Inertia: ${player.massKg.toInt()}kg", fontSize = 9.sp, color = Color(0xFF21005D).copy(alpha = 0.8f), fontFamily = FontFamily.Monospace)
                    val rad = PhysicsEngine.calculateTurningCircleRadius(player.template.baseHandling, player.scaleFactor)
                    Text("Turn circle: %.1fm".format(rad), fontSize = 9.sp, color = Color(0xFF21005D).copy(alpha = 0.8f), fontFamily = FontFamily.Monospace)
                }
            }

            // Right block: scrolling logs feed of matching events
            Card(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0))
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Text(
                        "RACE DIARY LOGS",
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF49454F),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        liveRace.logs.take(3).forEach { log ->
                            Text(
                                text = log,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (log.contains("COLLISION")) Color(0xFFB3261E) else if (log.contains("FINISHED") || log.contains("GREEN")) Color(0xFF388E3C) else Color(0xFF1D1B20),
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Steer Controls + Ability Action Blocks
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFFEF7FF))
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Interactive Steer Slider Controller
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "STEERING COMMAND",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF49454F),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Steer Clicker
                    IconButton(
                        onClick = { onSteer(-0.8f) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8DEF8))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Steer Left", tint = Color(0xFF21005D))
                    }

                    // Steer Slider
                    var sliderPosition by remember { mutableStateOf(0f) }
                    Slider(
                        value = sliderPosition,
                        onValueChange = {
                            sliderPosition = it
                            onSteer(it)
                        },
                        valueRange = -1.0f..1.0f,
                        onValueChangeFinished = {
                            sliderPosition = 0f
                            onSteer(0f)
                        },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF6750A4),
                            activeTrackColor = Color(0xFF6750A4).copy(0.4f),
                            inactiveTrackColor = Color(0xFFE8DEF8)
                        )
                    )

                    // Right Steer Clicker
                    IconButton(
                        onClick = { onSteer(0.8f) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8DEF8))
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Steer Right", tint = Color(0xFF21005D))
                    }
                }
            }

            // Ability activation button FAB + QUIT option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quit button
                Button(
                    onClick = onQuitRace,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD8E4)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFF9DEDC)),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("ABORT", fontSize = 11.sp, color = Color(0xFF31111D), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                // Ability firing FAB
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = onActivateAbility,
                        enabled = !player.isStunned && !player.isFinished,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(player.template.ability.emoji, fontSize = 20.sp)
                            Column {
                                Text(
                                    text = player.template.ability.name.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = when (player.template.ability.type) {
                                        AbilityType.OFFENSIVE_HARM -> "OFFENSIVE ACTION PROJECTS"
                                        AbilityType.UTILITY_NUMB -> "CONTROL UTILITY STATS"
                                        AbilityType.DEFENSIVE -> "DEFENSIVE BARRIER BLOCK"
                                    },
                                    fontSize = 7.5.sp,
                                    color = Color.White.copy(0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Pseudo3DRenderer(liveRace: LiveRaceSimulationState) {
    val player = liveRace.racers.find { it.isPlayer } ?: return
    val track = liveRace.track

    // Let's create an Outrun-style rendering block
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val horizon = h * 0.35f
        val camDepth = 0.55f // Projection lens setting
        val segmentSize = 3.0f // Size of virtual track segment steps

        // 1. Draw Space Sky / Mountains
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(track.backgroundColorHex), Color(0xFF09070C))
            ),
            topLeft = Offset.Zero,
            size = size
        )

        // Draw basic static mountains silhouette for 3D coordinate feel
        val mountainPath = Path().apply {
            moveTo(0f, horizon)
            lineTo(w * 0.15f, horizon - 25.dp.toPx())
            lineTo(w * 0.3f, horizon - 8.dp.toPx())
            lineTo(w * 0.55f, horizon - 35.dp.toPx())
            lineTo(w * 0.75f, horizon - 12.dp.toPx())
            lineTo(w * 0.9f, horizon - 28.dp.toPx())
            lineTo(w, horizon)
            close()
        }
        drawPath(mountainPath, color = Color(0xFF231C2D).copy(0.4f))

        // 2. Render scrolling road segments from background (far away) to foreground (near)
        // Draw 24 segments back-to-front
        val segmentsToRender = 28
        val playerCamZ = player.z - 1.5f // Camera is slightly behind player creature

        for (i in segmentsToRender downTo 1) {
            val segIdx = ((playerCamZ / segmentSize).toInt() + i)
            val segZ = segIdx * segmentSize
            val dz = segZ - playerCamZ

            if (dz <= 0) continue

            // Math projection coefficient
            val p = camDepth / dz

            // Get segment attributes
            val segCurvature = track.getCurvatureAt(segZ)
            val nextSegCurvature = track.getCurvatureAt(segZ + segmentSize)

            // Current segment projection properties
            val sy = horizon + (h - horizon) * p * 0.95f
            val roadWidth = track.idealRoadWidth * 23.dp.toPx() * p
            val roadCenterX = (w / 2f) + (segCurvature.curve - player.x) * roadWidth * 1.5f

            // Next segment projection properties
            val nextDz = dz + segmentSize
            val nextP = camDepth / nextDz
            val nextSy = horizon + (h - horizon) * nextP * 0.95f
            val nextRoadWidth = track.idealRoadWidth * 23.dp.toPx() * nextP
            val nextRoadCenterX = (w / 2f) + (nextSegCurvature.curve - player.x) * nextRoadWidth * 1.5f

            // Bounds coordinates
            val pLeftX = roadCenterX - roadWidth
            val pRightX = roadCenterX + roadWidth
            val nLeftX = nextRoadCenterX - nextRoadWidth
            val nRightX = nextRoadCenterX + nextRoadWidth

            // Alternating pattern colors for speed illusion
            val isEven = segIdx % 2 == 0
            val grassColor = if (isEven) Color(0xFF0D2817) else Color(0xFF143B22)
            val asphaltColor = if (isEven) Color(0xFF26232E) else Color(0xFF2D2A38)
            val curbColor = if (isEven) Color(0xFFD32F2F) else Color(0xFFECEFF1)

            // Draw field grass/terrain polygons (full horizontal fill except road bounds)
            val leftFieldPath = Path().apply {
                moveTo(0f, sy)
                lineTo(pLeftX, sy)
                lineTo(nLeftX, nextSy)
                lineTo(0f, nextSy)
                close()
            }
            drawPath(leftFieldPath, color = grassColor)

            val rightFieldPath = Path().apply {
                moveTo(w, sy)
                lineTo(pRightX, sy)
                lineTo(nRightX, nextSy)
                lineTo(w, nextSy)
                close()
            }
            drawPath(rightFieldPath, color = grassColor)

            // Draw primary road lane polygons
            val roadPath = Path().apply {
                moveTo(pLeftX, sy)
                lineTo(pRightX, sy)
                lineTo(nRightX, nextSy)
                lineTo(nLeftX, nextSy)
                close()
            }
            drawPath(roadPath, color = asphaltColor)

            // Draw curbstones shoulders
            val curbWidth = 14.dp.toPx() * p
            val nextCurbWidth = 14.dp.toPx() * nextP

            val leftCurbPath = Path().apply {
                moveTo(pLeftX - curbWidth, sy)
                lineTo(pLeftX, sy)
                lineTo(nLeftX, nextSy)
                lineTo(nLeftX - nextCurbWidth, nextSy)
                close()
            }
            drawPath(leftCurbPath, color = curbColor)

            val rightCurbPath = Path().apply {
                moveTo(pRightX, sy)
                lineTo(pRightX + curbWidth, sy)
                lineTo(nRightX + nextCurbWidth, nextSy)
                lineTo(nRightX, nextSy)
                close()
            }
            drawPath(rightCurbPath, color = curbColor)

            // Draw lane center dashed white divides (even index only)
            if (isEven) {
                val divideWidth = 2.dp.toPx() * p
                val dividerPath = Path().apply {
                    moveTo(roadCenterX - divideWidth, sy)
                    lineTo(roadCenterX + divideWidth, sy)
                    lineTo(nextRoadCenterX + divideWidth * 0.6f, nextSy)
                    lineTo(nextRoadCenterX - divideWidth * 0.6f, nextSy)
                    close()
                }
                drawPath(dividerPath, color = Color.White.copy(0.6f))
            }
        }

        // 3. Project and draw obstacles on top of segment positions
        liveRace.obstacles.forEach { obs ->
            if (obs.z < playerCamZ) return@forEach
            val dz = obs.z - playerCamZ
            if (dz > 110.0f) return@forEach // Clip far obstacles

            val p = camDepth / dz
            val sy = horizon + (h - horizon) * p * 0.95f
            val roadWidth = track.idealRoadWidth * 23.dp.toPx() * p
            val segCurve = track.getCurvatureAt(obs.z)
            val rx = (w / 2f) + (segCurve.curve - player.x) * roadWidth * 1.5f + obs.x * roadWidth

            // Double scaling rules: multiplier by projection lens AND custom obstacle base scale
            val finalObsSize = (28.dp.toPx() * p * obs.scaleSize).coerceIn(4f, 150f)

            if (!obs.isSmashed) {
                // Render scaling custom obstacle emoji details
                drawCircle(
                    color = Color.Black.copy(0.15f),
                    radius = finalObsSize * 0.6f,
                    center = Offset(rx, sy + finalObsSize * 0.2f)
                )
                // Drawing text character inside Canvas
                drawContext.canvas.nativeCanvas.drawText(
                    obs.emoji,
                    rx - finalObsSize * 0.5f,
                    sy + finalObsSize * 0.2f,
                    android.graphics.Paint().apply {
                        textSize = finalObsSize
                        textAlign = android.graphics.Paint.Align.LEFT
                    }
                )
            } else {
                // Smashed debris cloud effect
                drawContext.canvas.nativeCanvas.drawText(
                    "💨",
                    rx - finalObsSize * 0.5f,
                    sy,
                    android.graphics.Paint().apply {
                        textSize = finalObsSize * 0.9f
                    }
                )
            }
        }

        // 4. Project and draw projectiles
        liveRace.projectiles.forEach { proj ->
            val dz = proj.z - playerCamZ
            if (dz <= 0 || dz > 110.0f) return@forEach

            val p = camDepth / dz
            val sy = horizon + (h - horizon) * p * 0.95f
            val roadWidth = track.idealRoadWidth * 23.dp.toPx() * p
            val segCurve = track.getCurvatureAt(proj.z)
            val rx = (w / 2f) + (segCurve.curve - player.x) * roadWidth * 1.5f + proj.x * roadWidth
            val finalSize = (24.dp.toPx() * p).coerceIn(4f, 80f)

            // Draw glowing halo sphere
            drawCircle(
                color = Color(0xFF00E5FF).copy(0.3f),
                radius = finalSize * 0.8f,
                center = Offset(rx, sy)
            )

            drawContext.canvas.nativeCanvas.drawText(
                proj.emoji,
                rx - finalSize * 0.5f,
                sy + finalSize * 0.3f,
                android.graphics.Paint().apply {
                    textSize = finalSize
                }
            )
        }

        // 5. Project and draw competitors (Bots & Player)
        // Sort racers by 'z' so closest ones are drawn last (overlap others)
        liveRace.racers.sortedBy { it.z }.forEach { racer ->
            if (racer.z < playerCamZ - 4f) return@forEach
            val dz = racer.z - playerCamZ
            if (dz > 120.0f) return@forEach

            val p = camDepth / dz
            val sy = horizon + (h - horizon) * p * 0.95f
            val roadWidth = track.idealRoadWidth * 23.dp.toPx() * p
            val segCurve = track.getCurvatureAt(racer.z)
            
            // Adjust x coordinate on screen
            val rx = (w / 2f) + (segCurve.curve - player.x) * roadWidth * 1.5f + racer.x * roadWidth

            // !!! THE VARIABLE CREATURE SCALING RULE DIRECT DRAW REPRESENTATION !!!
            // We scale the draw radius directly proportional to 'scaleFactor' * 'projection' lens factor.
            // Tiny ants remain minuscule dots; giant ancient dragons occupy massive portions of the lanes!
            val baseSize = 25.dp.toPx()
            val finalRacerCircleSize = (baseSize * p * racer.scaleFactor).coerceIn(6f, 150f)

            // Draw shadow plate
            drawCircle(
                color = Color.Black.copy(0.35f),
                radius = finalRacerCircleSize * 0.9f,
                center = Offset(rx, sy + finalRacerCircleSize * 0.1f)
            )

            // Draw species circular glow shield if active
            if (racer.isInvincible) {
                drawCircle(
                    color = Color(0xFF00E5FF).copy(0.2f),
                    radius = finalRacerCircleSize * 1.3f,
                    center = Offset(rx, sy),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Draw species circular card body
            drawCircle(
                color = racer.template.category.color,
                radius = finalRacerCircleSize,
                center = Offset(rx, sy)
            )

            // Inner circle core for depth
            drawCircle(
                color = Color(0xFF13101E),
                radius = finalRacerCircleSize * 0.82f,
                center = Offset(rx, sy)
            )

            // Render details (Emoji and Placement text labels inside Canvas)
            val textPaintSize = (finalRacerCircleSize * 0.9f).coerceAtLeast(8f)
            drawContext.canvas.nativeCanvas.drawText(
                racer.template.ability.emoji,
                rx - finalRacerCircleSize * 0.45f,
                sy + finalRacerCircleSize * 0.35f,
                android.graphics.Paint().apply {
                    textSize = textPaintSize
                }
            )

            // If giant or close, print name tag
            if (dz < 50.0f) {
                val labelSize = (11.dp.toPx() * p * 1.5f).coerceIn(6f, 24f)
                drawContext.canvas.nativeCanvas.drawText(
                    racer.name.substringBefore(" ("),
                    rx,
                    sy - finalRacerCircleSize * 1.3f,
                    android.graphics.Paint().apply {
                        textSize = labelSize
                        color = android.graphics.Color.WHITE
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.MONOSPACE
                        setShadowLayer(2f, 0f, 1f, android.graphics.Color.BLACK)
                    }
                )
                // Draw placement floating badge
                drawContext.canvas.nativeCanvas.drawText(
                    "P${racer.currentPlacement}",
                    rx,
                    sy + finalRacerCircleSize * 1.4f,
                    android.graphics.Paint().apply {
                        textSize = labelSize * 0.9f
                        color = android.graphics.Color.YELLOW
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}
