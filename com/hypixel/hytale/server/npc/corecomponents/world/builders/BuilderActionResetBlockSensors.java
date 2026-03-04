package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.BlockSetExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.world.ActionResetBlockSensors;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionResetBlockSensors extends BuilderActionBase {
   protected final AssetArrayHolder blockSets = new AssetArrayHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Resets a specific block sensor by name, or all block sensors";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Resets a specific block sensor by name, or all block sensors by clearing the current targeted block";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionResetBlockSensors(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionResetBlockSensors readConfig(@Nonnull JsonElement data) {
      this.getAssetArray(
         data,
         "BlockSets",
         this.blockSets,
         null,
         0,
         Integer.MAX_VALUE,
         BlockSetExistsValidator.withConfig(AssetValidator.ListCanBeEmpty),
         BuilderDescriptorState.Stable,
         "The searched blocksets to reset block sensors for",
         "The searched blocksets to reset block sensors for. If left empty, will reset all block sensors and found blocks"
      );
      return this;
   }

   public int[] getBlockSets(@Nonnull BuilderSupport support) {
      String[] names = this.blockSets.get(support.getExecutionContext());
      if (names == null) {
         return ArrayUtil.EMPTY_INT_ARRAY;
      } else {
         int[] indexes = new int[names.length];

         for (int i = 0; i < indexes.length; i++) {
            int index = BlockSet.getAssetMap().getIndex(names[i]);
            if (index == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown key! " + names[i]);
            }

            indexes[i] = index;
         }

         return indexes;
      }
   }
}
