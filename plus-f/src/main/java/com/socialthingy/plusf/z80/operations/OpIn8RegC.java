package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.*;

public class OpIn8RegC extends Operation {
    private final FlagsRegister flagsRegister;
    private final Register bReg;
    private final Register cReg;
    private final Register destRegister;
    private final IO io;

    public OpIn8RegC(final Processor processor, final Clock clock, final IO io, final Register register) {
        super(clock);
        this.flagsRegister = processor.flagsRegister();
        this.io = io;
        this.destRegister = register;
        this.bReg = processor.register("b");
        this.cReg = processor.register("c");
    }

    @Override
    public void execute() {
        final int value = io.read(cReg.get(), bReg.get());
        destRegister.set(value);

        flagsRegister.set(FlagsRegister.Flag.S, (byte) value < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, value == 0);
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.P, Bitwise.hasParity(value));
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.setUndocumentedFlagsFromValue(destRegister.get());
    }

    @Override
    public String toString() {
        return "in " + destRegister.name() + ", (c)";
    }
}
