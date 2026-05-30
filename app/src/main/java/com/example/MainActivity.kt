package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.RacingScreen
import com.example.game.RacingViewModel
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: RacingViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val progressState by viewModel.userProgress.collectAsStateWithLifecycle()
                val leaderboardEntries by viewModel.leaderboard.collectAsStateWithLifecycle()
                val liveRaceState by viewModel.liveRace.collectAsStateWithLifecycle()
                val selectedTrackState by viewModel.selectedTrack.collectAsStateWithLifecycle()
                val lastPlacement by viewModel.lastRacePlacement.collectAsStateWithLifecycle()
                val lastRaceTime by viewModel.lastRaceTime.collectAsStateWithLifecycle()
                val lastEpReward by viewModel.lastRaceEpReward.collectAsStateWithLifecycle()
                val lastRpReward by viewModel.lastRaceRpReward.collectAsStateWithLifecycle()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Guard / Loading state when Room values are loading from SQLite disk on initial boot
                    val progress = progressState
                    if (progress == null) {
                        BoxFiller(modifier = Modifier.padding(innerPadding))
                    } else {
                        when (currentScreen) {
                            RacingScreen.Welcome -> {
                                DashboardScreen(
                                    progress = progress,
                                    onNavigate = { screen ->
                                        viewModel.navigateTo(screen)
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            RacingScreen.Lab -> {
                                LabScreen(
                                    progress = progress,
                                    onBack = { viewModel.navigateTo(RacingScreen.Welcome) },
                                    onSelectCreature = { id ->
                                        viewModel.selectActiveCreature(id)
                                    },
                                    onUpgradeTier = { category, tier ->
                                        viewModel.upgradeCreatureTier(category, tier)
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            RacingScreen.Matchmaker -> {
                                MatchmakerScreen(
                                    progress = progress,
                                    selectedTrack = selectedTrackState,
                                    onSelectTrack = { id ->
                                        viewModel.selectTrack(id)
                                    },
                                    onBack = { viewModel.navigateTo(RacingScreen.Welcome) },
                                    onLaunchRace = { viewModel.navigateTo(RacingScreen.ActiveRace) },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            RacingScreen.ActiveRace -> {
                                RaceScreen(
                                    liveRace = liveRaceState,
                                    onSteer = { factor ->
                                        viewModel.updatePlayerSteer(factor)
                                    },
                                    onActivateAbility = {
                                        viewModel.activatePlayerAbility()
                                    },
                                    onQuitRace = { viewModel.navigateTo(RacingScreen.Welcome) },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            RacingScreen.RaceReport -> {
                                ReportScreen(
                                    progress = progress,
                                    track = selectedTrackState,
                                    placement = lastPlacement,
                                    raceTime = lastRaceTime,
                                    epReward = lastEpReward,
                                    rpReward = lastRpReward,
                                    onProceed = { viewModel.navigateTo(RacingScreen.Leaderboard) },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            RacingScreen.Leaderboard -> {
                                LeaderboardScreen(
                                    leaderboardEntries = leaderboardEntries,
                                    onBack = { viewModel.navigateTo(RacingScreen.Welcome) },
                                    modifier = Modifier.padding(innerPadding)
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
fun BoxFiller(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0A10)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFFFF5722))
    }
}
