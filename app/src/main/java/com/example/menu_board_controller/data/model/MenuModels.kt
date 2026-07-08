package com.example.menu_board_controller.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Category(
    val id: String,
    val name: String,
    @SerialName("orderIndex") val orderIndex: Int = 0,
    @SerialName("isVisible") val isVisible: Boolean = true,
    val products: List<Product> = emptyList()
)

@Serializable
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    @SerialName("isVisible") val isVisible: Boolean = true,
    @SerialName("categoryId") val categoryId: String
)

@Serializable
data class WebSocketMessage<T>(
    val type: String,
    val payload: T? = null
)

@Serializable
data class DeletePayload(
    val id: String
)

@Serializable
data class CreateCategoryPayload(
    val name: String,
    @SerialName("orderIndex") val orderIndex: Int
)

@Serializable
data class UpdateCategoryPayload(
    val id: String,
    val name: String? = null,
    @SerialName("orderIndex") val orderIndex: Int? = null,
    @SerialName("isVisible") val isVisible: Boolean? = null,
    // Add snake_case aliases just in case the server is sensitive
    @SerialName("order_index") val order_index: Int? = orderIndex,
    @SerialName("is_visible") val is_visible: Boolean? = isVisible
)

@Serializable
data class CreateProductPayload(
    val name: String,
    val price: Double,
    @SerialName("categoryId") val categoryId: String,
    @SerialName("category_id") val category_id: String = categoryId
)

@Serializable
data class UpdateProductPayload(
    val id: String,
    val name: String? = null,
    val price: Double? = null,
    @SerialName("isVisible") val isVisible: Boolean? = null,
    @SerialName("categoryId") val categoryId: String? = null,
    // Aliases
    @SerialName("is_visible") val is_visible: Boolean? = isVisible,
    @SerialName("category_id") val category_id: String? = categoryId
)
