package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Processor;

public class OpRla extends RotateOperation {

    public OpRla(final Processor processor, final Clock clock) {
        super(processor, clock);
    }

    @Override
    public void execute() {
        final int value = accumulator.get();
        final int highBit = value >> 7;
        accumulator.set(((value << 1) & 0xff) | (flagsRegister.get(FlagsRegister.Flag.C) ? 1 : 0));
        setCarryAndNegateAfterRotate(highBit);
        flagsRegister.setUndocumentedFlagsFromValue(accumulator.get());
    }

    @Override
    public String toString() {
        return "rla";
    }
}
