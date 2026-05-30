package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProgressEntity
import com.example.game.*

@Composable
fun ReportScreen(
    progress: UserProgressEntity,
    track: TrackTemplate,
    placement: Int,
    raceTime: String,
    epReward: Int,
    rpReward: Int,
    onProceed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCreature = CreatureRoster.getTemplateById(progress.activeCreatureId)
        ?: CreatureRoster.getTier(CreatureCategory.INSECT, 1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFEF7FF)) // Light background from Vibrant Palette
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Victory Standings Header Accent
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = when (placement) {
                    1 -> Color(0xFFFFD54F) // Gold
                    2 -> Color(0xFFCFD8DC) // Silver
                    3 -> Color(0xFFFF8A65) // Bronze
                    else -> Color(0xFF6750A4)
                },
                modifier = Modifier.size(60.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (placement) {
                    1 -> "VICTORY APEX"
                    2 -> "SECND RUNNER"
                    3 -> "PODUM FINISH"
                    else -> "RACE CONCLUDED"
                },
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF6750A4),
                letterSpacing = 1.sp
            )

            Text(
                text = "SYNCHRONOUS COMPATIBILITY REPORT",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF49454F),
                letterSpacing = 0.5.sp
            )
        }

        // Placement Card Display with the premium gradient of the theme
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFD0BCFF), Color(0xFFEADDFF))
                        )
                    )
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "#$placement",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF21005D)
                )

                Text(
                    text = "TIME: $raceTime",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF21005D).copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Track Area: ${track.name}",
                    fontSize = 11.sp,
                    color = Color(0xFF21005D).copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Gained Rewards Module (EP and RP cards styled after stats grids)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Evolution Points Reward Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF21005D))
                    Text(
                        "EVO REWARDS",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF21005D).copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        "+$epReward EP",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D)
                    )
                }
            }

            // Rank Points Reward Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFF9DEDC), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD8E4))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF31111D))
                    Text(
                        "RATINGS",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF31111D).copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = if (rpReward >= 0) "+$rpReward RP" else "$rpReward RP",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF31111D)
                    )
                }
            }
        }

        // Tactile Scaletronics Analysis
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF7F2FA))
                .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF6750A4))
                )
                Text(
                    text = "FOOTPRINT TELEMETRY REPORT",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF49454F),
                    fontWeight = FontWeight.Bold
                )
            }

            val formulaDescription = when {
                track.idealRoadWidth < 2.0f -> {
                    if (activeCreature.scale >= 4.0f) {
                        "With your massive ${activeCreature.name} scale factor [${activeCreature.scale}x], you scraped the narrow dew border nodes frequently, resulting in massive border scraping friction slowing down progression. Small scale organisms had superior turn circle parameters on this track."
                    } else {
                        "Your miniature ${activeCreature.name} scale factor [${activeCreature.scale}x] matched the corridor perfectly. You executed rapid diagonal corner turn maneuvers with instant responses, sliding cleanly past heavy, high-inertia competitors."
                    }
                }
                track.idealRoadWidth > 7.0f -> {
                    if (activeCreature.scale >= 4.0f) {
                        "Your colossal size footprint [${activeCreature.scale}x] was a massive tactical benefit on Canyons! High momentum allowed you to crush obstacles without interruption, translating high stride physics into supreme forward velocity."
                    } else {
                        "Although your agility turn circle was extremely tight, big scale dragons achieved supreme top speeds on the massive width zones. You were physically shoved during collisions due to low elastic mass ratings."
                    }
                }
                else -> "The balanced track width enabled standard physical scaling rules: your ${activeCreature.name} (mass: ${activeCreature.massKg.toInt()}kg) executed the race with steady corner drift ratios."
            }

            Text(
                text = formulaDescription,
                fontSize = 11.sp,
                color = Color(0xFF49454F),
                textAlign = TextAlign.Left,
                lineHeight = 15.sp
            )
        }

        // Proceed button styled as main CTAs
        Button(
            onClick = onProceed,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "COLLECT REWARDS & PROCEED",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}
