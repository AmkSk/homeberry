package sk.amk.homeberry.model.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import sk.amk.homeberry.model.HomeberryRequest

/**
 * @author Andrej Martinák <andrej.martinak@gmail.com>
 */
@Dao
interface RequestDao {
    @Query("SELECT * FROM homeberryrequest WHERE id = :id")
    fun getById(id: Long): HomeberryRequest

    @Query("SELECT * FROM homeberryrequest")
    fun getAll(): List<HomeberryRequest>

    @Query("SELECT * FROM homeberryrequest")
    fun getAllLiveData(): LiveData<List<HomeberryRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(request: HomeberryRequest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(requests: List<HomeberryRequest>)

    @Delete
    fun delete(request: HomeberryRequest)

    @Query("DELETE FROM homeberryrequest")
    fun deleteAll()
}
