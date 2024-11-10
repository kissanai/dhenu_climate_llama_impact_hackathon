package com.ml.dhenu.docqa.domain

import android.util.Log
import com.ml.dhenu.docqa.data.Chunk
import com.ml.dhenu.docqa.data.ChunksDB
import com.ml.dhenu.docqa.domain.embeddings.SentenceEmbeddingProvider
import org.koin.core.annotation.Single

@Single
class ChunksUseCase(
    private val chunksDB: ChunksDB,
    private val sentenceEncoder: SentenceEmbeddingProvider
) {

    fun addChunk(docId: Long, docFileName: String, chunkText: String) {
        val embedding = sentenceEncoder.encodeText(chunkText)
        Log.e("APP", "Embedding dims ${embedding.size}")
        chunksDB.addChunk(
            Chunk(
                docId = docId,
                docFileName = docFileName,
                chunkData = chunkText,
                chunkEmbedding = embedding
            )
        )
    }

    fun removeChunks(docId: Long) {
        chunksDB.removeChunks(docId)
    }

    fun getSimilarChunks(query: String, n: Int = 5): List<Pair<Float, Chunk>> {
        val queryEmbedding = sentenceEncoder.encodeText(query)
        return chunksDB.getSimilarChunks(queryEmbedding, n)
    }
}
