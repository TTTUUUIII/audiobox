package cn.touchair.audiobox.common

import cn.touchair.audiobox.util.BoxLogger

abstract class LoopThread(
    private val tag: String = "AudioBox#LoopThread#${nextUniqueId()}"
): Thread(tag) {
    override fun run() {
        super.run()
        onEnterLoop()
        while(isActive()) {
            if(!onLoop()) {
                try {
                    sleep(30)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        onExitLoop()
    }

    fun exit() {
        interrupt()
    }

    fun isActive(): Boolean = !interrupted()

    open fun onEnterLoop() {
        BoxLogger.info("onEnterLoop@$tag")
    }
    abstract fun onLoop(): Boolean
    open fun onExitLoop() {
        BoxLogger.info("onExitLoop@$tag")
    }

    companion object {
        private var uniqueId = 0
        fun nextUniqueId() = uniqueId++
    }
}