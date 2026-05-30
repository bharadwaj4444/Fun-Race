package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProgressEntity
import com.example.game.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchmakerScreen(
    progress: UserProgressEntity,
    selectedTrack: TrackTemplate,
    onSelectTrack: (String) -> Unit,
    onBack: () -> Unit,
    onLaunchRace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCreature = CreatureRoster.getTemplateById(progress.activeCreatureId)
        ?: CreatureRoster.getTier(CreatureCategory.INSECT, 1)

    // Generate random mock match profiles for matchmaking queue preview simulation
    val matchedCompetitors = remember(selectedTrack, progress.activeCreatureId) {
        listOf(
            Pair("Valkyrie", CreatureRoster.getTier(CreatureCategory.AVIAN, 2)),
            Pair("Brute force", CreatureRoster.getTier(CreatureCategory.MAMMAL, 3)),
            Pair("Acid Slinger", CreatureRoster.getTier(CreatureCategory.INSECT, 2))
        ).shuffled()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "MATCHMAKING GATEWAY",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "COGNITIVE TELEMETRY COMPATIBILITY RATINGS",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6750A4))
            )
        },
        containerColor = Color(0xFFFEF7FF) // Light background from Vibrant Palette
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFEF7FF))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Track Selection Header
            item {
                Text(
                    text = "SELECT RACING ZONE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF49454F),
                    letterSpacing = 1.sp
                )
            }

            // Cards for each track template
            items(PredefinedTracks.tracks) { track ->
                val isSelected = track.id == selectedTrack.id
                Card(
                    onClick = { onSelectTrack(track.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF6750A4) else Color(0xFFE6E1E5),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFFFD8E4) else Color.White // Pink accent vs standard white surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = track.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF31111D) else Color(0xFF1D1B20)
                            )

                            // Status metrics labels using light capsules
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${track.lengthMeters.toInt()}m",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF21005D),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD1E1FF)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "W: ${track.idealRoadWidth}m",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF001D35),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = track.description,
                            fontSize = 12.sp,
                            color = if (isSelected) Color(0xFF31111D).copy(alpha = 0.8f) else Color(0xFF49454F),
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Physics warnings reflecting structural scale compatibility limits
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color.White.copy(alpha = 0.50f) else Color(0xFFF7F2FA))
                                .border(0.5.dp, Color(0xFFCAC4D0).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF6750A4), modifier = Modifier.size(16.dp))
                                
                                val compatibilityMsg = when {
                                    track.idealRoadWidth < 2.0f -> {
                                        if (activeCreature.scale >= 4.0f) {
                                            "⚠️ COMPATIBILITY WARNING: Narrow width of Meadows heavily penalizes your large ${activeCreature.name} scale! You will scrap margins constantly."
                                        } else {
                                            "✅ COMPATIBILITY PERFECT: Narrow corridor perfect for your agile ${activeCreature.name}! Use swift turning circles to slip by."
                                        }
                                    }
                                    track.idealRoadWidth > 7.0f -> {
                                        if (activeCreature.scale >= 4.0f) {
                                            "✅ COMPATIBILITY PERFECT: Extreme wide speedway grants infinite straights to charge up top velocity on ${activeCreature.name}!"
                                        } else {
                                            "⚖️ COMPATIBILITY MEDIOCRE: Large canyon width favors giant strides. Your small racer will struggle to block giant competitors."
                                        }
                                    }
                                    else -> "✅ COMPATIBILITY STABLE: Standard grid corridor. Balanced turn circles and physical weights apply."
                                }

                                Text(
                                    text = compatibilityMsg,
                                    fontSize = 10.sp,
                                    color = Color(0xFF1D1B20),
                                    lineHeight = 13.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Matchmaking Lobby Simulator Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp, color = Color(0xFF6750A4))
                    Text(
                        text = "ESTABLISHING CHRONO LOBBY MATCH...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF49454F)
                    )
                }
            }

            // Competitor Match List
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2FA))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Player Slot First
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("👤", fontSize = 20.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("You (Challenger)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                                Text("${activeCreature.name} (Scale: ${activeCreature.scale}x  |  EP: ${progress.evolutionPoints})", fontSize = 10.sp, color = Color(0xFF49454F))
                            }
                            Text(
                                "RP ${progress.currentRp}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF6750A4)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFCAC4D0).copy(alpha = 0.5f))

                        // Random Matched BOT competitors
                        matchedCompetitors.forEachIndexed { idx, pair ->
                            val botName = pair.first
                            val template = pair.second

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(bottom = if (idx == matchedCompetitors.size - 1) 0.dp else 10.dp)
                            ) {
                                Text("🤖", fontSize = 18.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Bot ${botName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                                    Text("${template.name} (Scale: ${template.scale}x)", fontSize = 10.sp, color = Color(0xFF49454F))
                                }
                                Text(
                                    "RP ${(progress.currentRp + (idx + 1) * 150 - 100).coerceAtLeast(100)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF49454F)
                                )
                            }
                        }
                    }
                }
            }

            // Launch Action
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onLaunchRace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ENGAGE MATCH START",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
