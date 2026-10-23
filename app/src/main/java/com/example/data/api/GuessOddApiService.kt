package com.example.data.api

import com.example.data.model.ApiDrawItem
import com.example.data.model.ApiGuessOddResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.http.Url
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

interface GuessOddApi {
    @GET
    suspend fun fetchRawData(
        @Url fullUrl: String,
        @QueryMap queries: Map<String, String>
    ): Response<ResponseBody>
}

data class ApiResponseResult(
    val draws: List<ApiDrawItem>,
    val serverTime: String?,
    val rawJson: String,
    val statusCode: Int
)

class GuessOddNetworkClient {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://manage.im2015.com/")
        .client(okHttpClient)
        .build()

    private val api: GuessOddApi = retrofit.create(GuessOddApi::class.java)

    suspend fun fetchDraws(
        baseUrl: String,
        page: Int,
        limit: Int,
        type: Int
    ): ApiResponseResult {
        val queryMap = mutableMapOf<String, String>()
        queryMap["page"] = page.toString()
        queryMap["limit"] = limit.toString()
        queryMap["type"] = type.toString()

        val response = api.fetchRawData(baseUrl, queryMap)

        if (!response.isSuccessful) {
            val errCode = response.code()
            val errMessage = response.errorBody()?.string() ?: response.message()
            throw RuntimeException("HTTP $errCode: ${errMessage.ifEmpty { "Server error" }}")
        }

        val serverDateHeader = response.headers()["Date"]
        val formattedServerTime = formatServerDateHeader(serverDateHeader)

        val rawBody = response.body()?.string() ?: throw RuntimeException("Empty response body from API")

        val draws = parseResponse(rawBody)

        return ApiResponseResult(
            draws = draws,
            serverTime = formattedServerTime,
            rawJson = rawBody,
            statusCode = response.code()
        )
    }

    private fun parseResponse(rawJson: String): List<ApiDrawItem> {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) return emptyList()

        try {
            val adapter = moshi.adapter(ApiGuessOddResponse::class.java)
            val parsed = adapter.fromJson(trimmed)
            if (parsed?.data != null && parsed.data.isNotEmpty()) {
                return parsed.data
            }
        } catch (_: Exception) {
            // Fallback to manual JSONObject parsing
        }

        return tryManualJsonParse(trimmed)
    }

    private fun tryManualJsonParse(rawJson: String): List<ApiDrawItem> {
        val result = mutableListOf<ApiDrawItem>()
        try {
            val root = JSONObject(rawJson)
            val dataArray = root.optJSONArray("data")
            if (dataArray != null) {
                for (i in 0 until dataArray.length()) {
                    val obj = dataArray.optJSONObject(i) ?: continue

                    val id = if (obj.has("id")) obj.get("id") else null
                    val addTime = obj.optString("add_time", "")
                    val gameState = if (obj.has("game_state")) obj.get("game_state") else null
                    val gameType = if (obj.has("game_type")) obj.get("game_type") else null
                    val gameGuess = obj.optString("game_guess", "")
                    val gameLotteryNum = obj.optString("game_lottery_num", "")
                    val gameTime = obj.optString("game_time", "")
                    val gameOpenTime = obj.optString("game_open_time", "")
                    val _gameState = obj.optString("_game_state", "")
                    val _gameGuess = obj.optString("_game_guess", "")
                    val _gameTime = obj.optString("_game_time", "")
                    val _gameOpenTime = obj.optString("_game_open_time", "")

                    val time = obj.optString("time", "")
                    val serial = if (obj.has("serial")) obj.get("serial") else id
                    val number = if (obj.has("number")) obj.get("number") else null
                    val resVal = if (obj.has("result_value")) obj.optInt("result_value", 0) else null
                    val resStr = obj.optString("result", "")

                    result.add(
                        ApiDrawItem(
                            id = id,
                            addTime = addTime,
                            gameState = gameState,
                            gameType = gameType,
                            gameGuess = gameGuess,
                            gameLotteryNum = gameLotteryNum,
                            gameTime = gameTime,
                            gameOpenTime = gameOpenTime,
                            _gameState = _gameState,
                            _gameGuess = _gameGuess,
                            _gameTime = _gameTime,
                            _gameOpenTime = _gameOpenTime,
                            time = time,
                            serial = serial,
                            number = number,
                            resultValue = resVal,
                            result = resStr
                        )
                    )
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse API JSON: ${e.localizedMessage}")
        }
        return result
    }

    private fun formatServerDateHeader(headerValue: String?): String? {
        if (headerValue.isNullOrBlank()) return null
        return try {
            val httpDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
            httpDateFormat.timeZone = TimeZone.getTimeZone("GMT")
            val date: Date? = httpDateFormat.parse(headerValue)
            if (date != null) {
                val outputFormat = SimpleDateFormat("HH:mm:ss (GMT)", Locale.getDefault())
                outputFormat.timeZone = TimeZone.getTimeZone("GMT")
                outputFormat.format(date)
            } else {
                headerValue
            }
        } catch (_: Exception) {
            headerValue
        }
    }
}
