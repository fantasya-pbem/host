package de.x8bit.Fantasya.util.comparator;

import de.x8bit.Fantasya.Atlantis.Message;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MessageComparatorTest {
	
	@Test
	public void messagesAreComparedById() {
		Message msg1 = new Message();
		Message msg2 = new Message();
		Message msgWithSameId = new Message();
		
		MessageComparator comparator = new MessageComparator();
		
		assertEquals("Wrong ordering of messages.", -1, comparator.compare(msg1, msg2));
		assertEquals("Inconsistent ordering of messages.", 1, comparator.compare(msg2, msg1));
	}
}