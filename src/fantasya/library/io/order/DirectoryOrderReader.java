package fantasya.library.io.order;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.x8bit.Fantasya.Host.GameRules;
import fantasya.library.util.ExceptionFactory;

public class DirectoryOrderReader {
	
	private final Logger LOGGER = LoggerFactory.getLogger(DirectoryOrderReader.class);
	
	private String orderDirectory = null;
	
	public DirectoryOrderReader(String orderDirectory) {
		this.orderDirectory = orderDirectory;
	}
	
	public void readOrderFiles() {
		
		FilenameFilter fileNameFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (name.trim().toLowerCase().endsWith(".order"));
			}
		};
		
		File[] fileArray = new File(orderDirectory + "/" + GameRules.getRunde()).listFiles(fileNameFilter);
		
		if (fileArray == null) {
			NoOrdersFoundInDirectoryException e = new NoOrdersFoundInDirectoryException("No orders found for this turn in directory '" + orderDirectory + "/" + GameRules.getRunde() + "'.");
			e.printStackTrace();
			LOGGER.warn(ExceptionFactory.getExceptionDetails(e));
			fileArray = new File(orderDirectory).listFiles();
			if (fileArray == null) {
				e = new NoOrdersFoundInDirectoryException("No orders found in directory '" + orderDirectory + "'.");
				e.printStackTrace();
				LOGGER.warn(ExceptionFactory.getExceptionDetails(e));
				return;
			}
		}
		LOGGER.info(fileArray.length + " files for orders found.");
		
		FileOrderReader reader;
		for(int i = 0; i < fileArray.length; i++) {
			try {
				reader = new FileOrderReader(new CleanOrderReader(new BufferedReader(new InputStreamReader(new FileInputStream(fileArray[i]), "UTF8"))));
				reader.assignOrders();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				LOGGER.warn(ExceptionFactory.getExceptionDetails(e));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				LOGGER.warn(ExceptionFactory.getExceptionDetails(e));
			}
		}
	}
}