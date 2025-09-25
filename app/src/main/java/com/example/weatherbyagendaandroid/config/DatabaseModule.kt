package com.example.weatherbyagendaandroid.config

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherbyagendaandroid.dao.CityDao
import com.example.weatherbyagendaandroid.dao.entites.City
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(entities = [City::class], version = 1, exportSchema = false)
abstract class CityDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesCityDatabase(@ApplicationContext context: Context): CityDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            CityDatabase::class.java,
            "us_cities.db"
        )
        .createFromAsset("databases/us_cities.db")
        .build()
}