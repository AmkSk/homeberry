package sk.amk.homeberry.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.features.logging.ANDROID
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sk.amk.homeberry.HomeberryApp
import sk.amk.homeberry.model.HomeberryRequest
import java.net.ConnectException

/**
 * @author Andrej Martinák <andrej.martinak@gmail.com>
 */
class MainViewModel(val app: Application) : AndroidViewModel(app) {

    val state: MutableLiveData<MainState> = MutableLiveData()

    val requests = (app as HomeberryApp).db.requestDao().getAllLiveData()
    val baseUrl = (app as HomeberryApp).sharedPreferences.getString(
        HomeberryApp.BASE_URL_KEY,
        HomeberryApp.DEFAULT_BASE_URL
    )!!
    private var lastRunningJob: Job? = null
    private val httpClient = HttpClient {
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.ALL
        }
    }

    fun callRequest(request: HomeberryRequest) {
        state.postValue(MainState.RequestInProgress(request))
        lastRunningJob?.cancel()

        lastRunningJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    var url = ""

                    if (!baseUrl.contains("http") && !baseUrl.contains("https")) {
                        url += "http://"
                    }

                    url += "$baseUrl/${request.endpoint}"
                    val response = httpClient.get<String>(urlString = url)
                    state.postValue(MainState.RequestSuccess(response, request))
                } catch (exception: Exception) {
                    handleEndpointError(exception, request)
                }
            }
        }
    }

    fun cancelRequest() {
        lastRunningJob?.cancel()
    }

    private fun handleEndpointError(exception: Exception, request: HomeberryRequest) {
        Log.e("HomeBerry request error", exception.toString())
        val errorState = when (exception) {
            is ConnectException -> MainState.RequestFailureConnection(request)
            is CancellationException -> MainState.RequestCancelled()
            else -> MainState.RequestFailure(exception.message!!, request)
        }
        state.postValue(errorState)
    }

    override fun onCleared() {
        httpClient.close()
        lastRunningJob?.cancel()
        super.onCleared()
    }
}
