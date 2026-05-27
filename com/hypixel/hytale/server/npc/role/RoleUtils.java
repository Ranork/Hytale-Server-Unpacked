package com.hypixel.hytale.server.npc.role;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RoleUtils {
   public static void setItemInHand(
      @Nonnull Ref<EntityStore> ref, @Nonnull NPCEntity npcComponent, @Nullable String itemInHand, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (!InventoryHelper.useItem(ref, itemInHand, componentAccessor)) {
         NPCPlugin.get().getLogger().at(Level.WARNING).log("NPC of type '%s': Failed to use item '%s'", npcComponent.getRoleName(), itemInHand);
      }
   }

   public static void setArmor(
      @Nonnull Ref<EntityStore> ref, @Nonnull NPCEntity npcComponent, @Nullable String armor, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      InventoryComponent.Armor armorComponent = componentAccessor.getComponent(ref, InventoryComponent.Armor.getComponentType());
      if (armorComponent == null || !InventoryHelper.useArmor(armorComponent.getInventory(), armor)) {
         NPCPlugin.get().getLogger().at(Level.WARNING).log("NPC of type '%s': Failed to use armor '%s'", npcComponent.getRoleName(), armor);
      }
   }
}
