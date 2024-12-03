
import java.io.*;
import java.nio.channels.*;

/**
 */
public abstract class Content {

	/**
	 * Writes some data to the destination.
	 *
	 * @param to The channel to write to.
	 * @return True if all the content has been sent, false otherwise.
	 */
	public abstract boolean writeTo (WritableByteChannel to) 
		throws IOException;

	/**
	 * Gets the total number of byte that will be sent.
	 *
	 * @return The number of byte, or -1 if the size is unknown.
	 */
	public abstract long getSize ();

	/**
	 * Gets the content type.
	 *
	 * @return A mime type, such as "text/html", or null if the type
	 * is unknown.
	 */
	public abstract String getContentType ();

}
