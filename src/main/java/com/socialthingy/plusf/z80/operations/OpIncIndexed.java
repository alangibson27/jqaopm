package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Register;

public class OpIncIndexed implements Operation {

    private final Register register;

    public OpIncIndexed(final Register register) {
        this.register = register;
    }

    @Override
    public int execute() {
        register.set(register.get() + 1);
        return 10;
    }
}