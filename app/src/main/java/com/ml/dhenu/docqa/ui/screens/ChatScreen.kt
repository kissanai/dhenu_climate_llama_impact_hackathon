package com.ml.dhenu.docqa.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.ml.dhenu.docqa.R
import com.ml.dhenu.docqa.ui.theme.DocQATheme
import com.ml.dhenu.docqa.ui.viewmodels.ChatViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

// Add these functions at the top level of the file
private fun getSelectedLanguage(context: Context): String {
    val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
    return prefs.getString("selected_language", "english") ?: "english"
}

private fun getLanguageBasedOnSelection(language: String): String {
    val locale = when (language.lowercase()) {
        "hindi" -> "hi-IN"
        else -> Locale.ENGLISH.language
    }
    return locale
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onOpenDocsClick: (() -> Unit)) {
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }

    // Check language selection when screen is first displayed
    DisposableEffect(Unit) {
        val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val selectedLanguage = prefs.getString("selected_language", null)
        if (selectedLanguage == null) {
            showLanguageDialog = true
        }
        onDispose { }
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { /* Dialog cannot be dismissed */ },
            title = { Text("Select Language") },
            text = {
                Column {
                    val languages = listOf("English", "Hindi")
                    languages.forEach { language ->
                        IconButton(
                            onClick = {
                                // Save selection to SharedPreferences
                                context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
                                    .edit()
                                    .putString("selected_language", language.lowercase())
                                    .apply()
                                showLanguageDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(language)
                        }
                    }
                }
            },
            confirmButton = { /* No confirm button needed */ }
        )
    }

    DocQATheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_dhenu),
                                contentDescription = "Dhenu Icon",
                                modifier = Modifier
                                    .size(40.dp)
                            )
                            Text(
                                text = "Dhenu Climate CoPilot",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            showLanguageDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Open Documents"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                val chatViewModel: ChatViewModel = koinViewModel()
                Column {
                    QALayout(chatViewModel)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.QALayout(chatViewModel: ChatViewModel) {
    val question by remember { chatViewModel.questionState }
    val response by remember { chatViewModel.responseState }
    val isGeneratingResponse by remember { chatViewModel.isGeneratingResponseState }
    val retrievedContextList by remember { chatViewModel.retrievedContextListState }
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var isSpeechRecognizationSupported by remember { mutableStateOf(false) }
    val textToSpeech = remember {
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
            }
        }
    }

    var showTranslationDialog by remember { mutableStateOf(false) }
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }

    if (SpeechRecognizer.isRecognitionAvailable(context.applicationContext)) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
        isSpeechRecognizationSupported = true

    } else {
        isSpeechRecognizationSupported = false
        // Speech recognition is not supported on this device
        Toast.makeText(
            context,
            "Speech recognition is not supported on this device.",
            Toast.LENGTH_LONG
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f),
    ) {
        if (question.trim().isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.Blue, CircleShape),
                    onClick = {
                        if (isSpeechRecognizationSupported) {
                            if (!isListening) {
                                isListening = true
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                                intent.putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                                )

                                val language = getSelectedLanguage(context)
                                val languageLocaleString = getLanguageBasedOnSelection(language)

                                intent.putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE,
                                    languageLocaleString
                                );
                                speechRecognizer?.startListening(intent)
                            } else {
                                speechRecognizer?.stopListening()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Speech recognition is not supported on this device.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(40.dp),
                        imageVector = if (isListening) Icons.Filled.Stop else Icons.Default.KeyboardVoice,
                        contentDescription = if (isListening) "Stop recording" else "Start voice input",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isListening) "Listening..." else "Tap to speak",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
            }
        } else {
            LazyColumn {
                item {
                    Text(text = question, style = MaterialTheme.typography.headlineLarge)
                    if (isGeneratingResponse) {
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
                item {
                    if (!isGeneratingResponse) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier =
                            Modifier
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .padding(24.dp)
                                .fillMaxWidth()
                        ) {
                            MarkdownText(
                                modifier = Modifier.fillMaxWidth(),
                                markdown = response,
                                style =
                                TextStyle(
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = {
                                        val sendIntent: Intent =
                                            Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, response)
                                                type = "text/plain"
                                            }
                                        val shareIntent = Intent.createChooser(sendIntent, null)
                                        context.startActivity(shareIntent)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share the response",
                                        tint = Color.Black
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        textToSpeech?.let { tts ->
                                            // Stop any ongoing speech
                                            if (tts.isSpeaking) {
                                                tts.stop()
                                            } else {
                                                // Start speaking the response
                                                tts.speak(
                                                    response,
                                                    TextToSpeech.QUEUE_FLUSH,
                                                    null,
                                                    "response_tts"
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = "Read response aloud",
                                        tint = Color.Black
                                    )
                                }

                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Clean up TTS when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    // Add the translation dialog
    if (showTranslationDialog) {
        AlertDialog(
            onDismissRequest = { showTranslationDialog = false },
            title = { Text("Hindi Translation") },
            text = {
                if (isTranslating) {
                    LinearProgressIndicator()
                } else {
                    Text(translatedText)
                }
            },
            confirmButton = {
                IconButton(onClick = { showTranslationDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Pass textToSpeech to QueryInput
    QueryInput(
        chatViewModel,
        textToSpeech,
        isListening,
        speechRecognizer,
        isSpeechRecognizationSupported,
        onListeningChanged = { newValue -> isListening = newValue }
    )
}

@Composable
private fun QueryInput(
    chatViewModel: ChatViewModel,
    textToSpeech: TextToSpeech?,
    isListening: Boolean,
    speechRecognizer: SpeechRecognizer?,
    isSpeechRecognizationSupported: Boolean,
    onListeningChanged: (Boolean) -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    val context = LocalContext.current

    val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            // Called when the endpointer is ready for the user to start speaking.
        }

        override fun onBeginningOfSpeech() {
            // Called when the user starts to speak.
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Called when the sound level in the audio stream has changed.
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Called when partial recognition results are available.
        }

        override fun onEndOfSpeech() {
            // Called after the user stops speaking.
        }

        override fun onError(error: Int) {
            onListeningChanged(false)
        }

        override fun onResults(results: Bundle?) {
            // Called when recognition results are ready.
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

            val recognizedText =
                matches?.firstOrNull() ?: ""  // Get the first result or an empty string if null

            chatViewModel.questionState.value = recognizedText
            questionText = ""
            chatViewModel.isGeneratingResponseState.value = true

            var finalFormatOfText = ""
            val language = getSelectedLanguage(context)
            if (language == "english") {
                finalFormatOfText = recognizedText
                chatViewModel.qaUseCase.getAnswer(
                    finalFormatOfText,
                    context.getString(R.string.prompt_1)
                ) { queryResult ->
                    val aiResponse = queryResult.response

                    chatViewModel.isGeneratingResponseState.value = false
                    chatViewModel.responseState.value = aiResponse
                    chatViewModel.retrievedContextListState.value = queryResult.context

                    // Automatically start TTS when response is generated
                    val result = textToSpeech?.setLanguage(Locale.ENGLISH)

                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED
                    ) {
                        Log.e("TTS", "Hindi language is not supported or missing")
                    } else {
                        textToSpeech?.let { tts ->
                            tts.speak(aiResponse, TextToSpeech.QUEUE_FLUSH, null, "response_tts")
                        }
                    }
                }
            } else if (language == "hindi") {
                // Create translator
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.HINDI)
                    .setTargetLanguage(TranslateLanguage.ENGLISH)
                    .build()
                val translator = Translation.getClient(options)

                // Download model if needed and translate
                translator.downloadModelIfNeeded()
                    .addOnSuccessListener {
                        translator.translate(recognizedText)
                            .addOnSuccessListener { translatedResponse ->
                                finalFormatOfText = translatedResponse

                                chatViewModel.qaUseCase.getAnswer(
                                    finalFormatOfText,
                                    context.getString(R.string.prompt_1)
                                ) { queryResult ->
                                    val aiResponse = queryResult.response
                                    var finalFormatOfText = ""

                                    // Create translator
                                    val options = TranslatorOptions.Builder()
                                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                                        .setTargetLanguage(TranslateLanguage.HINDI)
                                        .build()
                                    val translator = Translation.getClient(options)

                                    // Download model if needed and translate
                                    translator.downloadModelIfNeeded()
                                        .addOnSuccessListener {
                                            translator.translate(aiResponse)
                                                .addOnSuccessListener { translatedResponse ->
                                                    finalFormatOfText = translatedResponse
                                                    Log.i(
                                                        "TTT Translated hindi to english = ",
                                                        translatedResponse
                                                    )

                                                    chatViewModel.isGeneratingResponseState.value =
                                                        false
                                                    chatViewModel.responseState.value =
                                                        finalFormatOfText
                                                    chatViewModel.retrievedContextListState.value =
                                                        queryResult.context

                                                    // Automatically start TTS when response is generated

                                                    val result = textToSpeech?.setLanguage(
                                                        Locale(
                                                            "hi",
                                                            "IN"
                                                        )
                                                    )

                                                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                                                        result == TextToSpeech.LANG_NOT_SUPPORTED
                                                    ) {
                                                        Log.e(
                                                            "TTS",
                                                            "Hindi language is not supported or missing"
                                                        )
                                                    } else {
                                                        textToSpeech?.let { tts ->
                                                            tts.speak(
                                                                finalFormatOfText,
                                                                TextToSpeech.QUEUE_FLUSH,
                                                                null,
                                                                "response_tts"
                                                            )
                                                        }
                                                    }

                                                }
                                                .addOnFailureListener { exception ->
                                                    finalFormatOfText = ""
                                                }
                                        }
                                        .addOnFailureListener { exception ->
                                            finalFormatOfText = ""
                                        }

                                }
                            }
                            .addOnFailureListener { exception ->
                                finalFormatOfText = ""
                            }
                    }
                    .addOnFailureListener { exception ->
                        finalFormatOfText = ""
                    }
            } else {
                finalFormatOfText = recognizedText
                chatViewModel.qaUseCase.getAnswer(
                    finalFormatOfText,
                    context.getString(R.string.prompt_1)
                ) { queryResult ->
                    val aiResponse = queryResult.response

                    chatViewModel.isGeneratingResponseState.value = false
                    chatViewModel.responseState.value = aiResponse
                    chatViewModel.retrievedContextListState.value = queryResult.context

                    // Automatically start TTS when response is generated

                    val result = textToSpeech?.setLanguage(Locale.ENGLISH)

                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED
                    ) {
                        Log.e("TTS", "Hindi language is not supported or missing")
                    } else {
                        textToSpeech?.let { tts ->
                            tts.speak(aiResponse, TextToSpeech.QUEUE_FLUSH, null, "response_tts")
                        }
                    }
                }
            }

            onListeningChanged(false)
            // matches contains the list of recognized words
        }

        override fun onPartialResults(partialResults: Bundle?) {
            // Called when partial recognition results are available.
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // Reserved for adding future events.
        }
    }
    speechRecognizer?.setRecognitionListener(recognitionListener)

    val keyboardController = LocalSoftwareKeyboardController.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            value = questionText,
            onValueChange = { questionText = it },
            shape = RoundedCornerShape(16.dp),
            colors =
            TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                disabledTextColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            placeholder = { Text(text = "Ask documents...") }
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            modifier = Modifier.background(Color.Blue, CircleShape),
            onClick = {
                keyboardController?.hide()
                if (!chatViewModel.qaUseCase.canGenerateAnswers()) {
                    Toast.makeText(context, "Add documents to execute queries", Toast.LENGTH_LONG)
                        .show()
                    return@IconButton
                }
                if (questionText.trim().isEmpty()) {
                    Toast.makeText(context, "Enter a query to execute", Toast.LENGTH_LONG).show()
                    return@IconButton
                }

                chatViewModel.questionState.value = questionText
                questionText = ""
                chatViewModel.isGeneratingResponseState.value = true


                val language = getSelectedLanguage(context)

                if (language == "english") {
                    chatViewModel.qaUseCase.getAnswer(
                        chatViewModel.questionState.value,
                        context.getString(R.string.prompt_1)
                    ) {
                        chatViewModel.isGeneratingResponseState.value = false
                        chatViewModel.responseState.value = it.response
                        chatViewModel.retrievedContextListState.value = it.context

                        // Automatically start TTS when response is generated
                        textToSpeech?.let { tts ->
                            tts.speak(it.response, TextToSpeech.QUEUE_FLUSH, null, "response_tts")
                        }
                    }
                } else if (language == "hindi") {
                    val options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.HINDI)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build()
                    val translator = Translation.getClient(options)
                    var finalFormatOfText = ""
                    // Download model if needed and translate
                    translator.downloadModelIfNeeded()
                        .addOnSuccessListener {
                            translator.translate(chatViewModel.questionState.value)
                                .addOnSuccessListener { translatedResponse ->
                                    finalFormatOfText = translatedResponse
                                    chatViewModel.qaUseCase.getAnswer(
                                        finalFormatOfText,
                                        context.getString(R.string.prompt_1)
                                    ) { queryResult ->
                                        val aiResponse = queryResult.response

                                        // Create translator
                                        val options = TranslatorOptions.Builder()
                                            .setSourceLanguage(TranslateLanguage.ENGLISH)
                                            .setTargetLanguage(TranslateLanguage.HINDI)
                                            .build()
                                        val translator = Translation.getClient(options)

                                        // Download model if needed and translate
                                        translator.downloadModelIfNeeded()
                                            .addOnSuccessListener {
                                                translator.translate(aiResponse)
                                                    .addOnSuccessListener { translatedResponse ->
                                                        finalFormatOfText = translatedResponse

                                                        chatViewModel.isGeneratingResponseState.value =
                                                            false
                                                        chatViewModel.responseState.value =
                                                            finalFormatOfText
                                                        chatViewModel.retrievedContextListState.value =
                                                            queryResult.context

                                                        // Automatically start TTS when response is generated

                                                        val result = textToSpeech?.setLanguage(
                                                            Locale(
                                                                "hi",
                                                                "IN"
                                                            )
                                                        )

                                                        if (result == TextToSpeech.LANG_MISSING_DATA ||
                                                            result == TextToSpeech.LANG_NOT_SUPPORTED
                                                        ) {
                                                            Log.e(
                                                                "TTS",
                                                                "Hindi language is not supported or missing"
                                                            )
                                                        } else {
                                                            textToSpeech?.let { tts ->
                                                                tts.speak(
                                                                    finalFormatOfText,
                                                                    TextToSpeech.QUEUE_FLUSH,
                                                                    null,
                                                                    "response_tts"
                                                                )
                                                            }
                                                        }

                                                    }
                                                    .addOnFailureListener { exception ->
                                                        finalFormatOfText = ""
                                                    }
                                            }
                                            .addOnFailureListener { exception ->
                                                finalFormatOfText = ""
                                            }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    finalFormatOfText = ""
                                }
                        }
                        .addOnFailureListener { exception ->
                            finalFormatOfText = ""
                        }

                } else {
                    chatViewModel.qaUseCase.getAnswer(
                        chatViewModel.questionState.value,
                        context.getString(R.string.prompt_1)
                    ) {
                        chatViewModel.isGeneratingResponseState.value = false
                        chatViewModel.responseState.value = it.response
                        chatViewModel.retrievedContextListState.value = it.context

                        // Automatically start TTS when response is generated
                        textToSpeech?.let { tts ->
                            tts.speak(it.response, TextToSpeech.QUEUE_FLUSH, null, "response_tts")
                        }
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Send query",
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            modifier = Modifier.background(Color.Blue, CircleShape),
            onClick = {
                keyboardController?.hide()
                // Reset all states to their initial values
                chatViewModel.isGeneratingResponseState.value = false
                chatViewModel.responseState.value = ""
                chatViewModel.questionState.value = ""
                chatViewModel.retrievedContextListState.value = emptyList()
                textToSpeech?.stop()
                questionText = ""  // Reset the input field
            }
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Clear chat",  // Updated description
                tint = Color.White
            )
        }
    }
}
