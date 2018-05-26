package de.x8bit.Fantasya.Host.serialization.postprocess;

/** Designates a post-processor after the loading of a table.
 *
 * If additional setup needs to be done after loading a table, you can hook up
 * a post processor with a Serializer. The post-processor is run after a table
 * has been loaded with the corresponding handlers.
 */
public interface PostProcessor {

	/** Does the post-processing. */
	public void process();
}