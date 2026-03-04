package com.hypixel.hytale.server.spawning.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.IntHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.BeaconSpawnExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.spawning.assets.spawns.config.BeaconNPCSpawn;
import com.hypixel.hytale.server.spawning.corecomponents.ActionTriggerSpawnBeacon;
import javax.annotation.Nonnull;

public class BuilderActionTriggerSpawnBeacon extends BuilderActionBase {
   protected final AssetHolder beaconId = new AssetHolder();
   protected final IntHolder range = new IntHolder();
   protected String targetSlot;

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Trigger the nearest spawn beacon matching the configuration id";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionTriggerSpawnBeacon(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionTriggerSpawnBeacon readConfig(@Nonnull JsonElement data) {
      this.requireAsset(
         data, "BeaconSpawn", this.beaconId, BeaconSpawnExistsValidator.required(), BuilderDescriptorState.Stable, "The beacon spawn config ID", null
      );
      this.requireInt(
         data, "Range", this.range, IntSingleValidator.greater0(), BuilderDescriptorState.Stable, "The distance to search for a beacon to trigger", null
      );
      this.getString(
         data,
         "TargetSlot",
         s -> this.targetSlot = s,
         null,
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "A slot to use as the target for the spawned NPC. If omitted the NPC itself will be used",
         null
      );
      return this;
   }

   public int getBeaconId(@Nonnull BuilderSupport support) {
      return BeaconNPCSpawn.getAssetMap().getIndex(this.beaconId.get(support.getExecutionContext()));
   }

   public int getRange(@Nonnull BuilderSupport support) {
      return this.range.get(support.getExecutionContext());
   }

   public int getTargetSlot(@Nonnull BuilderSupport support) {
      return this.targetSlot == null ? Integer.MIN_VALUE : support.getTargetSlot(this.targetSlot);
   }
}
