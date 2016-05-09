package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Register;

public class OpSetReg extends BitModificationOperation {

    private final Register register;

    public OpSetReg(final Register register, final int bitPosition) {
        super(bitPosition);
        this.register = register;
    }

    @Override
    public int execute() {
        register.set(set(register.get()));
        return 8;
    }
}