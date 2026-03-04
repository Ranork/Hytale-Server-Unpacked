package com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ItemExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.EntityFilterInventory;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderEntityFilterInventory extends BuilderEntityFilterBase {
   public static final int[] DEFAULT_FREE_SLOT_RANGE = new int[]{0, Integer.MAX_VALUE};
   public static final int[] DEFAULT_ITEM_COUNT_RANGE = new int[]{1, Integer.MAX_VALUE};
   public static final String[] DEFAULT_ITEM_PATTERNS = new String[]{"*"};
   protected final AssetArrayHolder items = new AssetArrayHolder();
   protected final NumberArrayHolder count = new NumberArrayHolder();
   protected final NumberArrayHolder freeSlots = new NumberArrayHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Test various conditions relating to entity inventory";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Test various conditions relating to entity inventory. This includes whether it contains a specific item, item count, and free slots";
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new EntityFilterInventory(this, builderSupport);
   }

   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.getAssetArray(
         data,
         "Items",
         this.items,
         DEFAULT_ITEM_PATTERNS,
         0,
         Integer.MAX_VALUE,
         ItemExistsValidator.withConfig(EnumSet.of(AssetValidator.Config.MATCHER)),
         BuilderDescriptorState.Stable,
         "A list of glob item patterns to match",
         null
      );
      this.getIntRange(
         data,
         "CountRange",
         this.count,
         DEFAULT_ITEM_COUNT_RANGE,
         IntSequenceValidator.betweenWeaklyMonotonic(0, Integer.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "The range of number of items that need to match the patterns",
         null
      );
      this.getIntRange(
         data,
         "FreeSlotRange",
         this.freeSlots,
         DEFAULT_FREE_SLOT_RANGE,
         IntSequenceValidator.betweenWeaklyMonotonic(0, Integer.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "The range designating the number of required free slots",
         "The range designating the number of required free slots. Setting min and max to zero would check if full"
      );
      return this;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nullable
   public String[] getItems(@Nonnull BuilderSupport support) {
      return this.items.get(support.getExecutionContext());
   }

   public int[] getCount(@Nonnull BuilderSupport support) {
      return this.count.getIntArray(support.getExecutionContext());
   }

   public int[] getFreeSlotsRange(@Nonnull BuilderSupport support) {
      return this.freeSlots.getIntArray(support.getExecutionContext());
   }
}
