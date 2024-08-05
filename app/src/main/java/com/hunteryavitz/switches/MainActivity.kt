package com.hunteryavitz.switches
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hunteryavitz.switches.ui.theme.SwitchesTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwitchesTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    var shouldShowBoarding by rememberSaveable { mutableStateOf(true) }

    if (shouldShowBoarding) {
        OnBoardingScreen(onContinueClicked = { shouldShowBoarding = false })
    } else {
        LinkedSwitches()
    }
}

@Composable
fun LinkedSwitches() {
    var round by remember { mutableStateOf(1) }

    fun generateRandomSwitches(size: Int): List<Boolean> {
        return generateSequence { List(size) { Random.nextBoolean() } }
            .first { !it.all { it } && it.any { it } }
    }

    var switches by remember { mutableStateOf(generateRandomSwitches(3)) }

    fun nextRound() {
        round++
        switches = generateRandomSwitches(switches.size + 1)
    }

    val allSwitchesOn = switches.all { it } || switches.all { !it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Switches",
            color = Color.White,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 16.dp))

        val rows = (switches.size + 2) / 3

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

                                val randomIndex = (0 until newSwitches.size)
                                    .filter { it != switchIndex}.random()
                                newSwitches[randomIndex] = !newSwitches[randomIndex]
                                switches = newSwitches
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        if (allSwitchesOn && switches.size <= 24) {
            OutlinedButton(
                onClick = {
                    nextRound()
                },
                modifier = Modifier.height(48.dp)
            ) {
                Text("NEXT ROUND",
                    color = Color.White,
                    fontSize = 22.sp)
            }
        } else if (allSwitchesOn) {
            Text("YOU WIN",
                color = Color.White,
                fontSize = 22.sp)
        }
    }
}

@Composable
fun OnBoardingScreen(
    onContinueClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Switches", fontSize = 32.sp, modifier = Modifier.padding(bottom = 16.dp))
            OutlinedButton(
                onClick = onContinueClicked
            ) {
                Text("EFF AROUND AND FIND OUT", fontSize = 22.sp, color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun OnBoardingPreview() {
    SwitchesTheme {
        OnBoardingScreen(onContinueClicked = {})
    }
}

@Preview(showBackground = true, widthDp = 320, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun LinkedSwitchesPreview() {
    SwitchesTheme {
        LinkedSwitches()
    }
}
