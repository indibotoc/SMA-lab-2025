package com.example.where2upt

data class HourSlot(
    val hour: Int,                   // 8, 9, 10...
    val isReserved: Boolean,
    val reservationTitle: String? = null,
    val reservationId: String? = null
)
