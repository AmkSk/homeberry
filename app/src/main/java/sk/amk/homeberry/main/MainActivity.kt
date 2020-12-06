package sk.amk.homeberry.main

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.materialdialogs.customview.customView
import kotlinx.android.synthetic.main.activity_main.buttonContainer
import kotlinx.android.synthetic.main.activity_main.buttonOpenSettings
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.activity_main.txtEmptyState
import sk.amk.homeberry.R
import sk.amk.homeberry.model.HomeberryRequest
import sk.amk.homeberry.settings.SettingsActivity

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var progressDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        title = ""

        viewModel.requests.observe(this, this::observeRequests)
        viewModel.state.observe(this, this::observeVmState)

        buttonOpenSettings.setOnClickListener { openSettings() }
    }

    override fun onResume() {
        super.onResume()

        intent.dataString?.toLong()?.let {
            viewModel.callRequest(it)
        }
    }

    override fun onPause() {
        progressDialog?.dismiss()
        viewModel.cancelRequest()
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_main_power -> showShutDownDialog()
            R.id.menu_main_restart -> showRebootDialog()
            R.id.menu_main_settings -> openSettings()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun observeVmState(state: MainState){
        if (state !is MainState.RequestInProgress) {
            progressDialog?.dismiss()
        }

        when (state) {
            is MainState.RequestInProgress -> showProgressDialog(state.request!!)
            is MainState.RequestSuccess -> handleSuccess(state.message, state.request!!)
            is MainState.RequestFailure -> handleError(state.message, state.request!!)
            is MainState.RequestFailureConnection -> showConnectionErrorDialog()
        }
    }

    private fun observeRequests(requests: List<HomeberryRequest>) {
        txtEmptyState.isVisible = requests.isEmpty()
        buttonOpenSettings.isVisible = requests.isEmpty()

        if (requests.isNotEmpty()) {
            generateButtons(requests)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            createShortcuts(requests)
        }
    }

    private fun generateButtons(requests: List<HomeberryRequest>) {
        for (request in requests) {
            val button = Button(this)
            button.text = request.name
            button.setOnClickListener { viewModel.callRequest(request) }
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val buttonMargin = calculateButtonMargin()
            params.setMargins(buttonMargin, 0, buttonMargin, buttonMargin)
            button.layoutParams = params
            buttonContainer.addView(button)
        }
    }

    private fun calculateButtonMargin(): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            BUTTON_MARGIN_DP,
            resources.displayMetrics
        ).toInt()
    }

    private fun showRebootDialog() {
        MaterialDialog(this).show {
            title(res = R.string.are_you_sure)
            message(res = R.string.main_reboot_dialog_message)
            positiveButton(res = R.string.yes) {
                viewModel.callRequest(HomeberryRequest("Reboot", "reboot"))
            }
            negativeButton(res = R.string.cancel)
        }
    }

    private fun showShutDownDialog() {
        MaterialDialog(this).show {
            title(res = R.string.are_you_sure)
            message(res = R.string.main_shutdown_dialog_message)
            positiveButton(res = R.string.yes) {
                viewModel.callRequest(HomeberryRequest("Shutdown", "shutdown"))
            }
            negativeButton(res = R.string.cancel)
        }
    }

    private fun showProgressDialog(request: HomeberryRequest) {
        val progressView = layoutInflater.inflate(R.layout.dialog_progress, null, false)
        progressView.findViewById<TextView>(R.id.txtDialogMessage).text =
            getString(R.string.main_progress_message, "${viewModel.baseUrl}/${request.endpoint}")
        progressDialog = MaterialDialog(this)
            .customView(view = progressView)
            .cancelOnTouchOutside(false)
            .onCancel { viewModel.cancelRequest() }

        progressDialog?.show()
    }

    private fun handleSuccess(message: String, request: HomeberryRequest) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()

        if (request.openApp) {
            val appOpened = openApp(this, request.openAppPackageName)

            if (appOpened.not()) {
                Toast.makeText(
                    this,
                    getString(R.string.error_app_not_found, request.openAppPackageName),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // close when app launched from shortcut
        if (intent.dataString != null) {
            finish()
        }
    }

    private fun handleError(errorMessage: String, request: HomeberryRequest) {
        MaterialDialog(this).show {
            title(text = getString(R.string.error_request_call_title, request.name))
            message(text = errorMessage)
            positiveButton(res = R.string.ok)
        }
    }

    private fun showConnectionErrorDialog() {
        MaterialDialog(this).show {
            title(res = R.string.error_request_connection_title)
            message(text = getString(R.string.error_request_connection_message, viewModel.baseUrl))
            positiveButton(res = R.string.ok)
        }
    }

    private fun openSettings() {
        viewModel.cancelRequest()
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createShortcuts(requests: List<HomeberryRequest>) {
        val shortcutManager = getSystemService(ShortcutManager::class.java)
        shortcutManager.removeAllDynamicShortcuts()

        val shortcuts: MutableList<ShortcutInfo> = mutableListOf()

        val shortcutsCount = if (requests.size >= MAX_DYNAMIC_SHORTCUTS_COUNT) {
            MAX_DYNAMIC_SHORTCUTS_COUNT
        } else {
            requests.size
        }

        for (i in 0 until shortcutsCount) {
            val request = requests[i]

            val shortcut = ShortcutInfo.Builder(this, request.id.toString())
                .setShortLabel(request.name)
                .setLongLabel(request.name)
                .setIcon(Icon.createWithResource(this, R.drawable.ic_icons8_raspberry_pi))
                .setIntent(
                    Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                        action = Intent.ACTION_VIEW
                        data = Uri.parse(request.id.toString())
                    })
                .build()

            shortcuts.add(shortcut)
        }

        shortcutManager!!.dynamicShortcuts = shortcuts
    }

    /** Open another app.
     * @param context current Context, like Activity, App, or Service
     * @param packageName the full package name of the app to open
     * @return true if likely successful, false if unsuccessful
     */
    private fun openApp(context: Context, packageName: String): Boolean {
        val manager = context.packageManager
        try {
            val intent = manager.getLaunchIntentForPackage(packageName) ?: return false
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            context.startActivity(intent)
            return true
        } catch (e: ActivityNotFoundException) {
            return false
        }
    }

    companion object {
        const val BUTTON_MARGIN_DP = 16f
        const val MAX_DYNAMIC_SHORTCUTS_COUNT = 4
    }
}
