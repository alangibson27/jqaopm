package com.socialthingy.plusf.spectrum;

public enum Model {
    _48K("48K", 69888, "/48.rom"),
    PLUS_2("+2", 70908, 8, 5, 2, 0, "/plus2-0.rom", "/plus2-1.rom");

    public final String displayName;
    public final double tstateLengthMs;
    public final int tstatesPerRefresh;
    public final int ramPageCount;
    public final int screenPage;
    public final int midPage;
    public final int highPage;
    public final String[] romFileNames;

    Model(
        final String displayName,
        final int tstatesPerRefresh,
        final int ramPageCount,
        final int screenPage,
        final int midPage,
        final int highPage,
        final String ... romFileNames
    ) {
        this.displayName = displayName;
        this.tstatesPerRefresh = tstatesPerRefresh;
        this.tstateLengthMs = 0.02 / tstatesPerRefresh;
        this.ramPageCount = ramPageCount;
        this.screenPage = screenPage;
        this.midPage = midPage;
        this.highPage = highPage;
        this.romFileNames = romFileNames;
    }

    Model(final String displayName, final int tstatesPerRefresh, final String romFileName) {
        this.displayName = displayName;
        this.tstatesPerRefresh = tstatesPerRefresh;
        this.tstateLengthMs = 0.02 / tstatesPerRefresh;
        this.romFileNames = new String[] {romFileName};
        this.ramPageCount = 0;
        this.screenPage = 0;
        this.midPage = 0;
        this.highPage = 0;
    }
}
