package com.hypixel.hytale.builtin.adventure.npcshop.npc;

import com.hypixel.hytale.builtin.adventure.npcshop.npc.builders.BuilderActionOpenShop;
import com.hypixel.hytale.builtin.adventure.shop.ShopPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class ActionOpenShop extends ActionBase {
   @Nonnull
   protected final String shopId;

   public ActionOpenShop(@Nonnull BuilderActionOpenShop builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.shopId = builder.getShopId(support);
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return super.canExecute(ref, role, sensorInfo, dt, store) && role.getStateSupport().getInteractionIterationTarget() != null;
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      Ref<EntityStore> playerReference = role.getStateSupport().getInteractionIterationTarget();
      if (playerReference == null) {
         return false;
      } else {
         PlayerRef playerRefComponent = store.getComponent(playerReference, PlayerRef.getComponentType());
         if (playerRefComponent == null) {
            return false;
         } else {
            Player playerComponent = store.getComponent(playerReference, Player.getComponentType());
            if (playerComponent == null) {
               return false;
            } else {
               playerComponent.getPageManager().openCustomPage(ref, store, new ShopPage(playerRefComponent, this.shopId));
               return true;
            }
         }
      }
   }
}
