package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;

public class OpXorAImmediate extends XorOperation {

    private final Processor processor;

    public OpXorAImmediate(final Processor processor) {
        super(processor);
        this.processor = processor;
    }

    @Override
    public int execute() {
        xor(processor.fetchNextPC());
        return 7;
    }
}