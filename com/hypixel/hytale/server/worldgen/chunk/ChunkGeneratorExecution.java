package com.hypixel.hytale.server.worldgen.chunk;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockStateChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedEntityChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenTimingsCollector;
import com.hypixel.hytale.server.worldgen.cache.CoreDataCacheEntry;
import com.hypixel.hytale.server.worldgen.chunk.populator.BlockPopulator;
import com.hypixel.hytale.server.worldgen.chunk.populator.CavePopulator;
import com.hypixel.hytale.server.worldgen.chunk.populator.PrefabPopulator;
import com.hypixel.hytale.server.worldgen.chunk.populator.WaterPopulator;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import javax.annotation.Nonnull;

public class ChunkGeneratorExecution {
   @Nonnull
   private final ChunkGenerator chunkGenerator;
   private final GeneratedBlockChunk blockChunk;
   private final GeneratedBlockStateChunk blockStateChunk;
   private final GeneratedEntityChunk entityChunk;
   private final Holder<ChunkStore>[] sections;
   @Nonnull
   private final BlockPriorityChunk priorityChunk;
   @Nonnull
   private final HeightThresholdInterpolator interpolator;
   private BlockPriorityModifier blockPriorityModifier = BlockPriorityModifier.NONE;

   public ChunkGeneratorExecution(
      int seed,
      @Nonnull ChunkGenerator chunkGenerator,
      GeneratedBlockChunk blockChunk,
      GeneratedBlockStateChunk blockStateChunk,
      GeneratedEntityChunk entityChunk,
      Holder<ChunkStore>[] sections
   ) {
      this.chunkGenerator = chunkGenerator;
      this.blockChunk = blockChunk;
      this.blockStateChunk = blockStateChunk;
      this.entityChunk = entityChunk;
      this.sections = sections;
      this.priorityChunk = ChunkGenerator.getResource().priorityChunk.reset();
      long start = -System.nanoTime();
      this.interpolator = new HeightThresholdInterpolator(this).populate(seed);
      chunkGenerator.getTimings().reportPrepare(start + System.nanoTime());
   }

   public void execute(int seed) {
      WorldGenTimingsCollector timings = this.chunkGenerator.getTimings();
      this.generateTintMapping(seed);
      this.generateEnvironmentMapping(seed);
      long start = -System.nanoTime();
      BlockPopulator.populate(seed, this);
      timings.reportBlocksGeneration(start + System.nanoTime());
      start = -System.nanoTime();
      CavePopulator.populate(seed, this);
      timings.reportCaveGeneration(start + System.nanoTime());
      start = -System.nanoTime();
      PrefabPopulator.populate(seed, this);
      timings.reportPrefabGeneration(start + System.nanoTime());
      WaterPopulator.populate(seed, this);
   }

   @Nonnull
   public ChunkGenerator getChunkGenerator() {
      return this.chunkGenerator;
   }

   public GeneratedBlockChunk getChunk() {
      return this.blockChunk;
   }

   public GeneratedBlockStateChunk getBlockStateChunk() {
      return this.blockStateChunk;
   }

   public GeneratedEntityChunk getEntityChunk() {
      return this.entityChunk;
   }

   @Nonnull
   public BlockPriorityChunk getPriorityChunk() {
      return this.priorityChunk;
   }

   @Nonnull
   public HeightThresholdInterpolator getInterpolator() {
      return this.interpolator;
   }

   public Holder<ChunkStore> getSection(int y) {
      return this.sections[y];
   }

   public ZoneBiomeResult zoneBiomeResult(int cx, int cz) {
      return this.interpolator.zoneBiomeResult(cx, cz);
   }

   @Nonnull
   public CoreDataCacheEntry[] getCoreDataEntries() {
      return this.interpolator.getEntries();
   }

   public long getIndex() {
      return this.blockChunk.getIndex();
   }

   public int getX() {
      return this.blockChunk.getX();
   }

   public int getZ() {
      return this.blockChunk.getZ();
   }

   public void setPriorityModifier(BlockPriorityModifier blockPriorityModifier) {
      this.blockPriorityModifier = blockPriorityModifier;
   }

   private void generateTintMapping(int seed) {
      int radius = 4;
      int[] rawTint = new int[(32 + radius * 2) * (32 + radius * 2)];
      int m = 32 + radius;

      for (int cx = -radius; cx < m; cx++) {
         for (int cz = -radius; cz < m; cz++) {
            rawTint[tintIndexLocal(cx, cz)] = this.zoneBiomeResult(cx, cz)
               .getBiome()
               .getTintContainer()
               .getTintColorAt(seed, this.globalX(cx), this.globalZ(cz));
         }
      }

      m = radius * radius;

      for (int cx = 0; cx < 32; cx++) {
         for (int cz = 0; cz < 32; cz++) {
            int r = 0;
            int g = 0;
            int b = 0;
            int counter = 0;

            for (int ix = -radius; ix <= radius; ix++) {
               for (int iz = -radius; iz <= radius; iz++) {
                  if (ix * ix + iz * iz <= m) {
                     int c = rawTint[tintIndexLocal(cx + ix, cz + iz)];
                     r += c >> 16 & 0xFF;
                     g += c >> 8 & 0xFF;
                     b += c & 0xFF;
                     counter++;
                  }
               }
            }

            if (counter > 0) {
               r /= counter;
               g /= counter;
               b /= counter;
               this.blockChunk.setTint(cx, cz, 0xFF000000 | r << 16 | g << 8 | b);
            } else {
               this.blockChunk.setTint(cx, cz, -65536);
            }
         }
      }
   }

   public static int tintIndexLocal(int x, int z) {
      return (x + 4) * 40 + z + 4;
   }

   private void generateEnvironmentMapping(int seed) {
      for (int cx = 0; cx < 32; cx++) {
         for (int cz = 0; cz < 32; cz++) {
            int envId = this.zoneBiomeResult(cx, cz).getBiome().getEnvironmentContainer().getEnvironmentAt(seed, this.globalX(cx), this.globalZ(cz));
            this.blockChunk.setEnvironmentColumn(cx, cz, envId);
         }
      }
   }

   public int getBlock(int x, int y, int z) {
      return this.blockChunk.getBlock(x, y, z);
   }

   public int getRotationIndex(int x, int y, int z) {
      return this.blockChunk.getRotationIndex(x, y, z);
   }

   public void setEnvironment(int x, int y, int z, int environment) {
      if (environment != Integer.MIN_VALUE) {
         this.blockChunk.setEnvironment(x, y, z, environment);
      }
   }

   public boolean setBlock(int x, int y, int z, byte type, int block) {
      return this.setBlock(x, y, z, type, block, null);
   }

   public boolean setBlock(int x, int y, int z, byte type, BlockFluidEntry entry) {
      return this.setBlock(x, y, z, type, entry.blockId(), null, -1, entry.rotation(), 0);
   }

   public boolean setBlock(int x, int y, int z, byte type, int block, int environment) {
      if (this.setBlock(x, y, z, type, block, null)) {
         this.setEnvironment(x, y, z, environment);
         return true;
      } else {
         return false;
      }
   }

   public boolean setBlock(int x, int y, int z, byte type, BlockFluidEntry entry, int environment) {
      if (this.setBlock(x, y, z, type, entry.blockId(), null, -1, entry.rotation(), 0)) {
         this.setEnvironment(x, y, z, environment);
         return true;
      } else {
         return false;
      }
   }

   public boolean setBlock(int x, int y, int z, byte type, int block, Holder<ChunkStore> holder) {
      return this.setBlock(x, y, z, type, block, holder, -1, 0, 0);
   }

   public boolean setBlock(int x, int y, int z, byte type, int block, Holder<ChunkStore> holder, int supportValue, int rotation, int filler) {
      if (y >= 0 && y < 320) {
         byte newPriority = (byte)(type & 31);
         byte newFlags = (byte)(type & -32);
         byte oldPriority = this.priorityChunk.get(x, y, z);
         byte oldModified = this.blockPriorityModifier.modifyCurrent(oldPriority, newPriority);
         if (type == -1) {
            newPriority = oldModified;
         }

         if (newPriority < oldModified) {
            return false;
         } else {
            newPriority = (byte)(this.blockPriorityModifier.modifyTarget(oldPriority, newPriority) | newFlags);
            this.priorityChunk.set(x, y, z, newPriority);
            this.blockChunk.setBlock(x, y, z, block, rotation, filler);
            this.blockStateChunk.setState(x, y, z, holder);
            Holder<ChunkStore> section = this.getSection(ChunkUtil.chunkCoordinate(y));
            if (supportValue >= 0) {
               BlockPhysics.setSupportValue(section, x, y, z, supportValue);
            } else {
               BlockType blockType = BlockType.getAssetMap().getAsset(block);
               if (blockType != null && blockType.hasSupport()) {
                  BlockPhysics.reset(section, x, y, z);
               } else {
                  BlockPhysics.clear(section, x, y, z);
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public boolean setFluid(int x, int y, int z, byte type, int fluid, int environment) {
      if (this.setFluid(x, y, z, type, fluid)) {
         this.setEnvironment(x, y, z, environment);
         return true;
      } else {
         return false;
      }
   }

   public boolean setFluid(int x, int y, int z, byte type, int fluid) {
      return this.setFluid(x, y, z, type, fluid, (byte)Fluid.getAssetMap().getAsset(fluid).getMaxFluidLevel());
   }

   public boolean setFluid(int x, int y, int z, byte type, int fluid, byte fluidLevel) {
      if (y >= 0 && y < 320) {
         byte newPriority = (byte)(type & 31);
         byte newFlags = (byte)(type & -32);
         byte oldPriority = this.priorityChunk.get(x, y, z);
         byte oldModified = this.blockPriorityModifier.modifyCurrent(oldPriority, newPriority);
         if (type == -1) {
            newPriority = oldModified;
         }

         if (newPriority >= oldModified) {
            newPriority = (byte)(this.blockPriorityModifier.modifyTarget(oldPriority, newPriority) | newFlags);
            this.priorityChunk.set(x, y, z, newPriority);
            Holder<ChunkStore> section = this.getSection(ChunkUtil.chunkCoordinate(y));
            FluidSection fluidSection = section.getComponent(FluidSection.getComponentType());
            if (fluidSection == null) {
               fluidSection = section.ensureAndGetComponent(FluidSection.getComponentType());
            }

            fluidSection.setFluid(x, y, z, fluid, fluidLevel);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public int getFluid(int x, int y, int z) {
      if (y >= 0 && y < 320) {
         Holder<ChunkStore> section = this.getSection(ChunkUtil.chunkCoordinate(y));
         FluidSection fluidSection = section.ensureAndGetComponent(FluidSection.getComponentType());
         return fluidSection.getFluidId(x, y, z);
      } else {
         return Integer.MIN_VALUE;
      }
   }

   public void overrideBlock(int x, int y, int z, byte type, int block) {
      this.overrideBlock(x, y, z, type, block, null);
   }

   public void overrideBlock(int x, int y, int z, byte type, int block, Holder<ChunkStore> holder) {
      this.overrideBlock(x, y, z, type, block, holder, 0, 0);
   }

   public void overrideBlock(int x, int y, int z, byte type, BlockFluidEntry entry) {
      this.overrideBlock(x, y, z, type, entry.blockId(), null, entry.rotation(), 0);
   }

   public void overrideBlock(int x, int y, int z, byte type, int block, Holder<ChunkStore> holder, int rotation, int filler) {
      this.priorityChunk.set(x, y, z, type);
      this.blockChunk.setBlock(x, y, z, block, rotation, filler);
      this.blockStateChunk.setState(x, y, z, holder);
      Holder<ChunkStore> section = this.getSection(ChunkUtil.chunkCoordinate(y));
      BlockType blockType = BlockType.getAssetMap().getAsset(block);
      if (blockType != null && blockType.hasSupport()) {
         BlockPhysics.reset(section, x, y, z);
      } else {
         BlockPhysics.clear(section, x, y, z);
      }
   }

   public void overrideFluid(int x, int y, int z, byte type, int fluid) {
      if (y >= 0 && y < 320) {
         this.priorityChunk.set(x, y, z, type);
         Holder<ChunkStore> section = this.getSection(ChunkUtil.chunkCoordinate(y));
         FluidSection fluidSection = section.ensureAndGetComponent(FluidSection.getComponentType());
         fluidSection.setFluid(x, y, z, fluid, (byte)Fluid.getAssetMap().getAsset(fluid).getMaxFluidLevel());
      }
   }

   protected int localX(int x) {
      return x - this.blockChunk.getX() * 32;
   }

   protected int localZ(int z) {
      return z - this.blockChunk.getZ() * 32;
   }

   public int globalX(int localX) {
      return ChunkUtil.minBlock(this.blockChunk.getX()) + localX;
   }

   public int globalZ(int localZ) {
      return ChunkUtil.minBlock(this.blockChunk.getZ()) + localZ;
   }
}
