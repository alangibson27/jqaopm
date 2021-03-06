package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpCcf extends Operation {
    private final FlagsRegister flagsRegister;
    private final Register accumulator;

    public OpCcf(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        final boolean oldCarry = flagsRegister.get(FlagsRegister.Flag.C);
        flagsRegister.set(FlagsRegister.Flag.H, oldCarry);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.C, !oldCarry);
        flagsRegister.setUndocumentedFlagsFromValue(accumulator.get());
    }

    @Override
    public String toString() {
        return "ccf";
    }
}
