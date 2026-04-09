package com.example.stockdemo.data.mapper

import com.example.stockdemo.data.remote.dto.*
import com.example.stockdemo.domain.model.location.Location
import com.example.stockdemo.domain.model.order.DeliveryOrder
import com.example.stockdemo.domain.model.product.Product
import com.example.stockdemo.domain.model.stock.Stock
import com.example.stockdemo.domain.model.stock.StockIn
import com.example.stockdemo.domain.model.stock.StockOut
import com.example.stockdemo.domain.model.user.User

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

fun LocationDto.toDomain(): Location = Location(
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

fun StockInDto.toDomain(): StockIn = StockIn(
    stockInId = stockInId,
    stockInCode = stockInCode,
    productId = productId,
    product = product?.let {
        com.example.stockdemo.domain.model.stock.Product(
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
        com.example.stockdemo.domain.model.stock.Product(
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
