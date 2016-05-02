package com.socialthingy.qaopm.spectrum.remote;

import org.apache.commons.lang3.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;

public class TimestampedData<T extends Serializable> implements Serializable {
    private final long timestamp;
    private final long systemTime;
    private final T data;

    public static <T extends Serializable> TimestampedData<T> from(final DatagramPacket data) throws IOException {
        try (ByteArrayInputStream bis =
                     new ByteArrayInputStream(data.getData(), data.getOffset(), data.getLength())) {
            return SerializationUtils.deserialize(bis);
        }
    }

    public TimestampedData(final Long timestamp, final T data) {
        this.timestamp = timestamp;
        this.data = data;
        this.systemTime = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getSystemTime() {
        return systemTime;
    }

    public T getData() {
        return data;
    }

    public DatagramPacket toPacket() {
        final byte[] data = SerializationUtils.serialize(this);
        final DatagramPacket packet = new DatagramPacket(data, data.length);
        if (data.length > 0x10000) {
            System.out.println("TOO BIG " + data.length);
        }
        return packet;
    }
}
