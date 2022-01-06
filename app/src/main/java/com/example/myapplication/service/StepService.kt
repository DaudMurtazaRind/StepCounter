package com.example.myapplication.service

import android.app.*
import android.app.Notification.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.UpdateUiCallBack
import com.example.myapplication.accelerometer.StepCount
import com.example.myapplication.accelerometer.StepValuePassListener
import com.example.myapplication.database.DatabaseClient
import com.example.myapplication.models.Steps
import com.example.myapplication.ui.MainActivity
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val CHANNEL_ID = "200"

class StepService : Service(), SensorEventListener {

    lateinit var db: DatabaseClient
    private var CURRENT_DATE = ""
    private var sensorManager: SensorManager? = null
    private var mBatInfoReceiver: BroadcastReceiver? = null
    private var CURRENT_STEP = 0
    private var stepSensorType = -1
    private var hasRecord = false
    private var hasStepCount = 0
    private var previousStepCount = 0
    private var mNotificationManager: NotificationManager? = null
    private var mStepCount: StepCount? = null
    private val stepBinder = StepBinder()
    private var mBuilder: NotificationCompat.Builder? = null

    override fun onCreate() {
        super.onCreate()
        initNotification()
        initTodayData()
        initBroadcastReceiver()
        Thread { startStepDetector() }.start()
    }

    private fun getTodayDate(): String {
        val date = Date(System.currentTimeMillis())
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(date)
    }

    private fun initNotification() {
        createNotificationChannel()
        val hangIntent = Intent(this, MainActivity::class.java)
        val hangPendingIntent =
            PendingIntent.getActivity(this, 0, hangIntent, FLAG_UPDATE_CURRENT)
        val remoteView = RemoteViews(this.packageName, R.layout.notification_collapsed)
        remoteView.setTextViewText(
            R.id.app_Name,
            this.packageName.split(".").last().toString().capitalize()
        )
        remoteView.setTextViewText(R.id.txt_Steps, this.CURRENT_STEP.toString())
        mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        mBuilder?.setCustomContentView(remoteView)
            ?.setPriority(NotificationCompat.PRIORITY_DEFAULT)
            ?.setAutoCancel(false)
            ?.setOngoing(true)
            ?.setSmallIcon(R.drawable.ic_images_1)
            ?.setColor(255)
        val notification: Notification? = mBuilder?.build()
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        startForeground(notifyId_Step, notification)
    }

    private fun initTodayData() {
        CURRENT_DATE = getTodayDate()
        mStepCount?.setSteps(CURRENT_STEP)
        updateNotification()
    }

    private fun initBroadcastReceiver() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SHUTDOWN)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        filter.addAction(Intent.ACTION_DATE_CHANGED)
        filter.addAction(Intent.ACTION_TIME_CHANGED)
        filter.addAction(Intent.ACTION_TIME_TICK)
        mBatInfoReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (Intent.ACTION_SCREEN_ON == action) {

                } else if (Intent.ACTION_SCREEN_OFF == action) {

                } else if (Intent.ACTION_USER_PRESENT == action) {

                } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS == intent.action) {

                } else if (Intent.ACTION_SHUTDOWN == intent.action) {

                } else if (Intent.ACTION_DATE_CHANGED == action) {
                    save()
                    isNewDay()
                } else if (Intent.ACTION_TIME_CHANGED == action) {
                    save()
                    isCall()
                    isNewDay()
                } else if (Intent.ACTION_TIME_TICK == action) {
                    isCall()
                    isNewDay()
                }
            }
        }
        registerReceiver(mBatInfoReceiver, filter)
    }

    private fun isNewDay() {
        val time = "00:00"
        if (time == SimpleDateFormat("HH:mm").format(Date()) || CURRENT_DATE != getTodayDate()) {
            CURRENT_STEP = 0
            initTodayData()
        }
    }

    private fun isCall() {
        val time =
            getSharedPreferences("share_date", MODE_MULTI_PROCESS).getString("achieveTime", "21:00")
        val plan =
            getSharedPreferences("share_date", MODE_MULTI_PROCESS).getString("planWalk_QTY", "0")
        val remind = getSharedPreferences("share_date", MODE_MULTI_PROCESS).getString("remind", "1")
        Logger.d(
            "time=" + time + "\n" +
                    "new SimpleDateFormat(\"HH: mm\").format(new Date()))=" + SimpleDateFormat("HH:mm").format(
                Date()
            )
        )
        if (((("1" == remind)) &&
                    (CURRENT_STEP < plan!!.toInt()) &&
                    ((time == SimpleDateFormat("HH:mm").format(Date()))))
        ) {
        }
    }

    private fun updateNotification() {
        val remoteView = RemoteViews(this.packageName, R.layout.notification_collapsed)
        remoteView.setTextViewText(
            R.id.app_Name,
            this.packageName.split(".").last().toString().capitalize()
        )
        remoteView.setTextViewText(R.id.txt_Steps, this.CURRENT_STEP.toString())
        val hangIntent = Intent(this, MainActivity::class.java)
        val hangPendingIntent =
            PendingIntent.getActivity(this, 0, hangIntent, FLAG_UPDATE_CURRENT)
        val notification: Notification? =
            mBuilder?.setCustomContentView(remoteView)
                ?.setContentIntent(hangPendingIntent)
                ?.build()
        mNotificationManager!!.notify(notifyId_Step, notification)
        mCallback?.updateUi(CURRENT_STEP)
    }

    private var mCallback: UpdateUiCallBack? = null

    fun registerCallback(paramICallback: UpdateUiCallBack?) {
        mCallback = paramICallback
    }

    var notifyId_Step = 100

    var notify_remind_id = 200

    override fun onBind(intent: Intent?): IBinder? {
        return stepBinder
    }

    inner class StepBinder() : Binder() {
        fun getService(): StepService? {
            return this@StepService
        }
    }

    fun getStepCount(): Int {
        return CURRENT_STEP
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startStepDetector() {
        if (sensorManager != null) {
            sensorManager = null
        }
        sensorManager = this
            .getSystemService(SENSOR_SERVICE) as SensorManager
        val VERSION_CODES = Build.VERSION.SDK_INT
        if (VERSION_CODES >= 19) {
            addCountStepListener()
        } else {
            addBasePedometerListener()
        }
    }

    private fun addCountStepListener() {
        val countSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val detectorSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (countSensor != null) {
            stepSensorType = Sensor.TYPE_STEP_COUNTER
            sensorManager!!.registerListener(
                this@StepService,
                countSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } else if (detectorSensor != null) {
            stepSensorType = Sensor.TYPE_STEP_DETECTOR
            sensorManager!!.registerListener(
                this@StepService,
                detectorSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } else {
            addBasePedometerListener()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (stepSensorType == Sensor.TYPE_STEP_COUNTER) {
            val tempStep = event.values[0].toInt()
            if (!hasRecord) {
                hasRecord = true
                hasStepCount = tempStep
            } else {
                val thisStepCount = tempStep - hasStepCount
                val thisStep = thisStepCount - previousStepCount
                CURRENT_STEP += (thisStep)
                previousStepCount = thisStepCount
            }
            Logger.d("tempStep$tempStep")
        } else if (stepSensorType == Sensor.TYPE_STEP_DETECTOR) if (event.values[0] == 1.0F) {
            CURRENT_STEP++
        }
        updateNotification()
    }

    private fun addBasePedometerListener() {
        mStepCount = StepCount()
        mStepCount?.setSteps(CURRENT_STEP)
        val sensor = sensorManager
            ?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val isAvailable = sensorManager!!.registerListener(
            mStepCount?.getStepDetector(), sensor,
            SensorManager.SENSOR_DELAY_UI
        )
        mStepCount?.initListener(object : StepValuePassListener {
            override fun stepChanged(steps: Int) {
                CURRENT_STEP = steps
                updateNotification()
            }
        })
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun save() {
        CoroutineScope(IO).launch {
            db = DatabaseClient.getInstance(applicationContext)
            val step = db.appDatabase.stepsDao().getSingleStepsRecord(CURRENT_DATE)
            val tempStep = CURRENT_STEP
            if (step == null) {
                var steps = Steps(0, tempStep, CURRENT_DATE)
                db.appDatabase.stepsDao().insert(steps)
            } else{
                db.appDatabase.stepsDao().update(CURRENT_STEP + step.steps, CURRENT_DATE)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        unregisterReceiver(mBatInfoReceiver)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
