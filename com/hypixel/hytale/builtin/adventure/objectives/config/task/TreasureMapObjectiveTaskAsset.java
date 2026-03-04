package com.hypixel.hytale.builtin.adventure.objectives.config.task;

import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.builtin.adventure.objectives.config.taskcondition.TaskConditionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.worldlocationproviders.WorldLocationProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TreasureMapObjectiveTaskAsset extends ObjectiveTaskAsset {
   @Nonnull
   public static final BuilderCodec<TreasureMapObjectiveTaskAsset> CODEC = BuilderCodec.builder(
         TreasureMapObjectiveTaskAsset.class, TreasureMapObjectiveTaskAsset::new, BASE_CODEC
      )
      .append(
         new KeyedCodec<>("Chests", new ArrayCodec<>(TreasureMapObjectiveTaskAsset.ChestConfig.CODEC, TreasureMapObjectiveTaskAsset.ChestConfig[]::new)),
         (treasureMapObjectiveTaskAsset, chestConfigs) -> treasureMapObjectiveTaskAsset.chestConfigs = chestConfigs,
         treasureMapObjectiveTaskAsset -> treasureMapObjectiveTaskAsset.chestConfigs
      )
      .addValidator(Validators.nonEmptyArray())
      .add()
      .build();
   protected TreasureMapObjectiveTaskAsset.ChestConfig[] chestConfigs;

   public TreasureMapObjectiveTaskAsset(
      String descriptionId, TaskConditionAsset[] taskConditions, Vector3i[] mapMarkers, TreasureMapObjectiveTaskAsset.ChestConfig[] chestConfigs
   ) {
      super(descriptionId, taskConditions, mapMarkers);
      this.chestConfigs = chestConfigs;
   }

   protected TreasureMapObjectiveTaskAsset() {
   }

   @Nonnull
   @Override
   public ObjectiveTaskAsset.TaskScope getTaskScope() {
      return ObjectiveTaskAsset.TaskScope.PLAYER;
   }

   public TreasureMapObjectiveTaskAsset.ChestConfig[] getChestConfigs() {
      return this.chestConfigs;
   }

   @Override
   protected boolean matchesAsset0(ObjectiveTaskAsset task) {
      return task instanceof TreasureMapObjectiveTaskAsset treasureMapObjectiveTaskAsset
         ? Arrays.equals((Object[])treasureMapObjectiveTaskAsset.chestConfigs, (Object[])this.chestConfigs)
         : false;
   }

   @Nonnull
   @Override
   public String toString() {
      return "TreasureMapObjectiveTaskAsset{chestConfigs=" + Arrays.toString((Object[])this.chestConfigs) + "} " + super.toString();
   }

   public static class ChestConfig {
      @Nonnull
      public static final BuilderCodec<TreasureMapObjectiveTaskAsset.ChestConfig> CODEC = BuilderCodec.builder(
            TreasureMapObjectiveTaskAsset.ChestConfig.class, TreasureMapObjectiveTaskAsset.ChestConfig::new
         )
         .append(new KeyedCodec<>("MinRadius", Codec.FLOAT), (chestConfig, aFloat) -> chestConfig.minRadius = aFloat, chestConfig -> chestConfig.minRadius)
         .addValidator(Validators.greaterThan(0.0F))
         .add()
         .<Float>append(
            new KeyedCodec<>("MaxRadius", Codec.FLOAT), (chestConfig, aFloat) -> chestConfig.maxRadius = aFloat, chestConfig -> chestConfig.maxRadius
         )
         .addValidator(Validators.greaterThan(1.0F))
         .add()
         .<String>append(
            new KeyedCodec<>("DropList", new ContainedAssetCodec<>(ItemDropList.class, ItemDropList.CODEC)),
            (chestConfig, s) -> chestConfig.droplistId = s,
            chestConfig -> chestConfig.droplistId
         )
         .addValidator(Validators.nonNull())
         .addValidator(ItemDropList.VALIDATOR_CACHE.getValidator())
         .add()
         .append(
            new KeyedCodec<>("WorldLocationCondition", WorldLocationProvider.CODEC),
            (chestConfig, worldLocationCondition) -> chestConfig.worldLocationProvider = worldLocationCondition,
            chestConfig -> chestConfig.worldLocationProvider
         )
         .add()
         .<String>append(
            new KeyedCodec<>("ChestBlockTypeKey", Codec.STRING),
            (chestConfig, blockTypeKey) -> chestConfig.chestBlockTypeKey = blockTypeKey,
            chestConfig -> chestConfig.chestBlockTypeKey
         )
         .addValidator(Validators.nonNull())
         .addValidator(BlockType.VALIDATOR_CACHE.getValidator())
         .add()
         .afterDecode(
            chestConfig -> {
               if (chestConfig.minRadius >= chestConfig.maxRadius) {
                  throw new IllegalArgumentException(
                     "ChestConfig.MinRadius (" + chestConfig.minRadius + ") needs to be greater than ChestConfig.MaxRadius (" + chestConfig.maxRadius + ")"
                  );
               }
            }
         )
         .build();
      protected float minRadius = 10.0F;
      protected float maxRadius = 20.0F;
      protected String droplistId;
      protected WorldLocationProvider worldLocationProvider;
      protected String chestBlockTypeKey;

      public float getMinRadius() {
         return this.minRadius;
      }

      public float getMaxRadius() {
         return this.maxRadius;
      }

      public String getDroplistId() {
         return this.droplistId;
      }

      public WorldLocationProvider getWorldLocationProvider() {
         return this.worldLocationProvider;
      }

      public String getChestBlockTypeKey() {
         return this.chestBlockTypeKey;
      }

      @Override
      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            TreasureMapObjectiveTaskAsset.ChestConfig that = (TreasureMapObjectiveTaskAsset.ChestConfig)o;
            if (Float.compare(that.minRadius, this.minRadius) != 0) {
               return false;
            } else if (Float.compare(that.maxRadius, this.maxRadius) != 0) {
               return false;
            } else if (this.droplistId != null ? this.droplistId.equals(that.droplistId) : that.droplistId == null) {
               if (this.worldLocationProvider != null ? this.worldLocationProvider.equals(that.worldLocationProvider) : that.worldLocationProvider == null) {
                  return this.chestBlockTypeKey != null ? this.chestBlockTypeKey.equals(that.chestBlockTypeKey) : that.chestBlockTypeKey == null;
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = this.minRadius != 0.0F ? Float.floatToIntBits(this.minRadius) : 0;
         result = 31 * result + (this.maxRadius != 0.0F ? Float.floatToIntBits(this.maxRadius) : 0);
         result = 31 * result + (this.droplistId != null ? this.droplistId.hashCode() : 0);
         result = 31 * result + (this.worldLocationProvider != null ? this.worldLocationProvider.hashCode() : 0);
         return 31 * result + (this.chestBlockTypeKey != null ? this.chestBlockTypeKey.hashCode() : 0);
      }

      @Nonnull
      @Override
      public String toString() {
         return "ChestConfig{minRadius="
            + this.minRadius
            + ", maxRadius="
            + this.maxRadius
            + ", droplistId='"
            + this.droplistId
            + "', worldLocationCondition="
            + this.worldLocationProvider
            + ", chestBlockTypeKey="
            + this.chestBlockTypeKey
            + "}";
      }
   }
}
