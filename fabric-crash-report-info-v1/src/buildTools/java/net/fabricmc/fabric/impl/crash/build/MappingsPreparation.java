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

package net.fabricmc.fabric.impl.crash.build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public class MappingsPreparation {
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: java MappingsPreparation <output-file>");
			System.exit(1);
		}

		prepare(Path.of(args[0]));
	}

	public static void prepare(Path out) throws IOException {
		MemoryMappingTree mappings = new MemoryMappingTree();

		try (InputStream is = MappingsPreparation.class.getResourceAsStream("/mappings/mappings.tiny");
				InputStreamReader reader = new InputStreamReader(is);
				BufferedReader bufferedReader = new BufferedReader(reader)) {
			MappingDstNsReorder dstNsReorder = new MappingDstNsReorder(mappings, List.of("named"));
			MappingSourceNsSwitch sourceNsSwitch = new MappingSourceNsSwitch(dstNsReorder, "intermediary", true);
			MappingReader.read(bufferedReader, sourceNsSwitch);
		}

		int namedNs = mappings.getNamespaceId("named");

		for (MappingTree.ClassMapping classMapping : mappings.getClasses()) {
			List<? extends MappingTree.MethodMapping> toRemove = classMapping.getMethods().stream()
					.filter(methodMapping -> methodMapping.getSrcName().equals(methodMapping.getDstName(namedNs)))
					.toList();

			toRemove.forEach(methodMapping -> classMapping.removeMethod(methodMapping.getSrcName(), methodMapping.getSrcDesc()));
		}

		List<String> toRemove = mappings.getClasses().stream()
				.filter(classMapping -> classMapping.getSrcName().equals(classMapping.getDstName(namedNs))
						&& classMapping.getMethods().isEmpty()).map(MappingTree.ClassMapping::getSrcName).toList();
		toRemove.forEach(mappings::removeClass);

		BinaryMappingsWriter.write(out, mappings);
	}
}
