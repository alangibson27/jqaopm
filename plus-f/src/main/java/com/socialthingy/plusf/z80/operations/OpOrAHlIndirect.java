package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpOrAHlIndirect extends OrOperation {
    private final Register hlReg;
    private final Memory memory;

    public OpOrAHlIndirect(final Processor processor, final Memory memory) {
        super(processor);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int address = hlReg.get();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(address, 3);
        or(memory.get(address));
    }

    @Override
    public String toString() {
        return "or (hl)";
    }
}
