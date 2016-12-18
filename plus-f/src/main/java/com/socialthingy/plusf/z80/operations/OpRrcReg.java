package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpRrcReg extends RotateOperation {
    private final Register register;

    public OpRrcReg(final Processor processor, final Register register) {
        super(processor);
        this.register = register;
    }

    @Override
    public int execute() {
        final int result = rrcValue(register.get());
        setSignZeroAndParity(result);
        register.set(result);
        return 8;
    }

    @Override
    public String toString() {
        return "rrc " + register.name();
    }
}