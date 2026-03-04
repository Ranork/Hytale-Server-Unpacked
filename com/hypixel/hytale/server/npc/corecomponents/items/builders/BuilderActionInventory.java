package com.hypixel.hytale.server.npc.corecomponents.items.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.IntHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ItemExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.items.ActionInventory;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;

public class BuilderActionInventory extends BuilderActionBase {
   protected final EnumHolder<ActionInventory.Operation> operation = new EnumHolder<>();
   protected final AssetHolder item = new AssetHolder();
   protected final IntHolder count = new IntHolder();
   protected final BooleanHolder useTarget = new BooleanHolder();
   protected final IntHolder slot = new IntHolder();

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionInventory(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Add or remove items from inventory.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Add or remove a number of items from an inventory. Can also be used to equip them.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Action> readConfig(@Nonnull JsonElement data) {
      this.getEnum(
         data,
         "Operation",
         this.operation,
         ActionInventory.Operation.class,
         ActionInventory.Operation.Add,
         BuilderDescriptorState.Stable,
         "Operation to perform",
         null
      );
      this.getInt(data, "Count", this.count, 1, IntSingleValidator.greater0(), BuilderDescriptorState.Stable, "Number of items to add/remove", null);
      this.getAsset(
         data,
         "Item",
         this.item,
         "",
         ItemExistsValidator.withConfig(AssetValidator.CanBeEmpty),
         BuilderDescriptorState.Stable,
         "Item type to add, remove, or equip",
         null
      );
      this.getBoolean(data, "UseTarget", this.useTarget, true, BuilderDescriptorState.Stable, "Use the sensor-provided target for the action", null);
      this.getInt(
         data,
         "Slot",
         this.slot,
         0,
         null,
         BuilderDescriptorState.Stable,
         "The hotbar or off-hand slot to affect",
         "The hotbar or off-hand to effect. Only valid for Hotbar/OffHand Set/Equip operations"
      );
      this.requireFeatureIf(this.useTarget, true, Feature.LiveEntity);
      return this;
   }

   @Override
   public boolean validate(
      String configName, @Nonnull NPCLoadTimeValidationHelper validationHelper, ExecutionContext context, Scope globalScope, @Nonnull List<String> errors
   ) {
      boolean result = super.validate(configName, validationHelper, context, globalScope, errors);
      ActionInventory.Operation op = this.operation.get(context);
      switch (op) {
         case SetHotbar:
         case EquipHotbar:
            result &= validationHelper.validateHotbarHasSlot(this.slot.get(context), "ActionInventory", errors);
            break;
         case SetOffHand:
         case EquipOffHand:
            result &= validationHelper.validateOffHandHasSlot(this.slot.get(context), "ActionInventory", errors);
      }

      return result;
   }

   public ActionInventory.Operation getOperation(@Nonnull BuilderSupport builderSupport) {
      return this.operation.get(builderSupport.getExecutionContext());
   }

   public String getItem(@Nonnull BuilderSupport builderSupport) {
      return this.item.get(builderSupport.getExecutionContext());
   }

   public int getCount(@Nonnull BuilderSupport builderSupport) {
      return this.count.get(builderSupport.getExecutionContext());
   }

   public boolean getUseTarget(@Nonnull BuilderSupport support) {
      return this.useTarget.get(support.getExecutionContext());
   }

   public int getSlot(@Nonnull BuilderSupport support) {
      return this.slot.get(support.getExecutionContext());
   }
}
