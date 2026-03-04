package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ItemExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.world.ActionSetBlockToPlace;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionSetBlockToPlace extends BuilderActionBase {
   protected final AssetHolder block = new AssetHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Set the block type the NPC will place";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionSetBlockToPlace(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionSetBlockToPlace readConfig(@Nonnull JsonElement data) {
      this.requireAsset(data, "Block", this.block, ItemExistsValidator.requireBlock(), BuilderDescriptorState.Stable, "The block item type", null);
      return this;
   }

   public String getBlockType(@Nonnull BuilderSupport support) {
      return this.block.get(support.getExecutionContext());
   }
}
