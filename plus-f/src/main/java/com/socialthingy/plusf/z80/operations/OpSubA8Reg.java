package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpSubA8Reg extends ArithmeticOperation {

    private final Register register;
    private final String toString;

    public OpSubA8Reg(final Processor processor, final Register register, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
        this.register = register;

        this.toString = (useCarryFlag ? "sbc a, " : "sub ") + register.name();
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        accumulator.set(sub(register.get(), true));
    }

    @Override
    public String toString() {
        return toString;
    }
}