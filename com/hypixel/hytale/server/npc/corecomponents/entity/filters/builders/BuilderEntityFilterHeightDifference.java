package com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.ComponentContext;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.EntityFilterHeightDifference;
import javax.annotation.Nonnull;

public class BuilderEntityFilterHeightDifference extends BuilderEntityFilterBase {
   public static final double[] DEFAULT_HEIGHT_DIFFERENCE_RANGE = new double[]{-Double.MAX_VALUE, Double.MAX_VALUE};
   protected final BooleanHolder useEyePosition = new BooleanHolder();
   protected final NumberArrayHolder heightDifference = new NumberArrayHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Matches entities within the given height range";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new EntityFilterHeightDifference(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.getDoubleRange(
         data,
         "HeightDifference",
         this.heightDifference,
         DEFAULT_HEIGHT_DIFFERENCE_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(-Double.MAX_VALUE, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "The height range to test entities in",
         "The height range to test entities in. Extends into negatives for positions below the NPC"
      );
      this.getBoolean(data, "UseEyePosition", this.useEyePosition, true, BuilderDescriptorState.Stable, "Use eye position for height difference checks", null);
      this.requireContext(InstructionType.Any, ComponentContext.NotSelfEntitySensor);
      return this;
   }

   public double[] getHeightDifference(@Nonnull BuilderSupport support) {
      return this.heightDifference.get(support.getExecutionContext());
   }

   public boolean isUseEyePosition(@Nonnull BuilderSupport support) {
      return this.useEyePosition.get(support.getExecutionContext());
   }
}
