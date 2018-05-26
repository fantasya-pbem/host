package de.x8bit.Fantasya.Host.ZAT.Battle.util;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.CommandLineArg;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import de.x8bit.Fantasya.Host.ZAT.Battle.Gefecht;
import de.x8bit.Fantasya.Host.ZAT.Battle.Gruppe;
import de.x8bit.Fantasya.Host.ZAT.Battle.GruppenKonflikt;
import de.x8bit.Fantasya.Host.ZAT.Battle.GruppenPaarung;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.util.Codierung;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author hb
 */
public class DuellAnalyse implements CommandLineArg {

    public static boolean AKTIV = false;
    public static DuellAnalyse Instance = null;

    Region r;

    int einheitA;
    int einheitB;

    Krieger a;
    Krieger b;

    Gefecht gefecht;

    Map<Unit, SortedMap<String, String>> messages = new HashMap<Unit, SortedMap<String, String>>();

    String majorPhase;
    int minor;

    public DuellAnalyse() {
        r = new Ebene();
        r.setName("Arena");
        r.setCoords(new Coords(0,0,0));
        r.Init();

        einheitA = -1;
        einheitB = -1;

        Instance = this;
    }

    public static DuellAnalyse I() {
        return DuellAnalyse.Instance;
    }


    public void execute(String arg) {
        if (arg != null) {
            Message.Mute();
            ZATMode.CurrentMode().setDebug(true);
            DuellAnalyse.AKTIV = true;

            System.out.println();
            System.out.println("----------------------------------------------------------------------");
            System.out.println("Ein einzelnes Kampfduell wird simuliert.");
            System.out.println("Quelldatei: " + arg);
            System.out.println("----------------------------------------------------------------------");
            System.out.println();

            liesSituation(arg);

            ruesteAus();

            kampf();

            meldungen();

        } else {
            System.out.println();
            System.out.println("Du musst eine Datei angeben, aus der die Details des Duells geladen werden.");
            System.out.println();
            System.out.println("Also z.B.");
            System.out.println("java -jar Fantasya.jar -X DuellAnalyse MeinDuell.txt");
            System.out.println("Der Inhalt der Datei muss in etwa so aussehen:");
            System.out.println();
            System.out.println("; Das hier ist ein Kommentar - wird bei der Verarbeitung ignoriert.");
            System.out.println("[abc]");
            System.out.println("Elf");
            System.out.println("	Partei [1]");
            System.out.println("	1 Person");
            System.out.println("	steht vorne");
            System.out.println("	Hiebwaffen 1650");
            System.out.println("	1 Schwert");
            System.out.println("	1 Eisenschild");
            System.out.println("	1 Plattenpanzer");
            System.out.println("	");
            System.out.println("[123]");
            System.out.println("Ork");
            System.out.println("	Partei [2]");
            System.out.println("	1 Person");
            System.out.println("	kämpft vorn");
            System.out.println("	heißt Orkenbrut");
            System.out.println("	Hiebwaffen 840");
            System.out.println("	Ausdauer 1350");
            System.out.println("	1 Kriegshammer");
            System.out.println();
            System.out.println("Magier sind leider noch nicht als Duellanten geeignet.");
            System.out.println();
        }


    }

    private void liesSituation(String fileName) {
        majorPhase = "AAAA"; minor = 1;
        
        Partei p0 = new Partei();
        p0.setNummer(0);
        p0.setEMail("noone@foo.bar");
        p0.setMonster(1);
        p0.setRasse("Monster");
        Partei.PROXY.add(p0);

        KampfAufstellung ka = new KampfAufstellung(fileName);


        einheitA = ka.getEinheitA();
        Unit ua = Unit.Load(einheitA);
        // pa = Partei.Load(ua.getOwner());

        einheitB = ka.getEinheitB();
        Unit ub = Unit.Load(einheitB);
        // pb = Partei.Load(ub.getOwner());

        beschreibeEinheit(ua);
        beschreibeEinheit(ub);
    }

    private void ruesteAus() {
        majorPhase = "BBBB"; minor = 1;

        Unit ua = Unit.Get(einheitA);
        Unit ub = Unit.Get(einheitB);

        Gruppe ga = new Gruppe(ua.getOwner(), ua.getTarnPartei());
        ga.getUnits().add(ua);
        
        Gruppe gb = new Gruppe(ub.getOwner(), ub.getTarnPartei());
        gb.getUnits().add(ub);
        
        GruppenKonflikt konflikt = new GruppenKonflikt();
        konflikt.getSeiteA().add(ga);
        konflikt.getSeiteB().add(gb);
        konflikt.getOriginalAngriffe().add(new GruppenPaarung(ga, gb));

        gefecht = new Gefecht(r, konflikt, null);
        gefecht.setZielVorgaben(new HashMap<Unit, Zielvorgabe>()); // keine Zielvorgaben
    }

    private void kampf() {
        majorPhase = "CCCC"; minor = 1;

        gefecht.kampfrunde(null); // keine Seite hat einen taktischen Vorteil
    }

    private void meldungen() {
        System.out.println("Meldungen:");
        for (Unit u : messages.keySet()) {
            System.out.println(u.getName() + ":");
            for (String key : messages.get(u).keySet()) {
                System.out.println("\t" + key + " - " + messages.get(u).get(key));
            }
            System.out.println();
        }
    }

    public void message(Unit u, String msg) {
        String minorS = Integer.toString(minor);
        if (minor < 1000) minorS = " " + minorS;
        if (minor < 100) minorS = " " + minorS;
        if (minor < 10) minorS = " " + minorS;
        String key = majorPhase + "-" + minorS;
        
        if (!messages.containsKey(u)) messages.put(u, new TreeMap<String, String>());
        messages.get(u).put(key, msg);

        minor++;
    }

    private void beschreibeEinheit(Unit u) {
        message(u, "[" + u.getNummerBase36() + "]");
        message(u, u.getRasse());
        message(u, u.getPersonen() + " Personen");
        message(u, "Partei [" + Codierung.toBase36(u.getOwner()) + "]");
        message(u, "steht " + u.getKampfposition().name().toLowerCase());
        for (Skill sk : u.getSkills()) {
            if (sk.getLerntage() <= 0) continue;
            message(u, sk.getName() + " " + (sk.getLerntage() / u.getPersonen()));
        }
        for (Item it : u.getItems()) {
            if (it.getAnzahl() <= 0) continue;
            message(u, it.getAnzahl() + " " + it.getName());
        }
    }

    @Override
    public List<String> getCommandLineOptions() {
		List<String> retval = new ArrayList<String>();
		retval.add("DuellAnalyse");
		return retval;
    }

    @Override
    public void executeCommandLineOption(String commandLineOption, String arg) {
		if (commandLineOption.equals("DuellAnalyse")) {
            execute(arg);
        } else {
            throw new UnsupportedOperationException(commandLineOption + " - diese Aktion wird nicht unterstützt.");
        }
    }



}
