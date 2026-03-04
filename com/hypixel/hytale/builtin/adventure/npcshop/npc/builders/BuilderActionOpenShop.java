package com.hypixel.hytale.builtin.adventure.npcshop.npc.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.adventure.npcshop.npc.ActionOpenShop;
import com.hypixel.hytale.builtin.adventure.npcshop.npc.ShopExistsValidator;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class BuilderActionOpenShop extends BuilderActionBase {
   @Nonnull
   protected final AssetHolder shopId = new AssetHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Open the shop UI for the current player";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionOpenShop(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionOpenShop readConfig(@Nonnull JsonElement data) {
      this.requireAsset(data, "Shop", this.shopId, ShopExistsValidator.required(), BuilderDescriptorState.Stable, "The shop to open", null);
      this.requireInstructionType(EnumSet.of(InstructionType.Interaction));
      return this;
   }

   public String getShopId(@Nonnull BuilderSupport support) {
      return this.shopId.get(support.getExecutionContext());
   }
}
