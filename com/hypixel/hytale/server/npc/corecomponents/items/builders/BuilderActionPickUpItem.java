package com.hypixel.hytale.server.npc.corecomponents.items.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ItemExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionWithDelay;
import com.hypixel.hytale.server.npc.corecomponents.items.ActionPickUpItem;
import com.hypixel.hytale.server.npc.instructions.Action;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class BuilderActionPickUpItem extends BuilderActionWithDelay {
   protected final DoubleHolder range = new DoubleHolder();
   protected final EnumHolder<ActionPickUpItem.StorageTarget> pickupTarget = new EnumHolder<>();
   protected AssetArrayHolder items = new AssetArrayHolder();
   protected boolean hoover;

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Pick up an item";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Pick up an item. In hoover mode, will match to the Item array. Otherwise, requires a target to be provided e.g. by a DroppedItemSensor";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionPickUpItem(this, builderSupport);
   }

   @Nonnull
   public BuilderActionPickUpItem readConfig(@Nonnull JsonElement data) {
      this.getDouble(
         data, "Range", this.range, 1.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "The range the item will be picked up from", null
      );
      this.getEnum(
         data,
         "StorageTarget",
         this.pickupTarget,
         ActionPickUpItem.StorageTarget.class,
         ActionPickUpItem.StorageTarget.Hotbar,
         BuilderDescriptorState.Experimental,
         "Where to prioritise putting the item",
         null
      );
      this.getBoolean(
         data,
         "Hoover",
         s -> this.hoover = s,
         false,
         BuilderDescriptorState.Stable,
         "Suck up all items in range",
         "Suck up all items in range with optional cooldown. Can be filtered with a list of glob patterns. Ignored outside hoover mode"
      );
      this.getAssetArray(
         data,
         "Items",
         this.items,
         null,
         0,
         Integer.MAX_VALUE,
         ItemExistsValidator.withConfig(EnumSet.of(AssetValidator.Config.LIST_NULLABLE, AssetValidator.Config.LIST_CAN_BE_EMPTY, AssetValidator.Config.MATCHER)),
         BuilderDescriptorState.Stable,
         "A list of glob item patterns to match",
         "A list of glob item patterns to match for hoover mode. If omitted, will match any item. Ignored outside hoover mode"
      );
      this.requireFeatureIf("Hoover", false, this.hoover, EnumSet.of(Feature.Drop));
      return this;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   public String[] getItems(BuilderSupport support) {
      return this.items.get(support.getExecutionContext());
   }

   public boolean getHoover() {
      return this.hoover;
   }

   public double getRange(@Nonnull BuilderSupport support) {
      return this.range.get(support.getExecutionContext());
   }

   public ActionPickUpItem.StorageTarget getStorageTarget(@Nonnull BuilderSupport support) {
      return this.pickupTarget.get(support.getExecutionContext());
   }
}
