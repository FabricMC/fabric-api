package net.fabricmc.fabric.test.renderer.client;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.fabric.impl.client.renderer.RendererManager;

// a first for Fabric API, a client test mod has its own "MixinConfigPlugin".
// be not afraid! this just makes it easier to test RendererProvider overrides, and it's the least
// cursed solution. it's also not present in production.
public class TestPrePrePreLaunchMixinConfigPlugin implements IMixinConfigPlugin {
	static {
		// if it crashes, that means the ordering is #*@!ed up.
		// fabric-renderer-indigo, g, h, i, j
		RendererManager.RendererProviderNode g = new RendererManager.RendererProviderNode("g", null);
		RendererManager.RendererProviderNode h = new RendererManager.RendererProviderNode("h", null);
		RendererManager.RendererProviderNode j = new RendererManager.RendererProviderNode("j", null);
		RendererManager.RendererProviderNode i = new RendererManager.RendererProviderNode("i", null);
		RendererManager.nodeMap.put("g", g);
		RendererManager.nodeMap.put("h", h);
		RendererManager.nodeMap.put("j", j);
		RendererManager.nodeMap.put("i", i);
		RendererManager.overrides.put("g", List.of("j"));
		RendererManager.overrides.put("h", List.of());
		RendererManager.overrides.put("j", List.of("h"));
		RendererManager.overrides.put("i", List.of());
	}

	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(
			String targetClassName,
			ClassNode targetClass,
			String mixinClassName,
			IMixinInfo mixinInfo
	) {
	}

	@Override
	public void postApply(
			String targetClassName,
			ClassNode targetClass,
			String mixinClassName,
			IMixinInfo mixinInfo
	) {
	}
}
