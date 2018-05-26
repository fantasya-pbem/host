package de.x8bit.Fantasya.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;


/** A custom appender for the logback logging system.
 *
 * It only counts if any warning messages have been received.
 * Note: To use this logger, it has to be added to the logback configuration.
 * This is usually done by the file logbback-test.xml in the test base directory.
 */

public class FakeAppender extends AppenderBase<ILoggingEvent> {

	private static boolean warning = false;

	/** Resets the flag for warning log messages. */
	public static void reset() {
		warning = false;
	}

	/** Returns whether a warning log message has been received since the last reset. */
	public static boolean receivedWarningMessage() {
		return warning;
	}

	/** Called whenever the logging framework sends a message. */
	@Override
	protected void append(ILoggingEvent e) {
		if (e.getLevel() == Level.WARN) {
			warning = true;
		}
	}
}
