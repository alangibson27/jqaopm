package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpLdAI implements Operation {
    private final Register iReg;
    private final Register aReg;

    public OpLdAI(final Processor processor) {
        this.iReg = processor.register("i");
        this.aReg = processor.register("a");
    }

    @Override
    public int execute() {
        aReg.set(iReg.get());
        return 9;
    }
}