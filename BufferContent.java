
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

/**
 * Sends the contents of a ByteBuffer.
 *
 */
public class BufferContent extends Content {

	/**
	 * The buffer that data is read from.
	 */
	private ByteBuffer buffer;

	/**
	 * The total number of bytes to be sent.
	 */
	private long size;

	/**
	 * The content type.
	 */
	private String type;

	public BufferContent (ByteBuffer buffer, String type) throws IOException {
		this.buffer = buffer;
		this.size = buffer.remaining();
	}

	public boolean writeTo (WritableByteChannel to) throws IOException {
		to.write(buffer);
		return buffer.hasRemaining();
	}

	public long getSize () {
		return size;
	}

	public String getContentType () {
		return type;
	}
	
}
