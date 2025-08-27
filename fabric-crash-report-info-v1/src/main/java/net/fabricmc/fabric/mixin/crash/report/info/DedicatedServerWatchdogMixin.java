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

package net.fabricmc.fabric.mixin.crash.report.info;

import java.lang.management.ThreadInfo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.server.dedicated.DedicatedServerWatchdog;

import net.fabricmc.fabric.impl.crash.report.info.ThreadPrinting;

@Mixin(DedicatedServerWatchdog.class)
public class DedicatedServerWatchdogMixin {
	@ModifyArg(method = "createCrashReport(Ljava/lang/String;J)Lnet/minecraft/util/crash/CrashReport;",
			at = @At(value = "INVOKE",
					target = "Ljava/lang/StringBuilder;append(Ljava/lang/Object;)Ljava/lang/StringBuilder;",
					ordinal = 0)
	)
	private static Object printEntireThreadDump(Object object) {
		if (object instanceof ThreadInfo threadInfo) {
			return ThreadPrinting.fullThreadInfoToString(threadInfo);
		}

		return object;
	}
}
