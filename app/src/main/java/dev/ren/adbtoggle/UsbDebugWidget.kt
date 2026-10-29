package dev.ren.adbtoggle

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/** 누르면 USB 디버깅을 켜고 끄는 홈 화면 위젯. */
class UsbDebugWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        updateAll(context)
    }

    override fun onDisabled(context: Context) {
        UsbDebugWatcher.stop(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE) {
            UsbDebugging.toggle(context)
            // 쓰기에 실패했다면 권한이 사라진 것이므로 '권한 필요' 상태로 다시 그려진다.
            updateAll(context)
        }
    }

    companion object {
        private const val ACTION_TOGGLE = "dev.ren.adbtoggle.action.TOGGLE"

        /** 놓여 있는 모든 위젯을 실제 상태로 다시 그린다. */
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, UsbDebugWidget::class.java))
            if (ids.isEmpty()) return
            manager.updateAppWidget(ids, render(context))
            // 위젯이 있는 동안은 설정 앱 등에서 바꾼 상태도 따라가도록 감시를 (다시) 건다.
            UsbDebugWatcher.start(context)
        }

        /**
         * 켜짐/꺼짐은 레이아웃 자체를 바꿔서 표현한다. 색이 리소스로 남아 있어야
         * 런처가 그릴 때 다크 모드와 배경화면 색을 따라간다.
         */
        private fun render(context: Context): RemoteViews {
            val permitted = UsbDebugging.hasPermission(context)
            val on = UsbDebugging.isEnabled(context)
            val state = when {
                !permitted -> R.string.state_need_permission
                on -> R.string.state_on
                else -> R.string.state_off
            }
            return RemoteViews(context.packageName, if (on) R.layout.widget_on else R.layout.widget_off).apply {
                setTextViewText(R.id.state, context.getString(state))
                // 권한이 없으면 토글 대신 안내 화면을 연다.
                setOnClickPendingIntent(
                    android.R.id.background,
                    if (permitted) toggleIntent(context) else openAppIntent(context),
                )
            }
        }

        private fun toggleIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, UsbDebugWidget::class.java).setAction(ACTION_TOGGLE),
            PendingIntent.FLAG_IMMUTABLE,
        )

        private fun openAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
