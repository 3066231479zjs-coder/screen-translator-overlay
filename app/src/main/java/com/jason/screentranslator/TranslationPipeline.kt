package com.jason.screentranslator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class TranslationPipeline {
    suspend fun translate(text: String): String = withContext(Dispatchers.IO) {
        val config = TranslationSettings.current()
        if (text.isBlank()) return@withContext "未识别到文字"
        if (!config.isConfigured()) {
            return@withContext "识别文本：\n$text\n\n提示：尚未配置翻译接口，已先显示 OCR 原文。"
        }
        runCatching { requestOpenAiCompatibleTranslation(config, text) }
            .getOrElse { error -> "识别文本：\n$text\n\n翻译失败：${error.message ?: "未知错误"}" }
    }

    private fun requestOpenAiCompatibleTranslation(config: TranslationSettings, text: String): String {
        val url = URL(config.baseUrl.trimEnd('/') + "/chat/completions")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer ${config.apiKey}")
            setRequestProperty("Content-Type", "application/json")
        }
        val body = JSONObject()
            .put("model", config.model)
            .put("temperature", 0.2)
            .put(
                "messages",
                JSONArray()
                    .put(JSONObject().put("role", "system").put("content", "你是手机屏幕 OCR 翻译助手。把用户提供的文字翻译成简洁自然的中文；如果原文已经是中文，就只做简短整理。只输出译文。"))
                    .put(JSONObject().put("role", "user").put("content", text))
            )
        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(body.toString())
        }
        val responseText = if (connection.responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            throw IllegalStateException("HTTP ${connection.responseCode}: $errorText")
        }
        return JSONObject(responseText)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
    }
}

data class TranslationSettings(
    val baseUrl: String,
    val apiKey: String,
    val model: String
) {
    fun isConfigured(): Boolean = baseUrl.isNotBlank() && apiKey.isNotBlank() && model.isNotBlank()

    companion object {
        fun current(): TranslationSettings = TranslationSettings(
            baseUrl = BuildConfig.TRANSLATION_BASE_URL,
            apiKey = BuildConfig.TRANSLATION_API_KEY,
            model = BuildConfig.TRANSLATION_MODEL
        )
    }
}
