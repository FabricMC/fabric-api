package net.fabricmc.fabric.api.datagen.v1;

import com.google.gson.JsonElement;

import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.data.client.BlockStateSupplier;
import net.minecraft.util.Identifier;

/**
 * Basic constructor class for generating non-variant aligned blockstates,
 * since there is no built-in class for non-variant blockstates.
 */
public class SimpleBlockStateSupplier implements BlockStateSupplier {
	private final Block block;
	private final JsonObject jsonObject;

	/**
	 * Constructor for the SimpleBlockStateSupplier. Generates the entire jsonObject.
	 *
	 * @param block Block to be applied to.
	 * @param modelLocation Location of the block's default model.
	 */
	public SimpleBlockStateSupplier(Block block, Identifier modelLocation)
	{
		this.block=block;
		this.jsonObject=new JsonObject();

		JsonObject variants = new JsonObject();
		JsonObject defaultVariant = new JsonObject();
		defaultVariant.addProperty("model", modelLocation.getNamespace() + ":" + modelLocation.getPath());
		variants.add("", defaultVariant);

		this.jsonObject.add("variants", variants);
	}

	/**
	 * Returns the block this is being applied to.
	 *
	 * @return this block
	 */
	@Override
	public Block getBlock() {
		return this.block;
	}

	/**
	 * Returns the resulting {@link JsonElement} from the class.
	 *
	 * @return this jsonObject
	 */
	@Override
	public JsonElement get() {
		return this.jsonObject;
	}
}
