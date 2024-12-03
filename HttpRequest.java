
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

/**
 * A simple representation of an HTTP request
 *
 */
public class HttpRequest extends Handler {
	
	/**
	 * The request method, "GET", "POST", etc.
	 */
	private String method = null;

	/**
	 * The requested URI 
	 */
	private String requestURI = null;

	/**
	 * Keeps the text representation of the request before it is parsed.
	 */
	private StringBuffer input;

	/**
	 * Creates a new empty request.
	 */
	protected HttpRequest (NioWebServer server) { 
		super(server);
		input = new StringBuffer();
	}

	/**
	 * Gets the request method, "GET", "POST", etc. Calling this method 
	 * before parse() has been called will return null.
	 */
	public String getMethod () { 
			return method; 
	}

	/**
	 * Wheter this request allows content in the response.
	 *
	 */
	public boolean allowsContent () {
		return (method == null || !method.equals("HEAD"));
	}

	/**
	 * Gets the request URI. Calling this method 
	 * before parse() has been called will return null.
	 */
	public String getRequestURI () { 
			return requestURI; 
	}

	/**
	 * Adds data to this request.
	 *
	 * @return true if the request is complete. A request is 
	 * considered complete if it ends in two newlines.
	 */
	private boolean append (CharSequence data) {
		input.append(data);

		// FIX - cannot handle requests with a body
		boolean complete = CharSequences.endsWith(input, "\r\n\r\n") 
				|| CharSequences.endsWith(input, "\n\n");

		return complete;
	}

	/**
	 * Parses the request and sets the data fields accordingly.
	 *
	 * @throws ServerException If the request could not be understood.
	 */
	public void parse () throws ServerException, IOException {
		// parse request line
		int fstsp = CharSequences.indexOf(input, ' ');
		int sndsp = CharSequences.indexOf(input, ' ', fstsp+1);
		if (fstsp == -1 || sndsp == -1) {
			throw new ServerException(400, "Bad request");
		}
		method = input.substring(0, fstsp);
		requestURI = server.getCodings().urlDecode(
			input.substring(fstsp+1, sndsp));
	}

	public int getInterestOps () {
		return SelectionKey.OP_READ;
	}

	/**
	 * Reads data from the channel and adds it to the request.
	 */
	public void handle (SelectableChannel channel) throws IOException {
		SocketChannel socketChannel = (SocketChannel)channel;

		try {
			CharBuffer buf = server.getCodings().readAndDecode(socketChannel);
			boolean complete = append(buf);
			
			if (complete) {
				HttpResponse response = new HttpResponse(server);
				response.initResponse(this, socketChannel);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			channel.close();
		}
	}

}
