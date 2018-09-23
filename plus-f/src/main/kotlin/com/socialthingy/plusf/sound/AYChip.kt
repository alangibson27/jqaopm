package com.socialthingy.plusf.sound

import com.jsyn.unitgen.*
import com.socialthingy.plusf.z80.IO

class AYChip(private val toneChannels: List<ToneChannel>) : IO {
    private val amplitudeModeFlag = 1 shl 4

    private val noiseCFlag = 1 shl 5
    private val noiseBFlag = 1 shl 4
    private val noiseAFlag = 1 shl 3

    private val toneCFlag = 1 shl 2
    private val toneBFlag = 1 shl 1
    private val toneAFlag = 1

    private var selectedRegister = 0

    private val registers = IntArray(18)

    fun setPeriod(channel: Int, period: Int): Double {
        return toneChannels[channel].setTonePeriod(period)
    }

    override fun recognises(low: Int, high: Int): Boolean = (high and 128) != 0 && (low and 2) == 0
    override fun read(low: Int, high: Int): Int = registers[selectedRegister]
    override fun write(low: Int, high: Int, value: Int) {
        if ((high and 64) != 0) selectRegister(value and 0xf) else writeRegister(value)
    }

    private fun selectRegister(register: Int) {
        selectedRegister = register
    }

    private fun writeRegister(value: Int) {
        if (registers[selectedRegister] != value) {
            registers[selectedRegister] = value

            when(selectedRegister) {
                in 1..5 -> {
                    val channel = selectedRegister / 2
                    val lowReg = channel * 2
                    val highReg = lowReg + 1
                    setPeriod(channel, (registers[highReg] shl 8) + registers[lowReg])
                }

                6 -> {
                    val period = value and 31
                    toneChannels.forEach { it.setNoisePeriod(period) }
                }

                7 -> {
                    val znoiseA = (value and noiseAFlag) == 0
                    val ztoneA = (value and toneAFlag) == 0
                    val znoiseB = (value and noiseBFlag) == 0
                    val ztoneB = (value and toneBFlag) == 0
                    val znoiseC = (value and noiseCFlag) == 0
                    val ztoneC = (value and toneCFlag) == 0

                    toneChannels[0].enable(ztoneA, znoiseA)
                    toneChannels[1].enable(ztoneB, znoiseB)
                    toneChannels[2].enable(ztoneC, znoiseC)
                }

                8, 9, 10 -> {
                    val channel = selectedRegister - 8
                    if ((value and amplitudeModeFlag) == 0) {
                        toneChannels[channel].setAmplitude(value and 15)
                    }
                }

                11 -> {
//                    val envelopePeriod = (registers[12] shl 8) + registers[11]
//                    val envelopeFrequency = 3500000.0 / (256 * envelopePeriod)
                }

                12 -> {
//                    val envelopePeriod = (registers[12] shl 8) + registers[11]
//                    val envelopeFrequency = 3500000.0 / (256 * envelopePeriod)
                }
            }
        }
    }
}

class ToneChannel(val tone: SquareOscillator, val noise: FunctionOscillator) {
    init {
        tone.amplitude.set(1.0)
        tone.frequency.set(0.0)
        noise.amplitude.set(1.0)
        noise.frequency.set(0.0)
    }

    fun enable(toneChannel: Boolean, noiseChannel: Boolean) {
        tone.isEnabled = toneChannel
        noise.isEnabled = noiseChannel
    }

    private val amplitudeStep = 1.0 / 15

    fun setAmplitude(amplitude: Int) {
        noise.amplitude.set(amplitudeStep * amplitude)
        tone.amplitude.set(amplitudeStep * amplitude)
    }

    fun setTonePeriod(period: Int): Double {
        val frequency = periodToFrequency(period)
        tone.frequency.set(frequency)
        return frequency
    }

    fun setNoisePeriod(period: Int): Double {
        val frequency = periodToFrequency(period)
        noise.frequency.set(frequency)
        return frequency
    }

    private fun periodToFrequency(period: Int): Double = if (period == 0) {
        0.0
    } else {
        3546900.0 / (16 * period)
    }
}