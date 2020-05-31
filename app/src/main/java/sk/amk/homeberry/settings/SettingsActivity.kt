package sk.amk.homeberry.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.content_settings.*
import sk.amk.homeberry.R
import sk.amk.homeberry.model.HomeberryRequest

class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        setTitle(R.string.settings_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.updateUi.observe(this, Observer {
            layoutEndpointsContainer.removeAllViews()
            viewModel.requests.forEach {
                layoutEndpointsContainer.addView(
                        SettingsRequestView.create(this, viewModel, it)
                )
            }
            editTextBaseUrl.setText(viewModel.baseUrl.value)
        })

        viewModel.errorState.observe(this, Observer { state ->
            when (state) {
                SettingsErrorSate.ERROR_IMPORT_INVALID_JSON -> showInvalidJsonFileErrorDialog()
                SettingsErrorSate.ERROR_IMPORT_INVALID_CONFIG -> showInvalidConfigErrorDialog()
            }
        })

        editTextBaseUrl.setText(viewModel.baseUrl.value)
        editTextBaseUrl.addTextChangedListener {
            viewModel.updateBaseUrl(it.toString())
        }

        fab.setOnClickListener {
            val request = HomeberryRequest()
            viewModel.createNewRequest(request)
            layoutEndpointsContainer.addView(
                    SettingsRequestView.create(this, viewModel, request)
            )
            scrollViewSettings.post {
                scrollViewSettings.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.menu_settings_export -> exportSettings()
            R.id.menu_settings_import -> openFileChooser()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_FILE_PICKER && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                val content = contentResolver.openInputStream(uri)
                        ?.bufferedReader().use { it?.readText() }
                if (content == null) {
                    showInvalidJsonFileErrorDialog()
                } else {
                    viewModel.importConfig(content)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun exportSettings() {
        val uri = ConfigFileProvider.createFileUri(this, viewModel.createConfigJson())

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = MIME_TYPE_JSON
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.settings_export_message)))
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = MIME_TYPE_JSON
        }
        startActivityForResult(intent, REQUEST_FILE_PICKER)
    }

    private fun showInvalidJsonFileErrorDialog() {
        MaterialDialog(this).show {
            title(res = R.string.oops)
            message(res = R.string.settings_error_import_invalid_json)
            positiveButton(res = R.string.ok)
        }
    }

    private fun showInvalidConfigErrorDialog() {
        MaterialDialog(this).show {
            title(res = R.string.oops)
            message(res = R.string.settings_error_import_invalid_config)
            positiveButton(res = R.string.ok)
        }
    }

    companion object {
        private const val REQUEST_FILE_PICKER = 13
        private const val MIME_TYPE_JSON = "application/json"
    }
}
