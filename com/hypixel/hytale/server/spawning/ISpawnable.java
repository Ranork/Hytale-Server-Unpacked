package com.hypixel.hytale.server.spawning;

import javax.annotation.Nonnull;

public interface ISpawnable {
   @Nonnull
   String getIdentifier();

   @Nonnull
   SpawnTestResult canSpawn(@Nonnull SpawningContext var1);
}
