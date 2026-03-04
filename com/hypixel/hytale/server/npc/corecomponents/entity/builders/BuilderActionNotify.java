package com.hypixel.hytale.server.npc.corecomponents.entity.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleOrValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.ActionNotify;
import com.hypixel.hytale.server.npc.instructions.Action;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;

public class BuilderActionNotify extends BuilderActionBase {
   protected final StringHolder message = new StringHolder();
   protected double expirationTime;
   protected String usedTargetSlot;

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Directly notifies a target NPC with a beacon message";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Override
   public void registerTags(@Nonnull Set<String> tags) {
      super.registerTags(tags);
      tags.add("message");
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionNotify(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionNotify readConfig(@Nonnull JsonElement data) {
      this.requireString(data, "Message", this.message, StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "The message to send", null);
      this.getDouble(
         data,
         "ExpirationTime",
         v -> this.expirationTime = v,
         1.0,
         DoubleOrValidator.greaterEqual0OrMinus1(),
         BuilderDescriptorState.Experimental,
         "The number of seconds that the message should last. -1 represents infinite time.",
         "The number of seconds that the message should last and be acknowledged by the receiving NPC. -1 represents infinite time."
      );
      this.getString(
         data,
         "UseTargetSlot",
         s -> this.usedTargetSlot = s,
         null,
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "A marked target to send to instead of the target provided by a sensor. Omit to use the target provided by the sensor.",
         null
      );
      this.requireFeatureIf("UseTargetSlot", false, this.usedTargetSlot != null, EnumSet.of(Feature.NPC));
      return this;
   }

   public String getMessage(@Nonnull BuilderSupport support) {
      return this.message.get(support.getExecutionContext());
   }

   public double getExpirationTime() {
      return this.expirationTime;
   }

   public int getUsedTargetSlot(@Nonnull BuilderSupport support) {
      return this.usedTargetSlot == null ? Integer.MIN_VALUE : support.getTargetSlot(this.usedTargetSlot);
   }
}
