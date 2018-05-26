package de.x8bit.Fantasya.Host.serialization;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Effect;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.EVA.util.NeuerSpieler;
import de.x8bit.Fantasya.Host.serialization.basic.*;
import de.x8bit.Fantasya.Host.serialization.complex.*;
import de.x8bit.Fantasya.Host.serialization.db.Database;
import de.x8bit.Fantasya.Host.serialization.db.DatabaseAdapter;
import de.x8bit.Fantasya.Host.serialization.postprocess.LegacyParteiZeroProcessor;
import de.x8bit.Fantasya.Host.serialization.postprocess.PostProcessor;
import de.x8bit.Fantasya.Host.serialization.postprocess.RegionInitHandelProcessor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/** Factory to setup and call the new serialization system from the old code.
 *
 * The current plan is to transition the code from the old to the new
 * serialization system in parts. While this transition is in place, some data
 * is loaded by EvaFastLoader/Saver, and some through the various handlers here.
 * The switch is done via this factory. It gets the old data for the server
 * connection, and from this assembles a ready-to-use serializer.
 *
 * In the long run, discard EvaFastLoader/Saver and use proper Adapters together
 * with the SerializerFactory.
 */

public class MigrationSerializerFactory {

	/** Build the serializer with the old data.
	 *
	 * @param db      the old-style Datenbank object
	 * @return a serializer set up with all migrated data handlers.
	 */
	public static Serializer buildSerializer(Datenbank db) {
		Adapter adapter = buildDatabaseAdapter(db);
		
		LinkedHashMap<String,ComplexHandler> handlerMap = new LinkedHashMap<String,ComplexHandler>();
		
		// Load Parteien and their data first.
		handlerMap.put("partei", new CacheFillerHandler<Partei>(
				new ParteiSerializer(),
				Partei.PROXY));
		handlerMap.put("allianzen", new CacheLooperHandler<Partei>(
				new AllianzSerializer(Partei.PROXY),
				Partei.PROXY));
		handlerMap.put("steuern", new CacheLooperHandler<Partei>(
				new SteuerSerializer(Partei.PROXY),
				Partei.PROXY));
		handlerMap.put("property_parteien", new CacheLooperHandler<Partei>(
				new ParteienPropertySerializer(Partei.PROXY),
				Partei.PROXY));

		// Load regions and their associated data
		handlerMap.put("regionen", new MapCacheHandler<Region>(
				new RegionSerializer(),
				Region.CACHE));
		handlerMap.put("resourcen", new CacheLooperHandler<Region>(
				new ResourcenSerializer(Region.CACHE),
				Region.CACHE.values()));
		handlerMap.put("strassen", new CacheLooperHandler<Region>(
				new StrassenSerializer(Region.CACHE),
				Region.CACHE.values()));
		handlerMap.put("luxus", new CacheLooperHandler<Region>(
				new LuxusSerializer(Region.CACHE),
				Region.CACHE.values()));
		handlerMap.put("property_regionen", new CacheLooperHandler<Region>(
				new PropertySerializer<Region>(Region.CACHE.values()),
				Region.CACHE.values()));

		// load buildings and ships
		handlerMap.put("gebaeude", new CacheFillerHandler<Building>(
				new BuildingSerializer(Region.CACHE.keySet(), Unit.CACHE),
				Building.PROXY));
		handlerMap.put("property_gebaeude", new CacheLooperHandler<Building>(
				new PropertySerializer<Building>(Building.PROXY),
				Building.PROXY));
		handlerMap.put("schiffe", new CacheFillerHandler<Ship>(
				new ShipSerializer(Region.CACHE, Unit.CACHE),
				Ship.PROXY));

		// load units and everything around them
		handlerMap.put("einheiten", new CacheFillerHandler<Unit>(
				new EinheitenSerializer(Partei.PROXY, Region.CACHE.keySet()),
				Unit.CACHE));
		handlerMap.put("items", new CacheLooperHandler<Unit>(
				new ItemSerializer(Unit.CACHE),
				Unit.CACHE));
		handlerMap.put("skills", new CacheLooperHandler<Unit>(
				new SkillSerializer(Unit.CACHE),
				Unit.CACHE));
		handlerMap.put("spells", new CacheLooperHandler<Unit>(
				new SpellSerializer(Unit.CACHE),
				Unit.CACHE));
		handlerMap.put("property_einheiten", new CacheLooperHandler<Unit>(
				new PropertySerializer<Unit>(Unit.CACHE),
				Unit.CACHE));
		handlerMap.put("kontakte", new CacheLooperHandler<Unit>(
				new KontakteSerializer(Unit.CACHE),
				Unit.CACHE));
		handlerMap.put("effekt_einheiten", new CacheFillerHandler<Effect>(
				new EffekteSerializer(Unit.CACHE),
				Effect.PROXY));
		handlerMap.put("property_effekt", new CacheLooperHandler<Effect>(
				new PropertySerializer<Effect>(Effect.PROXY),
				Effect.PROXY));
		
		// Load new player
		handlerMap.put("neuespieler", new CacheFillerHandler<NeuerSpieler>(
				new NeuerSpielerSerializer(),
				NeuerSpieler.PROXY));


		// additional post-processors
		Map<String,PostProcessor> processorMap = new HashMap<String,PostProcessor>();
		
		// For (the current) legacy code: make sure that a party with id zero exists,
		// even if not defined in the table.
		processorMap.put("partei", new LegacyParteiZeroProcessor(Partei.PROXY));
		// not sure if it is needed: initialise the luxus goods in all regions.
		processorMap.put("regionen", new RegionInitHandelProcessor(Region.CACHE));

		
		return new Serializer(adapter, handlerMap, processorMap);
	}

	/** Assemble a DatabaseAdapter from the contact information. */
	private static Adapter buildDatabaseAdapter(Datenbank db) {
		String url = "jdbc:mysql://" + db.GetServer() + "/" + db.GetDatenbank();
		MysqlDataSource source = new MysqlDataSource();
		source.setURL(url);

		return new DatabaseAdapter( new Database(source, db.GetBenutzer(), db.GetPasswort()) );
	}
}