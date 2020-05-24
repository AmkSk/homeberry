package sk.amk.homeberry.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import sk.amk.homeberry.model.HomeberryRequest

/**
 * @author Andrej Martinák <andrej.martinak@gmail.com>
 */
@Database(entities = [HomeberryRequest::class], version = 1)
abstract class HomeberryDatabase : RoomDatabase() {
    abstract fun requestDao(): RequestDao
}