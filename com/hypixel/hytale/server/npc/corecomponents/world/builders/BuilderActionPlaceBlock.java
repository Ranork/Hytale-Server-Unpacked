package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.world.ActionPlaceBlock;
import com.hypixel.hytale.server.npc.instructions.Action;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class BuilderActionPlaceBlock extends BuilderActionBase {
   protected final DoubleHolder range = new DoubleHolder();
   protected final BooleanHolder allowEmptyMaterials = new BooleanHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Place a block (chosen by another action) at a position returned by a Sensor if close enough";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionPlaceBlock(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionPlaceBlock readConfig(@Nonnull JsonElement data) {
      this.getDouble(
         data,
         "Range",
         this.range,
         3.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "The range to target position before block will be placed",
         null
      );
      this.getBoolean(
         data,
         "AllowEmptyMaterials",
         this.allowEmptyMaterials,
         false,
         BuilderDescriptorState.Stable,
         "Whether it should be possible to replace blocks that have empty material",
         null
      );
      this.requireFeature(EnumSet.of(Feature.Position));
      return this;
   }

   public double getRange(@Nonnull BuilderSupport support) {
      return this.range.get(support.getExecutionContext());
   }

   public boolean isAllowEmptyMaterials(@Nonnull BuilderSupport support) {
      return this.allowEmptyMaterials.get(support.getExecutionContext());
   }
}
