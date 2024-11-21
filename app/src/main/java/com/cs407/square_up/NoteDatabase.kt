package com.cs407.square_up

import androidx.room.*
import java.util.Date

// Type converter for Date
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
//
//// Entity for Group
//@Entity(
//    tableName = "groups",
////    foreignKeys = [
////        ForeignKey(
////            entity = User::class,
////            parentColumns = ["userID"],
////            childColumns = ["userID"],
////            onDelete = ForeignKey.CASCADE
////        )
////    ],
//    indices = [Index(value = ["userID"])] // Index for foreign key
//)
//
//
//data class Group(
//    @PrimaryKey(autoGenerate = true) val groupID: Int = 0,
//    @ColumnInfo(name = "userID") val userID: Int,
//    @ColumnInfo(name = "group_name") val groupName: String,
//    @ColumnInfo(name = "date_created") val dateCreated: Date
//)

//@Entity(
//    tableName = "groups",
//    indices = [Index(value = ["userID"])] // Keep the index for userID
//)
//data class Group(
//    @PrimaryKey(autoGenerate = true) val groupID: Int = 0,
//    @ColumnInfo(name = "userID") val userID: Int,
//    @ColumnInfo(name = "group_name") val groupName: String,
//    @ColumnInfo(name = "date_created") val dateCreated: Date
//)

@Entity(
    tableName = "groups",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userID"],
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



//// User Entity (required as a foreign key reference for Group)
//@Entity(tableName = "users")
//data class User(
//    @PrimaryKey(autoGenerate = true) val userID: Int = 0,
//    @ColumnInfo(name = "name") val name: String,
//    @ColumnInfo(name = "email") val email: String,
//    @ColumnInfo(name = "username") val username: String,
//    @ColumnInfo(name = "password") val password: String,
//    @ColumnInfo(name = "notifications") val notifications: Boolean
//)

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)] // Ensure unique usernames
)
data class User(
    @PrimaryKey(autoGenerate = true) val userID: Int = 0,
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "email") val email: String = "",
    @ColumnInfo(name = "username") val username: String = "",
    @ColumnInfo(name = "password") val password: String = "",
    @ColumnInfo(name = "notifications") val notifications: Boolean = false
)


// Data Access Object (DAO) for Group
@Dao
interface GroupDao {
    @Insert
    suspend fun insertGroup(group: Group): Long

    @Query("SELECT * FROM groups WHERE userID = :userID")
    suspend fun getGroupsByUser(userID: Int): List<Group>

    @Delete
    suspend fun deleteGroup(group: Group)
}

// Room Database including Group and User entities and DAOs
@Database(entities = [Group::class, User::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    // Add other DAOs as needed
}




