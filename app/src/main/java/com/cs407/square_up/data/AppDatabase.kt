package com.cs407.square_up.data

import android.content.Context
import androidx.room.*
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

// User entity
@Entity(
    indices = [Index(value = ["userName"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val userName: String = "",
    val password: String = "",
    val email: String = "",
    val notifications: Boolean = false
)

// Transaction entity
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = User::class, // Reference to User entity
            parentColumns = ["userId"], // Column in User entity
            childColumns = ["UserWhoPaidID"], // Corresponding column in Transaction
            onDelete = ForeignKey.CASCADE // Cascade delete when User is deleted
        )
    ],
    indices = [Index(value = ["UserWhoPaidID"])] // Index for faster queries
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

// Define the UserDao inline
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM User WHERE userName = :username AND password = :password")
    suspend fun getUserByCredentials(username: String, password: String): User?


    @Query("SELECT * FROM User WHERE userId = :id")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM User")
    suspend fun getAllUsers(): List<User>
}

// DAO for Transaction
@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE TransactionID = :id")
    suspend fun getTransactionById(id: Int): Transaction?

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE UserWhoPaidID = :userId")
    suspend fun getTransactionsByUser(userId: Int): List<Transaction>
}

// Type converters for complex types
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


@Database(entities = [User::class, Transaction::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
