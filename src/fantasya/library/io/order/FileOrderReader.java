package fantasya.library.io.order;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.util.Codierung;
import fantasya.library.util.ExceptionFactory;

public class FileOrderReader {
	
	private final Logger LOGGER = LoggerFactory.getLogger(FileOrderReader.class);
	
	protected CleanOrderReader in;
	
	protected Partei faction = null;
	protected Unit unit = null;
	
	public FileOrderReader(CleanOrderReader in) {
		this.in = in;
	}
	
	public void assignOrders() {
		Set<Unit> orderUnitSet = new HashSet<Unit>();
		
		while (true) {
			String order = in.readOrder();
			
			// if order is null, return
			if (order == null) {
				return;
			}
			
			// new faction for orders
			if (order.toLowerCase().startsWith("fantasya") || order.toLowerCase().startsWith("eressea") || order.toLowerCase().startsWith("partei")) {
				// well, only one faction per file? at the moment the webinterface check only first line ode text for faction an pw. what is for second or third faction in one file?
				if (faction != null) {
					OrdersOfSeveralFactionsInOneFileException e = new OrdersOfSeveralFactionsInOneFileException("File has orders of more faction(s) than faction [" + faction.getNummerBase36() + "].");
					e.printStackTrace();
					LOGGER.warn(ExceptionFactory.getExceptionDetails(e));
					return;
				}
				// reset faction : Do not need, if one file can only have orders for one faction
				// faction = null;
				// reset unit
				unit = null;
				
				String[] orderTokenArray = order.split(" ");
				
				try {
					faction = Partei.getPartei(Codierung.fromBase36(orderTokenArray[1]));
					if (faction == null) {
						LOGGER.warn("Faction " + orderTokenArray[1] + " not found.");
					}
					else {
						LOGGER.info("Faction " + faction + " identified.");
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					LOGGER.warn("Order " + orderTokenArray[0] + " has no token for faction id.\n" + ExceptionFactory.getExceptionDetails(e));
				}
			}
			// end of orders of a faction
			else if (order.toLowerCase().startsWith("naechster")) {
				// reset faction
				faction = null;
				// reset unit
				unit = null;
				// one faction each file
				return;
			}
			// new unit of given faction
			else if (order.toLowerCase().startsWith("einheit") && (faction != null)) {
				// reset unit
				unit = null;
				
				String[] orderTokenArray = order.split(" ");
				
				try {
					unit = Unit.Get(Codierung.fromBase36(orderTokenArray[1]));
					/* if (unit == null) {
						String message = "Unit " + orderTokenArray[1] + " for faction " + faction + " not found.";
						LOGGER.info(message);
						new Fehler(message, faction);
					}
					else if (unit.getOwner() != faction.getNummer()) { */
					if (unit == null || unit.getOwner() != faction.getNummer()) {
						String message = "Faction " + faction + " is not owner of unit " + orderTokenArray[1] + ".";
						LOGGER.info(message);
						new Fehler(message, faction);
						unit = null;
					}
					else if (orderUnitSet.contains(unit)) {
						String message = "Unit " + unit + " already has orders. First orders will be computed.";
						LOGGER.info(message);
						new Fehler(message, faction);
						unit = null;
					}
					else {
						orderUnitSet.add(unit);
						// delete old orders
						unit.Befehle.clear();
						unit.BefehleExperimental.clear();
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					LOGGER.warn("Order " + orderTokenArray[0] + " has no token for unit id.\n" + ExceptionFactory.getExceptionDetails(e));
				}
			}
			// unit get the order
			// IMPORTANT: CleanOrderReader should never return an empty string!
			// Because of that, we do not check for 'order.length() != 0'
			else if ((faction != null) && (unit != null) 
					// && (!order.toLowerCase().startsWith("einheit")) 
					// && (!order.toLowerCase().startsWith("naechster"))
					/*
					 * 'faulenze' is already a correct long order
					 * implement it as a correct order!
					 */
					 && (!order.toLowerCase().startsWith("faulenze"))
					) {
				try {
					unit.BefehleExperimental.add(unit, order);
					unit.Befehle.add(order);
				} catch (IllegalArgumentException e) {
					// e.printStackTrace();
					// LOGGER.info(ExceptionFactory.getExceptionDetails(e));
					LOGGER.info(e.getMessage());
					new Fehler(e.getMessage(), unit);
				}
			}
		}
	}
	
	public void close() {
		in.close();
	}
}
