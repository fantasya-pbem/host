package de.x8bit.Fantasya.Host.ZAT.Battle;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Partei;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Skills.Taktik;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapon.WeaponType;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.ITeureWaffe;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.NahkampfWaffe;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WKriegselefant;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WSpeer;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WWaffenlos;
import de.x8bit.Fantasya.Host.ZAT.Battle.util.DuellAnalyse;
import de.x8bit.Fantasya.Host.ZAT.Battle.util.KriegerLebenspunkteComparator;
import de.x8bit.Fantasya.Host.ZAT.Battle.util.KriegerReiheLebenspunkteComparator;
import de.x8bit.Fantasya.util.Random;
import de.x8bit.Fantasya.util.StringUtils;

/**
 * Basisklasse aller Kämpfer
 * @author  mogel
 */
public class Krieger {
    public final static int REIHE_VORNE = 1;
    public final static int REIHE_HINTEN = 2;
    public final static int REIHE_NICHT = 3;
    public final static int REIHE_IMMUN = 4;
    
    public final static boolean DEBUG_WAFFEN = false;

	/** der Krieger gehört zu dieser Einheit */
	protected final Unit unit;
	public Unit getUnit() {
		return unit;
	}

    /**
     * die laufende Nummer dieses Kriegers in seiner Einheit - bspw. "20. Personen aus Schwertkämpfer [abc]"
     */
    private int index = 0;
    /**
     * @return die laufende Nummer dieses Kriegers in seiner Einheit
     */
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

	/** eine Sammlung aller Waffen für diesen Krieger */
	public List<Weapon> weapons;

	/** testet ob der Krieger eine bestimmte Waffe / Ausrüstung hat */
	public boolean hasWeapon(Class<? extends Weapon> weapon) {
		for(Weapon w : weapons) if (weapon.isInstance(w) ) return true;
		return false;
	}

    /**
     * @return true, wenn eine Waffe mit Waffe.istFernkampfTauglich() == true vorhanden ist.
     * @see Weapon.istFernkampfTauglich()
     */
    public boolean hatFernwaffe() {
        for(Weapon w : weapons) if (w.istFernkampfTauglich()) return true;
        return false;
    }

    /**
     * @return true, wenn der Krieger eine Waffe besitzt, die er im Nahkampf einsetzen kann. Waffenloser Kampf wird hier nicht berücksichtigt!
     */
    public boolean hatNahkampfWaffe() {
        for(Weapon w : weapons) {
            if (w instanceof NahkampfWaffe) return true;

            // TODO das sollte vielleicht allgemeingültiger gelöst werden...
            if (w.getClass() == WSpeer.class) return true;
            if (w.getClass() == WKriegselefant.class) return true;
        }
        return false;
    }
    
	/*
    public boolean hasWeaponType(WeaponType weapontype) {
		return false;
	} */
	
	/** eine Sammlung aller Effekte */
	private List<BattleEffects> effects;
	/** hinzufügen von Effecten - nur einmalig - keine Doppelten */
	public void addEffect(BattleEffects effect)	{
		boolean found = false;
		for(Object efx : effects) if (efx.getClass().equals(effect.getClass())) found = true;
		if (!found) effects.add(effect);
	}
	public List<BattleEffects> getEffects() { return effects; }


	
	/** erlittene Lebenspunkte für den Krieger */
	private int lebenspunkte = 0;
	/** erlittene Lebenspunkte für den Krieger */
	public int getLebenspunkte() { return lebenspunkte; }
	/** erlittene Lebenspunkte für den Krieger */
	public void setLebenspunkte(int value) { if (value < 0) value = 0; lebenspunkte = value; }
	
	/** max. Trefferpunkte für den Krieger, basierend auf Rasse & Ausdauer */
	private int trefferpunkte = 0;
	/** max. Trefferpunkte für den Krieger, basierend auf Rasse & Ausdauer */
	public int getTrefferpunkte() { return trefferpunkte; }
	/** max. Trefferpunkte für den Krieger, basierend auf Rasse & Ausdauer */
	public void setTrefferpunkte(int value) { trefferpunkte = value; }

	/** in dieser Reihe steht die Einheit (1 = vorne, 2 = hinten, 3 = nicht */
	protected int reihe = 0;
	/** in dieser Reihe steht die Einheit (1 = vorne, 2 = hinten, 3 = nicht */
    public int getReihe() { return reihe; }
	/** in dieser Reihe steht die Einheit (1 = vorne, 2 = hinten, 3 = nicht */
    public void setReihe(int reihe) { this.reihe = reihe; }
    public String getReiheName() { return Krieger.ReiheName(getReihe()); }
    public static String ReiheName(int reihe) {
        switch (reihe) {
            case 1: return "vorne";
            case 2: return "hinten";
            case 3: return "nicht";
            case 4: return "immun";
        }
        throw new IllegalStateException("Krieger::getReiheName(" + reihe + ")?");
    }
	
	/** diese Waffe wird benutzt */
	private Weapon __usedWeapon;
	/** diese Waffe wird benutzt */
	public Weapon usedWeapon() {
		if (__usedWeapon == null) return new WWaffenlos(unit);
		return __usedWeapon; 
	}
	
	

	public Krieger(Unit u) {
		weapons = new ArrayList<Weapon>();
		effects = new ArrayList<BattleEffects>();
        this.unit = u;
	}
	
	
	/**
	 * der Kampf zwischen zwei Kriegern
	 * @param attacker - jener hat den Streit begonnen
	 * @param defender - der soll den Kopf hinhalten
     * @param ks - in diesem Objekt wird der Ausgang der Einzelattacke registriert
	 * @return wird ein Wert zurück gegeben so bedeuted dies das der Angriff ein Collateral-Schaden war und noch einige einfach so sterben werden
	 */
	public static synchronized Einzelattacke _1on1_(Krieger attacker, Krieger defender, KampfrundenStatistik ks) {
		boolean m = !Message.IsMute();
		
		Einzelattacke retval = new Einzelattacke(attacker, defender);
        StringBuilder battle = new StringBuilder();
        StringBuilder shorty = new StringBuilder();

        if (m) battle.append(attacker + " attackiert " + defender + ": ");
        if (m) shorty.append(String.format("%-8s", defender.kurzCode()));

        // April 2011:
        Weapon attack_waffe = attacker.usedWeapon(); // wird in Gefecht.kampfrunde() gesetzt
		retval.setAngreiferWaffe(attack_waffe);

        // Angriffs-Wert
		int attack_value = attack_waffe.AttackValue(attacker.getReihe(), defender.getReihe());
		// hier wählt der Verteidiger auch gleich seine beste Waffe:
        int defence_value = defender.GetDefenceWeapon(attacker);		// Verteidigungs-Wert
        if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), "unmodifizert AV=" + attack_value + ", DV=" + defence_value + " .");

		// Waffen holen
		Weapon defence_waffe = defender.usedWeapon();	// Verteidigungs-Waffe
        // wenn und aber:
        // if ((!defence_waffe.istFernkampfTauglich()) && (attacker.getReihe() == Krieger.REIHE_VORNE) && (defender.getReihe() == Krieger.REIHE_VORNE)) {
            // dann benutzen wir lieber eine andere Waffe - wenn wir haben:
        // }

        retval.setVerteidigerWaffe(defence_waffe);
		if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), "der Verteidiger wehrt sich mit " + defence_waffe + ".");

		// die Waffen sind auf beiden Seiten klar ... also die Modifikationen
		float av = (float) attack_value;
		float dv = (float) defence_value;

        // -- Attack-Value
		for(Weapon waffe : attacker.weapons) {
            float mod = waffe.AttackModifikation_Attacker(attacker, defender);
            if (mod != 0f) {
                av *= 1f + (mod / 100f);
                if (m) new Debug("AV modifiziert durch Angreifer: " + mod + "% (" + waffe + ") = " + av);
                if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), "AV modifiziert durch Angreifer: " + mod + "% (" + waffe + ") = " + av);
            }
        }
		for(Weapon waffe : defender.weapons) {
            float mod = waffe.AttackModifikation_Defender(attacker, defender);
            if (mod != 0f) {
                av *= 1f + (mod / 100f);
                if (m) new Debug("AV modifiziert durch Verteidiger: " + mod + "% (" + waffe + ") = " + av);
                if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), "AV modifiziert durch Verteidiger: " + mod + "% (" + waffe + ") = " + av);
            }
        }

		// -- Defence-Value
        for(Weapon waffe : attacker.weapons) {
            float mod = waffe.DefenceModifikation_Attacker(attacker, defender);
            if (mod != 0f) {
                dv += dv * (mod / 100f);
                if (m) new Debug("DV modifiziert durch Angreifer: " + mod + "% (" + waffe + ") = " + dv);
                if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), "DV modifiziert durch Angreifer: " + mod + "% (" + waffe + ") = " + dv);
            }
        }
        for(Weapon waffe : defender.weapons) {
            float mod = waffe.DefenceModifikation_Defender(attacker, defender);
            if (mod != 0f) {
                dv += dv * (mod / 100f);
                if (m) new Debug("DV modifiziert durch Verteidiger: " + mod + "% (" + waffe + ") = " + dv);
                if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), "DV modifiziert durch Verteidiger: " + mod + "% (" + waffe + ") = " + dv);
            }
        }


		// -- durch die magischen Angriffseffekte iterieren und "auswerten"
		BattleEffectData bed = new BattleEffectData(attacker, defender, av, dv);
		for(BattleEffects efx : attacker.getEffects()) {
            if ((efx instanceof BattleEffectsAttack)) {
                if (m) new Debug("Der Angreifer steht unter dem Einfluss von " + efx);
                ((BattleEffects) efx).Calculate(bed);
            }
        }
		for (BattleEffects efx : defender.getEffects()) {
            if ((efx instanceof BattleEffectsDefence)) {
                if (m) new Debug("Der Verteidiger steht unter dem Einfluss von " + efx);
                ((BattleEffects) efx).Calculate(bed);
            }
        }
		dv = bed.getDefencevalue();
		av = bed.getAttackvalue();

		// -- mind. ein Punkt zum Angriff
		av = Math.round(av);
        if (av < 1) {
            av = 1;
            if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), "erhält einen Gnadenpunkt AV.");
        }
		// -- mind. ein Punkt zur Verteidigung
		dv = Math.round(dv);
        if (dv < 1){
            dv = 1;
            if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), defender + " erhält einen Gnadenpunkt DV.");
        }


        String fxMessage = bed.getMessage();
        if (fxMessage.length() > 0) retval.setVorwort(fxMessage);

        // (magische) Effekte können AV und DV auch auf 0 senken:
        attack_value = Math.round(av);
        defence_value = Math.round(dv);
        if (attack_value <= 0) attack_value = 0;
        if (defence_value <= 0) defence_value = 0;

        if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), "endgültiger AV=" + attack_value + ", DV=" + defence_value + " .");

        retval.setAv(attack_value);
        retval.setDv(defence_value);
		
		// jetzt entsprechend die Infos ausgeben
        if (m) battle.append("Angriff mit " + attacker.beschreibeWaffe(attack_waffe)).append("=Av" + attack_value );
        if (m) battle.append(" gegen ");
        if (m) battle.append(defender.beschreibeWaffe(defence_waffe)).append("=Dv" + defence_value );

        // TODO nicht jeder Beobachter soll das Talent sehen
        if (m) shorty.append( String.format("%-5s", attack_waffe.beschreibeKurz(true)) ).append("=").append(String.format("%2s", attack_value));
        if (m) shorty.append(" -> ");
        if (m) shorty.append( String.format("%-5s", defence_waffe.beschreibeKurz(true)) ).append("=").append(String.format("%2s", defence_value));


        // +*+ Angriff berechnen
		
		int wuerfelSeiten = attack_value + defence_value;
        int value = Random.rnd(1,  wuerfelSeiten + 1);
		retval.setWuerfelSeiten(wuerfelSeiten);
		retval.setGewuerfelt(value);

		if (m) battle.append(" - W" + wuerfelSeiten + " ergibt " + value + ", ");
        boolean attackSuccess = (value <= attack_value);
        if (DuellAnalyse.AKTIV && (attack_value > 0)) attackSuccess = true; // soll ja den Schaden berechnen, gell?

        retval.setErfolgreich(attackSuccess);

        if (m) {
			String s = String.format("%3s", value + (attackSuccess?"+":" "));
			shorty.append(" ").append(s).append(" ");
		}
        
		if (!attackSuccess) {
			// Angriff fehlgeschlagen ... *hehe* ... war wohl jemand besser (oder hat Glück gehabt)
			if (m) battle.append("geblockt.");

            ks.recordFehlschlag(attacker, defender);
            
            retval.setBeschreibung(battle.toString());
            retval.setKurzBeschreibung(shorty.toString());
			return retval;
		} else {
			if (m) battle.append("erfolgreich");
		}
		
		// +*+ Angriff war erfolgreich ... also Schaden berechnen
		
		int damagevalue = 0;
		int blockvalue = 0;
		// der Schaden wird nur von einer Waffe ausgeführt ... und zwar der Angriffswaffe !!
		if (attack_waffe != null) damagevalue = attack_waffe.DamageValue();
        if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), "unmodifizierter Schaden DMG=" + damagevalue + " (durchschnittlich " + attack_waffe.AverageDamageValue() + ").");

        // alle Waffen und Rüstungen tragen zum Block-Value bei
        for(Weapon waffe : defender.weapons) {
            int bv = waffe.BlockValue();
            if (bv > 0) if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), waffe + " liefert einen Schutz von BLK=" + bv + " (durchschnittlich " + waffe.AverageBlockValue() + ").");
            blockvalue += bv;
        }
        if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), "unmodifizierter Schutz BLK=" + blockvalue + ".");
        
		float dmg = (float) damagevalue;
		float blk = (float) blockvalue;

		// --- Modifikationen:

		// --- Schadensmodifikation
		for(Weapon waffe : attacker.weapons) {
            float coeff = (float)(waffe.DamageModifikation_Attacker(attacker, defender) / 100.0);
            if (Math.abs(coeff) > 1e-6) {
                float vorher = dmg;
                dmg *= (1f + coeff);

                if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), "DMG wird beim Angreifer durch " + waffe + " um " + (coeff*100) + "% modifiziert = " + blk + " .");
                if (m) new Debug("DMG wird von ATT " + attacker.kurzCode() + " durch " + waffe + " von " + vorher + " auf " + dmg + " verändert.");
            }
        }
		for(Weapon waffe : defender.weapons) {
            float coeff = (float)(waffe.DamageModifikation_Defender(attacker, defender) / 100.0);
            if (Math.abs(coeff) > 1e-6) {
                float vorher = dmg;
                dmg += dmg * coeff;

                if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), "DMG wird beim Verteidiger durch " + waffe + " um " + (coeff*100) + "% modifiziert = " + blk + " .");
                if (m) new Debug("DMG wird von DEF " + defender.kurzCode() + " durch " + waffe + " von " + vorher + " auf " + dmg + " verändert.");
            }
        }

		// --- Blockmodifikationen
		for(Weapon waffe : attacker.weapons) {
            float coeff = (float)waffe.BlockModifikation_Attacker(attacker, defender) / 100.0f;
            if (Math.abs(coeff) > 1e-6) {
                float vorher = blk;
                blk += blk * coeff;

                if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), "BLK wird beim Angreifer durch " + waffe + " um " + (coeff*100) + "% modifiziert = " + blk + " .");
                if (m) new Debug("BLK wird von ATT " + attacker.kurzCode() + " durch " + waffe + " von " + vorher + " auf " + blk + " verändert.");
            }
        }
		for(Weapon waffe : defender.weapons) {
            float coeff = (float)waffe.BlockModifikation_Defender(attacker, defender) / 100.0f;
            if (Math.abs(coeff) > 1e-6) {
                float vorher = blk;
                blk += blk * coeff;

                if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), "BLK wird beim Verteidiger durch " + waffe + " um " + (coeff*100) + "% modifiziert = " + blk + " .");
                if (m) new Debug("BLK wird von DEF " + defender.kurzCode() + " durch " + waffe + " von " + vorher + " auf " + blk + " verändert.");
            }
        }

		// -- durch die magischen Verteidigungseffekte iterieren und "auswerten"
		bed = new BattleEffectData(attacker, defender, dmg, blk);
		for(Object efx : attacker.getEffects()) if (efx instanceof BattleEffectsDamage) ((BattleEffectsDamage) efx).Calculate(bed);
		for(Object efx : defender.getEffects()) if (efx instanceof BattleEffectsBlock) ((BattleEffectsBlock) efx).Calculate(bed);
		dmg = bed.getAttackvalue();
		blk = bed.getDefencevalue();
		// -- mind. ein Punkt (außer es war schon 0!)
		damagevalue = (dmg > 0  && (int) dmg == 0) ? 1 : Math.round(dmg);
		blockvalue = (blk > 0  && (int) blk == 0) ? 1 : Math.round(blk);

        if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(attacker.getUnit(), "endgültige DMG=" + damagevalue + ", BLK=" + blockvalue + " .");

        // damage und block stehen fest!

        retval.setDamageValue(damagevalue);
        retval.setBlockValue(blockvalue);

		int damage = 0;
        battle.append(" - ");
        String s = "      ";
        if (damagevalue > 0) {
            if (blockvalue > 0) {
                if (m) battle.append(damagevalue + " Treffer, ");
                if (m) battle.append(blockvalue + " abgefangen, ");

                damage = damagevalue - blockvalue;
                if (damage < 0) damage = 0;

                if (m) s = String.format("%6s", (damagevalue + "-" + blockvalue));
            } else {
                damage = damagevalue;
            }
        } else {
            damage = 0;
        }
        if (m) shorty.append(s);

        if (damage > 0) {
            retval.setSchaden(damage);
            if (m) battle.append(damage + " Punkte Schaden.");
            if (m) shorty.append(String.format("%4s", damage)).append("D");
        } else {
            if (m) battle.append("kein Schaden.");
            if (m) shorty.append("     ");
        }

        if (m) shorty.append(" ");



        // Lebenspunkte dazuzählen ... da 0 Lebenspunkte super sind !!
		// quasi - je mehr Lebenspunkte um so toter ist der Krieger
		if (damage > 0) {
            int lp = defender.getLebenspunkte();
            defender.setLebenspunkte(lp + damage); // voller Wert ... ist egal, Krieger stirbt ja eh
            if (m) battle.append(" Verwundung von " + lp + " auf " + defender.getLebenspunkte() + " von max. " + defender.getTrefferpunkte() + ".");

            if (m) shorty.append(String.format("%-9s", lp + ">" + defender.getLebenspunkte() + ">" + defender.getTrefferpunkte()));

            if (defender.getLebenspunkte() >= defender.getTrefferpunkte()) {
				defender.removeWeapons();
                retval.setTot(true);
                if (m) shorty.append(" -- " + defender.kurzCode() + "t");
                
                // pro Kill pauschal 10 Punkte - TODO Unterschiede machen - wie auch immer ?!
                Partei p = Partei.getPartei(attacker.getUnit().getOwner());
                int punkte = p.getIntegerProperty("punkte.krieg", 0);
                punkte += 10;
                p.setProperty("punkte.krieg", punkte);
            }
        } else {
            if (m) shorty.append(String.format("%-9s", "(" + defender.getLebenspunkte() + ">" + defender.getTrefferpunkte() + ")"));
        }
		
        if (attackSuccess) {
            ks.recordSchlag(attacker, defender, damage);
        } else {
            ks.recordFehlschlag(attacker, defender);
        }

        retval.setBeschreibung(battle.toString());
        retval.setKurzBeschreibung(shorty.toString());
        
        // Magische Effekte, die bei einem Attacke abklingen, klingen hier ab.
        for(BattleEffects efx : attacker.getEffects()) {
        	efx.setEffectDownFor(Gefecht.BattleTime.ATTACK_ATTACKER);
        }
        // Magische Effecte, die bei der Verteidigung abklingen, klingen hier ab.
        for(BattleEffects efx : defender.getEffects()) {
        	efx.setEffectDownFor(Gefecht.BattleTime.ATTACK_DEFENDER);
        }
        
        
        return retval;
	}

    public boolean istTot() {
        return getLebenspunkte() >= getTrefferpunkte();
    }

    /**
	 * sucht die beste Waffe für diesen Angriff
	 * @param defenderReihe - normalerweise 1 (vorne); wenn kein Gegner mehr vorne steht, kann der Wert 2 (hinten) sinnvoll sein. Generell ist der Parameter nur ein Vorschlag und wird ggf. in der Methode geändert.
	 * @return die Waffe mit dem höchsten AV gegenüber dem Opfer
	 */
	protected Weapon angriffsWaffeWaehlen(int zielReihe) {
        __usedWeapon = null;
		float maxWert = 0;

        boolean willFernkaempfen = false;
        // wenn ich eine Fernkampf-Waffe habe und hinten stehe, will ich auch fernkämpfen:
        if (this.hatFernwaffe() && (this.getReihe() == Krieger.REIHE_HINTEN)) willFernkaempfen = true;
        // wenn ich NUR Fernwaffen habe, dann will ich sie auch benutzen:
        if (this.hatFernwaffe() && (!this.hatNahkampfWaffe())) willFernkaempfen = true;
        // wenn ich beides habe, aber hinten angreifen will:
        if ((this.hatFernwaffe() && this.hatNahkampfWaffe()) && (zielReihe == Krieger.REIHE_HINTEN)) willFernkaempfen = true;

        if ((this.getReihe() == Krieger.REIHE_VORNE) && willFernkaempfen) zielReihe = Krieger.REIHE_HINTEN;
        if ((this.getReihe() == Krieger.REIHE_HINTEN) && willFernkaempfen) {
            // falls die Zielreihe explizit hinten sein soll, bleibt sie
            if (zielReihe != Krieger.REIHE_HINTEN) zielReihe = Krieger.REIHE_VORNE;
        }

        if (DuellAnalyse.AKTIV) {
            if (willFernkaempfen) {
                DuellAnalyse.I().message(getUnit(), "versucht einen Fernangriff, Zielreihe: " + Krieger.ReiheName(zielReihe));
            } else {
                if (this.getReihe() == Krieger.REIHE_VORNE) DuellAnalyse.I().message(getUnit(), "geht in den Nahkampf.");
                if (this.getReihe() == Krieger.REIHE_HINTEN) DuellAnalyse.I().message(getUnit(), "steht ohne Fernkampfwaffe hinten.");
            }
        }

        int meineReihe = this.getReihe();
        if (meineReihe == REIHE_NICHT) meineReihe = REIHE_VORNE; // für den Fall, dass wir doch ins Gefecht gezogen werden:
        
		// Angriffstärke berechnen
		for(Weapon waffe : this.weapons) {
            if (waffe.getWeaponType() == WeaponType.Panzer) continue;
            if (waffe.getWeaponType() == WeaponType.Schild) continue;
            if (willFernkaempfen && !waffe.istFernkampfTauglich()) {
                if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(getUnit(), "benutzt nicht " + waffe + ", weil nicht geeignet für den Fernkampf.");
                continue;
            }
			int currentAV = waffe.AttackValue(meineReihe, zielReihe);

            int currentN = 0;
            if (waffe instanceof ITeureWaffe) {
                // ggf. nicht die Kosten "abbuchen"
                currentN = ((ITeureWaffe)waffe).numberOfAttacksNurZurInfo();
            } else {
                currentN = waffe.numberOfAttacks();
            }

            float currentAvgDmg = waffe.AverageDamageValue();

            float wert = (float)currentN * (float)currentAV * currentAvgDmg;
            if (Krieger.DEBUG_WAFFEN) new Debug(this + " könnte eine Waffe verwenden: " + waffe + " Av: " + currentAV + "*" + currentN + ", avgDmg: " + currentAvgDmg + ", Wert: " + wert);
            if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(getUnit(), "könnte eine Waffe verwenden: " + waffe + " AV " + currentAV + "*" + currentN + ", durchschnittliche DMG: " + currentAvgDmg + ", Wert: " + wert + " .");


			if (__usedWeapon == null) {
				__usedWeapon = waffe;
				maxWert = wert;
			} else {
				if (wert > maxWert) {
					__usedWeapon = waffe;
					maxWert = wert;
				}
			}
		}
        if (__usedWeapon == null) __usedWeapon = new WWaffenlos(unit);

        if (Krieger.DEBUG_WAFFEN) new Debug(this + " wählt die Angriffswaffe: " + __usedWeapon + ", Wert: " + maxWert);
        if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(getUnit(), "wählt die Angriffswaffe " + __usedWeapon + ", Wert: " + maxWert + " .");

        return __usedWeapon;
	}

	/**
	 * sucht die passende Verteidigungswaffe
	 * @param attacker - der böse Angreifer
	 * @return 
	 */
	private int GetDefenceWeapon(Krieger attacker) {
        __usedWeapon = null;
		int dv = 0;
		
		// Verteidigungsstärke berechnen
		for(Weapon waffe : this.weapons) {
			int current_dv = waffe.DefenceValue(this.reihe, attacker.reihe);
			if (__usedWeapon == null) {
				__usedWeapon = waffe;
				dv = current_dv;
			} else {
				if (current_dv > dv) {
					__usedWeapon = waffe;
					dv = current_dv;
				}
			}
		} 
		return dv;
	}

    public String getDienstgrad() {
        Weapon w = null;
        // die default-Waffe gegen die erste Reihe des Feindes:
        w = angriffsWaffeWaehlen(Krieger.REIHE_VORNE);
        
        WeaponType t = w.getWeaponType();
        if (t == WeaponType.keineWaffe) return "Waffenlose";
        if (t == WeaponType.Nahkampf) return "Nahkämpfer";
        if (t == WeaponType.Distanzkampf) return "Pikeniere";
        if (t == WeaponType.Fernkampf) return "Schützen";
        if (t == WeaponType.Tier) {
            if (!getUnit().istSpielerRasse()) return "Monster";
            return "Reiter";
        }
        return "Exotische";
    }
	
	/**
	 * gibt die Ausrüstung eines Kriegers zurück an seine Einheit
	 */
	public void removeWeapons()	{
		for(Weapon waffe : weapons)	{
			// jede Waffe kommt nur einmal pro Krieger vor, wenn sie vorkommt ... spezielle Waffen, wie sie
			// Monster benutzen, fallen einfach unter den Tisch - da die Waffen auch einfach da sind
            Class<? extends Item> ursprung = waffe.getUrsprungsItem();
            if (ursprung == null) {
                // throw new RuntimeException("Ursprungs-Item zu Waffe " + w.getSimpleName() + " ist nicht bekannt.");
            } else {
                unit.addItem(ursprung, 1);
            }
		}
        weapons.clear();

//        List<String> items = new ArrayList<String>();
//        for (Item it : unit.getItems()) items.add(it.toString());
//        new SysMsg(unit + " hat nach dem Kampf: " + StringUtils.aufzaehlung(items) + ".");
	}

    public String beschreibeWaffe(Weapon w) {
        StringBuilder retval = new StringBuilder();
        if (w != null) {
            retval.append(w.toString());
            if ((w.neededSkill() != null) && (w.neededSkill() != Skill.class)) {
                int tw = this.getUnit().Talentwert(w.neededSkill());
                retval.append(" T").append(tw);
            }
        } else {
            retval.append("unbewaffnet");
        }

        return retval.toString();
    }

    /**
     * @return Eine Auflistung der Ausrüstungsgegenstände UND (sofern vorhanden) spezieller kampfrelevanter Talente - Taktik und Magie
     */
    public String beschreibeAusruestung(boolean talenteZeigen) {
        StringBuilder retval = new StringBuilder();
        if (talenteZeigen) {
            for (Skill sk : this.getUnit().getSkills()) {
                if (sk.getLerntage() == 0) continue;
                if (
                        (sk instanceof Taktik)
                        || (sk instanceof Magie)
                ) {
                    if (retval.length() > 0) retval.append(" ");
                    retval.append(sk.getClass().getSimpleName()).append("T").append(this.getUnit().Talentwert(sk)).append("");
                }
            }
        }
        if (!this.weapons.isEmpty()) {
            for (Weapon w : this.weapons) {
                if (retval.length() > 0) retval.append(" ");
                retval.append("[").append(w.beschreibeKurz(talenteZeigen)).append("]");
            }
        } else {
            if (retval.length() > 0) retval.append(" ");
            retval.append("[keine Ausrüstung]");
        }
		if (talenteZeigen && (!this.effects.isEmpty())) {
            if (retval.length() > 0) retval.append(" ");
			retval.append("[Effekte: " + StringUtils.aufzaehlung(this.effects) + "]");
		}
        return retval.toString();
    }


    @Override
    public String toString() {
        if (this.getUnit().getPersonen() == 1) {
            return this.getUnit() + (effects.isEmpty()?"":" (" + StringUtils.aufzaehlung(effects) + ")");
        } else {
            return "Nr. " + this.getIndex() + " von " + this.getUnit() + (effects.isEmpty()?"":" (" + StringUtils.aufzaehlung(effects) + ")");
        }
    }

    public String detailedToString(boolean talenteZeigen) {
        return toString() + ", Rh. " + getReihe() + " mit " + beschreibeAusruestung(talenteZeigen);
    }

    /**
     * @return Eindeutiger Code dieses Kriegers, bestehend aus: <Einheit-ID(base36)>.<laufende Nummer der Person (des einzelnen Kriegers) in der Einheit(base10)>
     */
    public String kurzCode() {
        if (this.getUnit().getPersonen() == 1) return getUnit().getNummerBase36();
        return getUnit().getNummerBase36() + "." + getIndex();
    }

	/**
	 * @param alle Die Liste der Einzelkrieger.
	 */
	public static String HitpointReport(List<Krieger> alle) {
		if (alle.isEmpty()) {
			new SysMsg("Warnung: HitpointReport für eine leere Liste angefordert.");
			return "";
		}

		// sortieren: Die am schwersten verletzten zuerst
		Collections.sort(alle, Collections.reverseOrder(new KriegerLebenspunkteComparator()));
		
		StringBuilder sb = new StringBuilder();

		int i=0;
		int summeLeben = 0;
		int summeTreffer = 0;
		sb.append("(");
		for (Krieger k : alle) {
			if (i>0) sb.append("|");
			sb.append(k.getLebenspunkte() + "/" + k.getTrefferpunkte());

			summeLeben += k.getLebenspunkte();
			summeTreffer += k.getTrefferpunkte();

			i ++;
		}
		sb.append(")");

		return ""  + summeLeben + "/" + summeTreffer + "HP " + sb;
	}

	public static String HitpointSymbols(List<Krieger> alle) {
        return HitpointSymbols(alle, false);
    }
    /**
	 * @param alle Die Liste der Einzelkrieger.
	 */
	public static String HitpointSymbols(List<Krieger> alle, boolean html) {
		if (alle.isEmpty()) {
			new SysMsg("Warnung: HitpointSymbols für eine leere Liste angefordert.");
			return "";
		}

		// sortieren: Die am schwersten verletzten zuerst
        if (!html) {
            Collections.sort(alle, Collections.reverseOrder(new KriegerLebenspunkteComparator()));
        } else {
            Collections.sort(alle, Collections.reverseOrder(new KriegerReiheLebenspunkteComparator()));
        }

		StringBuilder sb = new StringBuilder();
		int prvReihe = -1;
        int i=0;
		for (Krieger k : alle) {
			float ratio = (float)k.getLebenspunkte() / (float)k.getTrefferpunkte();

            if (html) {
                if (k.getReihe() != prvReihe) {
                    if (i > 0) sb.append("</span>");
                    sb.append("<span \nclass=\"reihe" + k.getReihe() + "\">");
                }
                prvReihe = k.getReihe();
            }

			if (ratio < 0.000001) {
				sb.append('.');
			} else if (ratio < 0.33333) {
				sb.append('o');
			} else if (ratio < 0.66667) {
				sb.append('O');
			} else {
				sb.append('@');
			}

			i++;
			if ((i % 50) == 0) sb.append(" "); // für den NR...
		}
        if (html) sb.append("</span>\n");

		return sb.toString();
	}

    public static Set<Unit> Units(List<Krieger> krieger) {
        Set<Unit> units = new HashSet<Unit>();
        for (Krieger k:krieger) units.add(k.getUnit());
        return units;
    }

    /**
     * @param krieger
     * @return menschenlesbare Aufzählung aller in krieger vorkommenden verschiedenen "Dienstgrade"
     */
    public static String DienstgradeBeschreibung(List<Krieger> krieger) {
        Map<String, Integer> counts = new HashMap<String, Integer>();
        // StringBuffer sb = new StringBuffer();
        for (Krieger k : krieger) {
            // sb.append(k.getReihe());
            String dienstgrad = k.getDienstgrad();
            if (!counts.containsKey(dienstgrad)) counts.put(dienstgrad, 0);
            counts.put(dienstgrad, counts.get(dienstgrad) + 1);
        }
        List<String> beschr = new ArrayList<String>();
        for (String dienstgrad : counts.keySet()) {
            int cnt = counts.get(dienstgrad);
            beschr.add(cnt + " " + dienstgrad);
        }
        return StringUtils.aufzaehlung(beschr); // + sb.toString() + "\n";
    }

	/**
	 * berechnet die Distanz zwischen zwei Kriegern auf Grund der Reihe
	 * <ol start="0">
	 * <li>Nahkampf, also 1 on 1</li>
	 * <li>über eine Reihe hinweg (von hinten nach vorne oder von vorne nach hinten)</li>
	 * <li>über 2 Reihen hinweg (von hinten nach hinten)</li>
	 * </ol>
	 * @param reihe_attacker
	 * @param reihe_defender
	 * @return Distanz zwischen der Kriegern
	 */
	public static int Distanz(int reihe_attacker, int reihe_defender)	{
		return ((reihe_attacker - 1) + (reihe_defender - 1));
	}

    public class Schlag {
        protected final int sequenz;
        protected final Krieger krieger;

        public Schlag(int sequenz, Krieger krieger) {
            this.sequenz = sequenz;
            this.krieger = krieger;
        }

        public Krieger getKrieger() {
            return krieger;
        }

        public int getSequenz() {
            return sequenz;
        }
    }
}
