package com.hypixel.hytale.builtin.adventure.farming.config.stages.spread;

import com.hypixel.hytale.builtin.adventure.farming.states.FarmingBlock;
import com.hypixel.hytale.builtin.blockphysics.BlockPhysicsSystems;
import com.hypixel.hytale.builtin.blockphysics.BlockPhysicsUtil;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.map.IWeightedElement;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.range.IntRange;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.codec.WeightedMapCodec;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.LocalCachedChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public class DirectionalGrowthBehaviour extends SpreadGrowthBehaviour {
   @Nonnull
   public static final BuilderCodec<DirectionalGrowthBehaviour> CODEC = BuilderCodec.builder(
         DirectionalGrowthBehaviour.class, DirectionalGrowthBehaviour::new, BASE_CODEC
      )
      .append(
         new KeyedCodec<>(
            "GrowthBlockTypes", new WeightedMapCodec<>(DirectionalGrowthBehaviour.BlockTypeWeight.CODEC, new DirectionalGrowthBehaviour.BlockTypeWeight[0])
         ),
         (directionalGrowthBehaviour, blockTypeWeightIWeightedMap) -> directionalGrowthBehaviour.blockTypes = blockTypeWeightIWeightedMap,
         directionalGrowthBehaviour -> directionalGrowthBehaviour.blockTypes
      )
      .documentation("Defines a map of the possible BlockType to spread.")
      .addValidator(Validators.nonNull())
      .add()
      .<IntRange>append(
         new KeyedCodec<>("Horizontal", IntRange.CODEC),
         (directionalGrowthBehaviour, intRange) -> directionalGrowthBehaviour.horizontalRange = intRange,
         directionalGrowthBehaviour -> directionalGrowthBehaviour.horizontalRange
      )
      .documentation("Defines if the spread can happen horizontally. The range must be set with positive integers.")
      .add()
      .<IntRange>append(
         new KeyedCodec<>("Vertical", IntRange.CODEC),
         (directionalGrowthBehaviour, intRange) -> directionalGrowthBehaviour.verticalRange = intRange,
         directionalGrowthBehaviour -> directionalGrowthBehaviour.verticalRange
      )
      .documentation("Defines if the spread can happen vertically. The range must be set with positive integers.")
      .add()
      .<DirectionalGrowthBehaviour.VerticalDirection>append(
         new KeyedCodec<>("VerticalDirection", new EnumCodec<>(DirectionalGrowthBehaviour.VerticalDirection.class)),
         (directionalGrowthBehaviour, verticalDirection) -> directionalGrowthBehaviour.verticalDirection = verticalDirection,
         directionalGrowthBehaviour -> directionalGrowthBehaviour.verticalDirection
      )
      .documentation("Defines in which direction the vertical spread should happen. Possible values are: 'Upwards' and 'Downwards', default value: 'Upwards'.")
      .addValidator(Validators.nonNull())
      .add()
      .build();
   private static final int PLACE_BLOCK_TRIES = 100;
   protected IWeightedMap<DirectionalGrowthBehaviour.BlockTypeWeight> blockTypes;
   protected IntRange horizontalRange;
   protected IntRange verticalRange;
   protected DirectionalGrowthBehaviour.VerticalDirection verticalDirection = DirectionalGrowthBehaviour.VerticalDirection.BOTH;

   public IWeightedMap<DirectionalGrowthBehaviour.BlockTypeWeight> getBlockTypes() {
      return this.blockTypes;
   }

   public IntRange getHorizontalRange() {
      return this.horizontalRange;
   }

   public IntRange getVerticalRange() {
      return this.verticalRange;
   }

   public DirectionalGrowthBehaviour.VerticalDirection getVerticalDirection() {
      return this.verticalDirection;
   }

   @Override
   public void execute(
      @Nonnull ComponentAccessor<ChunkStore> componentAccessor,
      @Nonnull Ref<ChunkStore> sectionRef,
      @Nonnull Ref<ChunkStore> blockRef,
      int worldX,
      int worldY,
      int worldZ,
      float newSpreadRate
   ) {
      int x = 0;
      int z = 0;
      FastRandom random = new FastRandom();
      DirectionalGrowthBehaviour.BlockTypeWeight blockTypeWeight = this.blockTypes.get(random);
      if (blockTypeWeight != null) {
         String blockTypeKey = blockTypeWeight.getBlockTypeKey();
         World world = componentAccessor.getExternalData().getWorld();
         LocalCachedChunkAccessor chunkAccessor = LocalCachedChunkAccessor.atWorldCoords(world, worldX, worldZ, 1);

         for (int i = 0; i < 100; i++) {
            if (this.horizontalRange != null) {
               double angle = (float) (Math.PI * 2) * random.nextFloat();
               int radius = this.horizontalRange.getInt(random.nextFloat());
               x = MathUtil.fastRound(radius * TrigMathUtil.cos(angle));
               z = MathUtil.fastRound(radius * TrigMathUtil.sin(angle));
            }

            int targetX = worldX + x;
            int targetZ = worldZ + z;
            int chunkX = ChunkUtil.chunkCoordinate(targetX);
            int chunkZ = ChunkUtil.chunkCoordinate(targetZ);
            long chunkIndex = ChunkUtil.indexChunk(chunkX, chunkZ);
            WorldChunk worldChunkComponent = chunkAccessor.getChunkIfInMemory(chunkIndex);
            if (worldChunkComponent != null) {
               int targetY;
               if (this.verticalRange != null) {
                  int directionValue = switch (this.verticalDirection) {
                     case DOWNWARDS, UPWARDS -> this.verticalDirection.getValue();
                     case BOTH -> random.nextBoolean() ? 1 : -1;
                  };
                  targetY = worldY + this.verticalRange.getInt(random.nextFloat()) * directionValue;
               } else {
                  targetY = worldChunkComponent.getHeight(targetX, targetZ) + 1;
               }

               if (this.tryPlaceBlock(world, worldChunkComponent, targetX, targetY, targetZ, blockTypeKey, 0)) {
                  int finalTargetY = targetY;
                  world.execute(() -> {
                     long loadedChunkIndex = ChunkUtil.indexChunk(chunkX, chunkZ);
                     WorldChunk loadedChunk = chunkAccessor.getChunk(loadedChunkIndex);
                     if (loadedChunk != null) {
                        loadedChunk.placeBlock(targetX, finalTargetY, targetZ, blockTypeKey, Rotation.None, Rotation.None, Rotation.None);
                        BlockComponentChunk blockComponentChunk = loadedChunk.getBlockComponentChunk();
                        if (blockComponentChunk != null) {
                           decaySpread(componentAccessor, blockComponentChunk, targetX, finalTargetY, targetZ, newSpreadRate);
                        }
                     }
                  });
                  return;
               }
            }
         }
      }
   }

   private static void decaySpread(
      @Nonnull ComponentAccessor<ChunkStore> commandBuffer,
      @Nonnull BlockComponentChunk blockComponentChunk,
      int worldX,
      int worldY,
      int worldZ,
      float newSpreadRate
   ) {
      int blockIndex = ChunkUtil.indexBlockInColumn(worldX, worldY, worldZ);
      Ref<ChunkStore> blockRefPlaced = blockComponentChunk.getEntityReference(blockIndex);
      if (blockRefPlaced != null) {
         FarmingBlock farmingPlaced = commandBuffer.getComponent(blockRefPlaced, FarmingBlock.getComponentType());
         if (farmingPlaced != null) {
            farmingPlaced.setSpreadRate(newSpreadRate);
         }
      }
   }

   private boolean tryPlaceBlock(
      @Nonnull World world, @Nonnull WorldChunk chunk, int worldX, int worldY, int worldZ, @Nonnull String blockTypeKey, int rotation
   ) {
      if (chunk.getBlock(worldX, worldY, worldZ) != 0) {
         return false;
      } else if (!this.validatePosition(world, worldX, worldY, worldZ)) {
         return false;
      } else {
         BlockType blockTypeAsset = BlockType.getAssetMap().getAsset(blockTypeKey);
         if (blockTypeAsset == null) {
            return false;
         } else if (!chunk.testPlaceBlock(worldX, worldY, worldZ, blockTypeAsset, rotation)) {
            return false;
         } else {
            int chunkX = chunk.getX();
            int chunkY = ChunkUtil.indexSection(worldY);
            int chunkZ = chunk.getZ();
            ChunkStore chunkStore = world.getChunkStore();
            Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReference(chunkX, chunkY, chunkZ);
            if (sectionRef != null && sectionRef.isValid()) {
               Store<ChunkStore> store = chunkStore.getStore();
               BlockPhysics blockPhysicsComponent = store.getComponent(sectionRef, BlockPhysics.getComponentType());

               assert blockPhysicsComponent != null;

               FluidSection fluidSectionComponent = store.getComponent(sectionRef, FluidSection.getComponentType());

               assert fluidSectionComponent != null;

               BlockSection blockSectionComponent = store.getComponent(sectionRef, BlockSection.getComponentType());

               assert blockSectionComponent != null;

               int filler = blockSectionComponent.getFiller(worldX, worldY, worldZ);
               BlockPhysicsSystems.CachedAccessor cachedAccessor = BlockPhysicsSystems.CachedAccessor.of(
                  store, blockSectionComponent, blockPhysicsComponent, fluidSectionComponent, chunkX, chunkY, chunkZ, 14
               );
               return BlockPhysicsUtil.testBlockPhysics(
                     cachedAccessor,
                     blockSectionComponent,
                     blockPhysicsComponent,
                     fluidSectionComponent,
                     worldX,
                     worldY,
                     worldZ,
                     blockTypeAsset,
                     rotation,
                     filler
                  )
                  != 0;
            } else {
               return false;
            }
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "DirectionalGrowthBehaviour{blockTypes="
         + this.blockTypes
         + ", horizontalRange="
         + this.horizontalRange
         + ", verticalRange="
         + this.verticalRange
         + ", verticalDirection="
         + this.verticalDirection
         + "} "
         + super.toString();
   }

   public static class BlockTypeWeight implements IWeightedElement {
      @Nonnull
      public static final BuilderCodec<DirectionalGrowthBehaviour.BlockTypeWeight> CODEC = BuilderCodec.builder(
            DirectionalGrowthBehaviour.BlockTypeWeight.class, DirectionalGrowthBehaviour.BlockTypeWeight::new
         )
         .append(
            new KeyedCodec<>("Weight", Codec.DOUBLE), (blockTypeWeight, integer) -> blockTypeWeight.weight = integer, blockTypeWeight -> blockTypeWeight.weight
         )
         .documentation("Defines the probability to have this entry.")
         .addValidator(Validators.greaterThan(0.0))
         .add()
         .<String>append(
            new KeyedCodec<>("BlockType", Codec.STRING),
            (blockTypeWeight, blockTypeKey) -> blockTypeWeight.blockTypeKey = blockTypeKey,
            blockTypeWeight -> blockTypeWeight.blockTypeKey
         )
         .documentation("Defines the BlockType that'll be spread")
         .addValidator(Validators.nonNull())
         .add()
         .build();
      protected double weight = 1.0;
      protected String blockTypeKey;

      @Override
      public double getWeight() {
         return this.weight;
      }

      public String getBlockTypeKey() {
         return this.blockTypeKey;
      }

      @Nonnull
      @Override
      public String toString() {
         return "BlockTypeWeight{weight=" + this.weight + ", blockTypeKey=" + this.blockTypeKey + "}";
      }
   }

   private static enum VerticalDirection {
      DOWNWARDS(-1),
      BOTH(0),
      UPWARDS(1);

      private final int value;

      private VerticalDirection(int value) {
         this.value = value;
      }

      public int getValue() {
         return this.value;
      }
   }
}
