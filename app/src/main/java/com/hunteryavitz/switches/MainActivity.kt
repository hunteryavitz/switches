package com.hunteryavitz.switches

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
        GameScreen(onRestartClicked = { shouldShowBoarding = true }, highScore = highScore, context)
    }
}

@Composable
fun OnBoardingScreen(
    onContinueClicked: () -> Unit,
    highScore: Int,
    modifier: Modifier = Modifier
) {

    val fontFamily = FontFamily(
        Font(R.font.chakra_petch, FontWeight.Normal),
        Font(R.font.chakra_petch_bold, FontWeight.Bold)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.switches),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Text("SWITCHES",
                    modifier = Modifier.padding(4.dp),
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp,
                    color = colorResource(id = R.color.yellow))
            OutlinedButton(
                modifier = Modifier
                    .padding(12.dp)
                    .border(2.dp, colorResource(id = R.color.yellow), shape = MaterialTheme.shapes.medium),
                onClick = onContinueClicked,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.black).copy(alpha = 0.3f),
                )
            ) {
                Text(
                    "EFF AROUND AND FIND OUT",
                    fontSize = 18.sp,
                    fontFamily = fontFamily,
                    color = colorResource(id = R.color.yellow),
                    modifier = Modifier.padding(4.dp)
                )
            }
            if (highScore > 0) {
                Text("BEST SCORE: $highScore",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(16.dp)
                        .background(colorResource(id = R.color.yellow).copy(alpha = 0.5f))
                        .padding(8.dp))
            }
        }
    }
}

@Composable
fun GameScreen(
    onRestartClicked: () -> Unit,
    highScore: Int,
    context: Context,
    modifier: Modifier = Modifier
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
            .first { it ->
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

        for (i in 0 until size) {
            val current = indices[i]
            val next = indices[(i + 1) % size]
            relationships[current] = next
        }

        return relationships
    }

    var switches by remember { mutableStateOf(generateRandomSwitches(3)) }
    var relationships by remember { mutableStateOf(establishRandomSwitchMap(switches.size)) }
    var moveCount by remember { mutableStateOf(0) }
    val allSwitchesOn = switches.all { it }

    fun nextRound() {
        round++
        switches = generateRandomSwitches(switches.size + 1)
        relationships = establishRandomSwitchMap(switches.size)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.switches),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.fillMaxSize()
        )

        Box(
            modifier = modifier
                .fillMaxSize(1f)
                .background(Color.Black.copy(alpha = 0.3f))
        )

        if (allSwitchesOn) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
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
                        if (highScore == 0 || moveCount < highScore) {
                            saveHighScore(moveCount)
                        }
                        Text("YOU WIN",
                            color = Color.Yellow,
                            fontSize = 22.sp)
                        if ((highScore == 0) || (moveCount < highScore)) {
                            Text("NEW BEST SCORE",
                                color = Color.Black,
                                fontSize = 16.sp)
                        }
                        Text("TOTAL MOVES: $moveCount",
                            color = Color.Black,
                            fontSize = 18.sp,
                            modifier = modifier.padding(24.dp))
                        OutlinedButton(
                            onClick = {
                                onRestartClicked()
                            },
                        ) {
                            Text("RESTART",
                                color = Color.Yellow,
                                fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SWITCHES",
                color = Color.LightGray,
                fontSize = 32.sp,
                modifier = modifier.padding(bottom = 16.dp))

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
                    modifier = modifier.fillMaxWidth()
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
                                    moveCount++
                                },
                                modifier = modifier.padding(8.dp),
                                colors = switchColors
                            )
                        }
                    }
                }
            }

//                    Box(
//                    modifier = modifier
//                        .fillMaxSize()
//                        .background(Color.White.copy(alpha = 0.6f))
//                    )
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            modifier = modifier.fillMaxWidth()
//                        ) {
//                            if (allSwitchesOn && switches.size < 24) {
//                                OutlinedButton(
//                                    onClick = {
//                                        nextRound()
//                                    },
//                                ) {
//                                    Text("NEXT ROUND",
//                                        color = Color.Yellow,
//                                        fontSize = 18.sp)
//                                }
//                            } else if (allSwitchesOn && switches.size == 24) {
//                                if (highScore == 0 || moveCount < highScore) {
//                                    saveHighScore(moveCount)
//                                }
//                                Text("YOU WIN",
//                                    color = Color.Yellow,
//                                    fontSize = 22.sp)
//                                if ((highScore == 0) || (moveCount < highScore)) {
//                                    Text("NEW BEST SCORE",
//                                        color = Color.LightGray,
//                                        fontSize = 16.sp)
//                                }
//                                Text("TOTAL MOVES: $moveCount",
//                                    color = Color.DarkGray,
//                                    fontSize = 18.sp,
//                                    modifier = modifier.padding(24.dp))
//                                OutlinedButton(
//                                    onClick = {
//                                        onRestartClicked()
//                                    },
//                                ) {
//                                    Text("RESTART",
//                                        color = Color.Yellow,
//                                        fontSize = 18.sp)
//                                }
//                            }
//                        }
            }
        }
    }
