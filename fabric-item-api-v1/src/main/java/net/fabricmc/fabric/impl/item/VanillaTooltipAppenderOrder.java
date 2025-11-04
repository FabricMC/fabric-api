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

package net.fabricmc.fabric.impl.item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.service.MixinService;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import net.fabricmc.loader.api.FabricLoader;

public final class VanillaTooltipAppenderOrder {
	private static final List<DataComponentType<?>> VANILLA_ORDER = scrapeVanillaOrder();

	private VanillaTooltipAppenderOrder() {
	}

	public static void load() {
		// calling this method loads the class, eagerly populating VANILLA_ORDER
	}

	// Find the order in which vanilla tooltip appenders are run by inspecting the bytecode of ItemStack.appendTooltip.
	private static List<DataComponentType<?>> scrapeVanillaOrder() {
		try {
			ClassNode itemStackNode = MixinService.getService().getBytecodeProvider().getClassNode(Type.getInternalName(ItemStack.class));

			String methodName = FabricLoader.getInstance().getMappingResolver().mapMethodName(
					"intermediary",
					"net.minecraft.class_1799", // ItemStack
					"method_67194", // appendTooltip
					Type.getMethodDescriptor(
							Type.VOID_TYPE,
							Type.getObjectType("net/minecraft/class_1792$class_9635"), // Item.TooltipContext
							Type.getObjectType("net/minecraft/class_10712"), // TooltipDisplayComponent
							Type.getObjectType("net/minecraft/class_1657"), // PlayerEntity
							Type.getObjectType("net/minecraft/class_1836"), // TooltipType
							Type.getType(Consumer.class)
					)
			);
			String methodDesc = Type.getMethodDescriptor(
					Type.VOID_TYPE,
					Type.getType(Item.TooltipContext.class),
					Type.getType(TooltipDisplay.class),
					Type.getType(Player.class),
					Type.getType(TooltipFlag.class),
					Type.getType(Consumer.class)
			);

			String appendAttributeModifiersTooltipName = FabricLoader.getInstance().getMappingResolver().mapMethodName(
					"intermediary",
					"net.minecraft.class_1799", // ItemStack
					"method_57363", // appendAttributeModifiersTooltip
					Type.getMethodDescriptor(
							Type.VOID_TYPE,
							Type.getType(Consumer.class),
							Type.getObjectType("net/minecraft/class_10712"), // TooltipDisplayComponent
							Type.getObjectType("net/minecraft/class_1657") // PlayerEntity
					)
			);
			String appendAttributeModifiersTooltipDesc = Type.getMethodDescriptor(
					Type.VOID_TYPE,
					Type.getType(Consumer.class),
					Type.getType(TooltipDisplay.class),
					Type.getType(Player.class)
			);

			MethodNode appendTooltipMethod = itemStackNode.methods.stream()
					.filter(method -> method.name.equals(methodName) && method.desc.equals(methodDesc))
					.findAny()
					.orElseThrow(() -> new IllegalStateException("No appendTooltip method in ItemStack"));

			// Search for data component accesses within this method
			List<DataComponentType<?>> componentTypes = new ArrayList<>();
			Set<String> alreadyAddedComponents = new HashSet<>();
			String owner = Type.getInternalName(DataComponents.class);
			String desc = Type.getDescriptor(DataComponentType.class);

			for (AbstractInsnNode insn : appendTooltipMethod.instructions) {
				if (insn instanceof FieldInsnNode fieldInsn
						&& fieldInsn.getOpcode() == Opcodes.GETSTATIC
						&& fieldInsn.owner.equals(owner)
						&& fieldInsn.desc.equals(desc)
				) {
					String fieldName = fieldInsn.name;

					if (alreadyAddedComponents.add(fieldName)) {
						componentTypes.add((DataComponentType<?>) DataComponents.class.getField(fieldName).get(null));
					}
				} else if (insn instanceof MethodInsnNode methodInsn
						&& methodInsn.name.equals(appendAttributeModifiersTooltipName)
						&& methodInsn.desc.equals(appendAttributeModifiersTooltipDesc)
						&& methodInsn.owner.equals(Type.getInternalName(ItemStack.class))
				) {
					// Special case: attribute modifiers are extracted into a separate method
					componentTypes.add(DataComponents.ATTRIBUTE_MODIFIERS);
				}
			}

			if (componentTypes.isEmpty()) {
				throw new IllegalStateException("Found no component types in appendTooltip method");
			}

			return Collections.unmodifiableList(componentTypes);
		} catch (ReflectiveOperationException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<DataComponentType<?>> getVanillaOrder() {
		return VANILLA_ORDER;
	}
}
