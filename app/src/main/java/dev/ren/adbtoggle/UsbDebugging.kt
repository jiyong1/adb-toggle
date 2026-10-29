package dev.ren.adbtoggle

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings

/**
 * Settings.Global 의 USB 디버깅 스위치(adb_enabled)를 읽고 쓴다.
 *
 * 쓰기에는 WRITE_SECURE_SETTINGS 가 필요하고, 이 권한은 adb 로만 부여할 수 있다.
 */
object UsbDebugging {

    fun hasPermission(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) ==
            PackageManager.PERMISSION_GRANTED

    fun isEnabled(context: Context): Boolean = isOn(context, Settings.Global.ADB_ENABLED)

    /** 현재 실제 값을 뒤집는다. 권한이 없어 쓰지 못하면 false. */
    fun toggle(context: Context): Boolean = setEnabled(context, !isEnabled(context))

    fun grantCommand(context: Context): String =
        "adb shell pm grant ${context.packageName} ${Manifest.permission.WRITE_SECURE_SETTINGS}"

    private fun setEnabled(context: Context, enabled: Boolean): Boolean = try {
        // USB 디버깅은 개발자 옵션의 하위 설정이라, 개발자 옵션이 꺼진 채로 켜면 설정 앱과 상태가 어긋난다.
        if (enabled && !isOn(context, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED)) {
            put(context, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, true)
        }
        put(context, Settings.Global.ADB_ENABLED, enabled)
    } catch (e: SecurityException) {
        false
    }

    private fun isOn(context: Context, key: String): Boolean =
        Settings.Global.getInt(context.contentResolver, key, 0) != 0

    private fun put(context: Context, key: String, on: Boolean): Boolean =
        Settings.Global.putInt(context.contentResolver, key, if (on) 1 else 0)
}
