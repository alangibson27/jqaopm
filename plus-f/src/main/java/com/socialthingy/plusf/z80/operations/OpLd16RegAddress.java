package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.*;

public class OpLd16RegAddress extends Operation {
    private final Processor processor;
    private final Memory memory;
    private final Register dest;

    public OpLd16RegAddress(final Processor processor, final Memory memory, final Register dest) {
        this.processor = processor;
        this.memory = memory;
        this.dest = dest;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int source = processor.fetchNextWord();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(initialPcValue + 2, 3);
        contentionModel.applyContention(initialPcValue + 3, 3);
        contentionModel.applyContention(source, 3);
        contentionModel.applyContention(source + 1, 3);
        dest.set(Word.from(memory.get(source), memory.get(source + 1)));
    }

    @Override
    public String toString() {
        return String.format("ld %s, (nn)", dest.name());
    }
}
