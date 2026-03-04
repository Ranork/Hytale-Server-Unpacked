package com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DeferEvaluateAssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StateStringValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.RoleExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.ActionRole;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionRole extends BuilderActionBase {
   protected final DeferEvaluateAssetHolder role = new DeferEvaluateAssetHolder();
   protected final BooleanHolder changeAppearance = new BooleanHolder();
   protected final StringHolder state = new StringHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Change the Role of the NPC";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionRole(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Action> readConfig(@Nonnull JsonElement data) {
      this.requireAsset(data, "Role", this.role, RoleExistsValidator.required(), BuilderDescriptorState.Stable, "The name of the Role to change to", null);
      this.getBoolean(
         data, "ChangeAppearance", this.changeAppearance, true, BuilderDescriptorState.Stable, "Whether the appearance of the new Role should be used", null
      );
      this.getString(data, "State", this.state, null, StateStringValidator.requireMainStateOrNull(), BuilderDescriptorState.Stable, "State name to set", null);
      return this;
   }

   public String getRole(@Nonnull BuilderSupport support) {
      return this.role.get(support.getExecutionContext());
   }

   public boolean getChangeAppearance(@Nonnull BuilderSupport support) {
      return this.changeAppearance.get(support.getExecutionContext());
   }

   public String getState(@Nonnull BuilderSupport support) {
      return this.state.get(support.getExecutionContext());
   }
}
