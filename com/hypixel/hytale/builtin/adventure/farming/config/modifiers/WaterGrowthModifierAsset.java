package com.hypixel.hytale.builtin.adventure.farming.config.modifiers;

import com.hypixel.hytale.builtin.adventure.farming.states.TilledSoilBlock;
import com.hypixel.hytale.builtin.weather.resources.WeatherResource;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.GrowthModifierAsset;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.time.Instant;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WaterGrowthModifierAsset extends GrowthModifierAsset {
   @Nonnull
   public static final BuilderCodec<WaterGrowthModifierAsset> CODEC = BuilderCodec.builder(
         WaterGrowthModifierAsset.class, WaterGrowthModifierAsset::new, ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Fluids", new ArrayCodec<>(Codec.STRING, String[]::new)), (asset, blocks) -> asset.fluids = blocks, asset -> asset.fluids)
      .addValidator(Fluid.VALIDATOR_CACHE.getArrayValidator().late())
      .add()
      .<String[]>append(new KeyedCodec<>("Weathers", Codec.STRING_ARRAY), (asset, weathers) -> asset.weathers = weathers, asset -> asset.weathers)
      .addValidator(Weather.VALIDATOR_CACHE.getArrayValidator())
      .add()
      .addField(new KeyedCodec<>("RainDuration", Codec.INTEGER), (asset, duration) -> asset.rainDuration = duration, asset -> asset.rainDuration)
      .afterDecode(asset -> {
         if (asset.fluids != null) {
            asset.fluidIds = new IntOpenHashSet();

            for (int i = 0; i < asset.fluids.length; i++) {
               asset.fluidIds.add(Fluid.getAssetMap().getIndex(asset.fluids[i]));
            }
         }

         if (asset.weathers != null) {
            asset.weatherIds = new IntOpenHashSet();

            for (int i = 0; i < asset.weathers.length; i++) {
               asset.weatherIds.add(Weather.getAssetMap().getIndex(asset.weathers[i]));
            }
         }
      })
      .build();
   protected String[] fluids;
   protected IntOpenHashSet fluidIds;
   protected String[] weathers;
   protected IntOpenHashSet weatherIds;
   protected int rainDuration;

   public String[] getFluids() {
      return this.fluids;
   }

   public IntOpenHashSet getFluidIds() {
      return this.fluidIds;
   }

   public String[] getWeathers() {
      return this.weathers;
   }

   public IntOpenHashSet getWeatherIds() {
      return this.weatherIds;
   }

   public int getRainDuration() {
      return this.rainDuration;
   }

   @Override
   public double getCurrentGrowthMultiplier(
      @Nonnull CommandBuffer<ChunkStore> commandBuffer,
      @Nonnull Ref<ChunkStore> sectionRef,
      @Nonnull Ref<ChunkStore> blockRef,
      int x,
      int y,
      int z,
      boolean initialTick
   ) {
      boolean hasWaterBlock = this.checkIfWaterSource(commandBuffer, sectionRef, x, y, z);
      boolean isRaining = this.checkIfRaining(commandBuffer, sectionRef, x, y, z);
      boolean active = hasWaterBlock || isRaining;
      TilledSoilBlock soil = getSoil(commandBuffer, sectionRef, x, y, z);
      if (soil != null) {
         if (soil.hasExternalWater() != active) {
            soil.setExternalWater(active);
            BlockSection blockSectionComponent = commandBuffer.getComponent(sectionRef, BlockSection.getComponentType());
            if (blockSectionComponent != null) {
               blockSectionComponent.setTicking(x, y, z, true);
            }
         }

         active |= isSoilWaterExpiring(
            commandBuffer.getExternalData().getWorld().getEntityStore().getStore().getResource(WorldTimeResource.getResourceType()), soil
         );
      }

      return !active ? 1.0 : super.getCurrentGrowthMultiplier(commandBuffer, sectionRef, blockRef, x, y, z, initialTick);
   }

   @Nullable
   private static TilledSoilBlock getSoil(@Nonnull CommandBuffer<ChunkStore> commandBuffer, @Nonnull Ref<ChunkStore> sectionRef, int x, int y, int z) {
      ChunkSection chunkSectionComponent = commandBuffer.getComponent(sectionRef, ChunkSection.getComponentType());
      if (chunkSectionComponent == null) {
         return null;
      } else {
         Ref<ChunkStore> chunkRef = chunkSectionComponent.getChunkColumnReference();
         if (chunkRef != null && chunkRef.isValid()) {
            BlockComponentChunk blockComponentChunk = commandBuffer.getComponent(chunkRef, BlockComponentChunk.getComponentType());
            if (blockComponentChunk == null) {
               return null;
            } else {
               int blockBelowIndex = ChunkUtil.indexBlockInColumn(x, y - 1, z);
               Ref<ChunkStore> blockBelowRef = blockComponentChunk.getEntityReference(blockBelowIndex);
               return blockBelowRef == null ? null : commandBuffer.getComponent(blockBelowRef, TilledSoilBlock.getComponentType());
            }
         } else {
            return null;
         }
      }
   }

   protected boolean checkIfWaterSource(@Nonnull CommandBuffer<ChunkStore> commandBuffer, @Nonnull Ref<ChunkStore> sectionRef, int x, int y, int z) {
      IntOpenHashSet waterBlocks = this.fluidIds;
      if (waterBlocks == null) {
         return false;
      } else {
         TilledSoilBlock soil = getSoil(commandBuffer, sectionRef, x, y, z);
         if (soil == null) {
            return false;
         } else {
            int[] fluids = getNeighbourFluids(commandBuffer, sectionRef, x, y - 1, z);
            if (fluids == null) {
               return false;
            } else {
               for (int block : fluids) {
                  if (waterBlocks.contains(block)) {
                     return true;
                  }
               }

               return false;
            }
         }
      }
   }

   @Nullable
   private static int[] getNeighbourFluids(@Nonnull CommandBuffer<ChunkStore> commandBuffer, @Nonnull Ref<ChunkStore> sectionRef, int x, int y, int z) {
      ChunkSection chunkSectionComponent = commandBuffer.getComponent(sectionRef, ChunkSection.getComponentType());
      return chunkSectionComponent == null
         ? null
         : new int[]{
            getFluidAtPos(x - 1, y, z, sectionRef, chunkSectionComponent, commandBuffer),
            getFluidAtPos(x + 1, y, z, sectionRef, chunkSectionComponent, commandBuffer),
            getFluidAtPos(x, y, z - 1, sectionRef, chunkSectionComponent, commandBuffer),
            getFluidAtPos(x, y, z + 1, sectionRef, chunkSectionComponent, commandBuffer)
         };
   }

   private static int getFluidAtPos(
      int posX,
      int posY,
      int posZ,
      @Nonnull Ref<ChunkStore> sectionRef,
      @Nonnull ChunkSection currentChunkSection,
      @Nonnull CommandBuffer<ChunkStore> commandBuffer
   ) {
      Ref<ChunkStore> chunkToUse = sectionRef;
      int chunkX = ChunkUtil.worldCoordFromLocalCoord(currentChunkSection.getX(), posX);
      int chunkY = ChunkUtil.worldCoordFromLocalCoord(currentChunkSection.getY(), posY);
      int chunkZ = ChunkUtil.worldCoordFromLocalCoord(currentChunkSection.getZ(), posZ);
      if (ChunkUtil.isSameChunkSection(chunkX, chunkY, chunkZ, currentChunkSection.getX(), currentChunkSection.getY(), currentChunkSection.getZ())) {
         chunkToUse = commandBuffer.getExternalData().getChunkSectionReference(chunkX, chunkY, chunkZ);
      }

      if (chunkToUse == null) {
         return Integer.MIN_VALUE;
      } else {
         FluidSection fluidSectionComponent = commandBuffer.getComponent(chunkToUse, FluidSection.getComponentType());
         return fluidSectionComponent == null ? Integer.MIN_VALUE : fluidSectionComponent.getFluidId(posX, posY, posZ);
      }
   }

   protected boolean checkIfRaining(@Nonnull CommandBuffer<ChunkStore> commandBuffer, @Nonnull Ref<ChunkStore> sectionRef, int x, int y, int z) {
      if (this.weatherIds == null) {
         return false;
      } else {
         ChunkSection chunkSectionComponent = commandBuffer.getComponent(sectionRef, ChunkSection.getComponentType());
         if (chunkSectionComponent == null) {
            return false;
         } else {
            Ref<ChunkStore> chunkRef = chunkSectionComponent.getChunkColumnReference();
            BlockChunk blockChunkComponent = commandBuffer.getComponent(chunkRef, BlockChunk.getComponentType());
            if (blockChunkComponent == null) {
               return false;
            } else {
               int blockId = blockChunkComponent.getBlock(x, y, z);
               Store<EntityStore> entityStore = commandBuffer.getExternalData().getWorld().getEntityStore().getStore();
               WeatherResource weatherResource = entityStore.getResource(WeatherResource.getResourceType());
               int environment = blockChunkComponent.getEnvironment(x, y, z);
               int weatherId;
               if (weatherResource.getForcedWeatherIndex() != 0) {
                  weatherId = weatherResource.getForcedWeatherIndex();
               } else {
                  weatherId = weatherResource.getWeatherIndexForEnvironment(environment);
               }

               if (!this.weatherIds.contains(weatherId)) {
                  return false;
               } else {
                  boolean unobstructed = true;

                  for (int searchY = y + 1; searchY < 320; searchY++) {
                     int block = blockChunkComponent.getBlock(x, searchY, z);
                     if (block != 0 && block != blockId) {
                        unobstructed = false;
                        break;
                     }
                  }

                  return unobstructed;
               }
            }
         }
      }
   }

   private static boolean isSoilWaterExpiring(@Nonnull WorldTimeResource worldTimeResource, @Nonnull TilledSoilBlock soilBlock) {
      Instant until = soilBlock.getWateredUntil();
      if (until == null) {
         return false;
      } else {
         Instant now = worldTimeResource.getGameTime();
         if (now.isAfter(until)) {
            soilBlock.setWateredUntil(null);
            return false;
         } else {
            return true;
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "WaterGrowthModifierAsset{blocks="
         + Arrays.toString((Object[])this.fluids)
         + ", blockIds="
         + this.fluidIds
         + ", weathers="
         + Arrays.toString((Object[])this.weathers)
         + ", weatherIds="
         + this.weatherIds
         + ", rainDuration="
         + this.rainDuration
         + "} "
         + super.toString();
   }
}
