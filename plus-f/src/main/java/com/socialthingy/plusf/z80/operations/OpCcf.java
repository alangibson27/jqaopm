package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpCcf implements Operation {
    private final FlagsRegister flagsRegister;
    private final Register accumulator;

    public OpCcf(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
    }

    @Override
    public int execute() {
        final boolean oldCarry = flagsRegister.get(FlagsRegister.Flag.C);
        flagsRegister.set(FlagsRegister.Flag.H, oldCarry);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.C, !oldCarry);
        flagsRegister.setUndocumentedFlagsFromValue(accumulator.get());
        return 4;
    }

    @Override
    public String toString() {
        return "ccf";
    }
}