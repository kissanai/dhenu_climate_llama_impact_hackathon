package com.ml.dhenu.docqa.domain.llm

import android.util.Log
import com.google.ai.client.generativeai.type.generationConfig
import com.ml.dhenu.docqa.util.ModelType
import com.ml.dhenu.docqa.util.PromptFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import org.pytorch.executorch.LlamaCallback
import org.pytorch.executorch.LlamaModule

@Single
class LlamaLocal {

    private var mModule: LlamaModule? = null


    init {
        loadDeviceModel()
    }

    fun loadDeviceModel() {
        try {
            if (mModule != null) {
                mModule?.resetNative()
                mModule = null
            }

            mModule =
                LlamaModule(
                    1,
                    "/data/local/tmp/llama/llama3_2.pte",
                    "/data/local/tmp/llama/tokenizer.model",
                    0.3f,
                )

            val loadResult = mModule!!.load()
            Log.i("TTT loadResult", loadResult.toString())
            if (loadResult != 0) {
                Log.i("TTT Not Success", "Loading Model")
            } else {
                Log.i("TTT Success", "Loading Model")
            }
        } catch (ex: Exception) {
            Log.i("TTT Error Loading Model", ex.printStackTrace().toString())
        }
    }

    fun getResponse(prompt: String, callBack: LlamaCallback) {
        mModule?.generate(prompt,((prompt.length * 0.75)+64).toInt(), callBack,false)
    }

    fun getCompleteResponse(prompt: String): String {
        // Initialize StringBuilder to collect the response
        val fullResponse = StringBuilder()
        
        // Use existing getResponse method but collect all chunks
        getResponse(prompt, object : LlamaCallback {
            override fun onResult(result: String?) {
                if (result != PromptFormat.getStopToken(ModelType.LLAMA_3_2)) {
                    fullResponse.append(result.orEmpty())
                }
            }
            
            override fun onStats(p0: Float) {}
        })
        
        return fullResponse.toString()
    }

    fun stop() {
        mModule?.stop()
    }

}