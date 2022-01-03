package com.example.myapplication.accelerometer

class StepCount : StepCountListener {
    private var count = 0
    private var mCount = 0
    private var mStepValuePassListener: StepValuePassListener? = null
    private var timeOfLastPeak: Long = 0
    private var timeOfThisPeak: Long = 0
    private val stepDetector: StepDetector
    fun getStepDetector(): StepDetector {
        return stepDetector
    }

    override fun countStep() {
        timeOfLastPeak = timeOfThisPeak
        timeOfThisPeak = System.currentTimeMillis()
        if (timeOfThisPeak - timeOfLastPeak <= 3000L) {
            if (count < 9) {
                count++
            } else if (count == 9) {
                count++
                mCount += count
                notifyListener()
            } else {
                mCount++
                notifyListener()
            }
        } else { //超时
            count = 1 //为1,不是0
        }
    }

    fun initListener(listener: StepValuePassListener?) {
        mStepValuePassListener = listener
    }

    fun notifyListener() {
        mStepValuePassListener?.stepChanged(mCount)
    }

    fun setSteps(initValue: Int) {
        mCount = initValue
        count = 0
        timeOfLastPeak = 0
        timeOfThisPeak = 0
        notifyListener()
    }

    init {
        stepDetector = StepDetector()
        stepDetector.initListener(this)
    }
}
