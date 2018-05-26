package de.x8bit.Fantasya.Host.EVA;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Steuerturm;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Steuereintreiben;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;

public class Steuertuerme extends EVABase implements NotACommand
{
	/**
	 * eine Liste aller Erpresser (pro Region)
	 */
	private SortedSet<Unit> treiber;

	/**
	 * eine Map aller Händler zu ihrem jeweiligen Brutto-Einkommen (pro Region)
	 */
	private Map<Unit, Integer> haendler;

	/**
	 * alle Partei-Paarungen mit Relevanz (pro Region)
	 */
	private Set<TributVerhaeltnis> tribute = new HashSet<TributVerhaeltnis>();


	public Steuertuerme() {
		super("treibe", null);
		
		// keine eigenen Templates ... Befehle ist TREIBEn
	}
	
	public void DoAction(Region r, String befehl) {	}

//	/**
//	 * sammelt die "freiwilligen" Steuern von den fremden Einheiten ... dazu werden
//	 * alle Eintreiber durchgegangen ... 20 * TW * Personen an Silber kann jede Einheit
//	 * einsammeln von den Händlern ... also werden hier die Eintreiber von vorne nach
//	 * hinten abgearbeitet und bei Bedarf wird der Händler gewechselt ... der Bedarf
//	 * tritt ein, wenn ein Eintreiber sein Einkommen eben erreicht hat :)
//	 */
//	private void Zahltag() {
//		for (Unit t : treiber) {
//			int max = t.getPersonen() * t.Talentwert(Steuereintreiben.class) * 20;
//
//			for (Unit h : haendler.keySet()) {
//				int steuer = Einkommen(h, t);
//
//				// der Treiber erreicht sein Limit?
//				if (t.getEinkommen() + steuer > max) steuer = max - t.getEinkommen();
//
//				// der Händler hat nicht genug Geld?
//				if (h.getItem(Silber.class).getAnzahl() - steuer < 0) {
//					steuer = h.getItem(Silber.class).getAnzahl();
//				}
//
//				// den Steuerbetrag dem Eintreiber gutschreiben...
//				t.setEinkommen(t.getEinkommen() + steuer);
//				t.setItem(Silber.class, t.getItem(Silber.class).getAnzahl() + steuer);
//
//				// ... und dem Steueropfer abziehen.
//				h.setEinkommen(h.getEinkommen() - steuer);
//				h.setItem(Silber.class, h.getItem(Silber.class).getAnzahl() - steuer);
//
//				if (t.getEinkommen() == max) break;
//			} // nächster Händler
//
//		} // nächster Eintreiber
//
//	}
	
//	/**
//	 * berechnet das zu versteuernde Einkommen dieser Einheit
//	 * @param steuerpflichtiger - Händler
//	 * @param finanzbeamter - Treiber
//	 * @return
//	 */
//	private int Einkommen(Unit steuerpflichtiger, Unit finanzbeamter)
//	{
//		Partei staat = Partei.getPartei(finanzbeamter.getOwner());
//		Steuer s = staat.getSteuern(steuerpflichtiger.getOwner());
//		int e = (int) ((float) haendler.get(steuerpflichtiger) * ((float)s.getRate() / 100.0));
//		return e == 0 ? 1 : e;
//	}
	
	private Set<Partei> getParteien(SortedSet<Unit> units) {
		Set<Partei> retval = new HashSet<Partei>();
		for (Unit u : units) {
			retval.add(Partei.getPartei(u.getOwner()));
		}
		return retval;
	}

	private Set<Partei> getParteien(Set<Unit> units) {
		SortedSet<Unit> unitList = new TreeSet<Unit>();
		unitList.addAll(units);
		return getParteien(unitList);
	}

	public boolean DoAction(Unit u, String[] befehl) { return false; }
	public void PostAction() {
		Set<Region> regions = new HashSet<Region>();
		for (Building b : Building.PROXY) {
			if (b.getClass() == Steuerturm.class) {
				regions.add(Region.Load(b.getCoords()));
			}
		}

		new ZATMsg("Steuern von anderen Völkern erpressen in " + regions.size() + " Regionen.");

		for (Region r : regions) {

			// Ausnahme! Nicht für diese Klasse suchen, sondern (nochmal)
			// für SteuernErpressen.class
			List<Einzelbefehl> befehle =
					BefehlsSpeicher.getInstance().get(SteuernErpressen.class, r.getCoords());

			// alle Steuerbeamten sammeln:
			treiber = new TreeSet<Unit>();

			for (Einzelbefehl eb : befehle) {
				// wurde der TREIBE-Befehl schon beim normalen Steuereintreiben abgearbeitet?
				if (eb.isPerformed()) continue;

				// Angaben über die "Akteure" selbst:
				Unit u = eb.getUnit();

				if (u.getGebaeude() == 0) continue; // ist in keinem Gebäude

				Building b = Building.getBuilding(u.getGebaeude());
				if (b.getClass() != Steuerturm.class) continue; // ist in keinem Steuerturm

				treiber.add(u);
				eb.setPerformed();
			}

			if (treiber.isEmpty()) return; // keine Eintreiber in einem Steuerturm vorhanden
			new Debug(treiber.size() + " Treiber");



			Set<Partei> besteuerer = getParteien(treiber);

			haendler = new HashMap<Unit, Integer>();
			// sammelt alle Händler in der Region ... Händler sind alle Einheiten
			// die ein Einkommen vorweisen können
			for(Unit unit : r.getUnits()) {
				if (unit.getEinkommen() > 0) {
					haendler.put(unit, unit.getEinkommen());
				}
			}
			new Debug(haendler.size() + " Händler");

			Set<Partei> besteuerte = getParteien(haendler.keySet());

			// alle Tribut-Paarungen bestimmen:
			tribute = new HashSet<TributVerhaeltnis>();
			for (Partei nehmer : besteuerer) {
				for (Partei geber : besteuerte) {
					// das sind wird selbst!
					if (nehmer.getNummer() == geber.getNummer()) continue;

					// die Geber genießen Steuerfreiheit
					if (nehmer.hatAllianz(geber.getNummer(), AllianzOption.Steuern)) continue;

					int rate = nehmer.getSteuern(geber.getNummer()).getRate();
					new Debug("Rate von " + nehmer + " für " + geber + " ist " + rate + " Prozent.");
					tribute.add(new TributVerhaeltnis(nehmer, geber, rate));
				}
			}

			new Debug(tribute.size() + " TVs");


			// Gesamtumfang der Abgaben bestimmen
			Map<Partei, Integer> gesamtEinnahmenTheoretisch = new HashMap<Partei, Integer>();
			for (Partei p : besteuerer) gesamtEinnahmenTheoretisch.put(p, 0);

			Map<Partei, Integer> gesamtAbgabenTheoretisch = new HashMap<Partei, Integer>();
			
			for (Partei geber : besteuerte) {
				gesamtAbgabenTheoretisch.put(geber, 0);
				for (TributVerhaeltnis tv : tribute) {
					if (tv.getGeber().getNummer() != geber.getNummer()) continue;

					for (Unit stpfl : haendler.keySet()) {
						if (stpfl.getOwner() != geber.getNummer()) continue;
						Partei nehmer = tv.getNehmer();

						int abgabe = (int) ((float) haendler.get(stpfl) * tv.getSteuersatz() + 0.5);

						gesamtAbgabenTheoretisch.put(geber, gesamtAbgabenTheoretisch.get(geber) + abgabe);
						tv.addSilberTheoretisch(abgabe);
						gesamtEinnahmenTheoretisch.put(nehmer, gesamtEinnahmenTheoretisch.get(nehmer) + abgabe);
					}
				}
			}


			// Gesamtkapazitäten der Eintreiber bestimmen
			Map<Partei, Integer> steuerKapazitaeten = new HashMap<Partei, Integer>();
			for (Partei nehmer : besteuerer) {
				steuerKapazitaeten.put(nehmer, 0);
				for (Unit u : treiber) {
					if (u.getOwner() == nehmer.getNummer()) {
						int max = u.getPersonen() * u.Talentwert(Steuereintreiben.class) * 20;
						steuerKapazitaeten.put(nehmer, steuerKapazitaeten.get(nehmer) + max);
					}
				}
			}

			/*
			 * Zwischen-Resumee:
			 *
			 *  tribute enthält alle de iure festgelegten Tributpaarungen, die
			 *  Steuerrate und auch den Betrag der fälligen Steuern.
			 *
			 *  gesamtAbgabenTheoretisch enthält für alle Geber-Parteien den
			 *  Gesamtbetrag der Abgaben. Dieser kann derzeit durchaus größer
			 *  als 100% der Einnahmen sein, weil die Steuern aller "Erpresser"
			 *  addiert werden.
			 *
			 *  gesamtEinnahmenTheoretisch enthält für alle Nehmer-Parteien den
			 *  Gesamtbetrag der Abgaben.
			 *
			 *  steuerKapazitaeten enthält für alle Nehmer-Parteien die maximale
			 *  Eintreibefähigkeit an Silber.
			 */

			// müssen bestimmte Steuern reduziert werden, weil die Eintreiber nicht hinterherkommen?
			// oder zu welchem Anteil können die Entreiber ihre Fähigkeiten ausschöpfen?
			for (Partei nehmer : besteuerer) {
				// mehr Steuern als Fähigkeit:
				if (steuerKapazitaeten.get(nehmer) < gesamtEinnahmenTheoretisch.get(nehmer)) {
					float quote = (float)steuerKapazitaeten.get(nehmer) / (float)gesamtEinnahmenTheoretisch.get(nehmer);

					for (TributVerhaeltnis tv : tribute) {
						if (tv.getNehmer().getNummer() != nehmer.getNummer()) continue;

						tv.setSteuersatzEffektiv(tv.getSteuersatz() * quote);
					}
				}
				// mehr Fähigkeit als Steuern:
				if (steuerKapazitaeten.get(nehmer) > gesamtEinnahmenTheoretisch.get(nehmer)) {
					float quote = (float)gesamtEinnahmenTheoretisch.get(nehmer) / (float)steuerKapazitaeten.get(nehmer);

					for (TributVerhaeltnis tv : tribute) {
						if (tv.getNehmer().getNummer() != nehmer.getNummer()) continue;

						tv.setEintreibeEffizienz(quote);
					}
				}
			}


			NumberFormat nf = NumberFormat.getPercentInstance();
			nf.setMinimumFractionDigits(1);
			nf.setMaximumFractionDigits(1);

			// es ist so weit:
			for (TributVerhaeltnis tv : tribute) {
				Partei geber = tv.getGeber();
				float effektiverSatz = tv.getSteuersatzEffektiv();
				for (Unit u : haendler.keySet()) {
					if (u.getOwner() != geber.getNummer()) continue;

					int steuer = (int) ((float) u.getEinkommen() * effektiverSatz + 0.5);
					u.setEinkommen(u.getEinkommen() - steuer);
					u.setItem(Silber.class, u.getItem(Silber.class).getAnzahl() - steuer);

					new Info(u + " muss bei einem effektiven Steuersatz von "
							+ nf.format(tv.getSteuersatzEffektiv()) + " "
							+ steuer + " Silber an " + tv.getNehmer() + " zahlen.",
							u, u.getCoords()
					);
				}
			}

			for (TributVerhaeltnis tv : tribute) {
				Partei nehmer = tv.getNehmer();
				float effizienz = tv.getEintreibeEffizienz();
				for (Unit u : treiber) {
					if (u.getOwner() != nehmer.getNummer()) continue;

					int max = u.getPersonen() * u.Talentwert(Steuereintreiben.class) * 20;
					int einnahmen = (int) ((float)max * effizienz + 0.5);
					u.setEinkommen(u.getEinkommen() + einnahmen);
					u.setItem(Silber.class, u.getItem(Silber.class).getAnzahl() + einnahmen);

					new Info(u + " treibt bei einem effektiven Steuersatz von " 
							+ nf.format(tv.getSteuersatzEffektiv()) + " "
							+ einnahmen + " Silber von " + tv.getGeber() + " ein.",
							u, u.getCoords()
					);
				}
			}

		} // nächste Region
	}

	public void PreAction() { }
    public void DoAction(Einzelbefehl eb) { }



	public class TributVerhaeltnis {
		final Partei nehmer;
		final Partei geber;
		/**
		 * Steuersatz de iure als Bruchteil von 1 - also nicht als Prozent!
		 */
		final float steuersatz;

		/**
		 * Summe aller theoretisch fälligen Steuern in diesem Tributverhältnis,
		 * in dieser Region. Wird nicht unbedingt vollständig abgeführt, das
		 * hängt davon ab, ob die Steuereintreiber dazu in der Lage sind.
		 */
		int silberTheoretisch = 0;

		/**
		 * Steuersatz de facto als Bruchteil von 1 - also nicht als Prozent!
		 * Der effektive Steuersatz kann sich vom theoretischen unterscheiden,
		 * wenn die Steuereintreiber nicht in der Lage sind, die Steuern
		 * komplett einzutreiben.
		 */
		float steuersatzEffektiv;

		/**
		 * Quote 0..1 die angibt, wie sehr die Fähigkeiten der Steuereintreiber
		 * ausgeschöpft werden können.
		 */
		float eintreibeEffizienz;

		/**
		 * @param nehmer
		 * @param geber
		 * @param steuersatz Steuersatz als Bruchteil von 1 - also nicht als Prozent!
		 */
		public TributVerhaeltnis(Partei nehmer, Partei geber, int steuersatz) {
			this.nehmer = nehmer;
			this.geber = geber;
			this.steuersatz = (float)steuersatz / 100f;
			this.steuersatzEffektiv = this.steuersatz;
			this.eintreibeEffizienz = 1;
		}

		public Partei getGeber() {
			return geber;
		}

		public Partei getNehmer() {
			return nehmer;
		}

		public float getSteuersatz() {
			return steuersatz;
		}

		public void addSilberTheoretisch(int silber) {
			silberTheoretisch += silber;
		}

		public int getSilberTheoretisch() {
			return silberTheoretisch;
		}

		public float getSteuersatzEffektiv() {
			return steuersatzEffektiv;
		}

		public void setSteuersatzEffektiv(float steuersatzEffektiv) {
			this.steuersatzEffektiv = steuersatzEffektiv;
		}

		public float getEintreibeEffizienz() {
			return eintreibeEffizienz;
		}

		public void setEintreibeEffizienz(float eintreibeEffizienz) {
			this.eintreibeEffizienz = eintreibeEffizienz;
		}

	}

}
