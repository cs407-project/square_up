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
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userWhoPaidID"], // Corrected to match the field name
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userWhoPaidID"])] // Corrected to match the field name
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "TransactionID")
    val transactionID: Int = 0,

    @ColumnInfo(name = "userWhoPaidID")
    val userWhoPaidID: Int,

    @ColumnInfo(name = "TransactionAmount")
    val transactionAmount: Double,

    @ColumnInfo(name = "TransactionDetails")
    val transactionDetails: String,

    @ColumnInfo(name = "TransactionDate")
    val transactionDate: Date,

    @ColumnInfo(name = "SplitPercentage")
    val splitPercentage: Double,

    @ColumnInfo(name = "Paid")
    val paid: Boolean,

    @ColumnInfo(name = "BudgetTags")
    val budgetTags: List<String> // Ensure type converters are configured correctly
)
@Entity(
    tableName = "groups",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userID"],
            onDelete = ForeignKey.CASCADE // Deletes groups when the user is deleted
        )
    ],
    indices = [Index(value = ["userID"])] // Optimize lookups by userID
)
data class Group(
    @PrimaryKey(autoGenerate = true) val groupID: Int = 0,
    @ColumnInfo(name = "userID") val userID: Int,
    @ColumnInfo(name = "group_name") val groupName: String,
    @ColumnInfo(name = "date_created") val dateCreated: Date
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

    @Query("SELECT * FROM User WHERE userName = :username ")
    suspend fun getUserByName(username: String): User?

    @Query("SELECT * FROM User WHERE userName = :username AND password = :password")
    suspend fun getUserByCredentials(username: String, password: String): User?

    @Query("SELECT * FROM User WHERE userId = :id")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT userName FROM User WHERE userId != :userId")
    suspend fun getAllOtherUsers(userId: Int): List<String>

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

@Dao
interface GroupDao {
    @Insert
    suspend fun insertGroup(group: Group): Long
    @Query("SELECT * FROM groups WHERE userID = :userID")
    suspend fun getGroupsByUser(userID: Int): List<Group>
    @Delete
    suspend fun deleteGroup(group: Group)
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


@Database(entities = [User::class, Transaction::class, Group::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun groupDao(): GroupDao
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
