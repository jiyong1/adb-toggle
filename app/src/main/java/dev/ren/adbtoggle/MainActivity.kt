package dev.ren.adbtoggle

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

/** 권한 안내, USB 디버깅 스위치, 위젯 추가 버튼이 있는 설정 화면. */
class MainActivity : Activity() {

    private lateinit var usbRow: View
    private lateinit var usbSwitch: Switch
    private lateinit var usbState: TextView
    private lateinit var permissionTitle: TextView
    private lateinit var permissionGuide: View

    private val handler = Handler(Looper.getMainLooper())
    private val recheckPermission = Runnable {
        if (UsbDebugging.hasPermission(this)) refresh() else waitForPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usbRow = findViewById(R.id.usb_row)
        usbSwitch = findViewById(R.id.usb_switch)
        usbState = findViewById(R.id.usb_state)
        permissionTitle = findViewById(R.id.permission_title)
        permissionGuide = findViewById(R.id.permission_guide)

        usbRow.setOnClickListener {
            UsbDebugging.toggle(this)
            refresh()
        }

        val command = UsbDebugging.grantCommand(this)
        findViewById<TextView>(R.id.grant_command).text = command
        findViewById<View>(R.id.copy_command).setOnClickListener { copy(command) }

        val widgets = getSystemService(AppWidgetManager::class.java)
        findViewById<View>(R.id.add_widget).apply {
            visibility = if (widgets.isRequestPinAppWidgetSupported) View.VISIBLE else View.GONE
            setOnClickListener {
                widgets.requestPinAppWidget(ComponentName(context, UsbDebugWidget::class.java), null, null)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(recheckPermission)
    }

    /** 화면과 위젯을 실제 상태에 맞춘다. */
    private fun refresh() {
        val permitted = UsbDebugging.hasPermission(this)
        val on = UsbDebugging.isEnabled(this)

        usbSwitch.isChecked = on
        usbState.setText(if (on) R.string.state_on else R.string.state_off)
        usbRow.isEnabled = permitted
        usbRow.alpha = if (permitted) 1f else 0.4f

        permissionTitle.setText(if (permitted) R.string.permission_granted else R.string.permission_required)
        permissionGuide.visibility = if (permitted) View.GONE else View.VISIBLE

        UsbDebugWidget.updateAll(this)
        if (!permitted) waitForPermission()
    }

    /** 권한은 PC에서 부여되어 앱이 알림을 받을 수 없으므로, 화면이 떠 있는 동안 1초마다 확인한다. */
    private fun waitForPermission() {
        handler.removeCallbacks(recheckPermission)
        handler.postDelayed(recheckPermission, 1_000)
    }

    private fun copy(text: String) {
        getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText(text, text))
        // Android 13부터는 시스템이 복사 확인을 직접 보여준다.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show()
        }
    }
}
