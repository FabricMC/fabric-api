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

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;

public class ThreadPrinting {
	/// A modified copy of [ThreadInfo#toString] without the MAX_FRAMES check.
	public static String fullThreadInfoToString(ThreadInfo threadInfo) {
		StringBuilder sb = new StringBuilder("\"" + threadInfo.getThreadName() + "\""
				+ (threadInfo.isDaemon() ? " daemon" : "")
				+ " prio=" + threadInfo.getPriority()
				+ " Id=" + threadInfo.getThreadId() + " "
				+ threadInfo.getThreadState());

		if (threadInfo.getLockName() != null) {
			sb.append(" on ").append(threadInfo.getLockName());
		}

		if (threadInfo.getLockOwnerName() != null) {
			sb.append(" owned by \"").append(threadInfo.getLockOwnerName())
					.append("\" Id=").append(threadInfo.getLockOwnerId());
		}

		if (threadInfo.isSuspended()) {
			sb.append(" (suspended)");
		}

		if (threadInfo.isInNative()) {
			sb.append(" (in native)");
		}

		sb.append('\n');

		StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();

		for (int i = 0; i < stackTraceElements.length; i++) {
			StackTraceElement ste = stackTraceElements[i];
			sb.append("\tat ").append(ste.toString());
			sb.append('\n');

			if (i == 0 && threadInfo.getLockInfo() != null) {
				Thread.State ts = threadInfo.getThreadState();
				switch (ts) {
				case BLOCKED -> {
					sb.append("\t-  blocked on ").append(threadInfo.getLockInfo());
					sb.append('\n');
				}
				case WAITING, TIMED_WAITING -> {
					sb.append("\t-  waiting on ").append(threadInfo.getLockInfo());
					sb.append('\n');
				}
				default -> {
				}
				}
			}

			for (MonitorInfo mi : threadInfo.getLockedMonitors()) {
				if (mi.getLockedStackDepth() == i) {
					sb.append("\t-  locked ").append(mi);
					sb.append('\n');
				}
			}
		}

		LockInfo[] locks = threadInfo.getLockedSynchronizers();

		if (locks.length > 0) {
			sb.append("\n\tNumber of locked synchronizers = ").append(locks.length);
			sb.append('\n');

			for (LockInfo li : locks) {
				sb.append("\t- ").append(li);
				sb.append('\n');
			}
		}

		sb.append('\n');
		return sb.toString();
	}
}
