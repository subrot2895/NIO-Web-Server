
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

/**
 * A simple class for sending HTTP responses.
 *
 */
public class HttpResponse extends Handler {

	
	private static final String CRLF_STR = "\r\n";

	/**
	 * The response code.
	 */
	private int code = 200;

	/**
	 * The response message.
	 */
	private String message = "OK";

	/**
	 * The response header.
	 */
	private StringBuffer header = new StringBuffer();

	/**
	 * The content source.
	 */
	private Content content;

	/**
	 * Creates a new HttpResponse.
	 */
	public HttpResponse (NioWebServer server) { 
		super(server);
	}

	/**
	 * Adds a name value pair to the header.
	 */
	public void addHeader (String name, long value) {
		addHeader(name, String.valueOf(value));
	}

	/**
	 * Adds a name value pair to the header.
	 */
	public void addHeader (String name, String value) {
		header.append(name).append(": ").append(value).append(CRLF_STR);
	}

	/**
	 * Writes the header to the channel.
	 */
	public void writeHeader (WritableByteChannel channel) throws IOException {
		
		channel.write(server.getCodings().encode(CharBuffer.wrap(
			"HTTP/1.1 " + code + " " + message 
			+ CRLF_STR +	header + CRLF_STR))); 
	}

	/**
	 * Sends an error header and a a simple error document to the client.
	 */
	public void writeError (int code, String message, 
							boolean sendContent,
							WritableByteChannel channel) throws IOException {
		this.code = code;
		this.message = message;

		String body = "<html><head><title>"+code+" "+message
			+"</title></head><body><h1>"+code+" "+message
			+"</h1></body></html>\n";

		if (sendContent) {
			ByteBuffer buf = server.getCodings().encode(CharBuffer.wrap(body));
			setContent(new BufferContent(buf, "text/html"));
		} else {
			setContent(null);
		}

		writeHeader(channel);
	}

	/**
	 * Sets the content producer for this response.
	 */
	public void setContent (Content content) {
		this.content = content;

		if (content != null) {
			addHeader("Content-Length", content.getSize());
			String type = content.getContentType();
			if (type != null)
				addHeader("Content-Type", type);
		}
	}

	public int getInterestOps () {
		return SelectionKey.OP_WRITE;
	}

	/**
	 * Checks the request and initializes the response.
	 */
	public void initResponse (HttpRequest request, SocketChannel channel) 
		throws IOException {
		
		addHeader("Server", server.getServerName());
		addHeader("Connection", "close");

		try {
			request.parse();
			server.getFileSender().handleFileRequest(request, this);
			writeHeader(channel);
		} catch (ServerException ex) {
			writeError(ex.getCode(), ex.getMessage(), request.allowsContent(), 
					   channel);
		} catch (IOException ex) {
			writeError(500, "Internal server error", request.allowsContent(),
					   channel);
			ex.printStackTrace();
		}

		server.log(request.getMethod() + " " 
				   + request.getRequestURI() + " -> "
				   + code + " " + message);

		server.register(channel, this);
	}
	
	/**
	 * Serve a complete request.
	 */
	public void handle (SelectableChannel channel) throws IOException {
		boolean completed = true;
		try {
			if (content != null)
				completed = content.writeTo((SocketChannel)channel);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (completed)
				channel.close();
		}
	}
		

}
