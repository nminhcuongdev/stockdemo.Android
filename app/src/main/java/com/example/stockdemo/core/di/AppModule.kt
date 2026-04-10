package com.example.stockdemo.core.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.stockdemo.BuildConfig
import com.example.stockdemo.feature.stock.data.local.StockDao
import com.example.stockdemo.feature.stock.data.local.StockDatabase
import com.example.stockdemo.feature.auth.data.local.UserPreferences
import com.example.stockdemo.feature.stock.data.remote.ApiService
import com.example.stockdemo.feature.chat.data.remote.PythonApiService
import com.example.stockdemo.feature.chat.data.repository.ChatRepositoryImpl
import com.example.stockdemo.feature.stock.data.repository.StockRepositoryImpl
import com.example.stockdemo.feature.auth.data.repository.UserRepositoryImpl
import com.example.stockdemo.feature.chat.domain.repository.ChatRepository
import com.example.stockdemo.feature.stock.domain.repository.StockRepository
import com.example.stockdemo.feature.auth.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(logging: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @Named("StockRetrofit")
    fun provideStockRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.STOCK_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    @Named("PythonRetrofit")
    fun providePythonRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.PYTHON_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(@Named("StockRetrofit") retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePythonApiService(@Named("PythonRetrofit") retrofit: Retrofit): PythonApiService {
        return retrofit.create(PythonApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideStockDatabase(app: Application): StockDatabase {
        return Room.databaseBuilder(
            app,
            StockDatabase::class.java,
            "stock_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideStockDao(db: StockDatabase): StockDao {
        return db.stockDao
    }

    @Provides
    @Singleton
    fun provideStockRepository(
        @ApplicationContext context: Context,
        api: ApiService,
        stockDao: StockDao
    ): StockRepository {
        return StockRepositoryImpl(context, api, stockDao)
    }

    @Provides
    @Singleton
    fun provideUserRepository(api: ApiService): UserRepository {
        return UserRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideChatRepository(api: PythonApiService): ChatRepository {
        return ChatRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }
}


