package com.hypixel.hytale.server.core.asset.type.fluid;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.common.util.MapUtil;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DefaultFluidTicker extends FluidTicker {
   public static final BuilderCodec<DefaultFluidTicker> CODEC = BuilderCodec.builder(DefaultFluidTicker.class, DefaultFluidTicker::new, BASE_CODEC)
      .appendInherited(
         new KeyedCodec<>("SpreadFluid", Codec.STRING),
         (ticker, o) -> ticker.spreadFluid = o,
         ticker -> ticker.spreadFluid,
         (ticker, parent) -> ticker.spreadFluid = parent.spreadFluid
      )
      .addValidator(Fluid.VALIDATOR_CACHE.getValidator().late())
      .add()
      .<Map>appendInherited(
         new KeyedCodec<>("Collisions", new MapCodec<>(DefaultFluidTicker.FluidCollisionConfig.CODEC, HashMap::new)),
         (ticker, o) -> ticker.rawCollisionMap = MapUtil.combineUnmodifiable(ticker.rawCollisionMap, o),
         ticker -> ticker.rawCollisionMap,
         (ticker, parent) -> ticker.rawCollisionMap = parent.rawCollisionMap
      )
      .documentation("Defines what happens when this fluid tries to spread into another fluid")
      .add()
      .build();
   private static final int MAX_DROP_DISTANCE = 5;
   public static final DefaultFluidTicker INSTANCE = new DefaultFluidTicker();
   private String spreadFluid;
   private int spreadFluidId;
   private Map<String, DefaultFluidTicker.FluidCollisionConfig> rawCollisionMap = Collections.emptyMap();
   @Nullable
   private transient Int2ObjectMap<DefaultFluidTicker.FluidCollisionConfig> collisionMap = null;

   @Nonnull
   @Override
   protected BlockTickStrategy spread(
      @Nonnull World world,
      long tick,
      @Nonnull FluidTicker.Accessor accessor,
      @Nonnull FluidSection fluidSection,
      BlockSection blockSection,
      @Nonnull Fluid fluid,
      int fluidId,
      byte fluidLevel,
      int worldX,
      int worldY,
      int worldZ
   ) {
      if (worldY == 0) {
         return BlockTickStrategy.SLEEP;
      } else {
         BlockTypeAssetMap<String, BlockType> blockMap = BlockType.getAssetMap();
         IndexedLookupTableAssetMap<String, Fluid> fluidMap = Fluid.getAssetMap();
         boolean isDifferentSectionBelow = fluidSection.getY() != ChunkUtil.chunkCoordinate(worldY - 1);
         FluidSection fluidSectionBelow = isDifferentSectionBelow ? accessor.getFluidSectionByBlock(worldX, worldY - 1, worldZ) : fluidSection;
         BlockSection blockSectionBelow = isDifferentSectionBelow ? accessor.getBlockSectionByBlock(worldX, worldY - 1, worldZ) : blockSection;
         if (fluidSectionBelow != null && blockSectionBelow != null) {
            int fluidBelowId = fluidSectionBelow.getFluidId(worldX, worldY - 1, worldZ);
            Fluid fluidBelow = fluidMap.getAsset(fluidBelowId);
            byte fluidLevelBelow = fluidSectionBelow.getFluidLevel(worldX, worldY - 1, worldZ);
            int spreadFluidId = this.getSpreadFluidId(fluidId);
            int blockIdBelow = blockSectionBelow.get(worldX, worldY - 1, worldZ);
            BlockType blockBelow = blockMap.getAsset(blockIdBelow);
            if (isSolid(blockBelow) || fluidBelowId != 0 && fluidBelowId != spreadFluidId && fluidBelowId == fluidId) {
               if (fluidBelowId == 0 || fluidBelowId != spreadFluidId) {
                  if (fluidLevel == 1 && fluid.getMaxFluidLevel() != 1) {
                     return BlockTickStrategy.SLEEP;
                  }

                  int offsets = this.getSpreadOffsets(blockMap, accessor, fluidSection, blockSection, worldX, worldY, worldZ, ORTO_OFFSETS, fluidId, 5);
                  if (offsets == 2147483646) {
                     return BlockTickStrategy.WAIT_FOR_ADJACENT_CHUNK_LOAD;
                  }

                  int childFillLevel = fluidLevel - 1;
                  if (spreadFluidId != fluidId) {
                     childFillLevel = Fluid.getAssetMap().getAsset(spreadFluidId).getMaxFluidLevel() - 1;
                  }

                  BlockType sourceBlock = blockMap.getAsset(blockSection.get(worldX, worldY, worldZ));
                  int sourceRotationIndex = blockSection.getRotationIndex(worldX, worldY, worldZ);
                  int sourceFiller = blockSection.getFiller(worldX, worldY, worldZ);

                  for (int i = 0; i < ORTO_OFFSETS.length; i++) {
                     if (offsets == 0 || (offsets & 1 << i) != 0) {
                        Vector2i offset = ORTO_OFFSETS[i];
                        int x = offset.x;
                        int z = offset.y;
                        int blockX = worldX + x;
                        int blockZ = worldZ + z;
                        if (!this.blocksFluidFrom(sourceBlock, sourceRotationIndex, -x, -z, sourceFiller)) {
                           boolean isDifferentSection = !ChunkUtil.isSameChunkSection(worldX, worldY, worldZ, blockX, worldY, blockZ);
                           FluidSection otherFluidSection = isDifferentSection ? accessor.getFluidSectionByBlock(blockX, worldY, blockZ) : fluidSection;
                           BlockSection otherBlockSection = isDifferentSection ? accessor.getBlockSectionByBlock(blockX, worldY, blockZ) : blockSection;
                           if (otherFluidSection == null || otherBlockSection == null) {
                              return BlockTickStrategy.WAIT_FOR_ADJACENT_CHUNK_LOAD;
                           }

                           BlockType block = blockMap.getAsset(otherBlockSection.get(blockX, worldY, blockZ));
                           int rotationIndex = otherBlockSection.getRotationIndex(blockX, worldY, blockZ);
                           int destFiller = otherBlockSection.getFiller(blockX, worldY, blockZ);
                           if (!this.blocksFluidFrom(block, rotationIndex, x, z, destFiller)) {
                              int otherFluidId = otherFluidSection.getFluidId(blockX, worldY, blockZ);
                              if (otherFluidId != 0 && otherFluidId != spreadFluidId) {
                                 DefaultFluidTicker.FluidCollisionConfig config = (DefaultFluidTicker.FluidCollisionConfig)this.getCollisionMap()
                                    .get(otherFluidId);
                                 if (config == null || executeCollision(world, accessor, otherFluidSection, otherBlockSection, config, blockX, worldY, blockZ)) {
                                    continue;
                                 }
                              }

                              byte fillLevel = otherFluidSection.getFluidLevel(blockX, worldY, blockZ);
                              if (otherFluidId != spreadFluidId || fillLevel < childFillLevel) {
                                 if (childFillLevel == 0) {
                                    otherFluidSection.setFluid(blockX, worldY, blockZ, 0, (byte)0);
                                 } else {
                                    otherFluidSection.setFluid(blockX, worldY, blockZ, spreadFluidId, (byte)childFillLevel);
                                    otherBlockSection.setTicking(blockX, worldY, blockZ, true);
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               return BlockTickStrategy.SLEEP;
            } else {
               DefaultFluidTicker.FluidCollisionConfig config = (DefaultFluidTicker.FluidCollisionConfig)this.getCollisionMap().get(fluidBelowId);
               if (config != null && !executeCollision(world, accessor, fluidSectionBelow, blockSectionBelow, config, worldX, worldY - 1, worldZ)) {
                  return BlockTickStrategy.CONTINUE;
               } else {
                  if (fluidBelowId == 0 && !isSolid(blockBelow) || fluidBelowId == spreadFluidId && fluidLevelBelow < fluidBelow.getMaxFluidLevel()) {
                     int spreadId = this.getSpreadFluidId(fluidId);
                     Fluid spreadFluid = fluidMap.getAsset(spreadId);
                     boolean changed = fluidSectionBelow.setFluid(worldX, worldY - 1, worldZ, spreadId, (byte)spreadFluid.getMaxFluidLevel());
                     if (changed) {
                        blockSectionBelow.setTicking(worldX, worldY - 1, worldZ, true);
                     }
                  }

                  return BlockTickStrategy.SLEEP;
               }
            }
         } else {
            return BlockTickStrategy.SLEEP;
         }
      }
   }

   private static boolean executeCollision(
      @Nonnull World world,
      @Nonnull FluidTicker.Accessor accessor,
      @Nonnull FluidSection fluidSection,
      BlockSection blockSection,
      @Nonnull DefaultFluidTicker.FluidCollisionConfig config,
      int blockX,
      int blockY,
      int blockZ
   ) {
      int blockToPlace = config.getBlockToPlaceIndex();
      if (blockToPlace != Integer.MIN_VALUE) {
         accessor.setBlock(blockX, blockY, blockZ, blockToPlace);
         setTickingSurrounding(accessor, blockSection, blockX, blockY, blockZ);
         fluidSection.setFluid(blockX, blockY, blockZ, 0, (byte)0);
      }

      int soundEvent = config.getSoundEventIndex();
      if (soundEvent != Integer.MIN_VALUE) {
         world.execute(() -> SoundUtil.playSoundEvent3d(soundEvent, SoundCategory.SFX, blockX, blockY, blockZ, world.getEntityStore().getStore()));
      }

      return !config.placeFluid;
   }

   @Override
   public boolean isSelfFluid(int selfFluidId, int otherFluidId) {
      return super.isSelfFluid(selfFluidId, otherFluidId) || otherFluidId == this.getSpreadFluidId(selfFluidId);
   }

   private int getSpreadFluidId(int fluidId) {
      if (this.spreadFluidId == 0) {
         if (this.spreadFluid != null) {
            this.spreadFluidId = Fluid.getAssetMap().getIndex(this.spreadFluid);
         } else {
            this.spreadFluidId = Integer.MIN_VALUE;
         }
      }

      return this.spreadFluidId == Integer.MIN_VALUE ? fluidId : this.spreadFluidId;
   }

   @Nonnull
   public Int2ObjectMap<DefaultFluidTicker.FluidCollisionConfig> getCollisionMap() {
      if (this.collisionMap == null) {
         Int2ObjectOpenHashMap<DefaultFluidTicker.FluidCollisionConfig> collisionMap = new Int2ObjectOpenHashMap(this.rawCollisionMap.size());

         for (Entry<String, DefaultFluidTicker.FluidCollisionConfig> entry : this.rawCollisionMap.entrySet()) {
            int index = Fluid.getAssetMap().getIndex(entry.getKey());
            if (index != Integer.MIN_VALUE) {
               collisionMap.put(index, entry.getValue());
            }
         }

         this.collisionMap = collisionMap;
      }

      return this.collisionMap;
   }

   public static class FluidCollisionConfig {
      public static final BuilderCodec<DefaultFluidTicker.FluidCollisionConfig> CODEC = BuilderCodec.builder(
            DefaultFluidTicker.FluidCollisionConfig.class, DefaultFluidTicker.FluidCollisionConfig::new
         )
         .appendInherited(
            new KeyedCodec<>("BlockToPlace", Codec.STRING), (o, v) -> o.blockToPlace = v, o -> o.blockToPlace, (o, p) -> o.blockToPlace = p.blockToPlace
         )
         .documentation("The block to place when a collision occurs")
         .add()
         .<String>appendInherited(
            new KeyedCodec<>("SoundEvent", Codec.STRING), (o, v) -> o.soundEvent = v, o -> o.soundEvent, (o, p) -> o.soundEvent = p.soundEvent
         )
         .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
         .add()
         .<Boolean>appendInherited(
            new KeyedCodec<>("PlaceFluid", Codec.BOOLEAN), (o, v) -> o.placeFluid = v, o -> o.placeFluid, (o, p) -> o.placeFluid = p.placeFluid
         )
         .documentation("Whether to still place the fluid on collision")
         .add()
         .build();
      private String blockToPlace;
      private int blockToPlaceIndex = Integer.MIN_VALUE;
      public boolean placeFluid = false;
      private String soundEvent;
      private int soundEventIndex = Integer.MIN_VALUE;

      public int getBlockToPlaceIndex() {
         if (this.blockToPlaceIndex == Integer.MIN_VALUE && this.blockToPlace != null) {
            this.blockToPlaceIndex = BlockType.getBlockIdOrUnknown(this.blockToPlace, "Unknown block type %s", this.blockToPlace);
         }

         return this.blockToPlaceIndex;
      }

      public int getSoundEventIndex() {
         if (this.soundEventIndex == Integer.MIN_VALUE && this.soundEvent != null) {
            this.soundEventIndex = SoundEvent.getAssetMap().getIndex(this.soundEvent);
         }

         return this.soundEventIndex;
      }
   }
}
