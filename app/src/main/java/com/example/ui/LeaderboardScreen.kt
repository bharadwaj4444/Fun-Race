package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LeaderboardEntryEntity
import com.example.game.CreatureRoster

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    leaderboardEntries: List<LeaderboardEntryEntity>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "SEASONAL LADDER BOARDS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "CHRONOS LEAGUE POSITIONS",
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFEF7FF))
                .padding(horizontal = 16.dp)
        ) {
            // General seasonal announcement header card (matching secondary lavender styling)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(36.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ACTIVE LEAGUE COHORT",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF21005D),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Compete against AI competitors of various scaling. Earn trophies and scaling points by winning races",
                            fontSize = 11.sp,
                            color = Color(0xFF21005D).copy(alpha = 0.8f),
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Text(
                text = "GLOBAL LADDER ENTRIES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF49454F),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Scrollable list of players/competitors
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(leaderboardEntries) { index, entry ->
                    val competitorPosition = index + 1
                    val template = CreatureRoster.getTemplateById(entry.creatureTemplateId)
                    val speciesEmoji = template?.ability?.emoji ?: "🦖"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (entry.isPlayer) 1.5.dp else 1.dp,
                                color = if (entry.isPlayer) Color(0xFF6750A4) else Color(0xFFE6E1E5),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (entry.isPlayer) Color(0xFFFFD8E4) else Color.White // Pink accent for player
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Position Indicator Badge
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        when (competitorPosition) {
                                            1 -> Color(0xFFFFD54F)
                                            2 -> Color(0xFFCFD8DC)
                                            3 -> Color(0xFFFF8A65).copy(alpha = 0.5f)
                                            else -> Color(0xFFF3EDF7)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$competitorPosition",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (competitorPosition) {
                                        1 -> Color(0xFF5D4037)
                                        2 -> Color(0xFF455A64)
                                        3 -> Color(0xFFD84315)
                                        else -> Color(0xFF49454F)
                                    },
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Species Avatar Icon
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFFF3EDF7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(speciesEmoji, fontSize = 18.sp)
                            }

                            // Name, species details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = entry.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (entry.isPlayer) Color(0xFF31111D) else Color(0xFF1D1B20)
                                    )
                                    if (entry.isPlayer) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF6750A4), modifier = Modifier.size(12.dp))
                                    }
                                }
                                Text(
                                    text = "${template?.name ?: "Unknown"} | Win: ${(entry.winRatio * 100).toInt()}%",
                                    fontSize = 10.sp,
                                    color = Color(0xFF49454F)
                                )
                            }

                            // RP & league rating badge
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${entry.rp} RP",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (entry.isPlayer) Color(0xFF6750A4) else Color(0xFF1D1B20)
                                )
                                Text(
                                    text = entry.rankType.uppercase(),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF6750A4),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
