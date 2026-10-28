package com.rpay.v2.core.utils.enums

enum class HTTPCode(val code: Int) {
    OK(200),
    BadRequest(400),
    Unauthorized(401),
    NotFound(404),
    InternalServerError(500)
    // Add more status codes as needed
}



enum class TransactionTypeLookup {
    Purchase,
    Reversal,
    Refund,
    RefundReversal,
    Gift,
    Game,
    CashLess
}

enum class TcpCommunicationKeyEnum {
    SendPreferences,
    EnableSystemOutOfService,
    DisableSystemOutOfService,
    SubscriptionCheck,
    NearpayTidAssginment,
    HasNoGifts,
    HasGifts,
    RebootPos,
    EnterTerminalSetting,
    MachineStatus,
}

enum class TransactionType {
    PURCHASE,
    REFUND,
    REVERSAL,
    SESSION
}


enum class SystemType {
    MDB,
    PULSE
}

enum class PulseType {
    POSITIVE,
    NEGATIVE
}
