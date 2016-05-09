package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.FlagsRegister.Flag;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

abstract class ArithmeticOperation implements Operation {
    protected final Register accumulator;
    protected final FlagsRegister flagsRegister;
    protected final Processor processor;
    protected final boolean useCarryFlag;

    ArithmeticOperation(final Processor processor, final boolean useCarryFlag) {
        this.processor = processor;
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
        this.useCarryFlag = useCarryFlag;
    }

    protected void add(int value) {
        final byte signedAccumulator = (byte) accumulator.get();

        if (useCarryFlag && flagsRegister.get(Flag.C)) {
            value = (value + 1) & 0xff;
        }

        final int[] result = Bitwise.add(accumulator.get(), value);
        accumulator.set(result[0]);
        flagsRegister.set(Flag.N, false);
        setCommonFlags(signedAccumulator, result);
    }

    protected int sub(int value) {
        final byte signedAccumulator = (byte) accumulator.get();

        if (useCarryFlag && flagsRegister.get(Flag.C)) {
            value = (value + 1) & 0xff;
        }

        final int[] result = Bitwise.sub(accumulator.get(), value);
        flagsRegister.set(Flag.N, true);
        setCommonFlags(signedAccumulator, result);
        return result[0];
    }

    protected void setCommonFlags(final byte signedAccumulator, final int[] result) {
        final byte signedResult = (byte) result[0];
        flagsRegister.set(Flag.S, signedResult < 0);
        flagsRegister.set(Flag.Z, result[0] == 0);
        flagsRegister.set(Flag.H, result[1] == 1);
        flagsRegister.set(Flag.P, (signedAccumulator < 0) != (signedResult < 0));
        flagsRegister.set(Flag.C, result[2] == 1);
    }

}