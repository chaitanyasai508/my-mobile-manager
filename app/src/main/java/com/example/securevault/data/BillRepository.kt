package com.example.securevault.data

import com.example.securevault.crypto.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Base64

class BillRepository(
    private val billDao: BillDao,
    private val cryptoManager: CryptoManager
) {

    data class DomainBill(
        val id: Int,
        val billName: String,
        val amount: String,
        val notes: String,
        val dueDate: Long,
        val frequency: String
    )

    val domainBills: Flow<List<DomainBill>> = billDao.getAllBills().map { entities ->
        entities.map { entity ->
            val amount = decryptField(entity.amountIv, entity.encryptedAmount)
            val notes = decryptField(entity.notesIv, entity.encryptedNotes)
            DomainBill(entity.id, entity.billName, amount, notes, entity.dueDate, entity.frequency)
        }
    }

    suspend fun getBill(id: Int): DomainBill? {
        val entity = billDao.getBillById(id) ?: return null
        val amount = decryptField(entity.amountIv, entity.encryptedAmount)
        val notes = decryptField(entity.notesIv, entity.encryptedNotes)
        return DomainBill(entity.id, entity.billName, amount, notes, entity.dueDate, entity.frequency)
    }

    suspend fun insertBill(billName: String, amount: String, notes: String, dueDate: Long, frequency: String) {
        val entity = encryptBill(billName, amount, notes, dueDate, frequency)
        billDao.insertBill(entity)
    }

    suspend fun updateBill(id: Int, billName: String, amount: String, notes: String, dueDate: Long, frequency: String) {
        val entity = encryptBill(billName, amount, notes, dueDate, frequency).copy(id = id)
        billDao.updateBill(entity)
    }

    suspend fun deleteBill(id: Int) {
        val entity = billDao.getBillById(id)
        if (entity != null) {
            billDao.deleteBill(entity)
        }
    }

    suspend fun deleteAll() {
        billDao.deleteAll()
    }

    private fun encryptBill(billName: String, amount: String, notes: String, dueDate: Long, frequency: String): Bill {
        val (amountIv, encryptedAmount) = cryptoManager.encrypt(amount.toByteArray())
        val (notesIv, encryptedNotes) = cryptoManager.encrypt(notes.toByteArray())

        return Bill(
            billName = billName,
            amountIv = Base64.getEncoder().encodeToString(amountIv),
            encryptedAmount = Base64.getEncoder().encodeToString(encryptedAmount),
            notesIv = Base64.getEncoder().encodeToString(notesIv),
            encryptedNotes = Base64.getEncoder().encodeToString(encryptedNotes),
            dueDate = dueDate,
            frequency = frequency
        )
    }

    private fun decryptField(ivBase64: String, encryptedBase64: String): String {
        return cryptoManager.decrypt(
            Base64.getDecoder().decode(ivBase64),
            Base64.getDecoder().decode(encryptedBase64)
        ).toString(Charsets.UTF_8)
    }
}
