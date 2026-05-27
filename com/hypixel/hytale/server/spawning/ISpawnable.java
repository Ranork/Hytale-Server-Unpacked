package com.hypixel.hytale.server.spawning;

import com.hypixel.hytale.server.npc.movement.MovementMode;
import java.util.Set;
import javax.annotation.Nonnull;

public interface ISpawnable {
   @Nonnull
   String getIdentifier();

   @Nonnull
   SpawnTestResult canSpawn(@Nonnull SpawningContext var1);

   void getMovementModes(@Nonnull SpawningContext var1, @Nonnull Set<MovementMode> var2, @Nonnull Set<MovementMode> var3, @Nonnull Set<MovementMode> var4);
}
