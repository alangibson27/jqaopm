package com.socialthingy.plusf.spectrum.io;

import com.socialthingy.plusf.spectrum.Model;

public class MemoryPlus2A extends Memory128K {
    private boolean specialPagingMode = false;
    private int[] activeSpecialBanks = new int[4];
    private int lastWriteTo0x1ffd = 0;

    MemoryPlus2A(final boolean addBankMarkerValues) {
        this();
        if (addBankMarkerValues) {
            for (int i = 0; i < 8; i++) {
                ramBanks[i][0] = i;
            }
        }
    }

    public MemoryPlus2A() {
        super(Model.PLUS_2A);
    }

    @Override
    public boolean recognises(int low, int high) {
        return port0x7ffd(low, high) || port0x1ffd(low, high);
    }

    @Override
    public int read(int low, int high) {
        return 0xff;
    }

    @Override
    public void write(int low, int high, int value) {
        if (!pagingDisabled) {
            if (port0x7ffd(low, high)) {
                lastWriteTo0x7ffd = value;
                final int newHighPage = value & 0b00000111;
                final int newScreenPage = (value & 0b00001000) == 0 ? 5 : 7;
                final int newRomLowBitSelector = (value & 0b00010000) >> 4;
                activeHighBank = newHighPage;
                activeScreenBank = newScreenPage;
                activeRomBank = (activeRomBank & 0b10) | newRomLowBitSelector;
                pagingDisabled = (value & 0b00100000) != 0;
            } else if (port0x1ffd(low, high)) {
                lastWriteTo0x1ffd = value;
                specialPagingMode = (value & 0b1) == 1;
                if (specialPagingMode) {
                    final int pagingSelector = (value & 0b110) >> 1;
                    switch (pagingSelector) {
                        case 0:
                            activeSpecialBanks[ROM_PAGE] = 0;
                            activeSpecialBanks[LOW_PAGE] = 1;
                            activeSpecialBanks[MIDDLE_PAGE] = 2;
                            activeSpecialBanks[HIGH_PAGE] = 3;
                            break;

                        case 1:
                            activeSpecialBanks[ROM_PAGE] = 4;
                            activeSpecialBanks[LOW_PAGE] = 5;
                            activeSpecialBanks[MIDDLE_PAGE] = 6;
                            activeSpecialBanks[HIGH_PAGE] = 7;
                            break;

                        case 2:
                            activeSpecialBanks[ROM_PAGE] = 4;
                            activeSpecialBanks[LOW_PAGE] = 5;
                            activeSpecialBanks[MIDDLE_PAGE] = 6;
                            activeSpecialBanks[HIGH_PAGE] = 3;
                            break;

                        case 3:
                            activeSpecialBanks[ROM_PAGE] = 4;
                            activeSpecialBanks[LOW_PAGE] = 7;
                            activeSpecialBanks[MIDDLE_PAGE] = 6;
                            activeSpecialBanks[HIGH_PAGE] = 3;
                            break;
                    }
                } else {
                    final int newRomHighBitSelector = (value & 0b100) >> 1;
                    activeRomBank = (activeRomBank & 0b01) | newRomHighBitSelector;
                }
            }
        }
    }

    @Override
    public int get(int addr) {
        if (specialPagingMode) {
            addr &= 0xffff;
            final int page = addr >> 14;
            final int offsetInPage = addr & 0x3fff;

            return ramBanks[activeSpecialBanks[page]][offsetInPage];
        } else {
            return super.get(addr);
        }
    }

    @Override
    public void set(int addr, int value) {
        if (specialPagingMode) {
            addr &= 0xffff;
            final int page = addr >> 14;
            final int offsetInPage = addr & 0x3fff;

            if (activeScreenBank == activeSpecialBanks[page] && offsetInPage < 0x1b00) {
                writeToDisplayMemory(addr, value);
            }

            ramBanks[activeSpecialBanks[page]][offsetInPage] = value;
        } else {
            super.set(addr, value);
        }
    }

    @Override
    public boolean contendedAddress(int addr) {
        final int page = addr >> 14;
        if (specialPagingMode) {
            return activeSpecialBanks[page] >= 4;
        } else {
            switch (page) {
                case 0:
                    return false;

                case 1:
                    return activeScreenBank >= 4;

                case 2:
                    return false;

                default:
                    return activeHighBank >= 4;
            }
        }
    }

    public int getLastWriteTo0x1ffd() {
        return lastWriteTo0x1ffd;
    }

    private boolean port0x7ffd(final int low, final int high) {
        return (high & 0b11000000) == 0b01000000 && (low & 0b10) == 0b00;
    }

    private boolean port0x1ffd(final int low, final int high) {
        return (high & 0b11110000) == 0b00010000 && (low & 0b10) == 0b00;
    }
}
