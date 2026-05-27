package com.hypixel.hytale.builtin.locate;

import com.hypixel.hytale.math.iterator.SpiralIterator;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.chunk.ZoneBiomeResult;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3i;

public final class SpiralSearchUtil {
   private SpiralSearchUtil() {
   }

   @Nullable
   public static Vector3i search(
      @Nonnull ChunkGenerator generator, int seed, int startX, int startZ, int maxRadius, @Nonnull Predicate<ZoneBiomeResult> predicate
   ) {
      int chunkX = Math.floorDiv(startX, 32);
      int chunkZ = Math.floorDiv(startZ, 32);
      int radiusInChunks = maxRadius / 32;
      SpiralIterator iterator = new SpiralIterator(chunkX, chunkZ, radiusInChunks);

      while (iterator.hasNext()) {
         long key = iterator.next();
         int blockX = ChunkUtil.xOfChunkIndex(key) * 32;
         int blockZ = ChunkUtil.zOfChunkIndex(key) * 32;
         ZoneBiomeResult result = generator.getZoneBiomeResultAt(seed, blockX, blockZ);
         if (predicate.test(result)) {
            return new Vector3i(blockX, 0, blockZ);
         }
      }

      return null;
   }
}
