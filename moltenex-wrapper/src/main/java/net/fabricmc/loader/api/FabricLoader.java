package net.fabricmc.loader.api;

import com.moltenex.loader.api.MoltenexLoader;

public class FabricLoader {
	public static MoltenexLoader getInstance(){
		return com.moltenex.loader.api.MoltenexLoader.Companion.getInstance();
	}
}
