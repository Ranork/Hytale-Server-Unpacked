package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GiveItemEffect extends TriggerEffect {
   @Nonnull
   public static final BuilderCodec<GiveItemEffect> CODEC = BuilderCodec.builder(GiveItemEffect.class, GiveItemEffect::new, BASE_CODEC)
      .append(new KeyedCodec<>("Item", Codec.STRING), (e, v) -> e.itemId = v, e -> e.itemId)
      .add()
      .append(new KeyedCodec<>("Quantity", Codec.INTEGER, false), (e, v) -> e.quantity = v, e -> e.quantity)
      .add()
      .append(
         new KeyedCodec<>("OverflowBehavior", new EnumCodec<>(GiveItemEffect.OverflowBehavior.class), false),
         (e, v) -> e.overflowBehavior = v,
         e -> e.overflowBehavior
      )
      .add()
      .build();
   @Nullable
   private String itemId;
   private int quantity = 1;
   @Nonnull
   private GiveItemEffect.OverflowBehavior overflowBehavior = GiveItemEffect.OverflowBehavior.DROP_REMAINDER;

   @Override
   public void execute(@Nonnull TriggerContext context) {
      if (this.itemId != null && !this.itemId.isBlank() && this.quantity > 0) {
         Store<EntityStore> store = context.getStore();
         Ref<EntityStore> entityRef = context.getEntityRef();
         if (store.getComponent(entityRef, Player.getComponentType()) != null) {
            Item item = Item.getAssetMap().getAsset(this.itemId);
            if (item != null) {
               ItemStack stack = new ItemStack(item.getId(), this.quantity);
               GiveItemEffect.OverflowBehavior behavior = this.overflowBehavior != null
                  ? this.overflowBehavior
                  : GiveItemEffect.OverflowBehavior.DROP_REMAINDER;

               ItemStackTransaction transaction = switch (behavior) {
                  case DROP_REMAINDER, IGNORE_REMAINDER -> Player.giveItem(stack, entityRef, store);
                  case REQUIRE_FULL_STACK -> {
                     PlayerSettings playerSettings = store.getComponent(entityRef, PlayerSettings.getComponentType());
                     if (playerSettings == null) {
                        playerSettings = PlayerSettings.defaults();
                     }

                     ItemContainer container = InventoryUtils.getContainerForItemPickup(entityRef, item, playerSettings, store);
                     yield container.addItemStack(stack, true, false, true);
                  }
               };
               if (behavior == GiveItemEffect.OverflowBehavior.DROP_REMAINDER) {
                  ItemStack remainder = transaction.getRemainder();
                  if (!ItemStack.isEmpty(remainder)) {
                     ItemUtils.dropItem(entityRef, remainder, store);
                  }
               }
            }
         }
      }
   }

   public static enum OverflowBehavior {
      DROP_REMAINDER,
      IGNORE_REMAINDER,
      REQUIRE_FULL_STACK;
   }
}
