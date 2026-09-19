package com.kjbilling.app.data.repository

import com.kjbilling.app.data.db.dao.ProductDao
import com.kjbilling.app.data.db.entity.ProductEntity
import com.kjbilling.app.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(private val dao: ProductDao) {

    fun getAll(): Flow<List<Product>> {
        return dao.getAll().map { list -> list.map { it.toDomain() } }
    }

    fun search(query: String): Flow<List<Product>> {
        return dao.search(query).map { list -> list.map { it.toDomain() } }
    }

    suspend fun getById(id: Long): Product? {
        return dao.getById(id)?.toDomain()
    }

    suspend fun save(product: Product): Long {
        return if (product.id == 0L) {
            dao.insert(ProductEntity.fromDomain(product))
        } else {
            dao.update(ProductEntity.fromDomain(product))
            product.id
        }
    }

    suspend fun delete(product: Product) {
        dao.delete(ProductEntity.fromDomain(product))
    }

    suspend fun incrementUseCount(id: Long) {
        dao.incrementUseCount(id, System.currentTimeMillis())
    }
}
