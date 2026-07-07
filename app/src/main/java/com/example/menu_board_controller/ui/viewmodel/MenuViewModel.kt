package com.example.menu_board_controller.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.menu_board_controller.data.model.Category
import com.example.menu_board_controller.data.remote.MenuWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MenuViewModel : ViewModel() {
    private val webSocketClient = MenuWebSocketClient("wss://menu-board-8brurgku2-0-projects.vercel.app")
    
    private val _menuData = MutableStateFlow<List<Category>>(emptyList())
    val menuData: StateFlow<List<Category>> = _menuData.asStateFlow()

    init {
        webSocketClient.connect()
        viewModelScope.launch {
            webSocketClient.messages.collect { message ->
                if (message.type == "menuData" || message.type == "menuUpdated") {
                    @Suppress("UNCHECKED_CAST")
                    _menuData.value = message.payload as List<Category>
                }
            }
        }
    }

    fun addCategory(name: String, orderIndex: Int) {
        webSocketClient.createCategory(name, orderIndex)
    }

    fun updateCategory(id: String, name: String?, orderIndex: Int?, isVisible: Boolean?) {
        webSocketClient.updateCategory(id, name, orderIndex, isVisible)
    }

    fun deleteCategory(id: String) {
        webSocketClient.deleteCategory(id)
    }

    fun addProduct(name: String, price: Double, categoryId: String) {
        webSocketClient.createProduct(name, price, categoryId)
    }

    fun updateProduct(id: String, name: String?, price: Double?, isVisible: Boolean?) {
        webSocketClient.updateProduct(id, name, price, isVisible)
    }

    fun deleteProduct(id: String) {
        webSocketClient.deleteProduct(id)
    }

    override fun onCleared() {
        super.onCleared()
        webSocketClient.disconnect()
    }
}
