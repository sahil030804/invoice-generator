package com.kjbilling.app.data.repository

import com.kjbilling.app.data.db.dao.CustomerDao
import com.kjbilling.app.data.db.entity.CustomerEntity
import com.kjbilling.app.domain.model.Customer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CustomerRepository(private val dao: CustomerDao) {

    fun getAll(): Flow<List<Customer>> {
        return dao.getAll().map { list -> list.map { it.toDomain() } }
    }

    fun search(query: String): Flow<List<Customer>> {
        return dao.search(query).map { list -> list.map { it.toDomain() } }
    }

    suspend fun getById(id: Long): Customer? {
        return dao.getById(id)?.toDomain()
    }

    suspend fun save(customer: Customer): Long {
        return if (customer.id == 0L) {
            dao.insert(CustomerEntity.fromDomain(customer))
        } else {
            dao.update(CustomerEntity.fromDomain(customer))
            customer.id
        }
    }

    suspend fun delete(customer: Customer) {
        dao.delete(CustomerEntity.fromDomain(customer))
    }

    suspend fun markUsed(id: Long) {
        dao.updateLastUsedAt(id, System.currentTimeMillis())
    }
}
