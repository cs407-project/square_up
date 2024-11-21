package com.cs407.square_up
import androidx.room.Entity
import androidx.room.PrimaryKey

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

//@Database(entities = [User::class], version = 1)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun userDao(): UserDao
//}

@Entity(tableName = "Budget")
data class Budget(
    @PrimaryKey(autoGenerate = true) val budgetID : Int,
    val userID : Int,
    val selectedBudget : String,
    val currentAmount : Long
)

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)
}

//database = Room.databaseBuilder(
//    applicationContext,
//    AppDatabase::class.java,
//    "my_database"
//).build()

