package de.x8bit.Fantasya.Host.ManualTests.mogel;

import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

public final class SMSKaufen extends TestBase {

	@Override
	protected void mySetupTest() {
		try {
			String demo = "";
			demo.codePointBefore(0);
		} catch(Exception ex)
		{
			new BigError(ex);
		}
	}

	@Override
	protected boolean verifyTest() {
		// TODO Auto-generated method stub
		return false;
	}

}
