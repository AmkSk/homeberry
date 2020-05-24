package sk.amk.homeberry.model

/**
 * Configuration that is exported/imported to JSON
 *
 * @author Andrej Martinák <andrej.martinak@gmail.com>
 */
class Config(var baseUrl: String, var requests: List<HomeberryRequest>)