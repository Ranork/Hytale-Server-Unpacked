package com.hypixel.hytale.builtin.adventure.shop;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ShopPageSupplier implements OpenCustomUIInteraction.CustomPageSupplier {
   @Nonnull
   public static final BuilderCodec<ShopPageSupplier> CODEC = BuilderCodec.builder(ShopPageSupplier.class, ShopPageSupplier::new)
      .appendInherited(
         new KeyedCodec<>("ShopId", Codec.STRING), (data, o) -> data.shopId = o, data -> data.shopId, (data, parent) -> data.shopId = parent.shopId
      )
      .add()
      .build();
   protected String shopId;

   @Nonnull
   @Override
   public CustomUIPage tryCreate(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull PlayerRef playerRef,
      @Nonnull InteractionContext context
   ) {
      return new ShopPage(playerRef, this.shopId);
   }
}
