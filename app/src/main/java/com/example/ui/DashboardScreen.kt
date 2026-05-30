package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProgressEntity
import com.example.game.CreatureRoster
import com.example.game.RacingScreen

@Composable
fun DashboardScreen(
    progress: UserProgressEntity,
    onNavigate: (RacingScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCreature = CreatureRoster.getTemplateById(progress.activeCreatureId)
        ?: CreatureRoster.getTier(com.example.game.CreatureCategory.INSECT, 1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFEF7FF)) // Light lavender background from Vibrant Palette
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Bar / Header modeled after HTML source
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Circular active creature avatar icon with primary background purple
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF6750A4))
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = activeCreature.ability.emoji,
                        fontSize = 20.sp
                    )
                }

                Column {
                    Text(
                        text = "SCALESHIFT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )
                    Text(
                        text = "Tier ${activeCreature.tier}: ${activeCreature.name}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6750A4)
                    )
                }
            }

            // Wallet/RP Pill on the right
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF3EDF7))
                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${progress.evolutionPoints} EP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFB3261E))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Showcase (Creature Evolution Card)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(elevation = 1.dp, shape = RoundedCornerShape(32.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFD0BCFF), Color(0xFFEADDFF)) // Vibrant Palette purple gradient
                        )
                    )
                    .drawBehind {
                        // Drawing subtle border-b analogy using drawing operations
                        drawLine(
                            color = Color(0xFF6750A4).copy(alpha = 0.2f),
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 4.dp.toPx()
                        )
                    }
                    .padding(24.dp)
            ) {
                // Scale reference label top-left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.10f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Scale: ${activeCreature.scale}x (Mass: ${activeCreature.massKg.toInt()}kg)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D)
                    )
                }

                // Center Showcase content
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = activeCreature.ability.emoji,
                        fontSize = 80.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Text(
                        text = activeCreature.name.uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color(0xFF21005D),
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = when (activeCreature.category) {
                            com.example.game.CreatureCategory.INSECT -> "Agile insectoid group. Rapid turn radius perfect for narrow grids."
                            com.example.game.CreatureCategory.MAMMAL -> "Heavy-weight mammal group with high collision momentum."
                            com.example.game.CreatureCategory.AVIAN -> "Aero-dynamic avian group. Highly balanced agility and velocity parameters."
                            com.example.game.CreatureCategory.REPTILE -> "Scaled reptile group. Steady drift scaling mechanics."
                            com.example.game.CreatureCategory.FANTASY_BEAST -> "Magical creature with powerful offensive capabilities."
                        },
                        fontSize = 11.sp,
                        color = Color(0xFF21005D).copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Evolution Progress display bottom-center
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Next Evo Pathway",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D).copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${((progress.evolutionPoints.toFloat() / 500f) * 100).coerceAtMost(100f).toInt()}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D).copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.White.copy(alpha = 0.40f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(5.dp))
                    ) {
                        val fraction = (progress.evolutionPoints.toFloat() / 500f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFF6750A4))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Speed card: `#FFD8E4` / text `#31111D`
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFF9DEDC), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD8E4))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SPEED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF31111D)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(activeCreature.baseSpeed * 10).toInt()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF31111D)
                    )
                }
            }

            // Handling card: `#E8DEF8` / text `#21005D`
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HANDLING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(activeCreature.baseHandling * 10).toInt()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF21005D)
                    )
                }
            }

            // Power card: `#D1E1FF` / text `#001D35`
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFBACFFF), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1E1FF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MASS CLASS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF001D35)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            activeCreature.massKg > 1000f -> "HEAVY"
                            activeCreature.massKg > 100f -> "MEDIUM"
                            else -> "LIGHT"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF001D35),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Abilities Slot Box modeled after the HTML design
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF7F2FA))
                .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF6750A4))
                )
                Text(
                    text = "EQUIPPED ABILITY SYSTEM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF49454F),
                    letterSpacing = 1.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Main active ability block
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE6E1E5), RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFD8E4))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(activeCreature.ability.emoji, fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = activeCreature.ability.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = "Cooldown 5s",
                            fontSize = 9.sp,
                            color = Color(0xFF49454F)
                        )
                    }
                }

                // Passive info block
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE6E1E5), RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFC2E7FF))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛡️", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = "Dynamic Scale",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = "Mass Buffer",
                            fontSize = 9.sp,
                            color = Color(0xFF49454F)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main CTA button modeled after HTML
        Button(
            onClick = { onNavigate(RacingScreen.Matchmaker) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RACE NOW",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.20f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            progress.currentRp < 1000 -> "RANK: BRONZE"
                            progress.currentRp < 2500 -> "RANK: SILVER"
                            progress.currentRp < 5000 -> "RANK: GOLD"
                            else -> "RANK: DRAGON"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary bottom grid tabs styled as Material 3 pill choices
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onNavigate(RacingScreen.Lab) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8DEF8)), // Secondary color from Vibrant Palette
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0BCFF))
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF21005D))
                Spacer(modifier = Modifier.width(6.dp))
                Text("EVOLVE LAB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
            }

            Button(
                onClick = { onNavigate(RacingScreen.Leaderboard) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD8E4)), // Terry color from Vibrant Palette
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF9DEDC))
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF31111D))
                Spacer(modifier = Modifier.width(6.dp))
                Text("LADDERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF31111D))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
