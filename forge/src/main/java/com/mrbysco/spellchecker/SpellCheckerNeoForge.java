package com.mrbysco.spellchecker;

import com.mrbysco.spellchecker.config.SpellCheckerConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Constants.MOD_ID)
public class SpellCheckerNeoForge {

	public SpellCheckerNeoForge(IEventBus eventBus, Dist dist, ModContainer container) {
		if (dist.isClient()) {
			container.registerConfig(ModConfig.Type.CLIENT, SpellCheckerConfig.clientSpec);
			eventBus.register(SpellCheckerConfig.class);

			CommonClass.init();
		} else {
			Constants.LOGGER.info("SpellChecker is a client only mod, it won't do anything on the server side.");
		}
	}
}