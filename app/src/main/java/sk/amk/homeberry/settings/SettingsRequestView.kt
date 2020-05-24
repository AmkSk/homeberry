package sk.amk.homeberry.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.textfield.TextInputEditText
import sk.amk.homeberry.R
import sk.amk.homeberry.model.HomeberryRequest

/**
 * @author Andrej Martinák <andrej.martinak@eman.cz>
 */
object SettingsRequestView {

    fun create(
            activity: SettingsActivity,
            viewModel: SettingsViewModel,
            request: HomeberryRequest
    ): View {
        val layoutInflater = LayoutInflater.from(activity)
        val view = layoutInflater.inflate(R.layout.layout_request_item, null, false)
        val layoutAppPackage = view.findViewById<ViewGroup>(R.id.layoutAppPackage)
        val editTextName = view.findViewById<TextInputEditText>(R.id.editTextName)
        val editTextEndpointUrl = view.findViewById<TextInputEditText>(R.id.editTextEndpointUrl)
        val editTextAppPackage = view.findViewById<TextInputEditText>(R.id.editTextAppPackage)
        val txtEndpointUrl = view.findViewById<TextView>(R.id.textFullUrl)
        val switch = view.findViewById<Switch>(R.id.switchAppLaunch)

        switch.setOnCheckedChangeListener { _, checked ->
            request.openApp = checked
            layoutAppPackage.isVisible = checked
            viewModel.updateRequest(request)
        }

        editTextName.setText(request.name)
        editTextEndpointUrl.setText(request.endpoint)
        switch.isChecked = request.openApp
        editTextAppPackage.setText(request.openAppPackageName)

        editTextName.addTextChangedListener {
            request.name = it.toString()
            viewModel.updateRequest(request)
        }

        editTextEndpointUrl.addTextChangedListener {
            txtEndpointUrl.text = "${viewModel.baseUrl.value}/${it.toString()}"
            request.endpoint = it.toString()
            viewModel.updateRequest(request)
        }

        viewModel.baseUrl.observe(activity, Observer {
            txtEndpointUrl.text = "$it/${editTextEndpointUrl.text}"
        })

        editTextAppPackage.addTextChangedListener {
            request.openAppPackageName = it.toString()
            viewModel.updateRequest(request)
        }

        view.findViewById<ImageButton>(R.id.buttonDelete).setOnClickListener {
            MaterialDialog(activity).show {
                title(res = R.string.are_you_sure)
                message(res = R.string.settings_delete_dialog_message)
                positiveButton(res = R.string.yes) { viewModel.deleteRequest(request) }
                negativeButton(res = R.string.no)
            }
        }

        return view
    }
}