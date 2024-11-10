package com.ml.dhenu.docqa.domain

import android.util.Log
import com.ml.dhenu.docqa.data.QueryResult
import com.ml.dhenu.docqa.data.RetrievedContext
import com.ml.dhenu.docqa.domain.llm.LlamaLocal
import com.ml.dhenu.docqa.util.PromptFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class QAUseCase(
    private val documentsUseCase: DocumentsUseCase,
    private val chunksUseCase: ChunksUseCase,
    private val llamaLocal: LlamaLocal
) {

    fun getAnswer(query: String, prompt: String, onResponse: ((QueryResult) -> Unit)) {
        var jointContext = ""
        val retrievedContextList = ArrayList<RetrievedContext>()
        chunksUseCase.getSimilarChunks(query, n = 2).forEach {
            jointContext += " " + it.second.chunkData
            retrievedContextList.add(RetrievedContext(it.second.docFileName, it.second.chunkData))
        }

        val inputPrompt = prompt.replace("\$CONTEXT", jointContext).replace("\$QUERY", query)
        val finalPrompt = PromptFormat.getTotalFormattedPrompt("",inputPrompt)

        CoroutineScope(Dispatchers.IO).launch {
//            Log.i("TTT = ", inputPrompt)
//            llamaLocal.getResponse(finalPrompt, object : LlamaCallback{
//                override fun onResult(result: String?) {
//                    if (result == PromptFormat.getStopToken(ModelType.LLAMA_3_2)) {
//                        return
//                    }
//
//                    if (result == "\n\n" || result == "\n") {
//                        if (mResultMessage.isNotEmpty()) {
//                            mResultMessage += result.orEmpty()
//                        }
//                    } else {
//                        mResultMessage += result.orEmpty()
//                    }
//
//                    onResponse(QueryResult(mResultMessage, retrievedContextList))
//                }
//
//                override fun onStats(p0: Float) {
//
//                }
//
//            })

            val response = llamaLocal.getCompleteResponse(finalPrompt) // New method to get complete response
            onResponse(QueryResult(response, retrievedContextList))
        }
    }

    fun stop() {
        llamaLocal.stop()
    }

    fun canGenerateAnswers(): Boolean {
        return documentsUseCase.getDocsCount() > 0
    }
}
