package org.johnnei.javatorrent.internal.utp;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.johnnei.javatorrent.internal.utp.protocol.packet.UtpPacket;
import org.johnnei.javatorrent.internal.utp.stream.PacketReader;

public class UtpMultiplexer implements Closeable, Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(UtpMultiplexer.class);

	private static final int BUFFER_SIZE = 32_768;

	private final PacketReader packetReader;

	private final UtpSocketRegistry socketRegistry;

	private DatagramChannel channel;

	public UtpMultiplexer(PacketReader packetReader, UtpSocketRegistry socketRegistry, int port) throws IOException {
		this.packetReader = packetReader;
		this.socketRegistry = socketRegistry;
		channel = DatagramChannel.open();
		channel.bind(new InetSocketAddress(port));
		channel.configureBlocking(true);
	}

	@Override
	public void run() {
		// Receive message
		ByteBuffer buffer;
		SocketAddress socketAddress;
		try {
			buffer = ByteBuffer.allocate(BUFFER_SIZE);
			socketAddress = channel.receive(buffer);
			buffer.flip();
		} catch (IOException e) {
			LOGGER.error("Failed to read message.", e);
			return;
		}

		// Transform message
		UtpPacket packet = packetReader.read(buffer);

		// Retrieve socket
		UtpSocket socket = socketRegistry.getSocket(socketAddress, packet.getHeader().getConnectionId());

		// FIXME Dispatch buffer to socket
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}
}
