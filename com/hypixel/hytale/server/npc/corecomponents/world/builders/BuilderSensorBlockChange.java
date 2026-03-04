package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.BlockSetExistsValidator;
import com.hypixel.hytale.server.npc.blackboard.view.event.block.BlockEventType;
import com.hypixel.hytale.server.npc.corecomponents.world.SensorBlockChange;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorBlockChange extends BuilderSensorEvent {
   protected final AssetHolder blockSet = new AssetHolder();
   protected final EnumHolder<BlockEventType> blockEventType = new EnumHolder<>();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Matches when a block from a blockset within a certain range is changed or interacted with";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorBlockChange(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      super.readConfig(data);
      this.requireAsset(data, "BlockSet", this.blockSet, BlockSetExistsValidator.required(), BuilderDescriptorState.Stable, "Block set to listen for", null);
      this.getEnum(
         data,
         "EventType",
         this.blockEventType,
         BlockEventType.class,
         BlockEventType.DAMAGE,
         BuilderDescriptorState.Stable,
         "The event type to listen for",
         null
      );
      return this;
   }

   public int getBlockSet(@Nonnull BuilderSupport support) {
      String key = this.blockSet.get(support.getExecutionContext());
      int index = BlockSet.getAssetMap().getIndex(key);
      if (index == Integer.MIN_VALUE) {
         throw new IllegalArgumentException("Unknown key! " + key);
      } else {
         return index;
      }
   }

   public BlockEventType getEventType(@Nonnull BuilderSupport support) {
      return this.blockEventType.get(support.getExecutionContext());
   }
}
