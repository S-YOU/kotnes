class Apu {
    private val squares = arrayOf(Square(), Square())

    private var cycle = 0
    private var step = 0

    fun run(cycle: Int) {
        this.cycle += cycle
        if (this.cycle >= DIVIDE_COUNT_FOR_240HZ) {
            this.cycle -= DIVIDE_COUNT_FOR_240HZ
            squares.forEach { it.updateEnvelope() }
            if (step % 2 == 1) {
                squares.forEach {
                    it.updateLengthCounter()
                    it.updateSweep()
                }
            }
            step++
            if (step == 4) {
                step = 0
            }
        }
    }

    fun write(addr: Int, data: Int) {
        when {
            addr < 0x03 -> squares[0].write(addr, data)
            addr < 0x07 -> squares[1].write(addr, data)
        }
    }

    companion object {
        private const val DIVIDE_COUNT_FOR_240HZ = 7457
    }
}