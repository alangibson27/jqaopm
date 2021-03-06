package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpRrcReg extends RotateOperation {
    private final Register register;

    public OpRrcReg(final Processor processor, final Register register) {
        super(processor);
        this.register = register;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        final int result = rrcValue(register.get());
        setSignZeroAndParity(result);
        register.set(result);
    }

    @Override
    public String toString() {
        return "rrc " + register.name();
    }
}
