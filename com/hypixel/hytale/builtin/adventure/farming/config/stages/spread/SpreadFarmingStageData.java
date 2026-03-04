package com.hypixel.hytale.builtin.adventure.farming.config.stages.spread;

import com.hypixel.hytale.builtin.adventure.farming.states.FarmingBlock;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.range.IntRange;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingStageData;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpreadFarmingStageData extends FarmingStageData {
   @Nonnull
   public static final BuilderCodec<SpreadFarmingStageData> CODEC = BuilderCodec.builder(
         SpreadFarmingStageData.class, SpreadFarmingStageData::new, FarmingStageData.BASE_CODEC
      )
      .append(
         new KeyedCodec<>("Executions", IntRange.CODEC),
         (spreadFarmingStageData, intRange) -> spreadFarmingStageData.executions = intRange,
         spreadFarmingStageData -> spreadFarmingStageData.executions
      )
      .documentation("Defines the number of times the stage will be repeated. Range must be positive, min value must be >= 1.")
      .addValidator(Validators.nonNull())
      .add()
      .<IntRange>append(
         new KeyedCodec<>("SpreadDecayPercent", IntRange.CODEC),
         (spreadFarmingStageData, intRange) -> spreadFarmingStageData.spreadDecayPercent = intRange,
         spreadFarmingStageData -> spreadFarmingStageData.spreadDecayPercent
      )
      .documentation(
         "The amount to reduce (linear decay) the spread rate (chance to spread) for any spawned blocks that also have a spread stage. Range must be positive."
      )
      .addValidator(Validators.nonNull())
      .add()
      .<SpreadGrowthBehaviour[]>append(
         new KeyedCodec<>("GrowthBehaviours", new ArrayCodec<>(SpreadGrowthBehaviour.CODEC, SpreadGrowthBehaviour[]::new)),
         (spreadFarmingStageData, spreadGrowthBehaviour) -> spreadFarmingStageData.spreadGrowthBehaviours = spreadGrowthBehaviour,
         spreadFarmingStageData -> spreadFarmingStageData.spreadGrowthBehaviours
      )
      .documentation("Defines an array of the different growth behaviours that'll be run for each execution.")
      .addValidator(Validators.nonEmptyArray())
      .add()
      .afterDecode(
         stageData -> {
            if (stageData.executions != null && stageData.executions.getInclusiveMin() < 1) {
               throw new IllegalArgumentException(
                  "The min value for Executions range must be >= 1! Current min value is: " + stageData.executions.getInclusiveMin()
               );
            } else if (stageData.spreadDecayPercent != null && stageData.spreadDecayPercent.getInclusiveMin() < 0) {
               throw new IllegalArgumentException(
                  "The min value for SpreadDecayPercent range must be >= 0! Current min value is: " + stageData.spreadDecayPercent.getInclusiveMin()
               );
            }
         }
      )
      .build();
   protected IntRange executions;
   protected IntRange spreadDecayPercent;
   protected SpreadGrowthBehaviour[] spreadGrowthBehaviours;

   public IntRange getExecutions() {
      return this.executions;
   }

   public IntRange getSpreadDecayPercent() {
      return this.spreadDecayPercent;
   }

   public SpreadGrowthBehaviour[] getSpreadGrowthBehaviours() {
      return this.spreadGrowthBehaviours;
   }

   @Override
   public boolean implementsShouldStop() {
      return true;
   }

   @Override
   public boolean shouldStop(
      @Nonnull ComponentAccessor<ChunkStore> commandBuffer, @Nonnull Ref<ChunkStore> sectionRef, @Nonnull Ref<ChunkStore> blockRef, int x, int y, int z
   ) {
      FarmingBlock farmingBlockComponent = commandBuffer.getComponent(blockRef, FarmingBlock.getComponentType());
      if (farmingBlockComponent == null) {
         return true;
      } else {
         ChunkSection chunkSectionComponent = commandBuffer.getComponent(sectionRef, ChunkSection.getComponentType());
         if (chunkSectionComponent == null) {
            return true;
         } else {
            int worldX = ChunkUtil.worldCoordFromLocalCoord(chunkSectionComponent.getX(), x);
            int worldY = ChunkUtil.worldCoordFromLocalCoord(chunkSectionComponent.getY(), y);
            int worldZ = ChunkUtil.worldCoordFromLocalCoord(chunkSectionComponent.getZ(), z);
            float spreadRate = farmingBlockComponent.getSpreadRate();
            float executions = this.executions.getInt(HashUtil.random(worldX, worldY, worldZ, farmingBlockComponent.getGeneration())) * spreadRate;
            int executed = farmingBlockComponent.getExecutions();
            return spreadRate <= 0.0F || executed >= executions;
         }
      }
   }

   @Override
   public void apply(
      @Nonnull ComponentAccessor<ChunkStore> commandBuffer,
      @Nonnull Ref<ChunkStore> sectionRef,
      @Nonnull Ref<ChunkStore> blockRef,
      int x,
      int y,
      int z,
      @Nullable FarmingStageData previousStage
   ) {
      super.apply(commandBuffer, sectionRef, blockRef, x, y, z, previousStage);
      FarmingBlock farmingBlockComponent = commandBuffer.getComponent(blockRef, FarmingBlock.getComponentType());
      if (farmingBlockComponent != null) {
         ChunkSection chunkSectionComponent = commandBuffer.getComponent(sectionRef, ChunkSection.getComponentType());
         if (chunkSectionComponent != null) {
            int worldX = ChunkUtil.worldCoordFromLocalCoord(chunkSectionComponent.getX(), x);
            int worldY = ChunkUtil.worldCoordFromLocalCoord(chunkSectionComponent.getY(), y);
            int worldZ = ChunkUtil.worldCoordFromLocalCoord(chunkSectionComponent.getZ(), z);
            float spreadRate = farmingBlockComponent.getSpreadRate();
            int generation = farmingBlockComponent.getGeneration();
            double executions = Math.floor(this.executions.getInt(HashUtil.random(worldX, worldY, worldZ, generation)) * spreadRate);
            int executed = farmingBlockComponent.getExecutions();
            if (!(spreadRate <= 0.0F) && !(executed >= executions)) {
               for (int i = 0; i < this.spreadGrowthBehaviours.length; i++) {
                  SpreadGrowthBehaviour spreadGrowthBehaviour = this.spreadGrowthBehaviours[i];
                  float decayRate = this.spreadDecayPercent.getInt(HashUtil.random(i | (long)generation << 32, worldX, worldY, worldZ)) / 100.0F;
                  spreadGrowthBehaviour.execute(commandBuffer, sectionRef, blockRef, worldX, worldY, worldZ, spreadRate - decayRate);
               }

               farmingBlockComponent.setExecutions(++executed);
            }
         }
      }
   }

   @Override
   public void remove(
      @Nonnull ComponentAccessor<ChunkStore> commandBuffer, @Nonnull Ref<ChunkStore> sectionRef, @Nonnull Ref<ChunkStore> blockRef, int x, int y, int z
   ) {
      super.remove(commandBuffer, sectionRef, blockRef, x, y, z);
   }

   @Nonnull
   @Override
   public String toString() {
      return "SpreadFarmingStageData{executions="
         + this.executions
         + ", spreadDecayPercent="
         + this.spreadDecayPercent
         + ", spreadGrowthBehaviours="
         + Arrays.toString((Object[])this.spreadGrowthBehaviours)
         + "} "
         + super.toString();
   }
}
