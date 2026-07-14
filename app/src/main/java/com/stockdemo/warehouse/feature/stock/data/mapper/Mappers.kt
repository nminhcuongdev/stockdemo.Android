package com.stockdemo.warehouse.feature.stock.data.mapper

import com.stockdemo.warehouse.feature.auth.data.repository.UserDto
import com.stockdemo.warehouse.feature.stock.data.local.DeliveryOrderEntity
import com.stockdemo.warehouse.feature.stock.data.local.LocationEntity
import com.stockdemo.warehouse.feature.stock.data.local.PendingStockInEntity
import com.stockdemo.warehouse.feature.stock.data.local.PendingStockOutEntity
import com.stockdemo.warehouse.feature.stock.data.local.ProductEntity
import com.stockdemo.warehouse.feature.stock.data.local.StockEntity
import com.stockdemo.warehouse.feature.auth.domain.model.User
import com.stockdemo.warehouse.feature.stock.data.remote.DeliveryOrderDto
import com.stockdemo.warehouse.feature.stock.data.remote.LocationDto
import com.stockdemo.warehouse.feature.stock.data.remote.LowStockItemDto
import com.stockdemo.warehouse.feature.stock.data.remote.ProductDto
import com.stockdemo.warehouse.feature.stock.data.remote.StockDto
import com.stockdemo.warehouse.feature.stock.data.remote.StockInDto
import com.stockdemo.warehouse.feature.stock.data.remote.StockMovementReportDto
import com.stockdemo.warehouse.feature.stock.data.remote.StockMovementReportItemDto
import com.stockdemo.warehouse.feature.stock.data.remote.StockOutDto
import com.stockdemo.warehouse.feature.stock.data.remote.StockTakeDto
import com.stockdemo.warehouse.feature.stock.data.remote.StockTakeItemDto
import com.stockdemo.warehouse.feature.stock.domain.model.Location
import com.stockdemo.warehouse.feature.stock.domain.model.LowStockItem
import com.stockdemo.warehouse.feature.stock.domain.model.DeliveryOrder
import com.stockdemo.warehouse.feature.stock.domain.model.Product
import com.stockdemo.warehouse.feature.stock.domain.model.Stock
import com.stockdemo.warehouse.feature.stock.domain.model.StockIn
import com.stockdemo.warehouse.feature.stock.domain.model.StockMovementReport
import com.stockdemo.warehouse.feature.stock.domain.model.StockMovementReportItem
import com.stockdemo.warehouse.feature.stock.domain.model.StockOut
import com.stockdemo.warehouse.feature.stock.domain.model.StockTake
import com.stockdemo.warehouse.feature.stock.domain.model.StockTakeItem
import com.stockdemo.warehouse.feature.stock.domain.model.StockInRequest
import com.stockdemo.warehouse.feature.stock.domain.model.UpdateQuantityRequest

fun UserDto.toDomain(): User = User(
    userId = userId,
    username = username,
    fullName = fullName,
    role = role,
    isActive = isActive,
    createdDate = createdDate,
    lastLoginDate = lastLoginDate
)

fun ProductDto.toDomain(): Product = Product(
    productId = productId,
    productCode = productCode,
    productName = productName,
    description = description,
    unit = unit,
    isActive = isActive,
    createdDate = createdDate,
    updatedDate = updatedDate
)

fun ProductDto.toEntity(): ProductEntity = ProductEntity(
    productId = productId,
    productCode = productCode,
    productName = productName,
    description = description,
    unit = unit,
    isActive = isActive,
    createdDate = createdDate,
    updatedDate = updatedDate
)

fun ProductEntity.toDomain(): Product = Product(
    productId = productId,
    productCode = productCode,
    productName = productName,
    description = description,
    unit = unit,
    isActive = isActive,
    createdDate = createdDate,
    updatedDate = updatedDate
)

fun LocationDto.toDomain(): Location = Location(
    locationId = locationId,
    locationCode = locationCode,
    locationName = locationName,
    isActive = isActive,
    createdDate = createdDate,
    updatedDate = updatedDate
)

fun LocationDto.toEntity(): LocationEntity = LocationEntity(
    locationId = locationId,
    locationCode = locationCode,
    locationName = locationName,
    isActive = isActive,
    createdDate = createdDate,
    updatedDate = updatedDate
)

fun LocationEntity.toDomain(): Location = Location(
    locationId = locationId,
    locationCode = locationCode,
    locationName = locationName,
    isActive = isActive,
    createdDate = createdDate,
    updatedDate = updatedDate
)

fun StockDto.toDomain(): Stock = Stock(
    stockId = stockId,
    productId = productId,
    locationId = locationId,
    quantity = quantity,
    qrCode = qrCode,
    lastUpdated = lastUpdated,
    product = product?.toDomain(),
    location = location?.toDomain()
)

fun StockDto.toEntity(): StockEntity = StockEntity(
    stockId = stockId,
    productId = productId,
    locationId = locationId,
    quantity = quantity,
    qrCode = qrCode,
    lastUpdated = lastUpdated
)

fun StockEntity.toDomain(): Stock = Stock(
    stockId = stockId,
    productId = productId,
    locationId = locationId,
    quantity = quantity,
    qrCode = qrCode,
    lastUpdated = lastUpdated,
    product = null,
    location = null
)

fun StockEntity.toDomain(product: Product?, location: Location?): Stock = Stock(
    stockId = stockId,
    productId = productId,
    locationId = locationId,
    quantity = quantity,
    qrCode = qrCode,
    lastUpdated = lastUpdated,
    product = product,
    location = location
)

fun DeliveryOrderDto.toDomain(): DeliveryOrder = DeliveryOrder(
    deliveryOrderId = deliveryOrderId,
    poNumber = poNumber,
    productId = productId,
    quantity = quantity,
    qrCode = qrCode,
    status = status,
    deliveryDate = deliveryDate,
    createdDate = createdDate,
    updatedDate = updatedDate,
    product = product?.toDomain()
)

fun DeliveryOrderDto.toEntity(): DeliveryOrderEntity = DeliveryOrderEntity(
    deliveryOrderId = deliveryOrderId,
    productId = productId,
    poNumber = poNumber,
    deliveryDate = deliveryDate,
    quantity = quantity,
    qrCode = qrCode,
    createdDate = createdDate,
    updatedDate = updatedDate,
    status = status
)

fun DeliveryOrderEntity.toDomain(product: Product?): DeliveryOrder = DeliveryOrder(
    deliveryOrderId = deliveryOrderId,
    productId = productId,
    poNumber = poNumber,
    quantity = quantity,
    qrCode = qrCode,
    status = status,
    deliveryDate = deliveryDate,
    createdDate = createdDate,
    updatedDate = updatedDate,
    product = product
)

fun StockInDto.toDomain(): StockIn = StockIn(
    stockInId = stockInId,
    stockInCode = stockInCode,
    productId = productId,
    product = product?.let {
        com.stockdemo.warehouse.feature.stock.domain.model.Product(
            productId = it.productId,
            productCode = it.productCode,
            productName = it.productName,
            description = it.description,
            unit = it.unit,
            isActive = it.isActive,
            createdDate = it.createdDate,
            updatedDate = it.updatedDate
        )
    },
    locationId = locationId,
    location = location?.toDomain(),
    quantity = quantity,
    qrCode = qrCode,
    createdBy = createdBy,
    user = user?.toDomain(),
    createdDate = createdDate
)

fun StockOutDto.toDomain(): StockOut = StockOut(
    stockOutId = stockOutId,
    stockOutCode = stockOutCode,
    productId = productId,
    locationId = locationId,
    quantity = quantity,
    qrCode = qrCode,
    createdBy = createdBy,
    createdDate = createdDate,
    product = product?.let {
        com.stockdemo.warehouse.feature.stock.domain.model.Product(
            productId = it.productId,
            productCode = it.productCode,
            productName = it.productName,
            description = it.description,
            unit = it.unit,
            isActive = it.isActive,
            createdDate = it.createdDate,
            updatedDate = it.updatedDate
        )
    },
    location = location?.toDomain(),
    user = user?.toDomain()
)

fun StockInRequest.toPendingEntity(): PendingStockInEntity = PendingStockInEntity(
    productId = productId,
    locationId = locationId,
    qrCode = qrCode,
    quantity = quantity,
    userId = userId,
    createdAt = System.currentTimeMillis()
)

fun LowStockItemDto.toDomain(): LowStockItem = LowStockItem(
    productId = productId,
    productCode = product?.productCode ?: "",
    productName = product?.productName ?: "",
    unit = product?.unit ?: "",
    currentQuantity = currentQuantity,
    minQuantity = minQuantity,
    shortage = shortage
)

fun StockTakeItemDto.toDomain(): StockTakeItem = StockTakeItem(
    productId = productId,
    productName = product?.productName ?: "",
    systemQuantity = systemQuantity,
    countedQuantity = countedQuantity,
    variance = variance
)

fun StockTakeDto.toDomain(): StockTake = StockTake(
    stockTakeId = stockTakeId,
    status = status,
    items = items?.map { it.toDomain() } ?: emptyList()
)

fun StockMovementReportItemDto.toDomain(): StockMovementReportItem = StockMovementReportItem(
    productId = productId,
    productCode = product?.productCode ?: "",
    productName = product?.productName ?: "",
    unit = product?.unit ?: "",
    totalIn = totalIn,
    totalOut = totalOut,
    currentStock = currentStock
)

fun StockMovementReportDto.toDomain(): StockMovementReport = StockMovementReport(
    totalIn = totalIn,
    totalOut = totalOut,
    totalStock = totalStock,
    items = items?.map { it.toDomain() } ?: emptyList()
)

fun UpdateQuantityRequest.toPendingEntity(stockId: Int): PendingStockOutEntity = PendingStockOutEntity(
    stockId = stockId,
    quantity = quantity,
    createdBy = createdBy,
    createdAt = System.currentTimeMillis()
)



