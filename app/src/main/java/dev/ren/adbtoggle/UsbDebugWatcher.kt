package dev.ren.adbtoggle

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.provider.Settings

/**
 * 설정 앱 등 위젯 밖에서 USB 디버깅을 바꿔도 위젯이 따라가게 한다.
 *
 * 상주 서비스 없이 JobScheduler 콘텐츠 트리거로 adb_enabled 변경을 기다린다.
 * 트리거는 한 번 울리면 끝나므로 실행될 때마다 다시 건다.
 */
class UsbDebugWatcher : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        // 다시 그리면서 감시도 다시 건다. 이 잡이 끝나기 전에 걸어야 그 사이의 변경을 놓치지 않는다.
        UsbDebugWidget.updateAll(this)
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean = false

    companion object {
        private const val JOB_ID = 1

        fun start(context: Context) {
            val adbEnabled = Settings.Global.getUriFor(Settings.Global.ADB_ENABLED)
            val job = JobInfo.Builder(JOB_ID, ComponentName(context, UsbDebugWatcher::class.java))
                .addTriggerContentUri(JobInfo.TriggerContentUri(adbEnabled, 0))
                // 기본값(수 초)보다 짧게 잡아 바뀌자마자 반영한다.
                .setTriggerContentUpdateDelay(500)
                .setTriggerContentMaxDelay(1_000)
                .build()
            context.getSystemService(JobScheduler::class.java).schedule(job)
        }

        fun stop(context: Context) {
            context.getSystemService(JobScheduler::class.java).cancel(JOB_ID)
        }
    }
}
