package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpRrHlIndirect extends RotateOperation {

    private final int[] memory;
    private final Register hlReg;

    public OpRrHlIndirect(final Processor processor, final int[] memory) {
        super(processor);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public int execute() {
        final int address = hlReg.get();
        final int result = rrValue(unsafe.getInt(memory, 16L + ((address) * 4)));
        setSignZeroAndParity(result);
        Memory.set(memory, address, result);
        return 15;
    }

    @Override
    public String toString() {
        return "rr (hl)";
    }
}