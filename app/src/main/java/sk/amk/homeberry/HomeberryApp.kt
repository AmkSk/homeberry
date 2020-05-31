package sk.amk.homeberry

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import sk.amk.homeberry.model.database.HomeberryDatabase

/**
 * @author Andrej Martinák <andrej.martinak@gmail.com>
 */
class HomeberryApp : Application() {

    lateinit var db: HomeberryDatabase
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(
                applicationContext,
                HomeberryDatabase::class.java,
                "homeberryDatabase"
        ).build()

        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val SHARED_PREFERENCES_NAME = "homeberyPreferences"
        const val DEFAULT_BASE_URL = "192.168.1.1:8080"
        const val BASE_URL_KEY = "baseUrl"
    }
}
