package com.hypixel.hytale.server.npc.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import javax.annotation.Nonnull;

public abstract class BuilderEntityFilterWithToggle extends BuilderBase<IEntityFilter> {
   protected final BooleanHolder enabled = new BooleanHolder();

   @Nonnull
   @Override
   public Builder<IEntityFilter> readCommonConfig(@Nonnull JsonElement data) {
      super.readCommonConfig(data);
      this.getBoolean(data, "Enabled", this.enabled, true, BuilderDescriptorState.Stable, "Whether this entity filter should be enabled", null);
      return this;
   }

   @Nonnull
   @Override
   public Class<IEntityFilter> category() {
      return IEntityFilter.class;
   }

   @Override
   public boolean isEnabled(ExecutionContext context) {
      return this.enabled.get(context);
   }
}
