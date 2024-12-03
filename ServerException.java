
class ServerException extends Exception {

	private int code;

	public ServerException(int code, String message) {
		super(message);
		this.code = code;
	}

	public int getCode() {
		return code;
	}

}
