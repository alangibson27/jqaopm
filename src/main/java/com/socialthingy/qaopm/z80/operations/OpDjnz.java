package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpDjnz implements Operation {

    private final Processor processor;
    private final Register pcReg;
    private final Register bReg;

    public OpDjnz(final Processor processor) {
        this.processor = processor;
        this.pcReg = processor.register("pc");
        this.bReg = processor.register("b");
    }

    @Override
    public int execute() {
        final byte offset = (byte) processor.fetchNextPC();
        final int bValue = bReg.set(bReg.get() - 1);
        if (bValue > 0) {
            pcReg.set(pcReg.get() + offset);
            return 13;
        } else {
            return 8;
        }
    }
}
