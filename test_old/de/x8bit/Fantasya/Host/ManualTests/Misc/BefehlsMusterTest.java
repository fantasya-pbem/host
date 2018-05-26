package de.x8bit.Fantasya.Host.ManualTests.Misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hb
 */
public class BefehlsMusterTest extends TestBase {

    @Override
    protected void mySetupTest() {
        BefehlsSpeicher bs = BefehlsSpeicher.getInstance();

        try {
            File file = new File("BefehlsMuster.txt");

            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), "UTF8"));

            out.append(bs.toString()).append("\r\n");

            out.flush();
            out.close();

            new TestMsg(file.getAbsolutePath() + " geschrieben.");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        this.getTestWorld().setContinueWithZAT(false);
    }

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
