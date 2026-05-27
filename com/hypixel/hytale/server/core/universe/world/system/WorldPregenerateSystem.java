package com.hypixel.hytale.server.core.universe.world.system;

import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box2D;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class WorldPregenerateSystem extends StoreSystem<ChunkStore> {
   private static final int PROGRESS_INTERVAL = 100;
   private static final int MAX_IN_FLIGHT = ForkJoinPool.getCommonPoolParallelism();
   private static final Set<Dependency<ChunkStore>> DEPENDENCIES = Set.of(new SystemGroupDependency<>(Order.AFTER, ChunkStore.INIT_GROUP));

   @Nonnull
   @Override
   public Set<Dependency<ChunkStore>> getDependencies() {
      return DEPENDENCIES;
   }

   @Override
   public void onSystemAddedToStore(@Nonnull Store<ChunkStore> store) {
      World world = store.getExternalData().getWorld();
      Box2D region = world.getWorldConfig().getChunkConfig().getPregenerateRegion();
      if (region != null) {
         world.getLogger().at(Level.INFO).log("Ensuring region is generated: %s", region);
         CompletableFuture.runAsync(() -> pregenerate(world, region));
      }
   }

   private static void pregenerate(@Nonnull World world, @Nonnull Box2D region) {
      long start = System.nanoTime();
      int lowX = MathUtil.floor(region.min.x);
      int lowZ = MathUtil.floor(region.min.y);
      int highX = MathUtil.floor(region.max.x);
      int highZ = MathUtil.floor(region.max.y);
      int chunksAlongX = (highX - lowX) / 32 + 1;
      int chunksAlongZ = (highZ - lowZ) / 32 + 1;
      int total = chunksAlongX * chunksAlongZ;
      List<CompletableFuture<Ref<ChunkStore>>> inFlight = new ReferenceArrayList();
      int count = 0;

      for (int x = lowX; x <= highX; x += 32) {
         for (int z = lowZ; z <= highZ; z += 32) {
            inFlight.add(world.getChunkStore().getChunkReferenceAsync(ChunkUtil.indexChunkFromBlock(x, z)).whenComplete((chunk, throwable) -> {
               if (throwable != null) {
                  ((HytaleLogger.Api)world.getLogger().at(Level.SEVERE).withCause(throwable)).log("Failed to load/generate chunk:");
               }
            }));
            if (++count % 100 == 0) {
               world.getLogger().at(Level.INFO).log("Pregenerated %d/%d chunks", count, total);
            }

            inFlight.removeIf(CompletableFuture::isDone);
            if (inFlight.size() >= MAX_IN_FLIGHT) {
               CompletableFuture.anyOf(inFlight.toArray(CompletableFuture[]::new)).join();
               inFlight.removeIf(CompletableFuture::isDone);
            }
         }
      }

      CompletableFuture.allOf(inFlight.toArray(CompletableFuture[]::new)).join();
      long end = System.nanoTime();
      world.getLogger().at(Level.INFO).log("Finished loading %d chunks. Finished in %s", total, FormatUtil.nanosToString(end - start));
   }

   @Override
   public void onSystemRemovedFromStore(@Nonnull Store<ChunkStore> store) {
   }
}
