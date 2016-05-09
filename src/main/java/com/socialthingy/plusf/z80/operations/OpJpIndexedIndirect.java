package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpJpIndexedIndirect implements Operation {

    private final Register pcReg;
    private final Register indexRegister;

    public OpJpIndexedIndirect(final Processor processor, final Register indexRegister) {
        this.pcReg = processor.register("pc");
        this.indexRegister = indexRegister;
    }

    @Override
    public int execute() {
        pcReg.set(indexRegister.get());
        return 8;
    }
}