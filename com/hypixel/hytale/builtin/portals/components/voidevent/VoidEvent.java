package com.hypixel.hytale.builtin.portals.components.voidevent;

import com.hypixel.hytale.builtin.portals.PortalsPlugin;
import com.hypixel.hytale.builtin.portals.components.voidevent.config.VoidEventConfig;
import com.hypixel.hytale.builtin.portals.components.voidevent.config.VoidEventStage;
import com.hypixel.hytale.builtin.portals.integrations.PortalGameplayConfig;
import com.hypixel.hytale.builtin.portals.utils.spatial.SpatialHashGrid;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VoidEvent implements Component<EntityStore> {
   public static final double MIN_BLOCKS_BETWEEN_SPAWNERS = 62.0;
   @Nonnull
   private SpatialHashGrid<Ref<EntityStore>> voidSpawners = new SpatialHashGrid<>(62.0);
   @Nullable
   private VoidEventStage activeStage;

   public static ComponentType<EntityStore, VoidEvent> getComponentType() {
      return PortalsPlugin.getInstance().getVoidEventComponentType();
   }

   @Nullable
   public static VoidEventConfig getConfig(@Nonnull World world) {
      PortalGameplayConfig portalConfig = world.getGameplayConfig().getPluginConfig().get(PortalGameplayConfig.class);
      return portalConfig != null ? portalConfig.getVoidEvent() : null;
   }

   @Nonnull
   public SpatialHashGrid<Ref<EntityStore>> getVoidSpawners() {
      return this.voidSpawners;
   }

   @Nullable
   public VoidEventStage getActiveStage() {
      return this.activeStage;
   }

   public void setActiveStage(@Nullable VoidEventStage activeStage) {
      this.activeStage = activeStage;
   }

   @Nullable
   @Override
   public Component<EntityStore> clone() {
      VoidEvent clone = new VoidEvent();
      clone.voidSpawners = this.voidSpawners;
      clone.activeStage = this.activeStage;
      return clone;
   }
}
