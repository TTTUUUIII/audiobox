package cn.touchair.audiobox.common

abstract class LoopThread(tag: String = "AudioBox#LoopThread#${nextUniqueId()}"): Thread(tag) {
    override fun run() {
        super.run()
        onEnterLoop()
        while(isActive()) {
            onLoop()
            try {
                sleep(1)
            } catch (e: InterruptedException) {
                break
            }
        }
        onExitLoop()
    }

    fun exit() {
        interrupt()
    }

    fun isActive(): Boolean = !interrupted()

    open fun onEnterLoop() {}
    abstract fun onLoop()
    open fun onExitLoop() {}

    companion object {
        private var uniqueId = 0
        fun nextUniqueId() = uniqueId++
    }
}