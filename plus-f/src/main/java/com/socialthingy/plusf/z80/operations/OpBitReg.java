package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpBitReg extends BitOperation {

    private final Register register;
    private final String toString;

    public OpBitReg(final Processor processor, final Register register, final int bitPosition) {
        super(processor, bitPosition);
        this.register = register;

        this.toString = String.format("bit %d, %s", bitPosition, register.name());
    }

    @Override
    public int execute() {
        final int value = register.get();
        checkBit(value);
        if (bitPosition == 3) {
            flagsRegister.set(FlagsRegister.Flag.F3, bitSet(value, 3));
        }
        if (bitPosition == 5) {
            flagsRegister.set(FlagsRegister.Flag.F5, bitSet(value, 5));
        }
        return 8;
    }

    @Override
    public String toString() {
        return toString;
    }
}
