package com.bdsoftware.idorm.core.model

data class Campaign(
    val Id: String = "",
    val BannerUrl: String = "",
    val UrlRedirect: String? = null,
    val status: String? = "active",
    val deletedAt: String? = null
)
