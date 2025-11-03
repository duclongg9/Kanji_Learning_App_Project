package com.example.kanjilearning.data.mysql

/**
 * VI: Ngoại lệ quấn lỗi truy cập MySQL giúp tầng trên dễ debug.
 * EN: Exception wrapping MySQL access issues so upper layers can diagnose failures.
 */
class MySqlDataException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
