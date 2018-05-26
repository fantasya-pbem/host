package de.x8bit.Fantasya.log;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeAppenderTest {

	private Logger logger = LoggerFactory.getLogger("de.x8bit.Fantasya");

	@Test
	public void warningsCanBeQueriedAndReset() {
		FakeAppender.reset();
		assertFalse(FakeAppender.receivedWarningMessage());

		logger.warn("Whatever warning message.");
		assertTrue(FakeAppender.receivedWarningMessage());
	}

	@Test
	public void onlyWarningsAreCounted() {
		FakeAppender.reset();

		logger.trace("A trace message");
		logger.debug("Some debugging stuff.");
		logger.info ("Info message");
		logger.error("Critical error");

		assertFalse(FakeAppender.receivedWarningMessage());
	}
}