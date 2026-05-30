package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.delay
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProgressEntity
import com.example.game.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabScreen(
    progress: UserProgressEntity,
    onBack: () -> Unit,
    onSelectCreature: (String) -> Unit,
    onUpgradeTier: (CreatureCategory, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(CreatureCategory.INSECT) }
    val chain = CreatureRoster.evolutionChains[selectedCategory] ?: emptyList()
    val unlockedIds = progress.unlockedCreatureIds.split(",").toSet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "CREATURE EVOLUTION LAB",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "HYBRID MUTAGEN & COGNITIVE PHYSICS SELECTION",
                            fontSize = 8.sp,
                            color = Color(0xFFE8DEF8),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFE8DEF8))
                            .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${progress.evolutionPoints} EP",
                            color = Color(0xFF21005D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6750A4))
            )
        },
        containerColor = Color(0xFFFEF7FF) // Light theme background
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFEF7FF))
        ) {
            // Category Selector Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedCategory.ordinal,
                containerColor = Color(0xFFF3EDF7),
                contentColor = Color(0xFF6750A4),
                edgePadding = 8.dp,
                divider = {}
            ) {
                CreatureCategory.values().forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = {
                            Text(
                                text = category.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (selectedCategory == category) Color(0xFF6750A4) else Color(0xFF49454F)
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Futuristic Dynamic Schematic Render (Custom Canvas)
                item {
                    BioSchematicWidget(category = selectedCategory)
                }

                // Header for Tiers
                item {
                    Text(
                        text = "MORPHOLOGY PATHWAY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF49454F),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Render each Tier (1, 2, 3) representing the Evolution Chain
                items(chain.size) { index ->
                    val template = chain[index]
                    val isUnlocked = unlockedIds.contains(template.id)
                    val isActive = progress.activeCreatureId == template.id
                    val isPreviousUnlocked = if (index == 0) true else unlockedIds.contains(chain[index - 1].id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isActive) 1.5.dp else 1.dp,
                                color = if (isActive) Color(0xFF6750A4) else if (isUnlocked) Color(0xFFCAC4D0) else Color(0xFFB3261E).copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) Color(0xFFE8DEF8) else Color.White // Lavender accent on active
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Row Header: Tier, Name, Ability Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF3EDF7))
                                        .border(
                                            width = 1.dp,
                                            color = if (isUnlocked) Color(0xFFCAC4D0) else Color(0xFFCAC4D0).copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isUnlocked) template.ability.emoji else "🔒",
                                        fontSize = 22.sp
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "TIER ${template.tier}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF6750A4)
                                        )
                                        if (isActive) {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE2F0D9)),
                                                border = BorderStroke(0.5.dp, Color(0xFF4CAF50)),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    "DEPLOYED",
                                                    fontSize = 7.sp,
                                                    color = Color(0xFF388E3C),
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = template.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) Color(0xFF1D1B20) else Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = template.description,
                                fontSize = 12.sp,
                                color = if (isUnlocked) Color(0xFF1D1B20).copy(alpha = 0.8f) else Color.Gray,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Physics breakdown parameters matching the formula specs
                            PhysicsSpecTable(template = template)

                            Spacer(modifier = Modifier.height(16.dp))

                            // Abilities and Powers
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF7F2FA))
                                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(template.ability.emoji, fontSize = 16.sp)
                                    Column {
                                        Text(
                                            text = "${template.ability.name} (${when (template.ability.type) {
                                                AbilityType.OFFENSIVE_HARM -> "Offensive Action"
                                                AbilityType.UTILITY_NUMB -> "Control Effect"
                                                AbilityType.DEFENSIVE -> "Defensive Dodge"
                                            }})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1D1B20)
                                        )
                                        Text(
                                            text = template.ability.description,
                                            fontSize = 10.sp,
                                            color = Color(0xFF49454F),
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action buttons: Equip, Evolve, Locked
                            when {
                                isActive -> {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            disabledContainerColor = Color(0xFFE8DEF8)
                                        )
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF6750A4))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("DEPLOYED & MOUNTED", color = Color(0xFF6750A4), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                                isUnlocked -> {
                                    Button(
                                        onClick = { onSelectCreature(template.id) },
                                        modifier = Modifier.fillMaxWidth().height(44.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("EQUIP RACER SPECIES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                                !isPreviousUnlocked -> {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            disabledContainerColor = Color(0xFFCAC4D0).copy(0.3f)
                                        )
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("EVOLVE PREVIOUS TIER FIRST", color = Color.Gray, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                    }
                                }
                                else -> {
                                    val canAfford = progress.evolutionPoints >= template.upgradeCost
                                    Button(
                                        onClick = { onUpgradeTier(selectedCategory, template.tier) },
                                        enabled = canAfford,
                                        modifier = Modifier.fillMaxWidth().height(44.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFF9800),
                                            disabledContainerColor = Color(0xFFFF9800).copy(alpha = 0.2f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Build, contentDescription = null, tint = if (canAfford) Color.White else Color.Gray)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "EVOLVE TO TIER ${template.tier} (COST: ${template.upgradeCost} EP)",
                                            color = if (canAfford) Color.White else Color.Gray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhysicsSpecTable(template: CreatureTemplate) {
    val topSpeed = PhysicsEngine.calculateTopSpeed(template.baseSpeed, template.scale)
    val acceleration = PhysicsEngine.calculateAcceleration(template.baseAcceleration, template.scale)
    val radius = PhysicsEngine.calculateTurningCircleRadius(template.baseHandling, template.scale)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Physical Footprint / Scale:", fontSize = 10.sp, color = Color(0xFF49454F))
            Text("${template.scale}x Scale Factor", fontSize = 10.sp, color = Color(0xFF1D1B20), fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Inertia Mass Coefficient:", fontSize = 10.sp, color = Color(0xFF49454F))
            Text("${template.massKg} kg", fontSize = 10.sp, color = Color(0xFF1D1B20), fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Scaled Top Velocity:", fontSize = 10.sp, color = Color(0xFF49454F))
            Text("%.1f m/s".format(topSpeed), fontSize = 10.sp, color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Stride Startup Acceleration:", fontSize = 10.sp, color = Color(0xFF49454F))
            Text("%.1f m/s²".format(acceleration), fontSize = 10.sp, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Control Turn Circle Radius:", fontSize = 10.sp, color = Color(0xFF49454F))
            Text("%.1f meters".format(radius), fontSize = 10.sp, color = Color(0xFFB3261E), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BioSchematicWidget(category: CreatureCategory) {
    var tick by remember { mutableStateOf(0f) }

    // Tiny animation tick driving rotating lines
    LaunchedEffect(Unit) {
        while (true) {
            delay(30)
            tick += 0.04f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF7F2FA))
            .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = 45.dp.toPx()

            // Draw radial grid system
            drawCircle(
                color = category.color.copy(alpha = 0.15f),
                radius = baseRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = category.color.copy(alpha = 0.08f),
                radius = baseRadius * 1.5f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Dynamic diagnostic overlays representing creature species
            when (category) {
                CreatureCategory.INSECT -> {
                    // Insect hexagon grid overlay
                    val side = baseRadius * 0.9f
                    val angleOffset = tick * 0.2f
                    for (i in 0..5) {
                        val angle = (i * Math.PI / 3f) + angleOffset
                        val p1 = Offset(
                            center.x + (side * cos(angle)).toFloat(),
                            center.y + (side * sin(angle)).toFloat()
                        )
                        val nextAngle = ((i + 1) * Math.PI / 3f) + angleOffset
                        val p2 = Offset(
                            center.x + (side * cos(nextAngle)).toFloat(),
                            center.y + (side * sin(nextAngle)).toFloat()
                        )
                        drawLine(
                            color = category.color.copy(alpha = 0.5f),
                            start = p1,
                            end = p2,
                            strokeWidth = 1.5.dp.toPx()
                        )
                        drawCircle(category.color, radius = 3.dp.toPx(), center = p1)
                    }
                }
                CreatureCategory.MAMMAL -> {
                    // Heavy iron cross and chassis blocks (heavy weight)
                    val cross = baseRadius * 1.1f
                    drawLine(category.color.copy(0.4f), Offset(center.x - cross, center.y), Offset(center.x + cross, center.y), 2.dp.toPx())
                    drawLine(category.color.copy(0.4f), Offset(center.x, center.y - cross), Offset(center.x, center.y + cross), 2.dp.toPx())
                    
                    val sizeBox = baseRadius * 0.7f
                    drawRect(
                        color = category.color.copy(0.20f),
                        topLeft = Offset(center.x - sizeBox, center.y - sizeBox),
                        size = androidx.compose.ui.geometry.Size(sizeBox*2, sizeBox*2),
                        style = Stroke(2.dp.toPx())
                    )
                }
                CreatureCategory.REPTILE -> {
                    // Wavy sine patterns representing slithering snakes/lizards
                    val waveWidth = baseRadius * 1.6f
                    for (i in 0..100) {
                        val percent = i / 100f
                        val sx = center.x - (waveWidth/2f) + percent * waveWidth
                        val sy = center.y + sin(percent * Math.PI * 4f + tick).toFloat() * 18.dp.toPx()
                        drawCircle(category.color.copy(0.5f), radius = 1.5.dp.toPx(), center = Offset(sx, sy))
                    }
                }
                CreatureCategory.AVIAN -> {
                    // Radial sonic high-frequency sound spike orbits
                    val spikesCount = 24
                    val angleOffset = -tick * 0.4f
                    for (i in 0 until spikesCount) {
                        val angle = (i * Math.PI * 2f / spikesCount) + angleOffset
                        val waveDelta = sin(tick * 5f + i).toFloat() * 10.dp.toPx()
                        val rStart = baseRadius * 0.7f
                        val rEnd = baseRadius + waveDelta

                        val p1 = Offset(center.x + (rStart * cos(angle)).toFloat(), center.y + (rStart * sin(angle)).toFloat())
                        val p2 = Offset(center.x + (rEnd * cos(angle)).toFloat(), center.y + (rEnd * sin(angle)).toFloat())
                        drawLine(category.color.copy(0.6f), p1, p2, 1.dp.toPx())
                    }
                }
                CreatureCategory.FANTASY_BEAST -> {
                    // Overlapping fiery energy triangles or rings
                    for (i in 0..2) {
                        val ringRad = baseRadius * (0.5f + i * 0.4f)
                        val angleOffset = tick * (1.0f - i * 0.2f)
                        val px = center.x + cos(angleOffset).toFloat() * ringRad
                        val py = center.y + sin(angleOffset).toFloat() * ringRad
                        drawCircle(category.color.copy(alpha = 0.4f), radius = ringRad, center = center, style = Stroke(1.dp.toPx()))
                        drawCircle(category.color, radius = 4.dp.toPx(), center = Offset(px, py))
                    }
                }
            }
        }

        // Overlay text labels
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                "GENETIC WAVEFRONT SCAN",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF49454F),
                fontWeight = FontWeight.Bold
            )
            Text(
                "${category.displayName.uppercase()} BIOMETRY SYSTEM",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF21005D),
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
