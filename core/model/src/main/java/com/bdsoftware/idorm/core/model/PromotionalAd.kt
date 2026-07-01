package com.bdsoftware.idorm.core.model

data class PromotionalAd(
    val Id: String = "",
    val displayScreen: String = "home",
    val position: String = "middle", // "top" | "middle" | "bottom" | "popup"
    val Title: String? = null,
    val Description: String? = null,
    val ActionText: String = "",
    val ActionTextPosition: String? = "START", // START, CENTER, END
    val ActionStyle: String? = "DEFAULT", // DEFAULT, OUTLINE
    val Sort: Int = 0,
    val BannerUrl: String = "",
    val GradientStart: String? = null,
    val GradientEnd: String? = null,
    val Overlay: Int = 40, // 0 to 100
    val UrlRedirect: String? = null,
    val status: String? = "active",
    val clicks: Long? = 0,
    val views: Long? = 0,
    val startDate: String? = null,
    val endDate: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val deletedAt: String? = null
)
