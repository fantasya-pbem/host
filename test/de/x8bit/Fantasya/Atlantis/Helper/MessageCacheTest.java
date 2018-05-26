package de.x8bit.Fantasya.Atlantis.Helper;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class MessageCacheTest {
	
	private MessageCache cache = new MessageCache();
	private MessageCache emptyCache = new MessageCache();
	
	private Message msg = new Message();
	private Message msgWithPlayer = new Message();
	private Message msgWithCoords = new Message();
	private Message msgWithPlayerAndCoords = new Message();
	
	private Partei partei = new Partei();
	private Coords coords = new Coords(1,1,1);
	
	@Before
	public void setup() {
		partei.setNummer(1);
		
		msgWithPlayer.setPartei(partei);
		msgWithCoords.setCoords(coords);
		
		msgWithPlayerAndCoords.setPartei(partei);
		msgWithPlayerAndCoords.setCoords(coords);
		
		cache.add(msg);
		cache.add(msgWithPlayer);
		cache.add(msgWithCoords);
		cache.add(msgWithPlayerAndCoords);
	}
	
	@Test(expected = NullPointerException.class)
	public void nullElementsMayNotBeAdded() {
		cache.add(null);
	}
	
	@Test
	public void elementsCanBeAdded() {
		assertTrue("Adding must return true on success.", emptyCache.add(msg));
		assertTrue("Adding must return true on success.", emptyCache.add(msgWithPlayerAndCoords));
		assertTrue("Adding must return true on success.", emptyCache.add(msgWithCoords));
		assertTrue("Adding must return true on success.", emptyCache.add(msgWithPlayer));
	}
	
	@Test
	public void isEmptyReturnsEmptinessOfCache() {
		assertTrue("Cache must be initially empty.", emptyCache.isEmpty());
		emptyCache.add(msg);
		assertFalse("Cache must not be empty after adding.", emptyCache.isEmpty());
	}
	
	@Test
	public void getTheSizeOfTheCache() {
		assertEquals("Cache must be initially empty.", 0, emptyCache.size());
		emptyCache.add(msg);
		assertEquals("Cache must have a single element.", 1, emptyCache.size());
		emptyCache.add(msgWithPlayerAndCoords);
		assertEquals("Cache must have two elements.", 2, emptyCache.size());
	}
	
	@Test
	public void iterateOverAllElementsInTheCache() {
		emptyCache.add(msg);
		emptyCache.add(msgWithPlayer);

		Iterator<Message> iter = emptyCache.iterator();
		assertEquals("Incorrect sorting of elements", msg, iter.next());
		assertEquals("Incorrect sorting of elements", msgWithPlayer, iter.next());
	}
	
	@Test
	public void getAllMessagesOfASinglePlayer() {
		Set<Message> ownedMessages = cache.getAll(msgWithPlayerAndCoords.getPartei().getNummer());
		
		assertEquals("Wrong number of messages returned.", 2, ownedMessages.size());
		
		Iterator<Message> iter = ownedMessages.iterator();
		assertEquals("Incorrect returned elements or bad ordering.", msgWithPlayer, iter.next());
		assertEquals("Incorrect returned elements or bad ordering.", msgWithPlayerAndCoords, iter.next());
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void returnedMessagesOfPlayerSetMayNotBeModified() {
		cache.getAll(partei.getNummer()).clear();
	}
	
	@Test
	public void returnEmptySetForInvalidPlayer() {
		Set<Message> ownedMessages = cache.getAll(42);
		
		assertTrue("No empty set returned.", ownedMessages.isEmpty());
	}
	
	@Test
	public void getAllMessagesAtASingleCoordinate() {
		Set<Message> locatedMessages = cache.getAll(coords);
		
		assertEquals("Wrong number of messages returned.", 2, locatedMessages.size());
		Iterator<Message> iter = locatedMessages.iterator();
		assertEquals("Incorrect element returned or bad ordering.", msgWithCoords, iter.next());
		assertEquals("Incorrect element returned or bad ordering.", msgWithPlayerAndCoords, iter.next());
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void returnedMessagesAtCoordinateMayNotBeModified() {
		cache.getAll(coords).clear();
	}
	
	@Test
	public void returnEmptySetForInvalidCoordinate() {
		Set<Message>  locatedMessages = cache.getAll(new Coords(7,7,0));
		
		assertTrue("No empty set returned.", locatedMessages.isEmpty());
	}
	
	@Test
	public void getAllMessageAtASingleCoordinateAndForAPlayer() {
		// create another message with a definitely higher id.
		Message secondMsg = new Message();
		secondMsg.setCoords(coords);
		secondMsg.setPartei(partei);
		cache.add(secondMsg);
		
		Set<Message> messages = cache.getAll(coords, partei.getNummer());
		
		assertEquals("Wrong number of messages returned.", 2, messages.size());
		
		Iterator<Message> iter = messages.iterator();
		assertEquals("Incorrect element or bad ordering.", msgWithPlayerAndCoords, iter.next());
		assertEquals("Incorrect element or bad ordering.", secondMsg, iter.next());
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void returnedMessageForPlayerAndCoordinateMayNotBeModified() {
		cache.getAll(coords, partei.getNummer()).clear();
	}
	
	@Test
	public void returnEmptySetForInvalidPlayerOrCoords() {
		assertTrue("No empty set was returned for invalid input.",
				cache.getAll(coords, 42).isEmpty());
	}
	
	@Test
	public void removedElementsAreGoneFromCache() {
		cache.remove(msgWithPlayerAndCoords);
		
		assertEquals("Removal is not reflected in the size of the cache.",
				3, cache.size());
		assertFalse("Message was not removed properly.",
				cache.getAll(coords).contains(msgWithPlayerAndCoords));
		assertFalse("Message was not removed properly.",
				cache.getAll(partei.getNummer()).contains(msgWithPlayerAndCoords));
		assertFalse("Message was not removed properly.",
				cache.getAll(coords, partei.getNummer()).contains(msgWithPlayerAndCoords));
		
		for (Message item : cache) {
			if (item == msgWithPlayerAndCoords) {
				fail("Message was not removed properly.");
			}
		}
	}
	
	@Test
	public void removalReturnsWhetherElementWasRemoved() {
		assertTrue("Remove() must return true if element is removed.", cache.remove(msg));
		assertFalse("Remove() must return false if no element is found.", cache.remove(msg));
	}
	
	@Test
	public void clearRemovesAllElements() {
		cache.clear();
		
		assertTrue("Cache is not empty after clearing.", cache.isEmpty());
		assertTrue("Messages are still left.", cache.getAll(coords).isEmpty());
		assertTrue("Message are still left.", cache.getAll(partei.getNummer()).isEmpty());
		assertTrue("Messages are still left.", cache.getAll(coords, partei.getNummer()).isEmpty());
	}
	
	
	// remaining functions of the collection interface that we do not implement.
		
	@Test(expected = UnsupportedOperationException.class)
	public void containsNotImplemented() {
		cache.contains(msg);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void toArrayNotImplemented() {
		cache.toArray();
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void toArrayWithParameterNotImplemented() {
		cache.toArray(new Message[2]);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void containsAllNotImplemented() {
		cache.containsAll(Collections.singleton(msg));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void addAllNotImplemented() {
		cache.addAll(Collections.singleton(msg));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void removeAllNotImplemented() {
		cache.removeAll(Collections.singleton(msg));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void retainAllNotImplemented() {
		cache.retainAll(Collections.singleton(msg));
	}
}