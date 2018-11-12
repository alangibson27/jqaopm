package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.spectrum.Model;

public class Memory128K extends SpectrumMemory {
    public static final int ROM_PAGE = 0;
    public static final int LOW_PAGE = 1;
    public static final int MIDDLE_PAGE = 2;
    public static final int HIGH_PAGE = 3;

    protected boolean pagingDisabled = false;
    protected int[][] romBanks;
    protected int[][] ramBanks;
    protected int activeRomBank;
    protected int activeScreenBank;
    protected int activeHighBank;

    public Memory128K(final Clock clock, final Model model) {
        super(clock, model);

        romBanks = new int[model.romFileNames.length][];
        int pageIdx = 0;
        for (String romFileName : model.romFileNames) {
            romBanks[pageIdx++] = readRom(romFileName);
        }

        ramBanks = new int[model.ramPageCount][];
        for (int i = 0; i < model.ramPageCount; i++) {
            ramBanks[i] = new int[PAGE_SIZE];
        }

        activeRomBank = 0;
        activeScreenBank = model.screenPage;
        activeHighBank = model.highPage;
        copyBankIntoPage(romBanks[0], ROM_PAGE);
    }

    @Override
    protected void resetDisplayMemory() {
        screenChanged = true;
        System.arraycopy(ramBanks[activeScreenBank], 0, displayMemory, 0, 0x1b00);
    }

    @Override
    public boolean recognises(int low, int high) {
        return (high & 0b10000000) == 0 && (low & 0b10) == 0;
    }

    @Override
    public int read(int low, int high) {
        return 0;
    }

    @Override
    public void write(int low, int high, int value) {
        if (!pagingDisabled) {
            final int newHighPage = value & 0b00000111;
            final int newScreenPage = (value & 0b00001000) == 0 ? 5 : 7;
            final int newRomPage = (value & 0b00010000) == 0 ? 0 : 1;
            activeHighBank = newHighPage;
            activeScreenBank = newScreenPage;
            activeRomBank = newRomPage;
            pagingDisabled = (value & 0b00100000) != 0;
        }
    }

    @Override
    protected void handleMemoryContention(final int page) {
        if (page == 1 || page == 3 && (activeHighBank & 1) == 1) {
            handleContention();
        }
    }

    @Override
    public int get(int addr) {
        addr &= 0xffff;
        final int page = addr >> 14;
        final int offsetInPage = addr & 0x3fff;

        switch (page) {
            case ROM_PAGE:
                clock.tick(3);
                return romBanks[activeRomBank][offsetInPage];
            case LOW_PAGE:
                handleMemoryContention(page);
                clock.tick(3);
                return ramBanks[5][offsetInPage];
            case MIDDLE_PAGE:
                clock.tick(3);
                return ramBanks[2][offsetInPage];
            default:
                if ((activeHighBank & 0b1) == 1) {
                    handleMemoryContention(page);
                }
                clock.tick(3);
                return ramBanks[activeHighBank][offsetInPage];
        }
    }

    @Override
    public void set(int addr, final int value) {
        addr &= 0xffff;
        final int page = addr >> 14;
        final int offsetInPage = addr & 0x3fff;

        switch (page) {
            case LOW_PAGE:
                handleMemoryContention(page);
                clock.tick(3);
                ramBanks[5][offsetInPage] = value;
                if (activeScreenBank == 5 && offsetInPage < 0x1b00) {
                    writeToDisplayMemory(addr, value);
                }
                break;
            case MIDDLE_PAGE:
                clock.tick(3);
                ramBanks[2][offsetInPage] = value;
                break;
            case HIGH_PAGE:
                if ((activeHighBank & 0b1) == 1) {
                    handleMemoryContention(page);
                }
                clock.tick(3);
                ramBanks[activeHighBank][offsetInPage] = value;
                if (activeScreenBank == activeHighBank && offsetInPage < 0x1b00) {
                    writeToDisplayMemory(addr, value);
                }
                break;
        }
    }

    public void copyIntoBank(final int[] source, final int targetBank) {
        System.arraycopy(source, 0x0000, ramBanks[targetBank], 0x0000, PAGE_SIZE);
    }
}