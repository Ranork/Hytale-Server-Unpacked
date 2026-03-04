package com.hypixel.hytale.server.worldgen.chunk;

import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.cave.prefab.CavePrefabContainer;
import com.hypixel.hytale.server.worldgen.container.PrefabContainer;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabLoader;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.prefab.unique.UniquePrefabGenerator;
import com.hypixel.hytale.server.worldgen.util.LogUtil;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import com.hypixel.hytale.server.worldgen.zone.ZonePatternProvider;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ValidationUtil {
   public static boolean isInvalid(@Nonnull ZonePatternProvider zonePatternProvider, @Nonnull Executor executor) {
      return CompletableFuture.<Boolean>supplyAsync(() -> {
         Deque<String> trace = new ArrayDeque<>();
         boolean invalid = false;

         for (Zone zone : zonePatternProvider.getZones()) {
            trace.push("Zone[\"" + zone.name() + "\"]");

            try {
               invalid |= isZoneInvalid(zone, trace);
            } finally {
               trace.pop();
            }
         }

         return invalid;
      }, executor).join();
   }

   private static boolean isZoneInvalid(@Nonnull Zone zone, @Nonnull Deque<String> trace) {
      boolean invalid = false;

      for (UniquePrefabGenerator uniquePrefabGenerator : zone.uniquePrefabContainer().getGenerators()) {
         trace.push("UniquePrefabs[\"" + uniquePrefabGenerator.getName() + "\"]");

         try {
            invalid |= arePrefabsInvalid(uniquePrefabGenerator.getPrefabs(), trace);
         } finally {
            trace.pop();
         }
      }

      for (Biome biome : zone.biomePatternGenerator().getBiomes()) {
         trace.push("Biome[\"" + biome.getName() + "\"]");

         try {
            invalid |= isBiomeInvalid(biome, trace);
         } finally {
            trace.pop();
         }
      }

      if (zone.caveGenerator() != null) {
         for (CaveType caveType : zone.caveGenerator().getCaveTypes()) {
            trace.push("Cave[\"" + caveType.getName() + "\"].Entry");

            try {
               Set<String> encounteredNodes = new HashSet<>();
               invalid |= isCaveNodeInvalid(caveType.getEntryNode(), encounteredNodes, trace);
            } finally {
               trace.pop();
            }
         }
      }

      return invalid;
   }

   private static boolean isBiomeInvalid(@Nonnull Biome biome, @Nonnull Deque<String> trace) {
      boolean invalid = false;
      if (biome.getPrefabContainer() != null) {
         PrefabContainer.PrefabContainerEntry[] prefabContainerEntries = biome.getPrefabContainer().getEntries();

         for (int i = 0; i < prefabContainerEntries.length; i++) {
            trace.push("Prefabs[" + i + "]");

            try {
               invalid |= arePrefabsInvalid(prefabContainerEntries[i].getPrefabs(), trace);
            } finally {
               trace.pop();
            }
         }
      }

      return invalid;
   }

   private static boolean isCaveNodeInvalid(@Nonnull CaveNodeType caveNodeType, @Nonnull Set<String> encounteredNodes, @Nonnull Deque<String> trace) {
      if (!encounteredNodes.add(caveNodeType.getName())) {
         return false;
      } else {
         boolean invalid = false;
         if (caveNodeType.getPrefabContainer() != null) {
            CavePrefabContainer.CavePrefabEntry[] cavePrefabEntries = caveNodeType.getPrefabContainer().getEntries();

            for (int i = 0; i < cavePrefabEntries.length; i++) {
               trace.push("Prefabs[" + i + "]");

               try {
                  invalid |= arePrefabsInvalid(cavePrefabEntries[i].getPrefabs(), trace);
               } finally {
                  trace.pop();
               }
            }
         }

         CaveNodeType.CaveNodeChildEntry[] children = caveNodeType.getChildren();

         for (int i = 0; i < children.length; i++) {
            CaveNodeType[] nodes = children[i].getTypes().internalKeys();

            for (int n = 0; n < nodes.length; n++) {
               trace.push("Children[" + i + "].Node[" + n + "]");

               try {
                  invalid |= isCaveNodeInvalid(nodes[n], encounteredNodes, trace);
               } finally {
                  trace.pop();
               }
            }
         }

         return invalid;
      }
   }

   private static boolean arePrefabsInvalid(@Nonnull IWeightedMap<WorldGenPrefabSupplier> prefabs, @Nonnull Deque<String> trace) {
      boolean invalid = false;
      WorldGenPrefabSupplier[] suppliers = prefabs.internalKeys();

      for (int i = 0; i < suppliers.length; i++) {
         trace.push("Prefabs[" + i + "]");

         try {
            IPrefabBuffer prefab;
            try {
               prefab = suppliers[i].get();
            } catch (Throwable var27) {
               invalid = true;
               ((HytaleLogger.Api)LogUtil.getLogger().at(Level.SEVERE).withCause(var27))
                  .log("Failed to load prefab: %s loaded from %s", suppliers[i].getName(), String.join(".", trace));
               continue;
            }

            trace.push("{" + suppliers[i].getName() + "}");

            try {
               for (PrefabBuffer.ChildPrefab childMarker : prefab.getChildPrefabs()) {
                  trace.push("(" + childMarker.getX() + "," + childMarker.getY() + "," + childMarker.getZ() + ")");

                  try {
                     invalid |= isChildPrefabInvalid(childMarker, suppliers[i].getLoader(), trace);
                  } finally {
                     trace.pop();
                  }
               }
            } finally {
               trace.pop();
            }
         } finally {
            trace.pop();
         }
      }

      return invalid;
   }

   private static boolean isChildPrefabInvalid(
      @Nonnull PrefabBuffer.ChildPrefab childMarker, @Nonnull WorldGenPrefabLoader loader, @Nonnull Deque<String> trace
   ) {
      WorldGenPrefabSupplier[] suppliers;
      try {
         suppliers = loader.get(childMarker.getPath());
      } catch (Throwable var11) {
         ((HytaleLogger.Api)LogUtil.getLogger().at(Level.SEVERE).withCause(var11))
            .log("Failed to resolve child prefab: %s loaded from %s", childMarker.getPath(), String.join(".", trace));
         return true;
      }

      boolean invalid = false;

      for (WorldGenPrefabSupplier childSupplier : suppliers) {
         try {
            childSupplier.get();
         } catch (Throwable var10) {
            invalid = true;
            ((HytaleLogger.Api)LogUtil.getLogger().at(Level.SEVERE).withCause(var10))
               .log("Failed to load child prefab: %s loaded from %s", childSupplier.getName(), String.join(".", trace));
         }
      }

      return invalid;
   }
}
