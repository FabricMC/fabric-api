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

package net.fabricmc.fabric.impl.client.rendering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;

public class LayerInjectionPoint extends InjectionPoint {
	private final MemberInfo target;

	public LayerInjectionPoint(InjectionPointData data) {
		super(data);
		this.target = (MemberInfo) data.getTarget();
	}

	@Override
	public boolean find(String desc, InsnList insns, Collection<AbstractInsnNode> nodes) {
		List<AbstractInsnNode> targetNodes = new ArrayList<>();

		ListIterator<AbstractInsnNode> iterator = insns.iterator();

		outer: while (iterator.hasNext()) {
			AbstractInsnNode insn = iterator.next();

			if (insn.getOpcode() == Opcodes.INVOKEDYNAMIC && matchesInvokeDynamic((InvokeDynamicInsnNode) insn)) {
				// We have found our target InvokeDynamicInsnNode, now we need to find the next INVOKEVIRTUAL

				while (iterator.hasNext()) {
					insn = iterator.next();

					if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
						targetNodes.add(insn);
						break outer;
					}
				}
			}
		}

		nodes.addAll(targetNodes);
		return !targetNodes.isEmpty();
	}

	private boolean matchesInvokeDynamic(InvokeDynamicInsnNode insnNode) {
		for (Object bsmArg : insnNode.bsmArgs) {
			if (bsmArg instanceof Handle handle && matchesHandle(handle)) {
				return true;
			}
		}

		return false;
	}

	private boolean matchesHandle(Handle handle) {
		return handle.getOwner().equals(target.getOwner())
				&& handle.getName().equals(target.getName())
				&& handle.getDesc().equals(target.getDesc());
	}
}
