package com.hypixel.hytale.builtin.crafting.interaction;

import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LearnRecipeInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final KeyedCodec<String> ITEM_ID = new KeyedCodec<>("ItemId", Codec.STRING);
   @Nonnull
   public static final BuilderCodec<LearnRecipeInteraction> CODEC = BuilderCodec.builder(
         LearnRecipeInteraction.class, LearnRecipeInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Causes the user to learn the given recipe.")
      .appendInherited(
         new KeyedCodec<>("ItemId", Codec.STRING), (data, o) -> data.itemId = o, data -> data.itemId, (data, parent) -> data.itemId = parent.itemId
      )
      .add()
      .build();
   @Nullable
   protected String itemId;

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Server;
   }

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Ref<EntityStore> ref = context.getEntity();
      PlayerRef playerRefComponent = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
      if (playerRefComponent == null) {
         HytaleLogger.getLogger().at(Level.INFO).log("LearnRecipeInteraction requires a Player but was used for: %s", ref);
         context.getState().state = InteractionState.Failed;
      } else {
         String itemId = null;
         ItemContainer inventory = context.getHeldItemContainer();
         ItemStack itemInHand = context.getHeldItem();
         if (itemInHand != null) {
            itemId = itemInHand.getFromMetadataOrNull(ITEM_ID);
         }

         if (itemId == null) {
            if (this.itemId == null) {
               playerRefComponent.sendMessage(Message.translation("server.modules.learnrecipe.noIdSet"));
               context.getState().state = InteractionState.Failed;
               return;
            }

            itemId = this.itemId;
         }

         Item item = Item.getAssetMap().getAsset(itemId);
         Message itemNameMessage = item != null ? Message.translation(item.getTranslationKey()) : Message.raw("?");
         if (CraftingPlugin.learnRecipe(ref, itemId, commandBuffer)) {
            playerRefComponent.sendMessage(Message.translation("server.modules.learnrecipe.success").param("name", itemNameMessage));
         } else {
            playerRefComponent.sendMessage(Message.translation("server.modules.learnrecipe.alreadyKnown").param("name", itemNameMessage));
            context.getState().state = InteractionState.Failed;
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "LearnRecipeInteraction{itemId=" + this.itemId + "} " + super.toString();
   }
}
