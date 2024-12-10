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
    primaryKeys = ["transactionID", "userWhoPaidID"],
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
    @ColumnInfo(name = "transactionID")
    val transactionID: Int,

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
    var paid: Boolean,

    @ColumnInfo(name = "BudgetTag")
    val budgetTag: String,

    @ColumnInfo(name = "AmountOwed", defaultValue = "0.0") // Default value for new rows
    val amountOwed: Double = 0.0,

    @ColumnInfo(name = "InitialUser")
    val initialUser: Boolean = false
)
@Entity(
    tableName = "groups",
    primaryKeys = ["groupID", "userID"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userID"],
            onDelete = ForeignKey.CASCADE // Deletes groups when the user is deleted
        )
    ],
    indices = [
        Index(value = ["userID", "group_name"], unique = true)
    ]
)
data class Group(
    @ColumnInfo(name = "groupID")
    val groupID: Int = 0,

    @ColumnInfo(name = "userID")
    val userID: Int,

    @ColumnInfo(name = "group_name")
    val groupName: String,

    @ColumnInfo(name = "date_created")
    val dateCreated: Date
)

@Entity(tableName = "Budget")
data class Budget(
    @PrimaryKey(autoGenerate = true) val budgetID : Int,
    val userID : Int,
    val selectedBudget : String,
    val currentAmount : Long
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

    @Query("SELECT userName FROM User WHERE userId NOT IN (:userId, :groupUserIds)")
    suspend fun getUsersNotInGroup(userId: Int, groupUserIds: List<Int>): List<String>

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

    @Query("SELECT MAX(TransactionID) FROM transactions")
    suspend fun getMaxTransactionId(): Int?

    @Query("SELECT * FROM transactions WHERE BudgetTag = :tag")
    suspend fun getTransactionsByBudgetTag(tag: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE TransactionID = :id")
    suspend fun getTransactionById(id: Int): Transaction?

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE UserWhoPaidID = :userId")
    suspend fun getTransactionsByUser(userId: Int): List<Transaction>

    @Query("SELECT * FROM transactions WHERE UserWhoPaidID = :userId AND Paid = 0")
    suspend fun getAllUnpaidTransactionsForUser(userId: Int): List<Transaction>
}

@Dao
interface GroupDao {
    @Insert
    suspend fun insertGroup(group: Group): Long

    @Query("SELECT * FROM groups WHERE userID = :userID")
    suspend fun getGroupsByUser(userID: Int): List<Group>

    @Query("SELECT u.userName FROM groups g JOIN User u ON g.userID = u.userId WHERE g.groupID = :groupId")
    suspend fun getGroupMembersByGroupId(groupId: Int): List<String>

    @Query("SELECT groupID FROM groups WHERE userID = :userID AND group_name = :groupName")
    suspend fun getGroupIdByName(userID: Int, groupName: String): Int?

    @Query("SELECT DISTINCT group_name FROM groups WHERE userID = :userID")
    suspend fun getGroupNamesByUser(userID: Int): List<String>

    @Query("SELECT * FROM groups WHERE userID = :userID AND group_name = :groupName")
    fun getGroupByUserNameAndGroupName(userID: Int, groupName: String): Group?

    @Query("SELECT * FROM groups WHERE groupID = :groupId")
    suspend fun getGroupsByGroupId(groupId: Int): List<Group>

    @Query("SELECT MAX(groupID) FROM groups")
    suspend fun getMaxGroupId(): Int?

    @Delete
    suspend fun deleteGroup(group: Group)
}

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)
}

// Type converters for complex types
class Converters {

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}


@Database(entities = [User::class, Transaction::class, Group::class, Budget::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun groupDao(): GroupDao
    abstract fun budgetDao(): BudgetDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // This will delete the old DB and recreate a new one
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
