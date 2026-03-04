package com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.ActionModelAttachment;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import javax.annotation.Nonnull;

public class BuilderActionModelAttachment extends BuilderActionBase {
   protected final StringHolder slot = new StringHolder();
   protected final StringHolder attachment = new StringHolder();

   @Nonnull
   public ActionModelAttachment build(@Nonnull BuilderSupport builderSupport) {
      return new ActionModelAttachment(this, builderSupport);
   }

   @Nonnull
   public BuilderActionModelAttachment readConfig(@Nonnull JsonElement data) {
      this.requireString(data, "Slot", this.slot, StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "The attachment slot to set", null);
      this.requireString(data, "Attachment", this.attachment, null, BuilderDescriptorState.Stable, "The attachment to set, or empty to remove", null);
      return this;
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Set an attachment on the current NPC model";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   public String getSlot(@Nonnull BuilderSupport support) {
      return this.slot.get(support.getExecutionContext());
   }

   public String getAttachment(@Nonnull BuilderSupport support) {
      return this.attachment.get(support.getExecutionContext());
   }
}
