package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpExSpIndirectHl implements Operation {

    private final Register spReg;
    private final Register hReg;
    private final Register lReg;
    private final Memory memory;

    public OpExSpIndirectHl(final Processor processor, final Memory memory) {
        this.spReg = processor.register("sp");
        this.hReg = processor.register("h");
        this.lReg = processor.register("l");
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int oldH = hReg.get();
        final int oldL = lReg.get();

        final int spLow = spReg.get();
        final int spHigh = 0xffff & (spLow + 1);
        lReg.set(memory.get(spLow));
        hReg.set(memory.get(spHigh));

        memory.set( spLow, oldL);
        memory.set( spHigh, oldH);
        return 19;
    }

    @Override
    public String toString() {
        return "ex (sp), hl";
    }
}
