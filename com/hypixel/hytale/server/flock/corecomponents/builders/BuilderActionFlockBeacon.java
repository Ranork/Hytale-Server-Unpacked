package com.hypixel.hytale.server.flock.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.flock.corecomponents.ActionFlockBeacon;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleOrValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import java.util.Set;
import javax.annotation.Nonnull;

public class BuilderActionFlockBeacon extends BuilderActionBase {
   protected final StringHolder message = new StringHolder();
   protected String sendTargetSlot;
   protected double expirationTime;
   protected boolean sendToSelf;
   protected boolean sendToLeaderOnly;

   @Nonnull
   public ActionFlockBeacon build(@Nonnull BuilderSupport builderSupport) {
      return new ActionFlockBeacon(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Send beacon message to flock";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Let the NPC send out a message to the flock members";
   }

   @Override
   public void registerTags(@Nonnull Set<String> tags) {
      super.registerTags(tags);
      tags.add("message");
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Experimental;
   }

   @Nonnull
   public BuilderActionFlockBeacon readConfig(@Nonnull JsonElement data) {
      this.requireString(data, "Message", this.message, StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "Message to send to targets", null);
      this.getString(
         data,
         "SendTargetSlot",
         b -> this.sendTargetSlot = b,
         null,
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "The marked target slot to send. If omitted, sends own position",
         null
      );
      this.getDouble(
         data,
         "ExpirationTime",
         d -> this.expirationTime = d,
         1.0,
         DoubleOrValidator.greaterEqual0OrMinus1(),
         BuilderDescriptorState.Stable,
         "The number of seconds that the message should last. -1 represents infinite time.",
         "The number of seconds that the message should last and be acknowledged by the receiving NPC. -1 represents infinite time."
      );
      this.getBoolean(data, "SendToSelf", b -> this.sendToSelf = b, true, BuilderDescriptorState.Stable, "Send the message to self", null);
      this.getBoolean(
         data,
         "SendToLeaderOnly",
         b -> this.sendToLeaderOnly = b,
         false,
         BuilderDescriptorState.Stable,
         "Only send the message to the leader of the flock",
         null
      );
      return this;
   }

   public String getMessage(@Nonnull BuilderSupport builderSupport) {
      return this.message.get(builderSupport.getExecutionContext());
   }

   public int getSendTargetSlot(@Nonnull BuilderSupport support) {
      return this.sendTargetSlot == null ? Integer.MIN_VALUE : support.getTargetSlot(this.sendTargetSlot);
   }

   public double getExpirationTime() {
      return this.expirationTime;
   }

   public boolean isSendToSelf() {
      return this.sendToSelf;
   }

   public boolean isSendToLeaderOnly() {
      return this.sendToLeaderOnly;
   }
}
