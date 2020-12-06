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
    suspend fun getById(id: Long): HomeberryRequest

    @Query("SELECT * FROM homeberryrequest")
    suspend fun getAll(): List<HomeberryRequest>

    @Query("SELECT * FROM homeberryrequest")
    fun getAllLiveData(): LiveData<List<HomeberryRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: HomeberryRequest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(requests: List<HomeberryRequest>)

    @Delete
    suspend fun delete(request: HomeberryRequest)

    @Query("DELETE FROM homeberryrequest")
    suspend fun deleteAll()
}
