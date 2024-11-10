package com.ml.dhenu.docqa.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.ml.dhenu.docqa.domain.DocumentsUseCase
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class DocsViewModel(val documentsUseCase: DocumentsUseCase) : ViewModel() {

    val documentsFlow = documentsUseCase.getAllDocuments()
}
