package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpOutC8Reg extends Operation {
    private final Register bReg;
    private final Register cReg;
    private final Register sourceRegister;
    private final IO io;

    public OpOutC8Reg(final Processor processor, final IO io, final Register register) {
        this.io = io;
        this.sourceRegister = register;
        this.bReg = processor.register("b");
        this.cReg = processor.register("c");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        final int lowByte = cReg.get();
        final int highByte = bReg.get();
        contentionModel.applyIOContention(lowByte, highByte);
        io.write(lowByte, highByte, sourceRegister.get());
    }

    @Override
    public String toString() {
        return "out (c), " + sourceRegister.name();
    }
}
