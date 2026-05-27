package com.hypixel.hytale.builtin.locate.command;

import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.container.UniquePrefabContainer;
import java.util.Arrays;
import java.util.Comparator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3i;

final class PrefabSearchUtil {
   private PrefabSearchUtil() {
   }

   @Nullable
   static UniquePrefabContainer.UniquePrefabEntry findClosestPrefab(
      @Nonnull ChunkGenerator generator, int seed, int playerX, int playerZ, @Nonnull String name, boolean mapOnly
   ) {
      UniquePrefabContainer.UniquePrefabEntry[] uniquePrefabs = generator.getUniquePrefabs(seed);
      return uniquePrefabs != null && uniquePrefabs.length != 0
         ? Arrays.stream(uniquePrefabs)
            .filter(entry -> !mapOnly || entry.isShowOnMap())
            .filter(entry -> name.equalsIgnoreCase(entry.getName()))
            .min(Comparator.comparingDouble(entry -> {
               Vector3i pos = entry.getPosition();
               long dx = (long)pos.x - playerX;
               long dz = (long)pos.z - playerZ;
               return dx * dx + dz * dz;
            }))
            .orElse(null)
         : null;
   }
}
