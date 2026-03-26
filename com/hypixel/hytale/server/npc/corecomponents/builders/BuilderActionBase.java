package com.hypixel.hytale.server.npc.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import javax.annotation.Nonnull;

public abstract class BuilderActionBase extends BuilderBase<Action> {
   protected boolean once;
   protected final BooleanHolder enabled = new BooleanHolder();

   @Override
   public boolean canRequireFeature() {
      return true;
   }

   @Nonnull
   @Override
   public Builder<Action> readCommonConfig(@Nonnull JsonElement data) {
      super.readCommonConfig(data);
      this.getBoolean(data, "Once", v -> this.once = v, false, BuilderDescriptorState.Stable, "Execute only once", null);
      this.getBoolean(data, "Enabled", this.enabled, true, BuilderDescriptorState.Stable, "Whether this action should be enabled on the NPC", null);
      return this;
   }

   @Nonnull
   @Override
   public final Class<Action> category() {
      return Action.class;
   }

   @Override
   public final boolean isEnabled(ExecutionContext context) {
      return this.enabled.get(context);
   }

   public boolean isOnce() {
      return this.once;
   }
}
