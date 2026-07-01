package com.bdsoftware.idorm.core.model

data class Banner(
    val title: String = "",
    val description: String = "",
    val badgeText: String = "",
    val imageUrl: String = "",
    val sort: Int = 0,
    val status: String? = "active",
    val clicks: Long? = 0,
    val views: Long? = 0,
    val endDate: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val deletedAt: String? = null
)
