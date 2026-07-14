package com.stockdemo.warehouse.feature.stock.data.mapper

import com.stockdemo.warehouse.feature.stock.data.remote.LocationDto
import com.stockdemo.warehouse.feature.stock.data.remote.LowStockItemDto
import com.stockdemo.warehouse.feature.stock.data.remote.ProductDto
import com.stockdemo.warehouse.feature.stock.data.remote.StockDto
import com.stockdemo.warehouse.feature.stock.domain.model.StockInRequest
import com.stockdemo.warehouse.feature.stock.domain.model.UpdateQuantityRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MappersTest {

    private fun productDto() = ProductDto(
        productId = 11,
        productCode = "P-001",
        productName = "Carton",
        description = "Carton box",
        unit = "pcs",
        isActive = true,
        createdDate = "2026-04-23T00:00:00",
        updatedDate = null
    )

    private fun locationDto() = LocationDto(
        locationId = 21,
        locationCode = "A-01",
        locationName = "Shelf A-01",
        isActive = true,
        createdDate = "2026-04-23T00:00:00",
        updatedDate = null
    )

    @Test
    fun `ProductDto toDomain maps every field`() {
        val domain = productDto().toDomain()

        assertEquals(11, domain.productId)
        assertEquals("P-001", domain.productCode)
        assertEquals("Carton", domain.productName)
        assertEquals("pcs", domain.unit)
        assertEquals(true, domain.isActive)
    }

    @Test
    fun `ProductDto toEntity then toDomain is a round trip`() {
        val original = productDto()

        val roundTripped = original.toEntity().toDomain()

        assertEquals(original.toDomain(), roundTripped)
    }

    @Test
    fun `StockDto toDomain includes nested product and location`() {
        val dto = StockDto(
            stockId = 1,
            productId = 11,
            locationId = 21,
            quantity = 8,
            qrCode = "QR-001",
            lastUpdated = "2026-04-23T00:00:00",
            product = productDto(),
            location = locationDto()
        )

        val domain = dto.toDomain()

        assertEquals("QR-001", domain.qrCode)
        assertEquals("Carton", domain.product?.productName)
        assertEquals("Shelf A-01", domain.location?.locationName)
    }

    @Test
    fun `StockDto toDomain tolerates null product and location`() {
        val dto = StockDto(
            stockId = 1,
            productId = 11,
            locationId = 21,
            quantity = 8,
            qrCode = "QR-001",
            lastUpdated = "2026-04-23T00:00:00",
            product = null,
            location = null
        )

        val domain = dto.toDomain()

        assertNull(domain.product)
        assertNull(domain.location)
    }

    @Test
    fun `StockEntity toDomain drops relations that are not joined`() {
        val stock = StockDto(
            stockId = 1,
            productId = 11,
            locationId = 21,
            quantity = 8,
            qrCode = "QR-001",
            lastUpdated = "2026-04-23T00:00:00",
            product = productDto(),
            location = locationDto()
        ).toEntity()

        val domain = stock.toDomain()

        assertEquals(1, domain.stockId)
        assertNull(domain.product)
        assertNull(domain.location)
    }

    @Test
    fun `LowStockItemDto toDomain falls back to empty strings when product is null`() {
        val dto = LowStockItemDto(
            productId = 11,
            product = null,
            currentQuantity = 2,
            minQuantity = 10,
            maxQuantity = 50,
            shortage = 8
        )

        val domain = dto.toDomain()

        assertEquals(11, domain.productId)
        assertEquals("", domain.productCode)
        assertEquals("", domain.productName)
        assertEquals("", domain.unit)
        assertEquals(8, domain.shortage)
    }

    @Test
    fun `LowStockItemDto toDomain uses product fields when present`() {
        val dto = LowStockItemDto(
            productId = 11,
            product = productDto(),
            currentQuantity = 2,
            minQuantity = 10,
            maxQuantity = 50,
            shortage = 8
        )

        val domain = dto.toDomain()

        assertEquals("P-001", domain.productCode)
        assertEquals("Carton", domain.productName)
        assertEquals("pcs", domain.unit)
    }

    @Test
    fun `StockInRequest toPendingEntity copies request fields`() {
        val request = StockInRequest(
            locationId = 21,
            productId = 11,
            qrCode = "QR-001",
            quantity = 5,
            userId = 99
        )

        val entity = request.toPendingEntity()

        assertEquals(21, entity.locationId)
        assertEquals(11, entity.productId)
        assertEquals("QR-001", entity.qrCode)
        assertEquals(5, entity.quantity)
        assertEquals(99, entity.userId)
    }

    @Test
    fun `UpdateQuantityRequest toPendingEntity binds the given stock id`() {
        val request = UpdateQuantityRequest(quantity = 3, createdBy = 99)

        val entity = request.toPendingEntity(stockId = 10)

        assertEquals(10, entity.stockId)
        assertEquals(3, entity.quantity)
        assertEquals(99, entity.createdBy)
    }
}
