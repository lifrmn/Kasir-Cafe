package com.kasircafe.pos.di

import android.content.Context
import androidx.room.Room
import com.kasircafe.pos.data.api.PosApiService
import com.kasircafe.pos.data.database.AppDatabase
import com.kasircafe.pos.data.local.SessionDataStore
import com.kasircafe.pos.data.network.AuthInterceptor
import com.kasircafe.pos.data.repository.AuthRepository
import com.kasircafe.pos.data.repository.DashboardRepository
import com.kasircafe.pos.data.repository.ProductRepository
import com.kasircafe.pos.data.repository.TransactionRepository
import com.kasircafe.pos.data.repository.impl.AuthRepositoryImpl
import com.kasircafe.pos.data.repository.impl.DashboardRepositoryImpl
import com.kasircafe.pos.data.repository.impl.ProductRepositoryImpl
import com.kasircafe.pos.data.repository.impl.TransactionRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideApiBaseUrl(): String = "http://10.0.2.2:8080"

    @Provides
    @Singleton
    fun provideHttpClient(authInterceptor: AuthInterceptor): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(baseUrl: String, client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): PosApiService = retrofit.create(PosApiService::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "kasir_cafe.db").build()

    @Provides
    fun provideProductDao(db: AppDatabase) = db.productDao()

    @Provides
    fun provideTransactionDao(db: AppDatabase) = db.transactionDao()

    @Provides
    @Singleton
    fun provideProductRepository(api: PosApiService, db: AppDatabase): ProductRepository =
        ProductRepositoryImpl(api = api, dao = db.productDao())

    @Provides
    @Singleton
    fun provideAuthRepository(api: PosApiService, sessionDataStore: SessionDataStore): AuthRepository =
        AuthRepositoryImpl(api, sessionDataStore)

    @Provides
    @Singleton
    fun provideDashboardRepository(api: PosApiService): DashboardRepository =
        DashboardRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideTransactionRepository(api: PosApiService, db: AppDatabase): TransactionRepository =
        TransactionRepositoryImpl(api = api, dao = db.transactionDao())
}
