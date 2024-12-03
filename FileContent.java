
import java.io.*;
import java.nio.channels.*;

/**
 * Sends the contents of a file.
 *
 */
public class FileContent extends Content {

	/**
	 * The file channel that data is read from.
	 */
	private FileChannel file;

	/**
	 * The total number of bytes to be sent.
	 */
	private long size;

	/**
	 * The number of bytes already sent.
	 */
	private long offset;


	public FileContent (FileChannel file) throws IOException {
		this.file = file;
		this.size = file.size();
		this.offset = 0;
	}

	public boolean writeTo (WritableByteChannel to) throws IOException {
		long sent = file.transferTo(offset, size - offset, to);

		offset += sent;

		if (offset >= size) {
			file.close();
			return true;
		}

		return false;
	}

	public long getSize () {
		return size;
	}

	public String getContentType () {
		return null;
	}

}
