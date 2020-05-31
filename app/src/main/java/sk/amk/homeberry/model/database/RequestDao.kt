package sk.amk.homeberry.model.database

import androidx.lifecycle.LiveData
import androidx.room.*
import sk.amk.homeberry.model.HomeberryRequest

/**
 * @author Andrej Martinák <andrej.martinak@gmail.com>
 */
@Dao
interface RequestDao {
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
