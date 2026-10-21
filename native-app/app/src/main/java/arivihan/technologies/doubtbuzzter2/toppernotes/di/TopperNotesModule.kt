package arivihan.technologies.doubtbuzzter2.toppernotes.di

import arivihan.technologies.doubtbuzzter2.toppernotes.data.remote.TopperNotesApi
import arivihan.technologies.doubtbuzzter2.toppernotes.data.repository.TopperNotesRepositoryImpl
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.repository.TopperNotesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TopperNotesModule {

    @Provides
    @Singleton
    fun provideTopperNotesApi(retrofit: Retrofit): TopperNotesApi =
        retrofit.create(TopperNotesApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TopperNotesBindings {

    @Binds
    abstract fun bindTopperNotesRepository(
        impl: TopperNotesRepositoryImpl
    ): TopperNotesRepository
}
