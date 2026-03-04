package com.hypixel.hytale.server.core.modules.interaction.suppliers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.itemrepair.ItemRepairPage;
import com.hypixel.hytale.server.core.inventory.ItemContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ItemRepairPageSupplier implements OpenCustomUIInteraction.CustomPageSupplier {
   public static final BuilderCodec<ItemRepairPageSupplier> CODEC = BuilderCodec.builder(ItemRepairPageSupplier.class, ItemRepairPageSupplier::new)
      .appendInherited(
         new KeyedCodec<>("RepairPenalty", Codec.DOUBLE),
         (data, o) -> data.repairPenalty = o,
         data -> data.repairPenalty,
         (data, parent) -> data.repairPenalty = parent.repairPenalty
      )
      .add()
      .build();
   protected double repairPenalty;

   @Override
   public CustomUIPage tryCreate(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull PlayerRef playerRef,
      @Nonnull InteractionContext context
   ) {
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      ItemContext itemContext = context.createHeldItemContext();
      return itemContext == null
         ? null
         : new ItemRepairPage(playerRef, playerComponent.getInventory().getCombinedArmorHotbarUtilityStorage(), this.repairPenalty, itemContext);
   }
}
