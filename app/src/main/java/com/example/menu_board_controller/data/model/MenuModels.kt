package com.example.menu_board_controller.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val orderIndex: Int,
    val isVisible: Boolean = true,
    val products: List<Product> = emptyList()
)

@Serializable
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val isVisible: Boolean = true,
    val categoryId: String
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
    val orderIndex: Int
)

@Serializable
data class UpdateCategoryPayload(
    val id: String,
    val name: String? = null,
    val orderIndex: Int? = null,
    val isVisible: Boolean? = null
)

@Serializable
data class CreateProductPayload(
    val name: String,
    val price: Double,
    val categoryId: String
)

@Serializable
data class UpdateProductPayload(
    val id: String,
    val name: String? = null,
    val price: Double? = null,
    val isVisible: Boolean? = null,
    val categoryId: String? = null
)
