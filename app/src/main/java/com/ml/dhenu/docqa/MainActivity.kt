package com.ml.dhenu.docqa

import AppProgressDialog
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ml.dhenu.docqa.ui.screens.ChatScreen
import com.ml.dhenu.docqa.ui.screens.DocsScreen
import java.util.Locale
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import com.ml.dhenu.docqa.ui.viewmodels.DocsViewModel
import com.ml.dhenu.docqa.domain.readers.Readers

import hideProgressDialog
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import showProgressDialog

class MainActivity : ComponentActivity() {

    // Add required permissions
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
    )
    private val PERMISSION_REQUEST_CODE = 1111

    private val docsViewModel: DocsViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for all required permissions
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
        }

        Log.i("TTT Doc = ",docsViewModel.documentsUseCase.getDocsCount().toString())

        // Launch a coroutine to collect the Flow
//        CoroutineScope(Dispatchers.IO).launch {
//            docsViewModel.documentsUseCase.getAllDocuments().collect { documents ->
//                for (document in documents) {
//                    Log.i("TTT Doc", "ID: ${document.docId}, Name: ${document.docFileName}")
//                    docsViewModel.documentsUseCase.removeDocument(1)
//                    // Do whatever you need with each document here
//                }
//            }
//        }

        // Only load PDFs if no documents exist
        if (docsViewModel.documentsUseCase.getDocsCount() == 0L) {
            loadPDFsFromRaw()
        }

        enableEdgeToEdge()
        setContent {
            val navHostController = rememberNavController()
            NavHost(
                navController = navHostController,
                startDestination = "chat",
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                composable("docs") { DocsScreen(onBackClick = { navHostController.navigateUp() }) }
                composable("chat") {
                    ChatScreen(onOpenDocsClick = { navHostController.navigate("docs") })
                }
            }
            AppProgressDialog()
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun loadPDFsFromRaw() {
        showProgressDialog()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Example for a single PDF file named "sample.pdf" in raw
                resources.openRawResource(R.raw.kn).use { inputStream ->
                    val docsViewModel: DocsViewModel by inject()
                    docsViewModel.documentsUseCase.addDocument(
                        inputStream,
                        "kn.pdf",
                        Readers.DocumentType.PDF
                    )
                    withContext(Dispatchers.Main) {
                        hideProgressDialog()
                        inputStream.close()
                    }
                }

                // If you have multiple PDFs, you can list them here
                // resources.openRawResource(R.raw.another_pdf)...
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
