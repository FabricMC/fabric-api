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

package net.fabricmc.fabric.impl.crash.report.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.loader.api.FabricLoader;

public final class StackRemapper {
	private StackRemapper() {
	}

	public static Throwable remap(Throwable throwable) {
		if (!shouldRemap()) {
			return throwable;
		}

		if (isOOM(throwable)) {
			return throwable;
		}

		// Be super careful not to throw for an unrelated reason, we cannot trust that the logger is working.
		Logger logger = null;

		try {
			logger = LoggerFactory.getLogger(StackRemapper.class);
			return doRemap(throwable);
		} catch (Throwable t) {
			try {
				if (logger != null) {
					logger.error("Failed to remap stack trace", t);
				}
			} catch (Throwable ignored) {
				// Ignored, something is seriously wrong if we cannot log this.
			}

			return throwable;
		}
	}

	private static Throwable doRemap(Throwable throwable) throws Throwable {
		BinaryMappingReader mappings = new BinaryMappingReader();
		return remapThrowable(throwable, mappings);
	}

	private static Throwable remapThrowable(Throwable throwable, BinaryMappingReader mappings) {
		String message = throwable.getMessage();
		Throwable cause = throwable.getCause();
		Throwable[] suppressed = throwable.getSuppressed();
		StackTraceElement[] elements = throwable.getStackTrace();

		if (cause != null) {
			cause = remapThrowable(cause, mappings);
		}

		Throwable remapped = new Throwable(message, cause);
		remapped.setStackTrace(remapElements(elements, mappings));

		for (Throwable sup : suppressed) {
			remapped.addSuppressed(remapThrowable(sup, mappings));
		}

		return remapped;
	}

	private static StackTraceElement[] remapElements(StackTraceElement[] elements, BinaryMappingReader mappings) {
		StackTraceElement[] remapped = new StackTraceElement[elements.length];

		for (int i = 0; i < elements.length; i++) {
			StackTraceElement element = elements[i];
			String className = element.getClassName();
			String methodName = element.getMethodName();
			String fileName = element.getFileName();
			int lineNumber = element.getLineNumber();

			// TODO actually remap

			remapped[i] = new StackTraceElement(className, methodName, fileName, lineNumber);
		}

		return remapped;
	}

	// Don't attempt to remap the stack trace if the throwable is an OutOfMemoryError.
	// We need memory available to remap the stack trace, if there is no memory available, attempting to remap will even more of a problem.
	private static boolean isOOM(Throwable throwable) {
		if (throwable instanceof OutOfMemoryError) {
			return true;
		}

		Throwable cause = throwable.getCause();

		while (cause != null) {
			if (isOOM(cause)) {
				return true;
			}

			cause = cause.getCause();
		}

		for (Throwable suppressed : throwable.getSuppressed()) {
			if (isOOM(suppressed)) {
				return true;
			}
		}

		return true;
	}

	public static boolean shouldRemap() {
		return FabricLoader.getInstance().isDevelopmentEnvironment()
				|| "intermediary".equals(FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace());
	}
}
