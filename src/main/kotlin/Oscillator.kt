import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

class Oscillator {
    private val line = AudioSystem.getLine(
            DataLine.Info(
                    SourceDataLine::class.java,
                    AudioFormat(48000F, 16, 2, true, false)
            )
    ) as SourceDataLine

    var frequency = 0
    var volume = 0.0
        set(value) {
            Math.max(0.0, Math.min(1.0, value))
        }

    init {
        line.open()
        line.start()
    }

    fun setPulseWidth(pulseWidth: Double) {

    }

    fun start() {
        val info = SoundInfo()
        val span = info.ch * (info.bps / 8)
        for (i in 0 until 3000) {
            var v = 0
            for (j in 0 until 2) {
                v += (sqProduce() * volume / 16).toInt()
            }
            val b = (info.buf[i * span].toInt() and 0xFF) or (info.buf[i * span + 1].toInt() shl 8)
            val w = Math.min(32767, Math.max(-32767, b + v * 8000))
            info.buf[i * span] = (w and 0xFF).toByte()
            info.buf[i * span + 1] = (w shl 8).toByte()
        }
    }

    private fun sqProduce(): Double {

    }

    fun stop() {

    }

    fun play(info: SoundInfo) {
        line.write(info.buf, 0, info.sample * (info.bps / 8) * info.ch)
    }

    class SoundInfo(
            val buf: ByteArray = ByteArray(12000),
            val freq: Int = 48000,
            val bps: Int = 16,
            val ch: Int = 1,
            val sample: Int = 3000
    )
}