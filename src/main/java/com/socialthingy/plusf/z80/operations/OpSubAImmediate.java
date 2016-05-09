package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;

public class OpSubAImmediate extends ArithmeticOperation {

    public OpSubAImmediate(final Processor processor, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
    }

    @Override
    public int execute() {
        accumulator.set(sub(processor.fetchNextPC()));
        return 7;
    }
}