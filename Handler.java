
import java.io.*;
import java.nio.channels.*;

abstract class Handler {
	
	/**
	 * The server.
	 */
	protected NioWebServer server;

	protected Handler (NioWebServer server) {
		this.server = server;
	}

	public abstract int getInterestOps ();

	public abstract void handle (SelectableChannel channel) throws IOException;

}
