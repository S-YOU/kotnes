class Square {
    private val oscillator = Oscillator()

    private var enabledEnvelopeLoop = false
    private var enabledEnvelope = false
    private var envelopeRate = 0xF
    private var envelopeVolume = 0
    private var envelopeGeneratorCounter = 0

    private val enabledLengthCounter get() = !enabledEnvelopeLoop
    private var lengthCounter = 1

    private val volume: Double
        get() {
            val vol = if (enabledEnvelope) envelopeVolume else envelopeRate
            return vol / 0x0F / 0.01
        }

    private var enabledSweep = false
    private var sweepCycle = 0
    private var sweepMode = false
    private var sweepShiftAmount = 0
    private var sweepCounter = 0

    private var frequency = 0
    private var frequencyDivider = 0

    fun write(addr: Int, data: Int) {
        when (addr) {
            0x0 -> {
                val duty = (data shl 6) and 0x03
                val pulseWidth = Array(duty) { 1 }.fold(0.125) { acc, _ -> acc * 2 }
                oscillator.setPulseWidth(pulseWidth)

                enabledEnvelopeLoop = data and 0x20 != 0
                enabledEnvelope = data and 0x10 != 0
                envelopeRate = data and 0xF + 1

                oscillator.volume = volume
            }
            0x1 -> {
                enabledSweep = data and 0x80 != 0
                sweepCycle = (data shr 4) and 0x07 + 1
                sweepMode = data and 0x08 != 0
                sweepShiftAmount = data and 0x07
            }
            0x2 -> {
                frequencyDivider = frequencyDivider or data
            }
            0x3 -> {
                frequencyDivider = frequencyDivider or ((data and 0x7) shl 8)

                if (enabledLengthCounter) {
                    lengthCounter = TABLE_LENGTH_COUNTER[data and 0xF8]
                }

                frequency = (CPU_CLOCK / ((frequencyDivider + 1) * 32)).toInt()
                sweepCounter = 0
                envelopeVolume = 0xF
                oscillator.start()
                oscillator.frequency = frequency
            }
        }
    }

    fun updateLengthCounter() {
        if (enabledLengthCounter && lengthCounter > 0) {
            lengthCounter--
            if (lengthCounter == 0) {
                oscillator.stop()
            }
        }
    }

    fun updateSweep() {
        sweepCounter++
        if (sweepCounter % sweepCycle == 0) {
            if (enabledSweep && lengthCounter != 0 && sweepShiftAmount != 0) {
                val sign = if (sweepMode) 1 else -1
                frequency += (frequency shr sweepShiftAmount) * sign
                if (frequency > 0x7FF) {
                    oscillator.stop()
                } else if (frequency < 0x8) {
                    oscillator.stop()
                }
                oscillator.frequency = frequency
            }
        }
    }

    fun updateEnvelope() {
        if (--envelopeGeneratorCounter <= 0) {
            envelopeGeneratorCounter = envelopeRate
            if (envelopeVolume > 0) {
                envelopeVolume--
            } else {
                envelopeVolume = if (enabledEnvelopeLoop) 0xF else 0x0
            }
        }
        oscillator.volume = volume
    }

    companion object {
        private const val CPU_CLOCK = 1789772.5

        private val TABLE_LENGTH_COUNTER = arrayOf(
                0x0A, 0xFE, 0x14, 0x02, 0x28, 0x04, 0x50, 0x06,
                0xA0, 0x08, 0x3C, 0x0A, 0x0E, 0x0C, 0x1A, 0x0E,
                0x0C, 0x10, 0x18, 0x12, 0x30, 0x14, 0x60, 0x16,
                0xC0, 0x18, 0x48, 0x1A, 0x10, 0x1C, 0x20, 0x1E
        )
    }
}