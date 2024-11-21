package com.cs407.square_up

import androidx.room.*
import java.util.Date


@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = User::class, // Reference to User entity
            parentColumns = ["userId"], // Column in User entity
            childColumns = ["userWhoPaidId"], // Corresponding column in Transaction
            onDelete = ForeignKey.CASCADE // Cascade delete when User is deleted
        )
    ],
    indices = [Index(value = ["userWhoPaidId"])] // Index for faster queries
)

data class Transaction(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "TransactionID")
    val transactionID: Int = 0,

    @ColumnInfo(name = "UserWhoPaidID")
    val userWhoPaidID: Int,

    @ColumnInfo(name = "TransactionAmount")
    val transactionAmount: Double,

    @ColumnInfo(name = "TransactionDetails")
    val transactionDetails: String,

    @ColumnInfo(name = "TransactionDate")
    val transactionDate: Date,

    @ColumnInfo(name = "SplitPercentage")
    val splitPercentage: Float,

    @ColumnInfo(name = "Paid")
    val paid: Boolean,

    @ColumnInfo(name = "BudgetTags")
    val budgetTags: List<String>
)

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("""
        SELECT * 
        FROM transactions 
        WHERE TransactionID = :id
    """)
    suspend fun getTransactionById(id: Int): Transaction?

    @Query("""
    SELECT *
    FROM transactions
    """)
    suspend fun getAllTransactions(): List<Transaction>

    @Query("""
    SELECT *
    FROM transactions
    WHERE UserWhoPaidID = :userId
    """)
    suspend fun getTransactionsByUser(userId: Int): List<Transaction>
}

class Converters {
    @TypeConverter
    fun fromBudgetTagsList(budgetTags: List<String>?): String {
        return budgetTags?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toBudgetTagsList(budgetTagsString: String): List<String> {
        return if (budgetTagsString.isEmpty()) {
            emptyList()
        } else {
            budgetTagsString.split(",")
        }
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

}