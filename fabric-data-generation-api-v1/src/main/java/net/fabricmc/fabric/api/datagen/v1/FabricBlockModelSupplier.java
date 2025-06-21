package net.fabricmc.fabric.api.datagen.v1;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.function.Supplier;
import java.util.Map;

/**
 * Acceptable class for {@link net.minecraft.data.client.BlockStateModelGenerator}'s modelCollector.
 * makes generating more specific block models easier for modders.
 */

public class FabricBlockModelSupplier implements Supplier<JsonElement> {
    protected final JsonObject jsonObject;

    /**
     * Constructor for vanilla parent types.
     *
     * @param type The parent type for the model.
     */
    public FabricBlockModelSupplier(String type)
    {
        this.jsonObject = new JsonObject();
        this.jsonObject.addProperty("parent", "minecraft:block/" + type);
    }

    /**
     * Have an acceptable <modID> parameter in case of custom model parents.
     *
     * @param modID The modID as the prefix to the parent type in the Identifier.
     * @param type The parent type for the model.
     */
    public FabricBlockModelSupplier(String modID, String type)
    {
        this.jsonObject = new JsonObject();
        this.jsonObject.addProperty("parent", modID + ":block/" + type);
    }

    /**
     * Add HashMap textures to the model's JsonObject.
     *
     * @param fabricTextureMap {@link FabricTextureMap} containing the data for the textures.
     */
    public FabricBlockModelSupplier addTextureData(FabricTextureMap fabricTextureMap)
    {
		HashMap<String, Identifier> textureMap = fabricTextureMap.get();
        JsonObject textureData = new JsonObject();
        for (Map.Entry<String, Identifier> entry : textureMap.entrySet()) {
			String key = entry.getKey();
            Identifier identifier = entry.getValue();
            textureData.addProperty(key, identifier.getNamespace() + ":" + identifier.getPath());
        }

        this.jsonObject.add("textures", textureData);
        return this;
    }

    /**
     * Add a sample texture as 'all' of the textures. Can only be used on the cube_all parent.
     *
     * @param texture {@link Identifier} for the location of the texture.
     */
    public FabricBlockModelSupplier simpleCubeAllTextures(Identifier texture)
    {
        JsonObject textureData = new JsonObject();
        textureData.addProperty("all", texture.getNamespace() + ":" + texture.getPath());

        this.jsonObject.add("textures", textureData);
        return this;
    }

    /**
     * Returns the {@link JsonObject} for the Model.
     *
     * @return the supplier's {@link JsonObject}
     */
    @Override
    public JsonElement get() {
        return this.jsonObject;
    }
}
