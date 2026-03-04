package com.hypixel.hytale.server.npc.corecomponents.items.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumSetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.FloatHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ItemExistsValidator;
import com.hypixel.hytale.server.npc.config.ItemAttitudeGroup;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.items.SensorDroppedItem;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderSensorDroppedItem extends BuilderSensorBase {
   protected final DoubleHolder range = new DoubleHolder();
   protected final FloatHolder viewSector = new FloatHolder();
   protected final BooleanHolder hasLineOfSight = new BooleanHolder();
   protected final AssetArrayHolder items = new AssetArrayHolder();
   protected final EnumSetHolder<ItemAttitudeGroup.Sentiment> attitudes = new EnumSetHolder<>();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Triggers if a given item is within a certain range of the NPC.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Triggers if a given item is within a certain range of the NPC. Will match anything if Items and Attitudes are not defined, otherwise will match items meeting either criteria.";
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorDroppedItem(this, builderSupport);
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireDouble(data, "Range", this.range, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "The range within which to look", null);
      this.getFloat(
         data,
         "ViewSector",
         this.viewSector,
         0.0,
         DoubleRangeValidator.between(0.0, 360.0),
         BuilderDescriptorState.Stable,
         "View sector in which to look",
         null
      );
      this.getBoolean(data, "LineOfSight", this.hasLineOfSight, false, BuilderDescriptorState.Stable, "Requires line of sight to item", null);
      this.getAssetArray(
         data,
         "Items",
         this.items,
         null,
         0,
         Integer.MAX_VALUE,
         ItemExistsValidator.withConfig(EnumSet.of(AssetValidator.Config.LIST_NULLABLE, AssetValidator.Config.LIST_CAN_BE_EMPTY, AssetValidator.Config.MATCHER)),
         BuilderDescriptorState.Stable,
         "A list of glob item patterns to match. If omitted, will match any item",
         null
      );
      this.getEnumSet(
         data,
         "Attitudes",
         this.attitudes,
         ItemAttitudeGroup.Sentiment.class,
         EnumSet.noneOf(ItemAttitudeGroup.Sentiment.class),
         BuilderDescriptorState.Stable,
         "Attitudes to match",
         null
      );
      this.provideFeature(Feature.Drop);
      return this;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   public double getRange(@Nonnull BuilderSupport support) {
      return this.range.get(support.getExecutionContext());
   }

   public float getViewSectorRadians(@Nonnull BuilderSupport builderSupport) {
      return (float) (Math.PI / 180.0) * this.viewSector.get(builderSupport.getExecutionContext());
   }

   public boolean getHasLineOfSight(@Nonnull BuilderSupport support) {
      return this.hasLineOfSight.get(support.getExecutionContext());
   }

   @Nullable
   public String[] getItems(@Nonnull BuilderSupport support) {
      return this.items.get(support.getExecutionContext());
   }

   @Nonnull
   public EnumSet<Attitude> getAttitudes(@Nonnull BuilderSupport support) {
      EnumSet<ItemAttitudeGroup.Sentiment> set = this.attitudes.get(support.getExecutionContext());
      EnumSet<Attitude> attitudes = EnumSet.noneOf(Attitude.class);

      for (ItemAttitudeGroup.Sentiment sentiment : set) {
         attitudes.add(sentiment.getAttitude());
      }

      return attitudes;
   }
}
