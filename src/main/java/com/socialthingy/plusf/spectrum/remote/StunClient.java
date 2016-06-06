package com.socialthingy.plusf.spectrum.remote;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.header.MessageHeader;

import java.net.*;
import java.util.Optional;

public class StunClient {
    private static final String DEFAULT_STUN_SERVER = "jstun.javawi.de";
    private static final int DEFAULT_STUN_PORT = 3478;

    private final SocketAddress stunServer;
    private final DatagramSocket testSocket;

    public StunClient(final String stunServerAddress, final int stunServerPort, final DatagramSocket testSocket) {
        this.testSocket = testSocket;
        this.stunServer = new InetSocketAddress(stunServerAddress, stunServerPort);
    }

    public StunClient(final DatagramSocket testSocket) {
        this(DEFAULT_STUN_SERVER, DEFAULT_STUN_PORT, testSocket);
    }

    public Optional<InetSocketAddress> discoverAddress() {
        try {
            final MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
            sendMH.generateTransactionID();

            final ChangeRequest changeRequest = new ChangeRequest();
            sendMH.addMessageAttribute(changeRequest);

            final byte[] data = sendMH.getBytes();
            final DatagramPacket send = new DatagramPacket(data, data.length, stunServer);
            testSocket.send(send);

            MessageHeader receiveMH = new MessageHeader();
            while (!(receiveMH.equalTransactionID(sendMH))) {
                DatagramPacket receive = new DatagramPacket(new byte[200], 200);
                testSocket.receive(receive);
                receiveMH = MessageHeader.parseHeader(receive.getData());
                receiveMH.parseAttributes(receive.getData());
            }

            final MappedAddress ma = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
            return Optional.of(new InetSocketAddress(ma.getAddress().getInetAddress(), ma.getPort()));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
