package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Register;

public class OpLd16RegIndirectFrom8Reg implements Operation {

    private final Register destReference;
    private final Register source;
    private final int[] memory;

    public OpLd16RegIndirectFrom8Reg(final int[] memory, final Register destReference, final Register source) {
        this.memory = memory;
        this.destReference = destReference;
        this.source = source;
    }

    @Override
    public int execute() {
        memory[destReference.get()] = source.get();
        return 7;
    }
}
