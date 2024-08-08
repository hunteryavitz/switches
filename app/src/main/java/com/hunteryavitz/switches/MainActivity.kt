package com.hunteryavitz.switches

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import com.hunteryavitz.switches.ui.theme.SwitchesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwitchesTheme {
                MainApp(this)
            }
        }
    }
}

@Composable
fun MainApp(context: Context) {
    val sharedPreferences = context.getSharedPreferences("switches", Context.MODE_PRIVATE)

    fun getHighScore(): Int {
        return sharedPreferences.getInt("high_score", 0)
    }

    val highScore = getHighScore()
    var shouldShowBoarding by rememberSaveable { mutableStateOf(true) }

    if (shouldShowBoarding) {
        OnBoardingScreen(onContinueClicked = { shouldShowBoarding = false }, highScore = highScore)
    } else {
        LinkedSwitches(onRestartClicked = { shouldShowBoarding = true }, highScore = highScore, context)
    }
}

@Composable
fun OnBoardingScreen(
    onContinueClicked: () -> Unit,
    highScore: Int,
    modifier: Modifier = Modifier
) {
    Surface {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Switches",
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 16.dp),
                color = Color.LightGray)
            OutlinedButton(
                onClick = onContinueClicked,
            ) {
                Text("EFF AROUND AND FIND OUT",
                    fontSize = 18.sp,
                    color = Color.Yellow,
                    modifier = Modifier.padding(16.dp)
                )
            }
            if (highScore > 0) {
                Text("Best Score: $highScore",
                    fontSize = 18.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun LinkedSwitches(
    onRestartClicked: () -> Unit,
    highScore: Int,
    context: Context
) {
    val sharedPreferences = context.getSharedPreferences("switches", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    fun saveHighScore(score: Int) {
        if (highScore == 0 || score in 1 until highScore) {
            editor.putInt("high_score", score)
            editor.apply()
        }
    }

    var round by remember { mutableStateOf(1) }

    fun generateRandomSwitches(size: Int): List<Boolean> {
        return generateSequence { List(size) { Random.nextBoolean() } }
            .first {
                val trueCount = it.count { it }
                !it.all { it } &&
                        (size % 2 != 0 || trueCount % 2 == 0) &&
                        (size % 2 == 0 || trueCount % 2 == 1)
            }
    }

    fun establishRandomSwitchMap(size: Int): Map<Int, Int> {
        val relationships = mutableMapOf<Int, Int>()
        val indices = (0 until size).toMutableList()
        indices.shuffle()
        for (i in indices.indices) {
            val current = indices[i]
            val randomIndex = indices.filter { it != current }.random()
            relationships[current] = randomIndex
        }
        return relationships
    }

    var switches by remember { mutableStateOf(generateRandomSwitches(3)) }
    var relationships by remember { mutableStateOf(establishRandomSwitchMap(switches.size)) }
    var toggleCount by remember { mutableStateOf(0) }
    val allSwitchesOn = switches.all { it }

    fun nextRound() {
        round++
        switches = generateRandomSwitches(switches.size + 1)
        relationships = establishRandomSwitchMap(switches.size)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Switches",
            color = Color.LightGray,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 16.dp))

        val rows = (switches.size + 2) / 3

        val switchColors = SwitchDefaults.colors(
            checkedThumbColor = Color.Yellow,
            checkedTrackColor = Color.LightGray,
            uncheckedThumbColor = Color.Black,
            uncheckedTrackColor = Color.Gray
        )

        for (rowIndex in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (columnIndex in 0 until 3) {
                    val switchIndex = rowIndex * 3 + columnIndex
                    if (switchIndex < switches.size) {
                        Switch(
                            checked = switches[switchIndex],
                            onCheckedChange = {
                                val newSwitches = switches.toMutableList()
                                newSwitches[switchIndex] = it

                                val randomIndex = relationships[switchIndex] ?: switchIndex
                                newSwitches[randomIndex] = !newSwitches[randomIndex]

                                switches = newSwitches
                                toggleCount++
                            },
                            modifier = Modifier.padding(8.dp),
                            colors = switchColors
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.padding(32.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (allSwitchesOn && switches.size < 5) {
                OutlinedButton(
                    onClick = {
                        nextRound()
                    },
                ) {
                    Text("NEXT ROUND",
                        color = Color.Yellow,
                        fontSize = 18.sp)
                }
            } else if (allSwitchesOn && switches.size == 5) {
                if (highScore == 0 || toggleCount < highScore) {
                    saveHighScore(toggleCount)
                }
                Text("YOU WIN",
                    color = Color.Yellow,
                    fontSize = 22.sp)
                if ((highScore == 0) || (toggleCount < highScore)) {
                    Text("NEW BEST SCORE",
                        color = Color.LightGray,
                        fontSize = 16.sp)
                }
                Text("Total Moves: $toggleCount",
                    color = Color.DarkGray,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(24.dp))
                OutlinedButton(
                    onClick = {
                        onRestartClicked()
                    },
                ) {
                    Text("RESTART",
                        color = Color.Yellow,
                        fontSize = 18.sp)
                }
            } else {
                OutlinedButton(
                    onClick = {
                        onRestartClicked()
                    },
                ) {
                    Text(
                        "GIVE UP",
                        color = Color.Yellow,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
