package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpXorA8Reg extends XorOperation {

    private final Register register;

    public OpXorA8Reg(final Processor processor, final Register register) {
        super(processor);
        this.register = register;
    }

    @Override
    public int execute() {
        xor(register.get());
        return 4;
    }
}
