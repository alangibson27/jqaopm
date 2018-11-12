package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpRrHlIndirect extends RotateOperation {

    private final Memory memory;
    private final Register hlReg;

    public OpRrHlIndirect(final Processor processor, final Clock clock, final Memory memory) {
        super(processor, clock);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public void execute() {
        final int address = hlReg.get();
        final int result = rrValue(memory.get(address));
        setSignZeroAndParity(result);
        memory.set( address, result);
        clock.tick(7);
    }

    @Override
    public String toString() {
        return "rr (hl)";
    }
}
