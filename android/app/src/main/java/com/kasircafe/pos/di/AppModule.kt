package com.kasircafe.pos.di

import android.content.Context
import androidx.room.Room
import com.kasircafe.pos.data.api.PosApiService
import com.kasircafe.pos.data.database.AppDatabase
import com.kasircafe.pos.data.local.SessionDataStore
import com.kasircafe.pos.data.network.AuthInterceptor
import com.kasircafe.pos.data.repository.AuthRepository
import com.kasircafe.pos.data.repository.AuditRepository
import com.kasircafe.pos.data.repository.DashboardRepository
import com.kasircafe.pos.data.repository.ProductRepository
import com.kasircafe.pos.data.repository.TransactionRepository
import com.kasircafe.pos.data.repository.impl.AuthRepositoryImpl
import com.kasircafe.pos.data.repository.impl.AuditRepositoryImpl
import com.kasircafe.pos.data.repository.impl.DashboardRepositoryImpl
import com.kasircafe.pos.data.repository.impl.ProductRepositoryImpl
import com.kasircafe.pos.data.repository.impl.TransactionRepositoryImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideHttpClient(authInterceptor: AuthInterceptor): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(baseUrl: String, client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): PosApiService = retrofit.create(PosApiService::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "kasir_cafe.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideProductDao(db: AppDatabase) = db.productDao()

    @Provides
    fun provideTransactionDao(db: AppDatabase) = db.transactionDao()

    @Provides
    fun providePendingTransactionDao(db: AppDatabase) = db.pendingTransactionDao()

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
    fun provideAuditRepository(api: PosApiService): AuditRepository =
        AuditRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideTransactionRepository(api: PosApiService, db: AppDatabase, moshi: Moshi): TransactionRepository =
        TransactionRepositoryImpl(
            api = api,
            dao = db.transactionDao(),
            pendingDao = db.pendingTransactionDao(),
            moshi = moshi
        )
}
