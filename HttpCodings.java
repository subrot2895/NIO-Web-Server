
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.io.*;
import java.text.*;
import java.net.*;


/**
 * Contains utilities for reading and writing byte buffers
 */
public class HttpCodings {

	/**
	 * The size of the buffers used to read, write, encode and decode data.
	 */
	private static final int BUFFER_SIZE = 4096;

	/** 
	 * date format string representation.
	 */
	private static final String RFC_1123_DATE_FORMAT = 
	"E, dd MMM yyyy HH:mm:ss z";

	/** 
	 * timezone (GMT) name.
	 */
	private static final String RFC_1123_TIMEZONE = "GMT";

	/** 
	 * Charater set for encoding headers etc. (ISO-8859-1).
	 */
	private static final Charset TEXT_CHARSET = Charset.forName("ISO-8859-1");

	/**
	 * The character set used for URL decoding (UTF-8).
	 */
	private static final String URL_CHARSET = "UTF-8";

	private ByteBuffer byteBuffer;

	private CharBuffer charBuffer;

	private CharsetDecoder decoder;

	private CharsetEncoder encoder;

	
	private DateFormat dateFormat;

	/**
	 * Used when formatting dates to avoid creating unneccesary objects.
	 */
	private Date date;


	public HttpCodings () {
		byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
		charBuffer = CharBuffer.allocate(BUFFER_SIZE);
		decoder = TEXT_CHARSET.newDecoder();
		encoder = TEXT_CHARSET.newEncoder();
	 	dateFormat = new SimpleDateFormat(RFC_1123_DATE_FORMAT);
		dateFormat.setTimeZone(TimeZone.getTimeZone(RFC_1123_TIMEZONE));
		date = new Date();
	}

	public ByteBuffer encode (CharBuffer from) 
		throws CharacterCodingException {
		
		return encoder.encode(from);
	}

	public CharBuffer decode (ByteBuffer from) 
		throws CharacterCodingException {
		
		return decoder.decode(from);
	}

	public CharsetEncoder getEncoder () {
		return encoder;
	}

	/**
	 * Decodes all data from a ByteBuffer to a CharBuffer using
	 * the ISO-8859-1 character set.
	 *
	 * @throws CharacterCodingException If all the data in the source
	 * buffer does not fit in the destination buffer.
	 */
	public void decode (ByteBuffer from, CharBuffer to) 
		throws CharacterCodingException {
		
		CoderResult cr = decoder.decode(from, to, true);
		if (cr.isUnderflow()) {
			return;
		} else {
			throw new CharacterCodingException();
		}
	}

	/**
	 * Reads as much data as is available (or fits in the input buffer),
	 * and decodes it using ISO-8859-1.
	 *
	 */
	public CharBuffer readAndDecode (ReadableByteChannel channel) 
		throws IOException {
		channel.read(byteBuffer);
		byteBuffer.flip();
		charBuffer.clear();
		decode(byteBuffer, charBuffer); 
		byteBuffer.clear();
		charBuffer.flip();
		return charBuffer;
	}

	/**
	 * Decodes a <tt>application/x-www-form-urlencoded</tt> string
	 * using UTF-8.
	 */
	public String urlDecode (String s) throws IOException {
		return URLDecoder.decode(s, URL_CHARSET);
	}

	/**
	 * Formats a date according to the RFC 1123 date format.
	 * @param time The number of milliseconds after 
	 * January 1, 1970 00:00:00 GMT.
	 */
	public String formatDate (long time) {
		date.setTime(time);
		return dateFormat.format(date);
	}

	/**
	 * Formats a date according to the RFC 1123 date format.
	 */
	public String formatDate (Date date) {
		return dateFormat.format(date);
	}

}
