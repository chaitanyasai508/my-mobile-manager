package com.example.securevault.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY dueDate ASC")
    fun getAllBills(): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getBillById(id: Int): Bill?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill)

    @Update
    suspend fun updateBill(bill: Bill)

    @Delete
    suspend fun deleteBill(bill: Bill)
    
    @Query("DELETE FROM bills")
    suspend fun deleteAll()
}
