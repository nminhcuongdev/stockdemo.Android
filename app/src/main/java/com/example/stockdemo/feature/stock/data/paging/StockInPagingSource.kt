package com.example.stockdemo.feature.stock.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.stockdemo.feature.stock.data.mapper.toDomain
import com.example.stockdemo.feature.stock.data.remote.StockRemoteDataSource
import com.example.stockdemo.feature.stock.domain.model.StockIn
import retrofit2.HttpException
import java.io.IOException

class StockInPagingSource(
    private val remoteDataSource: StockRemoteDataSource
) : PagingSource<Int, StockIn>() {

    override fun getRefreshKey(state: PagingState<Int, StockIn>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StockIn> {
        val position = params.key ?: 1
        return try {
            val response = remoteDataSource.getStockInHistory(
                pageNumber = position,
                pageSize = params.loadSize
            )

            if (response.success && response.data != null) {
                val data = response.data
                val items = data.items.map { it.toDomain() }
                
                LoadResult.Page(
                    data = items,
                    prevKey = if (position == 1) null else position - 1,
                    nextKey = if (items.isEmpty() || position >= data.totalPages) null else position + 1
                )
            } else {
                LoadResult.Error(Exception(response.message))
            }
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }
}



