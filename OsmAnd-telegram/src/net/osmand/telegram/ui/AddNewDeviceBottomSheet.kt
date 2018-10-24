package net.osmand.telegram.ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import net.osmand.telegram.R
import net.osmand.telegram.ui.views.BottomSheetDialog


class AddNewDeviceBottomSheet : BaseDialogFragment() {

	override fun onCreateDialog(savedInstanceState: Bundle?) = BottomSheetDialog(context!!)

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val mainView = inflater.inflate(R.layout.add_new_device_dialog, container, false)

		dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

		mainView.findViewById<View>(R.id.scroll_view_container).setOnClickListener { dismiss() }

		BottomSheetBehavior.from(mainView.findViewById<View>(R.id.scroll_view))
			.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

				override fun onStateChanged(bottomSheet: View, newState: Int) {
					if (newState == BottomSheetBehavior.STATE_HIDDEN) {
						dismiss()
					}
				}

				override fun onSlide(bottomSheet: View, slideOffset: Float) {}
			})

		mainView.findViewById<TextView>(R.id.secondary_btn).apply {
			setText(R.string.shared_string_cancel)
			setOnClickListener { dismiss() }
		}

		mainView.findViewById<TextView>(R.id.primary_btn).apply {
			setText(R.string.add_device)
			setOnClickListener {
				val input = mainView.findViewById<EditText>(R.id.edit_text)
				val text = input.text
				if (text.isEmpty() || text.isBlank()) {
					input.setError("Device name cannot be empty")
				} else if (text.length > 200) {
					input.setError("Device name is too long")
				} else  {
					targetFragment?.also { target ->
						target.onActivityResult(
							targetRequestCode,
							NEW_DEVICE_REQUEST_CODE,
							Intent().putExtra("deviceName", text.toString())
						)
					}
					dismiss()
				}

			}
		}

		return mainView
	}

	companion object {

		const val NEW_DEVICE_REQUEST_CODE = 5

		private const val TAG = "DisableSharingBottomSheet"

		fun showInstance(fm: FragmentManager, target: Fragment ): Boolean {
			return try {
				AddNewDeviceBottomSheet().apply {
					setTargetFragment(target, NEW_DEVICE_REQUEST_CODE)
					show(fm, TAG)
				}
				true
			} catch (e: RuntimeException) {
				false
			}
		}
	}
}