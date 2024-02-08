package com.example.myapplicationtest

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplicationtest.ui.theme.MyApplicationTestTheme
import com.google.ai.client.generativeai.GenerativeModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            MyApplicationTestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val generativeModel = GenerativeModel(
                        modelName = "gemini-pro",
                        apiKey = "AIzaSyDovXdlRcPJf9c7ZJgepBnED4BJCewVDx8"

                    )
                    val viewModel = SummarizeViewModel(generativeModel)
                    SummarizeRoute(viewModel)
                }
            }
        }
    }
}

@Composable
internal fun SummarizeRoute(
    summarizeViewModel: SummarizeViewModel = viewModel()
) {
    val summarizeUiState by summarizeViewModel.uiState.collectAsState()

    SummarizeScreen(summarizeUiState, onSummarizeClicked = { inputText ->
        summarizeViewModel.summarize(inputText)
    })
}

@Composable
fun SummarizeScreen(

    uiState: SummarizeUiState = SummarizeUiState.Initial,
    onSummarizeClicked: (String) -> Unit = {}
) {
    var prompt by remember { mutableStateOf("") }
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }


    DisposableEffect(context) {
        tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                tts!!.language = Locale.US
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }
    LaunchedEffect(uiState) {
        if (uiState is SummarizeUiState.Success && uiState.outputText.isNotBlank()) {
            tts?.setPitch(0.001f)
            tts?.setSpeechRate(1.0f)
            tts?.speak(uiState.outputText, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF444479))
            .verticalScroll(rememberScrollState()),  // Add vertical scrolling
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.appchar),
            contentDescription = null,
            modifier = Modifier
                .size(220.dp)
                .padding(top = 30.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        TextField(
            value = prompt,
            label = { Text(stringResource(R.string.summarize_label)) },
            placeholder = { Text(stringResource(R.string.summarize_hint)) },
            onValueChange = { prompt = it },
            modifier = Modifier
                .padding(25.dp)
                .background(
                    color = Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(2.dp)
        )

        // Submit button
        TextButton(
            onClick = {
                onSummarizeClicked(prompt)
                if (uiState is SummarizeUiState.Success && uiState.outputText.isNotBlank()) {

                }

            },
            modifier = Modifier
                .testTag("submitButton")
                .fillMaxWidth()
                .padding(start = 25.dp)
                .padding(end = 25.dp)
                .background(
                    color = Color(0xFFBB86FC),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Text(
                text = stringResource(R.string.action_go),
                color = Color(0xFFF6C8FD),
                fontWeight = FontWeight.Bold
            )

        }


        when (uiState) {
            SummarizeUiState.Initial -> {
                // Nothing is shown
            }

            SummarizeUiState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    CircularProgressIndicator()
                }
            }

            is SummarizeUiState.Success -> {
                Row(modifier = Modifier.padding(all = 8.dp)) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = "Person Icon"
                    )
                    Text(
                        text = uiState.outputText,
                        modifier = Modifier
                            .padding(horizontal = 8.dp),
                        color = Color(0xFFF6C8FD)


                    )
                }
            }

            is SummarizeUiState.Error -> {
                Text(
                    text = uiState.errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(all = 8.dp)
                )
            }
        }
    }

}


@Composable
@Preview(showSystemUi = true)
fun SummarizeScreenPreview() {
    SummarizeScreen()
}
