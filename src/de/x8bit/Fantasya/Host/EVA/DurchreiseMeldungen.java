package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import java.util.HashSet;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Bewegung;
import de.x8bit.Fantasya.Host.EVA.util.Reisen.DurchreiseRecord;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.Reisen;

/**
 * @author hb
 */
class DurchreiseMeldungen extends EVABase implements NotACommand {
    protected Set<Record> sichtungen = new HashSet<Record>();

    public DurchreiseMeldungen() {
        super("Durchreisende...");

		for (DurchreiseRecord dr : Reisen.durchReisen) {
			Region region = Region.Load(dr.getCoords());
            Unit reisender = Unit.Load(dr.getUnitId());
			
			if (reisender == null) {
				// oh, die Einheit existiert nicht mehr?
				continue;
			}

			if (reisender.getCoords().equals(region.getCoords())) {
				// die derzeitige Region der Einheit - hier braucht es keine Meldung
				continue;
			}

			Partei rp = Partei.getPartei(reisender.getOwner());

			// so, alle (fremden?) Kandidaten-Parteien sammeln, die in dieser Region sind:
			boolean ertappt = false;
			for (Unit beob : region.getUnits()) {
				// TODO: sollen die eigenen Einheiten auch berichtet werden?
				if (beob.getOwner() == reisender.getOwner()) continue;

				// Getarnte werden nur ertappt, wenn talentierte Wahrnehmer anwesend sind.
				if (!beob.couldSeeInRegion(reisender, region)) continue;

				// Einheiten, die sich im selben Schiff befinden, nicht benachrichtigen:
				// (und auch nicht als gesehen-werden werten)
				if (beob.getSchiff() > 0) {
					if (beob.getSchiff() == reisender.getSchiff()) continue;
				}

				// wenn der Reisende eine Allianz mit dem Beobachter hat, fühlt er sich nicht beobachtet
				if (!rp.hatAllianz(beob.getOwner(), AllianzOption.Kontaktiere)) {
					ertappt = true;
				}

				// Alliierte mit KONTAKTIEREN nicht benachrichtigen:
				if (Partei.getPartei(beob.getOwner()).hatAllianz(reisender.getOwner(), AllianzOption.Kontaktiere)) continue;
				

				sichtungen.add(new Record(beob.getOwner(), region, reisender));
			}

			if (ertappt) {
				// ... den Reisenden warnen:
				String r = region + rp.getPrivateCoords(region.getCoords()).xy();

				new Bewegung(reisender + " ist bei der Durchreise in " + r + " beobachtet worden.", reisender);
			}
		}


        // ... und jetzt alle verpetzen:
        for (Record rec : sichtungen) {
            Region r = rec.getRegion();
            Partei p = Partei.getPartei(rec.getPartei());

            String region = r + p.getPrivateCoords(r.getCoords()).xy();

            if (rec.getReisender().getSchiff() == 0) {
                // Reise zu Lande
                new Bewegung("Beobachter melden: " + rec.getReisender() + " ist diesen Monat durch " + region + " gereist.",
                        p, r.getCoords());
            } else {
                // Reise im Schiff (egal, ob zu Lande)
                Ship ship = Ship.Load(rec.getReisender().getSchiff());
                if (ship != null) {
                    new Bewegung("Beobachter melden: " + ship + " ist diesen Monat durch " + region + " gefahren.",
                            p, r.getCoords());
                }
            }
        }

        // und Speicher freigeben.
        sichtungen.clear();
		Reisen.durchReisen.clear();
    }

    @Override
    public boolean DoAction(Unit u, String[] befehl) {
        return false;
    }

    @Override
    public void DoAction(Region r, String befehl) { }

    @Override
    public void PreAction() { }

    @Override
    public void PostAction() { }


    

    private class Record {
        protected final int partei;
        protected final Region region;
        protected final Unit reisender;

        public Record(int partei, Region region, Unit reisender) {
            this.partei = partei;
            this.region = region;
            this.reisender = reisender;
        }

        public int getPartei() {
            return partei;
        }

        public Region getRegion() {
            return region;
        }

        public Unit getReisender() {
            return reisender;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + this.partei;
            hash = 41 * hash + (this.region != null ? this.region.hashCode() : 0);
            hash = 41 * hash + (this.reisender != null ? this.reisender.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof Record)) return false;
            
            Record other = (Record) o;
            if (this.getPartei() != other.getPartei()) return false;
            if (!this.getRegion().equals(other.getRegion())) return false;
            if (!this.getReisender().equals(other.getReisender())) return false;

            return true;
        }

    }

    @Override
	public void DoAction(Einzelbefehl eb) { }

}
