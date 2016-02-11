package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.util.Word;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpLdAddressA implements Operation {

    private final Processor processor;
    private final int[] memory;
    private final Register aReg;

    public OpLdAddressA(final Processor processor, final int[] memory) {
        this.processor = processor;
        this.memory = memory;
        this.aReg = processor.register("a");
    }

    @Override
    public int execute() {
        final int destination = Word.from(processor.fetchNextPC(), processor.fetchNextPC());
        memory[destination] = aReg.get();
        return 13;
    }
}
