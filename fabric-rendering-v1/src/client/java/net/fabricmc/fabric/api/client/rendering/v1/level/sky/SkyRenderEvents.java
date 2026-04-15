/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.client.rendering.v1.level.sky;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * To be used when adding custom skies or to modify existing aspects of the skys rendering.
 */
public final class SkyRenderEvents {
	private SkyRenderEvents() {
	}

	/**
	 * Called before "renderEndSky" is invoked, determines if the end sky should render or not.
	 */
	public static final Event<PreEndSky> PRE_END_SKY = EventFactory.createArrayBacked(PreEndSky.class, callbacks -> context -> {
		for (final PreEndSky callback : callbacks) {
			if (!callback.execute(context)) {
				return false;
			}
		}

		return true;
	});

	/**
	 * Called after "renderEndSky" is invoked.
	 */
	public static final Event<PostEndSky> POST_END_SKY = EventFactory.createArrayBacked(PostEndSky.class, callbacks -> context -> {
		for (final PostEndSky callback : callbacks) {
			callback.execute(context);
		}
	});

	/**
	 * Called before "renderEndSky" is invoked, determines if the end sky should render or not.
	 */
	public static final Event<PreEndFlash> PRE_END_FLASH = EventFactory.createArrayBacked(PreEndFlash.class, callbacks -> context -> {
		for (final PreEndFlash callback : callbacks) {
			if (!callback.execute(context)) {
				return false;
			}
		}

		return true;
	});

	/**
	 * Called after "renderEndSky" is invoked.
	 */
	public static final Event<PostEndFlash> POST_END_FLASH = EventFactory.createArrayBacked(PostEndFlash.class, callbacks -> context -> {
		for (final PostEndFlash callback : callbacks) {
			callback.execute(context);
		}
	});

	/**
	 * Called before the top/bottom sky disc is rendered, determines if it should render or not.
	 */
	public static final Event<PreSkyDisc> PRE_SKY_DISC = EventFactory.createArrayBacked(PreSkyDisc.class, callbacks -> context -> {
		for (final PreSkyDisc callback : callbacks) {
			if (!callback.execute(context)) {
				return false;
			}
		}

		return true;
	});

	/**
	 * Called after the top/bottom sky disc is rendered.
	 */
	public static final Event<PostSkyDisc> POST_SKY_DISC = EventFactory.createArrayBacked(PostSkyDisc.class, callbacks -> context -> {
		for (final PostSkyDisc callback : callbacks) {
			callback.execute(context);
		}
	});

	/**
	 * Called when sunrise/sunset in the Overworld is rendered.
	 */
	public static final Event<PreSunriseSunset> PRE_SUNRISE_SUNSET = EventFactory.createArrayBacked(PreSunriseSunset.class, callbacks -> context -> {
		for (final PreSunriseSunset callback : callbacks) {
			if (!callback.execute(context)) {
				return false;
			}
		}

		return true;
	});

	/**
	 * Called when sunrise/sunset in the Overworld is rendered.
	 */
	public static final Event<PostSunriseSunset> POST_SUNRISE_SUNSET = EventFactory.createArrayBacked(PostSunriseSunset.class, callbacks -> context -> {
		for (final PostSunriseSunset callback : callbacks) {
			callback.execute(context);
		}
	});

	/**
	 * Called after the rendering of the sun/moon/stars.
	 */
	public static final Event<PostSunMoonStars> POST_SUN_MOON_STARS = EventFactory.createArrayBacked(PostSunMoonStars.class, callbacks -> context -> {
		for (final PostSunMoonStars callback : callbacks) {
			callback.execute(context);
		}
	});

	/**
	 * Called before the sun, moon, or stars are rendered.
	 */
	public static final Event<PreCelestial> PRE_CELESTIAL = EventFactory.createArrayBacked(PreCelestial.class, callbacks -> context -> {
		for (final PreCelestial callback : callbacks) {
			if (!callback.execute(context)) {
				return false;
			}
		}

		return true;
	});

	/**
	 * Called after the sun, moon, or stars are rendered.
	 */
	public static final Event<PostCelestial> POST_CELESTIAL = EventFactory.createArrayBacked(PostCelestial.class, callbacks -> context -> {
		for (final PostCelestial callback : callbacks) {
			callback.execute(context);
		}
	});

	@FunctionalInterface
	public interface PreEndSky {
		boolean execute(EndSkyRenderContext context);
	}

	@FunctionalInterface
	public interface PostEndSky {
		void execute(EndSkyRenderContext context);
	}

	@FunctionalInterface
	public interface PreEndFlash {
		boolean execute(EndFlashRenderContext context);
	}

	@FunctionalInterface
	public interface PostEndFlash {
		void execute(EndFlashRenderContext context);
	}

	@FunctionalInterface
	public interface PreSkyDisc {
		boolean execute(SkyDiscRenderContext context);
	}

	@FunctionalInterface
	public interface PostSkyDisc {
		void execute(SkyDiscRenderContext context);
	}

	@FunctionalInterface
	public interface PreSunriseSunset {
		boolean execute(SunriseSunsetRenderContext context);
	}

	@FunctionalInterface
	public interface PostSunriseSunset {
		void execute(SunriseSunsetRenderContext context);
	}

	@FunctionalInterface
	public interface PostSunMoonStars {
		void execute(SunMoonStarsRenderContext context);
	}

	@FunctionalInterface
	public interface PreCelestial {
		boolean execute(CelestialRenderContext context);
	}

	@FunctionalInterface
	public interface PostCelestial {
		void execute(CelestialRenderContext context);
	}
}
