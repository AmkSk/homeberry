package sk.amk.homeberry.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import sk.amk.homeberry.HomeberryApp
import sk.amk.homeberry.HomeberryApp.Companion.BASE_URL_KEY
import sk.amk.homeberry.HomeberryApp.Companion.DEFAULT_BASE_URL
import sk.amk.homeberry.model.Config
import sk.amk.homeberry.model.HomeberryRequest

/**
 * @author Andrej Martinák <andrej.martinak@gmail.com>
 */
class SettingsViewModel(val app: Application) : AndroidViewModel(app) {

    private val db = (app as HomeberryApp).db
    var baseUrl = MutableLiveData<String>()
    var requests: MutableList<HomeberryRequest> = mutableListOf()
    var updateUi = MutableLiveData<Boolean>()
    val errorState = MutableLiveData<SettingsErrorSate>()

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    init {
        baseUrl.value = (app as HomeberryApp).sharedPreferences.getString(
            BASE_URL_KEY,
            DEFAULT_BASE_URL
        )!!

        viewModelScope.launch {
            requests = db.requestDao().getAll().toMutableList()
            updateUi.value = true
        }
    }

    fun updateBaseUrl(url: String) {
        baseUrl.value = url

        (app as HomeberryApp).sharedPreferences
            .edit()
            .putString(BASE_URL_KEY, url)
            .apply()
    }

    fun createNewRequest(request: HomeberryRequest) {
        request.id = requests.size.toLong()
        requests.add(request)
    }

    fun updateRequest(request: HomeberryRequest) {
        val index = this.requests.indexOfFirst { it.id == request.id }
        this.requests[index] = request

        viewModelScope.launch {
            db.requestDao().insert(request)
        }
    }

    fun deleteRequest(request: HomeberryRequest) {
        viewModelScope.launch {
            db.requestDao().delete(request)
            requests.remove(request)
            updateUi.value = true
        }
    }

    fun createConfigJson(): String {
        val config = Config(baseUrl.value!!, requests)

        val jsonAdapter: JsonAdapter<Config> = moshi.adapter(Config::class.java).indent("    ")
        return jsonAdapter.toJson(config)
    }

    fun importConfig(configJson: String) {
        val jsonAdapter: JsonAdapter<Config> = moshi.adapter(Config::class.java)
        try {
            jsonAdapter.fromJson(configJson)?.run {
                this@SettingsViewModel.requests = requests.toMutableList()
                viewModelScope.launch {
                    db.requestDao().deleteAll()
                    db.requestDao().insertAll(requests)
                }
                updateBaseUrl(this.baseUrl)
                updateUi.value = true
            }
        } catch (invalidConfigException: JsonDataException) {
            errorState.value = SettingsErrorSate.ERROR_IMPORT_INVALID_CONFIG
        } catch (invalidJsonException: JsonEncodingException) {
            errorState.value = SettingsErrorSate.ERROR_IMPORT_INVALID_JSON
        }
    }
}
