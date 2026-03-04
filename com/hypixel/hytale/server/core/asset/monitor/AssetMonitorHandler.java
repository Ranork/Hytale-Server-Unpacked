package com.hypixel.hytale.server.core.asset.monitor;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public interface AssetMonitorHandler extends BiPredicate<Path, EventKind>, Consumer<Map<Path, EventKind>> {
   Object getKey();
}
