package com.rpay.v2.data.response

data class GetTerminalsResponse(
    val data: TerminalsDataDto?,
    val succeeded: Boolean,
    val errors: List<String>?,
    val httpStatusCode: Int
)

data class TerminalsDataDto(
    val terminals: List<TerminalDto>,
    val pages: PagesDto
)

data class TerminalDto(
    val uuid: String?,
    val tid: String?,
    val name: LocalizedTextDto?,
    val status: LocalizedTextDto?,
    val is_connected: Boolean,
    val is_assigned_to_user: Boolean,
    val bank: BankDto?,
    val merchant: MerchantDto?,
    val user: UserDto?,
    val created_at: String?,
    val updated_at: String?
)

data class BankDto(
    val name: String?
)

data class MerchantDto(
    val id: String?,
    val name: String?
)

data class UserDto(
    val id: String?,
    val email: String?,
    val mobile: String?
)

data class LocalizedTextDto(
    val english: String?,
    val arabic: String?
)

data class PagesDto(
    val total: Int,
    val current: Int
)