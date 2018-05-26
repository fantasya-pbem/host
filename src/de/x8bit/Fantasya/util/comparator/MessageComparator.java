package de.x8bit.Fantasya.util.comparator;

import de.x8bit.Fantasya.Atlantis.Message;
import java.util.Comparator;

public class MessageComparator implements Comparator<Message> {

	@Override
	public int compare(Message o1, Message o2) {
		if (o1.getEvaId() < o2.getEvaId()) {
			return -1;
		}
		if (o1.getEvaId() > o2.getEvaId()) {
			return 1;
		}
		return 0;
	}
	
}