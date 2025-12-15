package com.quickticket.app.network.model

data class TicketStatusResponse(
    val ticketUsedToday: Boolean,
    val lastTicketDate: String?
)
