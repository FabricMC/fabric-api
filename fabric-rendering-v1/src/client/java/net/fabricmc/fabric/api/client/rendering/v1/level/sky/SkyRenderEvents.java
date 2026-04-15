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
	 * Called during the rendering of the End skybox.
	 */
	public static final Event<EndSky> END_SKY = EventFactory.createArrayBacked(EndSky.class, callbacks -> context -> {
		for (final EndSky callback : callbacks) {
			callback.execute(context);
		}
	});

	/**
	 * Called during the rendering of the top/bottom half the skybox.
	 */
	public static final Event<SkyDisc> SKY_DISC = EventFactory.createArrayBacked(SkyDisc.class, callbacks -> context -> {
		for (final SkyDisc callback : callbacks) {
			callback.execute(context);
		}
	});

	/**
	 * Called during the rendering of the sunrise/sunset in the Overworld.
	 */
	public static final Event<SunriseSunset> SUNRISE_SUNSET = EventFactory.createArrayBacked(SunriseSunset.class, callbacks -> context -> {
		for (final SunriseSunset callback : callbacks) {
			callback.execute(context);
		}
	});

	/**
	 * Called after the rendering of the sun/moon/stars.
	 */
	public static final Event<SunMoonStars> SUN_MOON_STARS = EventFactory.createArrayBacked(SunMoonStars.class, callbacks -> context -> {
		for (final SunMoonStars callback : callbacks) {
			callback.execute(context);
		}
	});

	/**
	 * Called during the rendering of the sun/moon/stars.
	 */
	public static final Event<Celestial> CELESTIAL = EventFactory.createArrayBacked(Celestial.class, callbacks -> context -> {
		for (final Celestial callback : callbacks) {
			callback.execute(context);
		}
	});

	@FunctionalInterface
	public interface EndSky {
		void execute(EndSkyRenderContext context);
	}

	@FunctionalInterface
	public interface SkyDisc {
		void execute(SkyDiscRenderContext context);
	}

	@FunctionalInterface
	public interface SunriseSunset {
		void execute(SunriseSunsetRenderContext context);
	}

	@FunctionalInterface
	public interface SunMoonStars {
		void execute(SunMoonStarsRenderContext context);
	}

	@FunctionalInterface
	public interface Celestial {
		void execute(CelestialRenderContext context);
	}
}
