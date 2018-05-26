package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Steuer;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Serializes all taxes that a certain player demands of others. */
public class SteuerSerializer implements ObjectSerializer<Partei> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Collection<Partei> partylist;

	/** Creates a new serializer.
	 *
	 * @param collection the collection that contains all relevant partys.
	 * @throws IllegalArgumentException if the collection is not supplied.
	 */
	public SteuerSerializer(Collection<Partei> collection) {
		if (collection == null) {
			throw new IllegalArgumentException("Invalid partei list.");
		}

		this.partylist = collection;
	}

	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("owner")
				&& keys.contains("rate")
				&& keys.contains("partei"));
	}

	/** Loads all taxes and attaches them to the owning party.
	 *
	 * @param mapping a key/value map that describes the taxing behavior.
	 * @return the party that imposes the tax.
	 * @throws IllegalArgumentException if the party does not exist in the collection.
	 */
	@Override
	public Partei load(Map<String, String> mapping) {
		int taxmasterId = Integer.decode(mapping.get("owner"));
		int rate = Integer.decode(mapping.get("rate"));
		int clientId = Integer.decode(mapping.get("partei"));

		// look if the party exists
		Partei taxmaster = null;
		Partei client = null;
		for (Partei p : partylist) {
			if (p.getNummer() == taxmasterId) {
				taxmaster = p;
			} else if (p.getNummer() == clientId) {
				client = p;
			}
		}

		if (taxmaster == null) {
			logger.warn("Invalid taxation of {} by {}; Tax-collecting party not found.",
					clientId, taxmasterId);
			return null;
		}

		if (client == null) {
			logger.warn("Invalid taxation of {} by {}; Tax-paying party not found.",
					clientId, taxmasterId);
			return null;
		}

		taxmaster.setSteuern(clientId, rate);
		logger.debug("{} draws a tax of {} from {}.", taxmaster.getName(), rate, client.getName());
		return taxmaster;
	}

	/** Saves the tax data.
	 *
	 * @param partei the faction whose tax data is saved.
	 * @return a data object that contains all custom taxes that this party imposes.
	 */
	@Override
	public SerializedData save(Partei partei) {
		SerializedData data = new SerializedData();

		for (Steuer tax : partei.getSteuern()) {
			Map<String, String> taxMap = new HashMap<String, String>();
			taxMap.put("owner", String.valueOf(partei.getNummer()));
			taxMap.put("rate", String.valueOf(tax.getRate()));
			taxMap.put("partei", String.valueOf(tax.getFaction()));

			data.add(taxMap);
		}

		return data;
	}
}