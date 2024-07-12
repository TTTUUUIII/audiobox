package cn.touchair.audiobox.annotations

import androidx.annotation.IntDef

@IntDef(
    BufferType.BYTE,
    BufferType.SHORT,
    BufferType.FLOAT,
    BufferType.INTEGER
)
annotation class BufferType {
    companion object {
        const val BYTE = 1
        const val SHORT = 2
        const val FLOAT = 3
        const val INTEGER = 4
    }
}
