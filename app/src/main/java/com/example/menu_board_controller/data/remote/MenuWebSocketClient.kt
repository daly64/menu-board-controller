package com.example.menu_board_controller.data.remote

import android.util.Log
import com.example.menu_board_controller.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.*
import java.util.concurrent.TimeUnit

class MenuWebSocketClient(private val url: String) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(15, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()
        
    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _messages = MutableSharedFlow<WebSocketMessage<List<Category>>>(extraBufferCapacity = 100)
    val messages: SharedFlow<WebSocketMessage<List<Category>>> = _messages

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false // DO NOT send null fields, server might try to save them to NOT NULL columns
        isLenient = true
    }

    fun connect() {
        if (webSocket != null) return
        
        Log.d("MenuWS", "Connecting to $url")
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i("MenuWS", "CONNECTED")
                getMenu()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("MenuWS", "RCV: $text")
                try {
                    val root = json.parseToJsonElement(text).jsonObject
                    val type = root["type"]?.jsonPrimitive?.content ?: ""
                    
                    if (type == "menuData" || type == "menuUpdated") {
                        val payload = root["payload"]
                        if (payload != null && payload is JsonArray) {
                            val menu = json.decodeFromJsonElement<List<Category>>(payload)
                            _messages.tryEmit(WebSocketMessage(type, menu))
                        }
                    } else if (type == "error") {
                        Log.e("MenuWS", "SERVER ERROR: ${root["payload"]}")
                    } else if (type.isNotEmpty() && type != "getMenu") {
                        // For success responses like createdCategory, updatedProduct, etc.
                        getMenu()
                    }
                } catch (e: Exception) {
                    Log.e("MenuWS", "JSON PARSE FAIL: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("MenuWS", "CONNECTION FAILED: ${t.message}")
                this@MenuWebSocketClient.webSocket = null
                scope.launch {
                    delay(5000)
                    connect()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                this@MenuWebSocketClient.webSocket = null
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnect")
        webSocket = null
    }

    private inline fun <reified T> sendMsg(type: String, payload: T? = null) {
        try {
            val message = if (payload == null) {
                "{\"type\":\"$type\"}"
            } else {
                json.encodeToString(WebSocketMessage(type, payload))
            }
            Log.d("MenuWS", "SENDING: $message")
            val sent = webSocket?.send(message) ?: false
            if (!sent) {
                Log.w("MenuWS", "Send failed. Socket may be closed.")
            }
        } catch (e: Exception) {
            Log.e("MenuWS", "SERIALIZATION ERROR: ${e.message}")
        }
    }

    fun getMenu() = sendMsg<String>("getMenu")
    
    fun createCategory(name: String, orderIndex: Int) = 
        sendMsg("createCategory", CreateCategoryPayload(name, orderIndex))
    
    fun updateCategory(id: String, name: String?, orderIndex: Int?, isVisible: Boolean?) =
        sendMsg("updateCategory", UpdateCategoryPayload(id, name, orderIndex, isVisible))
    
    fun deleteCategory(id: String) = 
        sendMsg("deleteCategory", DeletePayload(id))
    
    fun createProduct(name: String, price: Double, categoryId: String) =
        sendMsg("createProduct", CreateProductPayload(name, price, categoryId))
    
    fun updateProduct(id: String, name: String?, price: Double?, isVisible: Boolean?, categoryId: String? = null) =
        sendMsg("updateProduct", UpdateProductPayload(id, name, price, isVisible, categoryId))
    
    fun deleteProduct(id: String) = 
        sendMsg("deleteProduct", DeletePayload(id))
}
