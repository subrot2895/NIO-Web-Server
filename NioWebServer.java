
import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.channels.*;

/**
 * The main class for the web server. Handles the accepting and 
 * serving threads.
 *
 */
public class NioWebServer implements Runnable {

	/**
	 * The string sent in the "Server" field of the repsonses.
	 */
	private static final String SERVER_NAME = "Jarvis Web Server";

	/**
	 * The default port for the server.
	 */
	private static final int DEFAULT_PORT = 8080;

	/**
	 * Handle various encoding and decoding.
	 */
	private HttpCodings codings;

	/**
	 * Serves requests for files and directories.
	 */
	private FileSender fileSender;

	/**
	 * Multiplexes all I/O (accepts, reads and writes)
	 */
	private Selector selector;

	/**
	 * Controls whether the server should keep running.
	 */
	private boolean keepRunning = true;

	/**
	 * Creates a new web server, but does not start it.
	 * 
	 * @param root The server root directory.
	 * @param addr The local address that the server should bind to. If this 
	 * is null, the wildcard address is used.
	 * @param port The port that the server will listen on.
	 */
	public NioWebServer (File root, InetAddress addr, int port) 
			throws FileNotFoundException, IOException {

		this.codings = new HttpCodings();
		this.fileSender = new FileSender(this, root);
		this.selector = Selector.open();
		new AcceptHandler(this, addr, port);
	}

	/**
	 * Gets the name of the server.
	 */
	public String getServerName () {
		return SERVER_NAME;
	}

	public HttpCodings getCodings () {
		return codings;
	}

	/**
	 * Logs a message.
	 */
	public void log (String msg) {
		System.err.println(msg);
	}

	protected FileSender getFileSender () {
		return fileSender;
	}

	/**
	 * Registers an socket with a handler for multiplexed I/O.
	 */
	protected void register (SelectableChannel channel, Handler handler) 
		throws IOException {
		channel.register(selector, handler.getInterestOps(), handler);
	}

	/**
	 * Stops the server. Requests in progress will not be finished.
	 */
	public synchronized void stopServer () {
		keepRunning = false;
		selector.wakeup();
	}

	/**
	 * Runs the server.
	 */
	public void run () {
		try {

			while (true) {
				synchronized (this) {
					if (!keepRunning)
						break;
				}

				selector.select();
				Iterator i = selector.selectedKeys().iterator();
				while (i.hasNext()) {
					SelectionKey key = (SelectionKey)i.next();
					i.remove();
					SelectableChannel channel = 
						(SelectableChannel)key.channel();
					Handler handler = (Handler)key.attachment();
					try {
						handler.handle(channel);
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
				
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Prints usage info and exits.
	 */
	public static void usage () {
			System.err.println("Usage: java NioWebServer [-r root] [-p port]");
			System.exit(2);
	}

	/**
	 * Runs the web server application. The format of the command line
	 * is "java NioWebServer [-r root] [-p port]"
	 */
	public static void main(String[] args) {
		File root = new File("../www");
		InetAddress addr = null;
		int port = DEFAULT_PORT;

		try {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-r")) {
					root = new File(args[++i]);
				} else if (args[i].equals("-p")) {
					port = Integer.parseInt(args[++i]);
				} else usage();
			}
		} catch (Exception ex) {
			usage();
		}

		try {
			System.err.println("Starting Jarvis server at port " + port 
							   + " with server root " + root);
			NioWebServer server = new NioWebServer(root, addr, port);
			server.run();
		} catch (FileNotFoundException ex) {
			System.err.println(ex.getMessage());
			System.exit(1);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

}
