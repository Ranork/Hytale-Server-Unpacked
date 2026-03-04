package com.hypixel.hytale.builtin.randomtick;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Random;
import javax.annotation.Nullable;

public class RandomTick implements Resource<ChunkStore> {
   private int blocksPerSectionPerTickStable = 1;
   private int blocksPerSectionPerTickUnstable = 3;
   private Random random = new Random();

   public static ResourceType<ChunkStore, RandomTick> getResourceType() {
      return RandomTickPlugin.get().getRandomTickResourceType();
   }

   public int getBlocksPerSectionPerTickStable() {
      return this.blocksPerSectionPerTickStable;
   }

   public int getBlocksPerSectionPerTickUnstable() {
      return this.blocksPerSectionPerTickUnstable;
   }

   public Random getRandom() {
      return this.random;
   }

   @Nullable
   @Override
   public Resource<ChunkStore> clone() {
      return new RandomTick();
   }
}
