package fantasya.library.io.order;

public class NoOrdersFoundInDirectoryException extends Exception {
	
	static final long serialVersionUID = 0;
	
	public NoOrdersFoundInDirectoryException (String message) {
		super(message);
	}
}
