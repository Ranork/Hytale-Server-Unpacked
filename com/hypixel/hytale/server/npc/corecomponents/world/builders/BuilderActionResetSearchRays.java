package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringArrayNoEmptyStringsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.world.ActionResetSearchRays;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionResetSearchRays extends BuilderActionBase {
   protected final AssetArrayHolder names = new AssetArrayHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Resets a specific search ray sensor cached position by name, or all search ray sensors";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionResetSearchRays(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionResetSearchRays readConfig(@Nonnull JsonElement data) {
      this.getStringArray(
         data,
         "Names",
         this.names,
         null,
         0,
         Integer.MAX_VALUE,
         StringArrayNoEmptyStringsValidator.get(),
         BuilderDescriptorState.Stable,
         "The search ray sensor ids",
         "The search ray sensor ids. If left empty, will reset all search ray sensors"
      );
      return this;
   }

   public int[] getIds(@Nonnull BuilderSupport support) {
      String[] ids = this.names.get(support.getExecutionContext());
      if (ids == null) {
         return ArrayUtil.EMPTY_INT_ARRAY;
      } else {
         int[] indexes = new int[ids.length];

         for (int i = 0; i < indexes.length; i++) {
            indexes[i] = support.getSearchRaySlot(ids[i]);
         }

         return indexes;
      }
   }
}
