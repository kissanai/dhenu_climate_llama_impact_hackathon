package com.ml.dhenu.docqa.util


class PromptFormat {

    companion object {
        val SYSTEM_PLACEHOLDER: String = "{{ system_prompt }}"
        val USER_PLACEHOLDER: String = "{{ user_prompt }}"
        val ASSISTANT_PLACEHOLDER: String = "{{ assistant_response }}"
        val DEFAULT_SYSTEM_PROMPT: String = "Answer the questions in a few sentences"

        fun getSystemPromptTemplate(modelType: ModelType?): String {
            return when (modelType) {
                ModelType.LLAMA_3, ModelType.LLAMA_3_1, ModelType.LLAMA_3_2 -> """
    <|begin_of_text|><|start_header_id|>system<|end_header_id|>
    $SYSTEM_PLACEHOLDER<|eot_id|>
    """.trimIndent()

                ModelType.LLAVA_1_5 -> "USER: "
                else -> SYSTEM_PLACEHOLDER
            }
        }

        fun getUserPromptTemplate(modelType: ModelType?): String {
            return when (modelType) {
                ModelType.LLAMA_3, ModelType.LLAMA_3_1, ModelType.LLAMA_3_2, ModelType.LLAMA_GUARD_3 -> """
    <|start_header_id|>user<|end_header_id|>
    $USER_PLACEHOLDER<|eot_id|><|start_header_id|>assistant<|end_header_id|>
    """.trimIndent()

                ModelType.LLAVA_1_5 -> USER_PLACEHOLDER
                else -> USER_PLACEHOLDER
            }
        }

        fun getConversationFormat(modelType: ModelType?): String {
            return when (modelType) {
                ModelType.LLAMA_3, ModelType.LLAMA_3_1, ModelType.LLAMA_3_2 -> """
    ${getUserPromptTemplate(modelType)}
    $ASSISTANT_PLACEHOLDER<|eot_id|>
    """.trimIndent()

                ModelType.LLAVA_1_5 -> "$USER_PLACEHOLDER ASSISTANT:"
                else -> USER_PLACEHOLDER
            }
        }

        fun getFormattedSystemAndUserPrompt(prompt: String): String {
            return getFormattedSystemPrompt() + getFormattedUserPrompt(prompt)
        }

        fun getFormattedSystemPrompt(): String {
            return getSystemPromptTemplate(ModelType.LLAMA_3_2)
                .replace(SYSTEM_PLACEHOLDER, "")
        }

        fun getFormattedUserPrompt(prompt: String): String {
            return getUserPromptTemplate(ModelType.LLAMA_3_2).replace(USER_PLACEHOLDER, prompt)
        }

        fun getTotalFormattedPrompt(
            conversationHistory: String,
            rawPrompt: String
        ): String {
            if (conversationHistory.isEmpty()) {
                return getFormattedSystemAndUserPrompt(rawPrompt)
            }

            return (getFormattedSystemPrompt()
                    + conversationHistory
                    + getFormattedUserPrompt(rawPrompt))
        }

        fun getStopToken(modelType: ModelType?): String {
            return when (modelType) {
                ModelType.LLAMA_3, ModelType.LLAMA_3_1, ModelType.LLAMA_3_2, ModelType.LLAMA_GUARD_3 -> "<|eot_id|>"
                ModelType.LLAVA_1_5 -> "</s>"
                else -> ""
            }
        }

        fun getLlavaPresetPrompt(): String {
            return ("A chat between a curious human and an artificial intelligence assistant. The assistant"
                    + " gives helpful, detailed, and polite answers to the human's questions. USER: ")
        }
    }
}