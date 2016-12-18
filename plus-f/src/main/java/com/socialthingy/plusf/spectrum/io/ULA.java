package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.TapePlayer;
import com.socialthingy.plusf.z80.IO;
import com.socialthingy.plusf.z80.Memory;

import java.util.ArrayList;
import java.util.List;

public class ULA implements IO {
    private final TapePlayer tapePlayer;
    private final Keyboard keyboard;
    private final int[] memory;

    private boolean pagingDisabled = false;
    private int earBit;
    private int tapeCyclesAdvanced;
    private int currentCycleTstates;
    private boolean flashActive = false;
    private int cyclesUntilFlashChange = 16;
    private int borderColour;
    private List<Long> borderChanges = new ArrayList<>();
    private int unchangedBorderCycles = 0;

    public ULA(final Keyboard keyboard, final TapePlayer tapePlayer, final int[] memory) {
        this.keyboard = keyboard;
        this.tapePlayer = tapePlayer;
        this.memory = memory;
    }

    @Override
    public int read(int port, int accumulator) {
        if (port == 0xfe) {
            if (tapeCyclesAdvanced > 0) {
                earBit = tapePlayer.skip(tapeCyclesAdvanced) ? 1 << 6 : 0;
                tapeCyclesAdvanced = 0;
            }
            return keyboard.readKeyboard(accumulator) | earBit;
        }
        return 0;
    }

    @Override
    public void write(int port, int accumulator, int value) {
        if (port == 0xfe) {
            final int newBorderColour = value & 0b111;
            if (borderColour != newBorderColour) {
                unchangedBorderCycles = 0;
                borderColour = newBorderColour;
                borderChanges.add(((long) currentCycleTstates << 32) | borderColour);
            }
        }

        if (port == 0xfd && accumulator == 0x7f && !pagingDisabled) {
            final int newHighPage = value & 0b00000111;
            final int newScreenPage = (value & 0b00001000) == 0 ? 5 : 7;
            final int newRomPage = (value & 0b00010000) == 0 ? 0 : 1;
            Memory.setHighPage(memory, newHighPage);
            Memory.setScreenPage(newScreenPage);
            Memory.setRomPage(memory, newRomPage);

            pagingDisabled = (value & 0b00100000) != 0;
        }
    }

    public void setBorderColour(final int borderColour) {
        this.borderColour = borderColour & 0b111;
        borderChanges.clear();
        borderChanges.add((long) borderColour);
    }

    public List<Long> getBorderChanges() {
        return borderChanges;
    }

    public void newCycle() {
        if (borderChanges.size() == 1) {
            unchangedBorderCycles++;
        }
        borderChanges.clear();
        borderChanges.add((long) borderColour);
        cyclesUntilFlashChange--;
        if (cyclesUntilFlashChange == 0) {
            cyclesUntilFlashChange = 16;
            flashActive = !flashActive;
        }
        currentCycleTstates = 0;
    }

    public boolean borderNeedsRedrawing() {
        return unchangedBorderCycles < 2 && !borderChanges.isEmpty();
    }

    public boolean flashStatusChanged() {
        return cyclesUntilFlashChange == 16;
    }

    public boolean flashActive() {
        return flashActive;
    }

    public void advanceCycle(final int tstates) {
        currentCycleTstates += tstates;
        if (tapePlayer.isPlaying()) {
            tapeCyclesAdvanced += tstates;
        }
    }

    public void reset() {
        borderChanges.clear();
        unchangedBorderCycles = 0;
        pagingDisabled = false;
        earBit = 0;
        tapeCyclesAdvanced = 0;
        currentCycleTstates = 0;
        flashActive = false;
        cyclesUntilFlashChange = 16;
    }
}