package com.hypixel.hytale.builtin.adventure.farming.states;

import com.hypixel.hytale.builtin.adventure.farming.FarmingPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.time.Instant;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FarmingBlock implements Component<ChunkStore> {
   @Nonnull
   public static final String DEFAULT_STAGE_SET = "Default";
   @Nonnull
   public static final BuilderCodec<FarmingBlock> CODEC = BuilderCodec.builder(FarmingBlock.class, FarmingBlock::new)
      .append(
         new KeyedCodec<>("CurrentStageSet", Codec.STRING),
         (farmingBlock, currentStageSet) -> farmingBlock.currentStageSet = currentStageSet,
         farmingBlock -> "Default".equals(farmingBlock.currentStageSet) ? null : "Default"
      )
      .add()
      .append(
         new KeyedCodec<>("GrowthProgress", Codec.FLOAT),
         (farmingBlock, growthProgress) -> farmingBlock.growthProgress = growthProgress,
         farmingBlock -> farmingBlock.growthProgress == 0.0F ? null : farmingBlock.growthProgress
      )
      .add()
      .append(
         new KeyedCodec<>("LastTickGameTime", Codec.INSTANT),
         (farmingBlock, lastTickGameTime) -> farmingBlock.lastTickGameTime = lastTickGameTime,
         farmingBlock -> farmingBlock.lastTickGameTime
      )
      .add()
      .append(
         new KeyedCodec<>("Generation", Codec.INTEGER),
         (farmingBlock, generation) -> farmingBlock.generation = generation,
         farmingBlock -> farmingBlock.generation == 0 ? null : farmingBlock.generation
      )
      .add()
      .append(
         new KeyedCodec<>("PreviousBlockType", Codec.STRING),
         (farmingBlock, previousBlockType) -> farmingBlock.previousBlockType = previousBlockType,
         farmingBlock -> farmingBlock.previousBlockType
      )
      .add()
      .append(
         new KeyedCodec<>("SpreadRate", Codec.FLOAT),
         (farmingBlock, spreadRate) -> farmingBlock.spreadRate = spreadRate,
         farmingBlock -> farmingBlock.spreadRate == 1.0F ? null : farmingBlock.spreadRate
      )
      .add()
      .append(
         new KeyedCodec<>("Executions", Codec.INTEGER),
         (farmingBlock, executions) -> farmingBlock.executions = executions,
         farmingBlock -> farmingBlock.executions == 0 ? null : farmingBlock.executions
      )
      .add()
      .build();
   @Nonnull
   private String currentStageSet = "Default";
   private float growthProgress;
   private Instant lastTickGameTime;
   private int generation;
   private String previousBlockType;
   private float spreadRate = 1.0F;
   private int executions;

   public static ComponentType<ChunkStore, FarmingBlock> getComponentType() {
      return FarmingPlugin.get().getFarmingBlockComponentType();
   }

   public FarmingBlock() {
   }

   public FarmingBlock(
      @Nonnull String currentStageSet,
      float growthProgress,
      Instant lastTickGameTime,
      int generation,
      String previousBlockType,
      float spreadRate,
      int executions
   ) {
      this.currentStageSet = currentStageSet;
      this.growthProgress = growthProgress;
      this.lastTickGameTime = lastTickGameTime;
      this.generation = generation;
      this.previousBlockType = previousBlockType;
      this.spreadRate = spreadRate;
      this.executions = executions;
   }

   @Nonnull
   public String getCurrentStageSet() {
      return this.currentStageSet;
   }

   public void setCurrentStageSet(@Nullable String currentStageSet) {
      this.currentStageSet = currentStageSet != null ? currentStageSet : "Default";
   }

   public float getGrowthProgress() {
      return this.growthProgress;
   }

   public void setGrowthProgress(float growthProgress) {
      this.growthProgress = growthProgress;
   }

   public Instant getLastTickGameTime() {
      return this.lastTickGameTime;
   }

   public void setLastTickGameTime(Instant lastTickGameTime) {
      this.lastTickGameTime = lastTickGameTime;
   }

   public int getGeneration() {
      return this.generation;
   }

   public void setGeneration(int generation) {
      this.generation = generation;
   }

   public String getPreviousBlockType() {
      return this.previousBlockType;
   }

   public void setPreviousBlockType(String previousBlockType) {
      this.previousBlockType = previousBlockType;
   }

   public float getSpreadRate() {
      return this.spreadRate;
   }

   public void setSpreadRate(float spreadRate) {
      this.spreadRate = spreadRate;
   }

   public int getExecutions() {
      return this.executions;
   }

   public void setExecutions(int executions) {
      this.executions = executions;
   }

   @Nullable
   @Override
   public Component<ChunkStore> clone() {
      return new FarmingBlock(
         this.currentStageSet, this.growthProgress, this.lastTickGameTime, this.generation, this.previousBlockType, this.spreadRate, this.executions
      );
   }

   @Nonnull
   @Override
   public String toString() {
      return "FarmingBlock{currentStageSet='"
         + this.currentStageSet
         + "', growthProgress="
         + this.growthProgress
         + ", lastTickGameTime="
         + this.lastTickGameTime
         + ", generation="
         + this.generation
         + ", previousBlockType='"
         + this.previousBlockType
         + "', spreadRate="
         + this.spreadRate
         + ", executions="
         + this.executions
         + "}";
   }
}
