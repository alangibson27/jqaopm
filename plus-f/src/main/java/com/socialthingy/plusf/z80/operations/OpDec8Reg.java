package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpDec8Reg extends DecOperation {

    private final Register register;

    public OpDec8Reg(final Processor processor, final Clock clock, final Register register) {
        super(processor, clock);
        this.register = register;
    }

    @Override
    public void execute() {
        register.set(decrement(register.get()));
    }

    @Override
    public String toString() {
        return "dec " + register.name();
    }
}
