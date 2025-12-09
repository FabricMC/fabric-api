package net.fabricmc.fabric.test.rendering;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record SusDebugInfo(
		String sussyPlayerName,
		boolean isSuspicious
) {
	public static final StreamCodec<RegistryFriendlyByteBuf, SusDebugInfo> STREAM_CODEC = StreamCodec.of(
			(byteBuf, object) -> {
				byteBuf.writeUtf(object.sussyPlayerName);
				byteBuf.writeBoolean(object.isSuspicious);
			},
			byteBuf -> new SusDebugInfo(
					byteBuf.readUtf(),
					byteBuf.readBoolean()
			)
	);
}
