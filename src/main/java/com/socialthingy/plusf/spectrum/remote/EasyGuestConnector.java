package com.socialthingy.plusf.spectrum.remote;

import javafx.concurrent.Task;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class EasyGuestConnector extends Task<SocketAddress> {

    private static final SocketAddress CONNECTOR_ADDR = new InetSocketAddress("services.baskinginthesun.co.uk", 7070);
    private final DatagramSocket socket;
    private final String sessionId;

    public EasyGuestConnector(
        final DatagramSocket socket,
        final String sessionId
    ) {
        this.socket = socket;
        this.sessionId = sessionId;
    }

    @Override
    protected SocketAddress call() throws Exception {
        updateMessage("Joining emulator ...");
        final byte[] joinData = ("JOIN" + sessionId).getBytes();
        final DatagramPacket joinPacket = new DatagramPacket(joinData, joinData.length, CONNECTOR_ADDR);
        socket.send(joinPacket);

        final DatagramPacket joinAckPacket = new DatagramPacket(new byte[1024], 1024);
        socket.receive(joinAckPacket);
        final String joinAckData = new String(joinAckPacket.getData(), joinAckPacket.getOffset(), joinAckPacket.getLength());

        if (joinAckData.matches("[\\S]+:\\d+")) {
            final String[] parts = joinAckData.split(":", 2);
            updateMessage(String.format("Connection to emulator at %s established", joinAckData));
            return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
        } else if (joinAckData.equals("KO" + sessionId)) {
            throw new IllegalStateException("Connection rejected. Please check the codename is correct.");
        } else {
            throw new IllegalStateException("Connection data invalid. Please try again later.");
        }
    }
}
