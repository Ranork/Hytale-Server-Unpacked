package com.hypixel.hytale.server.npc.corecomponents.interaction.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.interaction.ActionSetInteractable;
import com.hypixel.hytale.server.npc.instructions.Action;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionSetInteractable extends BuilderActionBase {
   protected final BooleanHolder setTo = new BooleanHolder();
   protected String hint;
   protected boolean showPrompt = true;

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Set whether the currently iterated player in the interaction instruction should be able to interact with this NPC";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionSetInteractable(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionSetInteractable readConfig(@Nonnull JsonElement data) {
      this.getBoolean(
         data,
         "Interactable",
         this.setTo,
         true,
         BuilderDescriptorState.Stable,
         "Toggle whether the currently iterated player in the interaction instruction should be able to interact with this NPC",
         null
      );
      this.getString(
         data,
         "Hint",
         h -> this.hint = h,
         null,
         null,
         BuilderDescriptorState.Stable,
         "The interaction hint translation key to show for this player (e.g. 'interactionHints.trade')",
         null
      );
      this.getBoolean(
         data,
         "ShowPrompt",
         b -> this.showPrompt = b,
         true,
         BuilderDescriptorState.Stable,
         "Whether to show the F-key interaction prompt. Set to false for contextual-only interactions (e.g. shearing with tools). Defaults to true.",
         null
      );
      this.requireInstructionType(EnumSet.of(InstructionType.Interaction));
      return this;
   }

   public boolean getSetTo(@Nonnull BuilderSupport support) {
      return this.setTo.get(support.getExecutionContext());
   }

   @Nullable
   public String getHint() {
      return this.hint;
   }

   public boolean getShowPrompt() {
      return this.showPrompt;
   }
}
