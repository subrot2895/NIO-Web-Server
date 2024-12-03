
import java.net.*;
import java.io.*;
import java.nio.channels.*;

/**
 * Accepts connections.
 *
 */
class AcceptHandler extends Handler {

	public AcceptHandler (NioWebServer server, InetAddress addr, int port) 
		throws IOException {
		super(server);

		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		InetSocketAddress isa = new InetSocketAddress(addr, port); 
		ssc.socket().bind(isa);
		server.register(ssc, this);
	}

	public int getInterestOps () {
		return SelectionKey.OP_ACCEPT;
	}

	public void handle (SelectableChannel channel) throws IOException {
		ServerSocketChannel readyChannel = (ServerSocketChannel)channel;
		SocketChannel incomingChannel = readyChannel.accept();
		incomingChannel.configureBlocking(false);
		server.register(incomingChannel, new HttpRequest(server));
	}

}
