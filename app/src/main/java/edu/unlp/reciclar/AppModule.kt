package edu.unlp.reciclar

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.unlp.reciclar.data.local.AppDatabase
import edu.unlp.reciclar.data.local.DatabaseSeeder
import edu.unlp.reciclar.data.local.dao.CanjeDao
import edu.unlp.reciclar.data.local.dao.CuponDao
import edu.unlp.reciclar.data.local.dao.LogroDao
import edu.unlp.reciclar.data.local.dao.ReporteDao
import edu.unlp.reciclar.data.local.dao.ResiduoDao
import edu.unlp.reciclar.data.local.dao.UsuarioDao
import edu.unlp.reciclar.data.remote.ApiService
import edu.unlp.reciclar.data.remote.AuthAuthenticator
import edu.unlp.reciclar.data.remote.AuthInterceptor
import edu.unlp.reciclar.data.repository.AuthRepository
import edu.unlp.reciclar.data.repository.EstacionesRepository
import edu.unlp.reciclar.data.repository.RankingRepository
import edu.unlp.reciclar.data.repository.ReportesRepository
import edu.unlp.reciclar.data.repository.ResiduosRepository
import edu.unlp.reciclar.data.repository.UserRepository
import edu.unlp.reciclar.data.service.LogroService
import edu.unlp.reciclar.data.source.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ── Network ──────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): AuthInterceptor {
        return AuthInterceptor(sessionManager)
    }

    @Provides
    @Singleton
    fun provideAuthAuthenticator(
        sessionManager: SessionManager,
        apiService: dagger.Lazy<ApiService>
    ): AuthAuthenticator {
        return AuthAuthenticator(sessionManager, apiServiceProvider = { apiService.get() })
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        authAuthenticator: AuthAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(authAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    // ── Database ─────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .addCallback(DatabaseSeeder.callback)
            .build()
    }

    @Provides
    fun provideUsuarioDao(database: AppDatabase): UsuarioDao = database.usuarioDao()

    @Provides
    fun provideCanjeDao(database: AppDatabase): CanjeDao = database.canjeDao()

    @Provides
    fun provideCuponDao(database: AppDatabase): CuponDao = database.cuponDao()

    @Provides
    fun provideLogroDao(database: AppDatabase): LogroDao = database.logroDao()

    @Provides
    fun provideReporteDao(database: AppDatabase): ReporteDao = database.reporteDao()

    @Provides
    fun provideResiduoDao(database: AppDatabase): ResiduoDao = database.residuoDao()

    // ── Repositories ─────────────────────────────────────────

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService, usuarioDao: UsuarioDao): UserRepository {
        return UserRepository(apiService, usuarioDao)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: ApiService,
        sessionManager: SessionManager,
        userRepository: UserRepository
    ): AuthRepository {
        return AuthRepository(apiService, sessionManager, userRepository)
    }

    @Provides
    @Singleton
    fun provideRankingRepository(apiService: ApiService): RankingRepository {
        return RankingRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideResiduosRepository(apiService: ApiService, logroService: LogroService, userRepository: UserRepository, residuosDao: ResiduoDao): ResiduosRepository {
        return ResiduosRepository(apiService, logroService, userRepository, residuosDao)
    }

    @Provides
    @Singleton
    fun provideReportesRepository(userRepository: UserRepository, reporteDao: ReporteDao): ReportesRepository {
        return ReportesRepository(userRepository, reporteDao)
    }

    @Provides
    @Singleton
    fun provideEstacionesRepository(apiService: ApiService): EstacionesRepository {
        return EstacionesRepository(apiService)
    }
}