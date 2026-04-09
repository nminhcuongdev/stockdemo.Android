package com.example.stockdemo.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.stockdemo.data.local.StockDao
import com.example.stockdemo.data.local.StockDatabase
import com.example.stockdemo.data.local.UserPreferences
import com.example.stockdemo.data.remote.dto.ApiService
import com.example.stockdemo.data.remote.dto.PythonAPIService
import com.example.stockdemo.data.repository.ChatRepositoryImpl
import com.example.stockdemo.data.repository.StockRepositoryImpl
import com.example.stockdemo.data.repository.UserRepositoryImpl
import com.example.stockdemo.domain.repository.ChatRepository
import com.example.stockdemo.domain.repository.StockRepository
import com.example.stockdemo.domain.repository.UserRepository
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
            .baseUrl("http://10.84.30.46:8686/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    @Named("PythonRetrofit")
    fun providePythonRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.84.30.46:8000/")
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
    fun providePythonAPIService(@Named("PythonRetrofit") retrofit: Retrofit): PythonAPIService {
        return retrofit.create(PythonAPIService::class.java)
    }

    @Provides
    @Singleton
    fun provideStockDatabase(app: Application): StockDatabase {
        return Room.databaseBuilder(
            app,
            StockDatabase::class.java,
            "stock_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideStockDao(db: StockDatabase): StockDao {
        return db.stockDao
    }

    @Provides
    @Singleton
    fun provideStockRepository(api: ApiService): StockRepository {
        return StockRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideUserRepository(api: ApiService): UserRepository {
        return UserRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideChatRepository(api: PythonAPIService): ChatRepository {
        return ChatRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }
}