package com.ml.dhenu.docqa.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ml.dhenu.docqa.data.RetrievedContext
import com.ml.dhenu.docqa.domain.QAUseCase
import com.ml.dhenu.docqa.domain.llm.LlamaLocal
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChatViewModel(val qaUseCase: QAUseCase,val llamaModel: LlamaLocal) : ViewModel() {

    val questionState = mutableStateOf("")
    val responseState = mutableStateOf("")
    val isGeneratingResponseState = mutableStateOf(false)
    val retrievedContextListState = mutableStateOf(emptyList<RetrievedContext>())
}
