package com.example.stockdemo.feature.stock.data.local

import com.example.stockdemo.feature.stock.data.mapper.toDomain
import com.example.stockdemo.feature.stock.domain.model.DeliveryOrder
import com.example.stockdemo.feature.stock.domain.model.Location
import com.example.stockdemo.feature.stock.domain.model.Product
import com.example.stockdemo.feature.stock.domain.model.Stock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull

@Singleton
class StockLocalDataSource @Inject constructor(
    private val stockDao: StockDao
) {

    suspend fun getCachedStocks(): List<Stock> {
        val stockEntities = stockDao.getAllStocks().firstOrNull().orEmpty()
        val productMap = stockDao.getProducts().associateBy { it.productId }
        val locationMap = stockDao.getLocations().associateBy { it.locationId }

        return stockEntities.map { entity ->
            entity.toDomain(
                product = productMap[entity.productId]?.toDomain(),
                location = locationMap[entity.locationId]?.toDomain()
            )
        }
    }

    suspend fun cacheStocks(stocks: List<StockEntity>) {
        stockDao.insertStocks(stocks)
    }

    suspend fun cacheProducts(products: List<ProductEntity>) {
        if (products.isNotEmpty()) {
            stockDao.insertProducts(products)
        }
    }

    suspend fun cacheProduct(product: ProductEntity) {
        stockDao.insertProducts(listOf(product))
    }

    suspend fun replaceProducts(products: List<ProductEntity>) {
        stockDao.clearProducts()
        stockDao.insertProducts(products)
    }

    suspend fun hasProducts(): Boolean = stockDao.getProducts().isNotEmpty()

    suspend fun replaceLocations(locations: List<LocationEntity>) {
        stockDao.clearLocations()
        stockDao.insertLocations(locations)
    }

    suspend fun cacheLocation(location: LocationEntity) {
        stockDao.insertLocation(location)
    }

    suspend fun cacheLocations(locations: List<LocationEntity>) {
        if (locations.isNotEmpty()) {
            stockDao.insertLocations(locations)
        }
    }

    suspend fun replaceDeliveryOrders(items: List<DeliveryOrderEntity>) {
        stockDao.clearDeliveryOrders()
        stockDao.insertDeliveryOrders(items)
    }

    suspend fun getProductByCodes(codes: List<String>): Product? {
        return codes.firstNotNullOfOrNull { code ->
            stockDao.getProductByCode(code)?.toDomain()
        }
    }

    suspend fun getStockByQrCode(qrCode: String): Stock? {
        val entity = stockDao.getStockByQrCode(qrCode) ?: return null
        val product = stockDao.getProductById(entity.productId)?.toDomain()
        val location = stockDao.getLocations()
            .firstOrNull { it.locationId == entity.locationId }
            ?.toDomain()

        return entity.toDomain(product = product, location = location)
    }

    suspend fun getDeliveryOrderByQrCode(qrCode: String): DeliveryOrder? {
        val order = stockDao.getDeliveryOrderByQrCode(qrCode) ?: return null
        val product = stockDao.getProductById(order.productId)?.toDomain()
        return order.toDomain(product)
    }

    suspend fun getLocationByQrCode(qrCode: String): Location? {
        return stockDao.getLocationByCode(qrCode)?.toDomain()
    }

    suspend fun insertPendingStockIn(item: PendingStockInEntity) {
        stockDao.insertPendingStockIn(item)
    }

    suspend fun getPendingStockIns(): List<PendingStockInEntity> = stockDao.getPendingStockIns()

    suspend fun deletePendingStockIn(pendingId: Long) {
        stockDao.deletePendingStockIn(pendingId)
    }

    suspend fun markPendingStockInFailed(pendingId: Long, error: String) {
        stockDao.markPendingStockInFailed(pendingId, error)
    }

    suspend fun insertPendingStockOut(item: PendingStockOutEntity) {
        stockDao.insertPendingStockOut(item)
    }

    suspend fun getPendingStockOuts(): List<PendingStockOutEntity> = stockDao.getPendingStockOuts()

    suspend fun deletePendingStockOut(pendingId: Long) {
        stockDao.deletePendingStockOut(pendingId)
    }

    suspend fun markPendingStockOutFailed(pendingId: Long, error: String) {
        stockDao.markPendingStockOutFailed(pendingId, error)
    }
}
