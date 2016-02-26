package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Register;

public class OpDecIndexed implements Operation {

    private final Register register;

    public OpDecIndexed(final Register register) {
        this.register = register;
    }

    @Override
    public int execute() {
        register.set(register.get() - 1);
        return 10;
    }
}
