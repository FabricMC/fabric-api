package net.fabricmc.fabric.test.debug;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record SusDebugInfo(
		String playerName,
		boolean isSuspicious
) {
	public static final StreamCodec<RegistryFriendlyByteBuf, SusDebugInfo> STREAM_CODEC = StreamCodec.of(
			(byteBuf, object) -> {
				byteBuf.writeUtf(object.playerName);
				byteBuf.writeBoolean(object.isSuspicious);
			},
			byteBuf -> new SusDebugInfo(
					byteBuf.readUtf(),
					byteBuf.readBoolean()
			)
	);
}
