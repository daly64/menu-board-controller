package com.example.menu_board_controller.data.remote

import android.util.Log
import com.example.menu_board_controller.data.model.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import okhttp3.*
import okio.ByteString

class MenuWebSocketClient(private val url: String) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    
    private val _messages = MutableSharedFlow<WebSocketMessage<*>>(extraBufferCapacity = 10)
    val messages: SharedFlow<WebSocketMessage<*>> = _messages

    private val json = Json { ignoreUnknownKeys = true }

    fun connect() {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected")
                getMenu()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Message received: $text")
                try {
                    val jsonElement = json.parseToJsonElement(text)
                    val type = jsonElement.jsonObject["type"]?.toString()?.removeSurrounding("\"")
                    
                    when (type) {
                        "menuData", "menuUpdated" -> {
                            val payload = jsonElement.jsonObject["payload"]
                            if (payload != null) {
                                val menu = json.decodeFromJsonElement<List<Category>>(payload)
                                _messages.tryEmit(WebSocketMessage(type, menu))
                            }
                        }
                        "error" -> {
                            val payload = jsonElement.jsonObject["payload"]
                            Log.e("WebSocket", "Error from server: $payload")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WebSocket", "Error parsing message", e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                Log.d("WebSocket", "Closing: $code / $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Failure: ${t.message}", t)
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
    }

    private fun sendMessage(type: String, payload: Any? = null) {
        val message = when (payload) {
            is CreateCategoryPayload -> Json.encodeToString(WebSocketMessage(type, payload))
            is UpdateCategoryPayload -> Json.encodeToString(WebSocketMessage(type, payload))
            is CreateProductPayload -> Json.encodeToString(WebSocketMessage(type, payload))
            is UpdateProductPayload -> Json.encodeToString(WebSocketMessage(type, payload))
            is DeletePayload -> Json.encodeToString(WebSocketMessage(type, payload))
            null -> "{\"type\":\"$type\"}"
            else -> ""
        }
        if (message.isNotEmpty()) {
            webSocket?.send(message)
        }
    }

    fun getMenu() = sendMessage("getMenu")
    
    fun createCategory(name: String, orderIndex: Int) = 
        sendMessage("createCategory", CreateCategoryPayload(name, orderIndex))
    
    fun updateCategory(id: String, name: String?, orderIndex: Int?, isVisible: Boolean?) =
        sendMessage("updateCategory", UpdateCategoryPayload(id, name, orderIndex, isVisible))
    
    fun deleteCategory(id: String) = 
        sendMessage("deleteCategory", DeletePayload(id))
    
    fun createProduct(name: String, price: Double, categoryId: String) =
        sendMessage("createProduct", CreateProductPayload(name, price, categoryId))
    
    fun updateProduct(id: String, name: String?, price: Double?, isVisible: Boolean?) =
        sendMessage("updateProduct", UpdateProductPayload(id, name, price, isVisible))
    
    fun deleteProduct(id: String) = 
        sendMessage("deleteProduct", DeletePayload(id))
}
