package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.FlagsRegister;
import com.socialthingy.qaopm.z80.Processor;

public class OpLdir extends BlockOperation {
    public OpLdir(final Processor processor, final int[] memory) {
        super(processor, memory, 1);
    }

    @Override
    public int execute() {
        blockTransfer();
        flagsRegister.set(FlagsRegister.Flag.P, false);
        return adjustPC();
    }
}
