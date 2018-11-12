package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpSlaHlIndirect extends SlaOperation {
    private final Register hlReg;
    private final Memory memory;

    public OpSlaHlIndirect(final Processor processor, final Clock clock, final Memory memory) {
        super(processor, clock);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public void execute() {
        final int address = hlReg.get();
        memory.set( address, shift(memory.get(address)));
        clock.tick(7);
    }

    @Override
    public String toString() {
        return "sla (hl)";
    }
}
