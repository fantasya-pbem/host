package de.x8bit.Fantasya.Host.EVA.util;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Host.EVA.BefehleEinlesen;
import de.x8bit.Fantasya.util.StringUtils;

/**
 *
 * @author hapebe
 */
public class BefehlsCheck {
	final Set<BefehlsCheckInfo> checkSet;

	final static BefehlsCheck instance = new BefehlsCheck();

	private BefehlsCheck() {
		this.checkSet = new HashSet<BefehlsCheckInfo>();
	}

	public static BefehlsCheck getInstance() {
		return instance;
	}

	public void addFile(File f) {
		if (!f.exists()) {
			throw new RuntimeException("Befehlsdatei " + f + " existiert nicht.");
		}

		BefehlsCheckInfo bci = new BefehlsCheckInfo(f);

		String basename = f.getName();
		String[] basenameParts = basename.split("\\.");
		basename = f.getParent() + "/" + StringUtils.join(basenameParts, ".");

		basenameParts[0] += "-clean";

		String cleanname = f.getParent() + "/" + StringUtils.join(basenameParts, ".");
		// new Debug("cleanname: " + cleanname);

		BefehleEinlesen.cleanBefehle(basename, cleanname);
		bci.setCleanFile(new File(cleanname));

		Partei p;
		try {
			p = BefehleEinlesen.getPartei(cleanname);
			bci.setPartei(p);
			bci.setRichtigesPasswort(true);
		} catch (FalschesPasswortException ex) {
			p = ex.getPartei();
			bci.setPartei(p);
			bci.setRichtigesPasswort(false);
		}
		
		if (p == null) {
			new SysMsg("Keine Partei gefunden in '" + basename + "'.");
			return;
		}
		
		if (this.checkSet.contains(bci)) {
			// es gibt schon eine Datei für die gleiche Partei:
			BefehlsCheckInfo other = null;
			for (BefehlsCheckInfo maybe : this.checkSet) {
				if (maybe.equals(bci)) {
					other = maybe;
					break;
				}
			}

			if (other.getSourceDate().after(bci.getSourceDate())) {
				// die andere Datei ist neuer...
				if ((!other.isRichtigesPasswort()) && bci.isRichtigesPasswort()) {
					// ... aber die neue ist richtiger:
					this.checkSet.remove(other);
					this.checkSet.add(bci);
				}
			} else {
				// diese Datei ist neuer:
				if (
						bci.isRichtigesPasswort()
						|| ((!bci.isRichtigesPasswort()) && (!other.isRichtigesPasswort()))
				) {
					this.checkSet.remove(other);
					this.checkSet.add(bci);
				}
			}
		} else {
			// das ist die erste Datei dieser Partei:
			this.checkSet.add(bci);
		}
	}

	public Set<Partei> getParteien() {
		Set<Partei> retval = new HashSet<Partei>();
		for (BefehlsCheckInfo bci : this.checkSet) {
			retval.add(bci.getPartei());
		}
		return retval;
	}

	public boolean hasValidBefehle(Partei p) {
		for (BefehlsCheckInfo bci : this.checkSet) {
			if (bci.getPartei().getNummer() == p.getNummer()) {
				if (bci.isRichtigesPasswort()) return true;
				return false;
			}
		}
		return false;
	}

	public File getCleanBefehle(Partei p) {
		if (!this.hasValidBefehle(p)) return null;
		for (BefehlsCheckInfo bci : this.checkSet) {
			if (bci.getPartei().getNummer() == p.getNummer()) {
				return bci.getCleanFile();
			}
		}
		return null;
	}

	private class BefehlsCheckInfo{
		final File sourceFile;
		final Date sourceDate;
		File cleanFile;
		Partei partei;
		boolean richtigesPasswort;

		public BefehlsCheckInfo(File sourceFile) {
			this.sourceFile = sourceFile;
			this.sourceDate = new Date(sourceFile.lastModified());
			this.richtigesPasswort = false;
		}

		public File getCleanFile() {
			return cleanFile;
		}

		public void setCleanFile(File cleanFile) {
			this.cleanFile = cleanFile;
		}

		public Partei getPartei() {
			return partei;
		}

		public void setPartei(Partei p) {
			this.partei = p;
		}

		public boolean isRichtigesPasswort() {
			return richtigesPasswort;
		}

		public void setRichtigesPasswort(boolean richtigesPasswort) {
			this.richtigesPasswort = richtigesPasswort;
		}

		public Date getSourceDate() {
			return sourceDate;
		}

		@SuppressWarnings("unused")
		public File getSourceFile() {
			return sourceFile;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof BefehlsCheckInfo)) return false;
			BefehlsCheckInfo other = (BefehlsCheckInfo)obj;

			if (other.getPartei().equals(this.getPartei())) return true;
			return false;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 97 * hash + (this.sourceDate != null ? this.sourceDate.hashCode() : 0);
			hash = 97 * hash + (this.partei != null ? this.partei.hashCode() : 0);
			return hash;
		}


	}

}
