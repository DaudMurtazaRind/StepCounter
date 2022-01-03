package com.example.myapplication.accelerometer

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

class StepDetector : SensorEventListener {

    var oriValues = FloatArray(3)
    val ValueNum = 4
    var tempValue = FloatArray(ValueNum)
    var tempCount = 0
    var isDirectionUp = false
    var continueUpCount = 0
    var continueUpFormerCount = 0
    var lastStatus = false
    var peakOfWave = 0f
    var valleyOfWave = 0f
    var timeOfThisPeak: Long = 0
    var timeOfLastPeak: Long = 0
    var timeOfNow: Long = 0
    var gravityNew = 0f
    var gravityOld = 0f
    val InitialValue = 1.3.toFloat()
    var ThreadValue = 2.0.toFloat()
    var TimeInterval = 250
    private var mStepListeners: StepCountListener? = null
    override fun onSensorChanged(event: SensorEvent) {
        for (i in 0..2) {
            oriValues[i] = event.values[i]
        }
        gravityNew =
            Math.sqrt((oriValues[0] * oriValues[0] + oriValues[1] * oriValues[1] + oriValues[2] * oriValues[2]).toDouble())
                .toFloat()
        detectorNewStep(gravityNew)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        //
    }

    fun initListener(listener: StepCountListener?) {
        mStepListeners = listener
    }

    fun detectorNewStep(values: Float) {
        if (gravityOld == 0f) {
            gravityOld = values
        } else {
            if (detectorPeak(values, gravityOld)) {
                timeOfLastPeak = timeOfThisPeak
                timeOfNow = System.currentTimeMillis()
                if (timeOfNow - timeOfLastPeak >= TimeInterval
                    && peakOfWave - valleyOfWave >= ThreadValue
                ) {
                    timeOfThisPeak = timeOfNow
                    mStepListeners!!.countStep()
                }
                if (timeOfNow - timeOfLastPeak >= TimeInterval
                    && peakOfWave - valleyOfWave >= InitialValue
                ) {
                    timeOfThisPeak = timeOfNow
                    ThreadValue = peakValleyThread(peakOfWave - valleyOfWave)
                }
            }
        }
        gravityOld = values
    }

    fun detectorPeak(newValue: Float, oldValue: Float): Boolean {
        lastStatus = isDirectionUp
        if (newValue >= oldValue) {
            isDirectionUp = true
            continueUpCount++
        } else {
            continueUpFormerCount = continueUpCount
            continueUpCount = 0
            isDirectionUp = false
        }
        return if (!isDirectionUp && lastStatus
            && (continueUpFormerCount >= 2 || oldValue >= 20)
        ) {
            peakOfWave = oldValue
            true
        } else if (!lastStatus && isDirectionUp) {
            valleyOfWave = oldValue
            false
        } else {
            false
        }
    }

    fun peakValleyThread(value: Float): Float {
        var tempThread = ThreadValue
        if (tempCount < ValueNum) {
            tempValue[tempCount] = value
            tempCount++
        } else {
            tempThread = averageValue(tempValue, ValueNum)
            for (i in 1 until ValueNum) {
                tempValue[i - 1] = tempValue[i]
            }
            tempValue[ValueNum - 1] = value
        }
        return tempThread
    }

    fun averageValue(value: FloatArray, n: Int): Float {
        var ave = 0f
        for (i in 0 until n) {
            ave += value[i]
        }
        ave = ave / ValueNum
        ave =
            if (ave >= 8) 4.3.toFloat() else if (ave >= 7 && ave < 8) 3.3.toFloat() else if (ave >= 4 && ave < 7) 2.3.toFloat() else if (ave >= 3 && ave < 4) 2.0.toFloat() else {
                1.3.toFloat()
            }
        return ave
    }
}
