package com.example.kanjilearning.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * VI: Lưu giao dịch mô phỏng thanh toán MoMo để audit và debug.
 * EN: Stores simulated MoMo transactions for receipts/history.
 */
@Entity(tableName = "payment_transactions")
data class PaymentTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "transaction_id")
    val transactionId: Long = 0,
    @ColumnInfo(name = "course_id")
    val courseId: Long,
    @ColumnInfo(name = "provider")
    val provider: String,
    @ColumnInfo(name = "amount")
    val amount: Int,
    @ColumnInfo(name = "status")
    val status: String,
    @ColumnInfo(name = "reference")
    val reference: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
