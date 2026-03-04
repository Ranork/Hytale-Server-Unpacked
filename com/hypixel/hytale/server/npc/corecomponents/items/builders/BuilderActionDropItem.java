package com.hypixel.hytale.server.npc.corecomponents.items.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ItemDropListExistsValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ItemExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionWithDelay;
import com.hypixel.hytale.server.npc.corecomponents.items.ActionDropItem;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.util.AimingHelper;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;

public class BuilderActionDropItem extends BuilderActionWithDelay {
   public static final double[] DEFAULT_THROW_DISTANCE = new double[]{1.0, 1.0};
   public static final double[] DEFAULT_DROP_SECTOR = new double[]{0.0, 0.0};
   protected final AssetHolder item = new AssetHolder();
   protected final AssetHolder dropList = new AssetHolder();
   protected float throwSpeed;
   protected double[] distance;
   protected double[] dropSector;
   protected boolean highPitch;

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Drop an item";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Drop an item. Can be a specific item, or from a drop table";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionDropItem(this, builderSupport);
   }

   @Nonnull
   public BuilderActionDropItem readConfig(@Nonnull JsonElement data) {
      this.getAsset(
         data,
         "Item",
         this.item,
         null,
         ItemExistsValidator.withConfig(AssetValidator.CanBeEmpty),
         BuilderDescriptorState.Stable,
         "A specific item to drop",
         null
      );
      this.getAsset(
         data,
         "DropList",
         this.dropList,
         null,
         ItemDropListExistsValidator.withConfig(AssetValidator.CanBeEmpty),
         BuilderDescriptorState.Stable,
         "A reference to an item drop list",
         null
      );
      this.getFloat(
         data,
         "ThrowSpeed",
         s -> this.throwSpeed = s,
         1.0F,
         DoubleRangeValidator.fromExclToIncl(0.0, Float.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "The throw speed to use",
         null
      );
      this.getDoubleRange(
         data,
         "Distance",
         s -> this.distance = s,
         DEFAULT_THROW_DISTANCE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "The range from which to pick a distance to throw the item",
         null
      );
      this.getDoubleRange(
         data,
         "DropSector",
         s -> this.dropSector = s,
         DEFAULT_DROP_SECTOR,
         DoubleSequenceValidator.betweenWeaklyMonotonic(-360.0, 360.0),
         BuilderDescriptorState.Stable,
         "The sector to spread drops in relative to view direction of NPC",
         "The sector to spread drops in relative to view direction of NPC in degrees."
      );
      this.getBoolean(data, "PitchHigh", v -> this.highPitch = v, false, BuilderDescriptorState.Stable, "Whether to pitch high or pitch low instead", null);
      this.validateOneSetAsset(this.item, this.dropList);
      return this;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Override
   public boolean validate(
      String configName, @Nonnull NPCLoadTimeValidationHelper validationHelper, ExecutionContext context, Scope globalScope, @Nonnull List<String> errors
   ) {
      boolean result = super.validate(configName, validationHelper, context, globalScope, errors);
      Model model = validationHelper.getSpawnModel();
      float height = model.getEyeHeight();
      double newThrowSpeed = AimingHelper.ensurePossibleThrowSpeed(this.distance[1], height, 32.0, this.throwSpeed);
      if (newThrowSpeed > this.throwSpeed) {
         errors.add(
            String.format(
               "%s: Throw speed %.2f is too low to achieve distance of %.2f in DropItem action. Needs to be at least %.2f",
               configName,
               this.throwSpeed,
               this.distance[1],
               Math.ceil(newThrowSpeed * 100.0) / 100.0
            )
         );
         result = false;
      }

      return result;
   }

   public String getItem(@Nonnull BuilderSupport support) {
      return this.item.get(support.getExecutionContext());
   }

   public String getDropList(@Nonnull BuilderSupport support) {
      return this.dropList.get(support.getExecutionContext());
   }

   public float getThrowSpeed() {
      return this.throwSpeed;
   }

   public double[] getDropSectorRadians() {
      return this.dropSector;
   }

   public double[] getDistance() {
      return this.distance;
   }

   public boolean isHighPitch() {
      return this.highPitch;
   }
}
