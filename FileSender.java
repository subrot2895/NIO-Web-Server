
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

/**
 * Handles GET and HEAD requests for files and directory listings.
 *
 */
public class FileSender {

	/**
	 * The file names to check when looking for a directory index.
	 */
	private static final String[] DIR_INDEX = { "index.html", "index.htm" };

	private NioWebServer server;

	/**
	 * The server root. Request paths are relative to this directory.
	 */
	private File rootFile;

	/**
	 * Creates a new FileSender.
	 *
	 */
	public FileSender (NioWebServer server, File rootFile) 
		throws IOException, FileNotFoundException {
		
		this.server = server;
		this.rootFile = rootFile.getCanonicalFile();
		if (!rootFile.isDirectory())
			throw new FileNotFoundException("Server root " 
											+ rootFile 
											+ " is not a directory.");
	}

	/**
	 * Checks the request method. 
	 *
	 * @throws ServerException If the method is not GET or HEAD
	 */
	private void checkMethod (HttpRequest request, HttpResponse response) 
		throws ServerException {

		String method = request.getMethod();
		if (!(method.equals("GET") || method.equals("HEAD"))) {
			response.addHeader("Allow", "GET, HEAD");
			throw new ServerException(501, "Method not implemented");
		}	
	}

	/**
	 * Gets the local filename that was requested.
	 */
	private File getFile (HttpRequest request) throws IOException {
		String requestPath = request.getRequestURI();
		StringBuffer path = new StringBuffer(
			requestPath.replace('/', File.separatorChar));

		// make path relative
		while (path.length() > 0 && path.charAt(0) == File.separatorChar) {
			path.deleteCharAt(0);
		}

		return new File(rootFile, path.toString());
	}

	/**
	 * Gets the file that that should be sent. If the requested file
	 * is a directory we look for index.html in that directory.
	 * 
	 * @param file The requested file
	 * @return The file to send
	 * @throws ServerException the file file cannot be found, does not 
	 * has read permissions or is outside of the sever root directory.
	 */
	private File checkFile (File file) 
		throws ServerException, IOException {

		if (file.isDirectory()) {
			for (int i = 0; i < DIR_INDEX.length; i++) {
				File index = new File(file, DIR_INDEX[i]);
				if (index.exists()) {
					// FIX - what if it is a directory?
					file = index;
					break;
				}
			}
		}
		
		if (!file.exists()) 
			throw new ServerException(404, "Not found");
		
		if (!file.canRead())
			throw new ServerException(403, "Forbidden");

		// check that the file is below the root
		if (!file.getCanonicalPath().startsWith(rootFile.getPath()))
			throw new ServerException(404, "Not found");

		return file;
	}

	/**
	 * Sets up the response according to what we have found out about the
	 * requested file.
	 *
	 * @param file The local file
	 */
	private void setupResponse (File file, boolean getContent, 
								HttpResponse response) throws IOException {

		response.addHeader(
			"Last-modified", 
			server.getCodings().formatDate(file.lastModified()));

		if (getContent) {
			if (file.isDirectory()) {
				String[] files= file.list();
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < files.length; i++){
					sb.append(files[i]).append('\n');
				}
				ByteBuffer buf = 
					server.getCodings().encode(CharBuffer.wrap(sb));
				response.setContent(new BufferContent(buf, "text/plain"));
			} else {
				FileInputStream fis = new FileInputStream(file);
				FileChannel fc = fis.getChannel();
				response.setContent(new FileContent(fc));
			}
		}
	}

	/**
	 * Handles a request for a file.
	 */
	public void handleFileRequest (HttpRequest request, HttpResponse response) 
		throws ServerException, IOException {
		checkMethod(request, response);
		File requestFile = getFile(request);
		File localFile = checkFile(requestFile);
		setupResponse(localFile, request.allowsContent(), response);
	}

}
