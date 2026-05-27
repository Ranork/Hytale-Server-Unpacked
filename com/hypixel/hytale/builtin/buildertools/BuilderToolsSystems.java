package com.hypixel.hytale.builtin.buildertools;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.asset.type.item.config.BuilderToolItemReferenceAsset;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class BuilderToolsSystems {
   public static class EnsureBuilderTools extends HolderSystem<EntityStore> {
      @Nonnull
      private static final ComponentType<EntityStore, InventoryComponent.Tool> TOOL_COMPONENT_TYPE = InventoryComponent.Tool.getComponentType();
      private final Set<Dependency<EntityStore>> dependencies = Set.of(new SystemDependency<>(Order.AFTER, PlayerSystems.PlayerInitSystem.class));

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return Query.and(Player.getComponentType(), TOOL_COMPONENT_TYPE);
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         Map<String, BuilderToolItemReferenceAsset> builderTools = BuilderToolItemReferenceAsset.getAssetMap().getAssetMap();
         InventoryComponent.Tool toolsComponent = holder.getComponent(TOOL_COMPONENT_TYPE);

         assert toolsComponent != null;

         ItemContainer toolsInventory = toolsComponent.getInventory();
         toolsInventory.clear();
         List<ItemStack> toolsToAdd = new ObjectArrayList();

         for (BuilderToolItemReferenceAsset builderTool : builderTools.values()) {
            String[] builderToolItems = builderTool.getItems();

            for (String builderToolItem : builderToolItems) {
               toolsToAdd.add(new ItemStack(builderToolItem));
            }
         }

         if (!toolsInventory.addItemStacks(toolsToAdd).succeeded()) {
            throw new IllegalArgumentException("Could not add items to the Tools container");
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }
   }
}
