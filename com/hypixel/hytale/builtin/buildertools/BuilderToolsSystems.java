package com.hypixel.hytale.builtin.buildertools;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.asset.type.item.config.BuilderToolItemReferenceAsset;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public class BuilderToolsSystems {
   public static class EnsureBuilderTools extends HolderSystem<EntityStore> {
      @Nonnull
      private static final ComponentType<EntityStore, Player> PLAYER_COMPONENT_TYPE = Player.getComponentType();

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return PLAYER_COMPONENT_TYPE;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         Player playerComponent = holder.getComponent(PLAYER_COMPONENT_TYPE);

         assert playerComponent != null;

         Map<String, BuilderToolItemReferenceAsset> builderTools = BuilderToolItemReferenceAsset.getAssetMap().getAssetMap();
         Inventory playerInventory = playerComponent.getInventory();
         ItemContainer playerTools = playerInventory.getTools();
         playerTools.clear();
         List<ItemStack> toolsToAdd = new ObjectArrayList();

         for (BuilderToolItemReferenceAsset builderTool : builderTools.values()) {
            String[] builderToolItems = builderTool.getItems();

            for (String builderToolItem : builderToolItems) {
               toolsToAdd.add(new ItemStack(builderToolItem));
            }
         }

         if (!playerTools.addItemStacks(toolsToAdd).succeeded()) {
            throw new IllegalArgumentException("Could not add items to the Tools container");
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }
}
