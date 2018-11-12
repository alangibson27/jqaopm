package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpAddAHlIndirect extends ArithmeticOperation {

    private final Memory memory;
    private final Register hlReg;

    public OpAddAHlIndirect(final Processor processor, final Clock clock, final Memory memory, final boolean useCarryFlag) {
        super(processor, clock, useCarryFlag);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }


    @Override
    public void execute() {
        add(memory.get(hlReg.get()));
        clock.tick(3);
    }

    @Override
    public String toString() {
        return useCarryFlag ? "adc a, (hl)" : "add a, (hl)";
    }
}
