package sk.amk.homeberry.main

import sk.amk.homeberry.model.HomeberryRequest

/**
 * @author Andrej Martinák <andrej.martinak@gmail.com>
 */
sealed class MainState(val message: String = "", val request: HomeberryRequest? = null) {
    class RequestInProgress(request: HomeberryRequest) : MainState(request = request)
    class RequestSuccess(message: String, request: HomeberryRequest) : MainState(message, request)
    class RequestFailure(message: String, request: HomeberryRequest) : MainState(message, request)
    class RequestFailureConnection(request: HomeberryRequest) : MainState(request = request)
    class RequestCancelled : MainState()
}