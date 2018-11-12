package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class OpPushIndexed extends Operation {
    private final Processor processor;
    private final IndexRegister indexRegister;

    public OpPushIndexed(final Processor processor, final Clock clock, final IndexRegister indexRegister) {
        super(clock);
        this.processor = processor;
        this.indexRegister = indexRegister;
    }

    @Override
    public void execute() {
        final int value = indexRegister.get();
        processor.pushByte((value & 0xff00) >> 8);
        processor.pushByte(value & 0x00ff);
        clock.tick(7);
    }

    @Override
    public String toString() {
        return "push " + indexRegister.name();
    }
}
