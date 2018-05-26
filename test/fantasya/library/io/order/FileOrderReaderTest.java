package fantasya.library.io.order;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Set;

import org.junit.Test;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;

public class FileOrderReaderTest {
	
	private FileOrderReader generateReader(String s) {
		return new FileOrderReader(new CleanOrderReader(new BufferedReader(new StringReader(s))));
	}
	
	private void generateTestFactions() {
		
		Partei.PROXY.clear();
		Unit.CACHE.clear();
		
		for (int i = 0; i < 2; i++) {
			Partei faction = new Partei();
			faction.setNummer(i + 1);
			for (int j = 0; j < 2; j++) {
				Mensch unit = new Mensch();
				unit.setNummer(1100 + (faction.getNummer() * 10) + j);
				unit.setPersonen(1);
				unit.setOwner(faction.getNummer());
				Unit.CACHE.add(unit);
			}
			Partei.PROXY.add(faction);
		}
		
		// faction 1 with units 1110 (uu) and 1111 (uv)
		// faction 2 with units 1120 (v4) and 1121 (v5)
	}
	
	// 1. get faction and unit for orders
	@Test
	public void testOrderToUnit() {
		String orderString = "BEWACHE";
		String orders = "PARTEI 1 \"test\"\nEINHEIT uu\nBEWACHE";
		generateTestFactions();
		FileOrderReader reader = generateReader(orders);
		
		reader.assignOrders();
		
		Partei faction = Partei.getPartei(1);
		Set<Unit> factionUnitSet = faction.getEinheiten();
		
		Unit orderUnit = null;
		
		for (Unit unit : factionUnitSet) {
			if (unit.getNummer() == 1110 /* uu */) {
				orderUnit = unit;
				break;
			}
		}
		
		String stringOrder = orderUnit.Befehle.get(0);
		
		assertEquals(orderString, stringOrder);
		
		/*Einzelbefehl ebOrder = null;
		
		for (Einzelbefehl singleOrder : orderUnit.BefehleExperimental) {
			ebOrder = singleOrder;
		}*/
		
		
		// assertEquals(orderString, ebOrder.getBefehl());
	}
	
	// 1.1. get orders for one faction
	@Test
	public void testOrderToUnits() {
		String orderStringUnitUU = "BEWACHE";
		String orderStringUnitUV = "TARNE";
		String orders = "PARTEI 1 \"test\"\nEINHEIT uu\nBEWACHE\nEINHEIT uv\nTARNE";
		generateTestFactions();
		FileOrderReader reader = generateReader(orders);
		
		reader.assignOrders();
		
		Partei faction = Partei.getPartei(1);
		Set<Unit> factionUnitSet = faction.getEinheiten();
		
		Unit orderUnit = null;
		
		for (Unit unit : factionUnitSet) {
			if (unit.getNummer() == 1110 /* uu */) {
				orderUnit = unit;
				break;
			}
		}
		
		String stringOrder = orderUnit.Befehle.get(0);
		
		assertEquals(orderStringUnitUU, stringOrder);
		
		/*Einzelbefehl ebOrder = null;
		
		for (Einzelbefehl singleOrder : orderUnit.BefehleExperimental) {
			ebOrder = singleOrder;
		}*/
		
		
		// assertEquals(orderString, ebOrder.getBefehl());
		
		orderUnit = null;
		
		for (Unit unit : factionUnitSet) {
			if (unit.getNummer() == 1111 /* uv */) {
				orderUnit = unit;
				break;
			}
		}
		
		stringOrder = orderUnit.Befehle.get(0);
		
		assertEquals(orderStringUnitUV, stringOrder);
	}
	
	// 1.1.1. get orders for one unit of one faction
	@Test
	public void testOrdersToUnit() {
		String[] OrderStringArray = {"BEWACHE", "TARNE"};
		String orders = "PARTEI 1 \"test\"\nEINHEIT uu\nBEWACHE\nTARNE";
		generateTestFactions();
		FileOrderReader reader = generateReader(orders);
		
		reader.assignOrders();
		
		Partei faction = Partei.getPartei(1);
		Set<Unit> factionUnitSet = faction.getEinheiten();
		
		Unit orderUnit = null;
		
		for (Unit unit : factionUnitSet) {
			if (unit.getNummer() == 1110 /* uu */) {
				orderUnit = unit;
				break;
			}
		}
		
		for (int i = 0; i < OrderStringArray.length; i++) {
			assertEquals(OrderStringArray[i], orderUnit.Befehle.get(i));
		}
	}
	
	// 1.1.2. get orders for more than one unit of one faction
	@Test
	public void testOrdersToUnits() {
		String[] OrderStringArrayUU = {"BEWACHE", "TARNE"};
		String[] OrderStringArrayUV = {"MACHE", "GIB"};
		String orders = "PARTEI 1 \"test\"\nEINHEIT uu\nBEWACHE\nTARNE\nEINHEIT uv\nMACHE\nGIB";
		generateTestFactions();
		FileOrderReader reader = generateReader(orders);
		
		reader.assignOrders();
		
		Partei faction = Partei.getPartei(1);
		Set<Unit> factionUnitSet = faction.getEinheiten();
		
		Unit orderUnit = null;
		
		for (Unit unit : factionUnitSet) {
			if (unit.getNummer() == 1110 /* uu */) {
				orderUnit = unit;
				break;
			}
		}
		
		for (int i = 0; i < OrderStringArrayUU.length; i++) {
			assertEquals(OrderStringArrayUU[i], orderUnit.Befehle.get(i));
		}
		
		orderUnit = null;
		
		for (Unit unit : factionUnitSet) {
			if (unit.getNummer() == 1111 /* uv */) {
				orderUnit = unit;
				break;
			}
		}
		
		for (int i = 0; i < OrderStringArrayUV.length; i++) {
			assertEquals(OrderStringArrayUV[i], orderUnit.Befehle.get(i));
		}
	}
	
	@Test
	public void testDoubleOrdersToUnit() {
		String[] orderStringArray = {"BEWACHE", "TARNE"};
		String orders = "PARTEI 1 \"test\"\nEINHEIT uu\nBEWACHE\nTARNE\nEINHEIT uu\nGIB z 2 Schwert\nLERNE Ausdauer";
		generateTestFactions();
		FileOrderReader reader = generateReader(orders);
		
		reader.assignOrders();
		
		Partei faction = Partei.getPartei(1);
		Set<Unit> factionUnitSet = faction.getEinheiten();
		
		Unit orderUnit = null;
		
		for (Unit unit : factionUnitSet) {
			if (unit.getNummer() == 1110 /* uu */) {
				orderUnit = unit;
				break;
			}
		}
		
		assertEquals(orderStringArray.length, orderUnit.Befehle.size());
	}
	
	@Test
	public void testOrdersToNotFactionUnit() {
		String orders = "PARTEI 1 \"test\"\nEINHEIT v5\nBEWACHE\nTARNE";
		generateTestFactions();
		FileOrderReader reader = generateReader(orders);
		
		reader.assignOrders();
		
		Partei faction = Partei.getPartei(2);
		Set<Unit> factionUnitSet = faction.getEinheiten();
		
		Unit orderUnit = null;
		
		for (Unit unit : factionUnitSet) {
			if (unit.getNummer() == 1121 /* v5 */) {
				orderUnit = unit;
				break;
			}
		}
		
		assertEquals(0, orderUnit.Befehle.size());
	}
}


// 1.2. get orders for more than one faction
// 1.2.1. get orders for one unit of more than one faction
// 1.2.2. get orders for more than one unit of more than one faction