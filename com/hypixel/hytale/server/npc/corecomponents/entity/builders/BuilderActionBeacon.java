package com.hypixel.hytale.server.npc.corecomponents.entity.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleOrValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntOrValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.TagSetExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.ActionBeacon;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import java.util.Set;
import javax.annotation.Nonnull;

public class BuilderActionBeacon extends BuilderActionBase {
   protected final StringHolder message = new StringHolder();
   protected double range;
   protected final AssetArrayHolder targetGroups = new AssetArrayHolder();
   protected final StringHolder targetToSendSlot = new StringHolder();
   protected double expirationTime;
   protected int sendCount;

   @Nonnull
   public ActionBeacon build(@Nonnull BuilderSupport builderSupport) {
      return new ActionBeacon(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Send Beacon Message";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Let the NPC send out a message to a target group of entities within a certain distance.";
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
   public BuilderActionBeacon readConfig(@Nonnull JsonElement data) {
      this.requireString(data, "Message", this.message, StringNotEmptyValidator.get(), BuilderDescriptorState.Experimental, "Message to send to targets", null);
      this.getDouble(
         data,
         "Range",
         d -> this.range = d,
         64.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "The maximum range to send the message",
         null
      );
      this.requireAssetArray(
         data,
         "TargetGroups",
         this.targetGroups,
         0,
         Integer.MAX_VALUE,
         TagSetExistsValidator.withConfig(AssetValidator.ListCanBeEmpty),
         BuilderDescriptorState.Experimental,
         "The target group(s) to send the message to",
         null
      );
      this.getString(
         data,
         "SendTargetSlot",
         this.targetToSendSlot,
         null,
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "The target slot of the marked entity to send. Omit to send own position",
         null
      );
      this.getDouble(
         data,
         "ExpirationTime",
         d -> this.expirationTime = d,
         1.0,
         DoubleOrValidator.greaterEqual0OrMinus1(),
         BuilderDescriptorState.Experimental,
         "The number of seconds that the message should last. -1 represents infinite time.",
         "The number of seconds that the message should last and be acknowledged by the receiving NPC. -1 represents infinite time."
      );
      this.getInt(
         data,
         "SendCount",
         i -> this.sendCount = i,
         -1,
         IntOrValidator.greater0OrMinus1(),
         BuilderDescriptorState.Experimental,
         "Tne number of entities to send the message to. -1 will send to all.",
         "Tne number of entities to send the message to. -1 will send to all. Entities will be chosen with a roughly even random distribution using reservoir sampling"
      );
      return this;
   }

   public String getMessage(@Nonnull BuilderSupport support) {
      return this.message.get(support.getExecutionContext());
   }

   public double getRange() {
      return this.range;
   }

   public int[] getTargetGroups(@Nonnull BuilderSupport support) {
      return WorldSupport.createTagSetIndexArray(this.targetGroups.get(support.getExecutionContext()));
   }

   public double getExpirationTime() {
      return this.expirationTime;
   }

   public int getSendCount() {
      return this.sendCount;
   }

   public int getTargetToSendSlot(@Nonnull BuilderSupport support) {
      String slotName = this.targetToSendSlot.get(support.getExecutionContext());
      return slotName == null ? Integer.MIN_VALUE : support.getTargetSlot(slotName);
   }
}
