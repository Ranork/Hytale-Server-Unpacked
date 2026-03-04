package com.hypixel.hytale.server.core.universe.world;

import javax.annotation.Nonnull;

public interface WorldProvider {
   @Nonnull
   World getWorld();
}
