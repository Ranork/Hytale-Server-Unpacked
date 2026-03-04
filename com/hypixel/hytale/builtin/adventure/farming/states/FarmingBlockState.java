package com.hypixel.hytale.builtin.adventure.farming.states;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.time.Instant;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated(forRemoval = true)
public class FarmingBlockState implements Component<ChunkStore> {
   @Nonnull
   public static final BuilderCodec<FarmingBlockState> CODEC = BuilderCodec.builder(FarmingBlockState.class, FarmingBlockState::new)
      .append(new KeyedCodec<>("BaseCrop", Codec.STRING), (state, crop) -> state.baseCrop = crop, state -> state.baseCrop)
      .add()
      .append(new KeyedCodec<>("StageStart", Codec.INSTANT), (state, start) -> state.stageStart = start, state -> state.stageStart)
      .add()
      .append(
         new KeyedCodec<>("CurrentFarmingStageIndex", Codec.INTEGER),
         (baseFarmingBlockState, integer) -> baseFarmingBlockState.currentFarmingStageIndex = integer,
         baseFarmingBlockState -> baseFarmingBlockState.currentFarmingStageIndex
      )
      .add()
      .append(
         new KeyedCodec<>("CurrentFarmingStageSetName", Codec.STRING),
         (farmingBlockState, s) -> farmingBlockState.currentFarmingStageSetName = s,
         farmingBlockState -> farmingBlockState.currentFarmingStageSetName
      )
      .add()
      .append(new KeyedCodec<>("SpreadRate", Codec.FLOAT), (blockState, aFloat) -> blockState.spreadRate = aFloat, blockState -> blockState.spreadRate)
      .add()
      .build();
   public boolean loaded;
   public String baseCrop;
   public Instant stageStart;
   public String currentFarmingStageSetName;
   public int currentFarmingStageIndex;
   public Instant[] stageCompletionTimes;
   public String stageSetAfterHarvest;
   public double lastGrowthMultiplier;
   public float spreadRate = 1.0F;

   public String getCurrentFarmingStageSetName() {
      return this.currentFarmingStageSetName;
   }

   public void setCurrentFarmingStageSetName(String currentFarmingStageSetName) {
      this.currentFarmingStageSetName = currentFarmingStageSetName;
   }

   public int getCurrentFarmingStageIndex() {
      return this.currentFarmingStageIndex;
   }

   public void setCurrentFarmingStageIndex(int currentFarmingStageIndex) {
      this.currentFarmingStageIndex = currentFarmingStageIndex;
   }

   public String getStageSetAfterHarvest() {
      return this.stageSetAfterHarvest;
   }

   public void setStageSetAfterHarvest(String stageSetAfterHarvest) {
      this.stageSetAfterHarvest = stageSetAfterHarvest;
   }

   public float getSpreadRate() {
      return this.spreadRate;
   }

   public void setSpreadRate(float spreadRate) {
      this.spreadRate = spreadRate;
   }

   @Nonnull
   @Override
   public String toString() {
      return "FarmingBlockState{loaded="
         + this.loaded
         + ", baseCrop="
         + this.baseCrop
         + ", stageStart="
         + this.stageStart
         + ", currentFarmingStageSetName='"
         + this.currentFarmingStageSetName
         + "', currentFarmingStageIndex="
         + this.currentFarmingStageIndex
         + ", stageCompletionTimes="
         + Arrays.toString((Object[])this.stageCompletionTimes)
         + ", stageSetAfterHarvest='"
         + this.stageSetAfterHarvest
         + "', lastGrowthMultiplier="
         + this.lastGrowthMultiplier
         + "} "
         + super.toString();
   }

   @Nullable
   @Override
   public Component<ChunkStore> clone() {
      return this;
   }

   protected static class RefreshFlags {
      protected static final int REFRESH_ALL_FLAG = 1;
      protected static final int UNLOADING_FLAG = 2;
      protected static final int RETROACTIVE_FLAG = 4;
      protected static final int DEFAULT = 1;
      protected static final int ON_UNLOADING = 3;
      protected static final int ON_LOADING = 5;
      protected static final int NONE = 0;
   }
}
