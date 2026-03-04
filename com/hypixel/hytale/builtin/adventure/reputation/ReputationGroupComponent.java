package com.hypixel.hytale.builtin.adventure.reputation;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ReputationGroupComponent implements Component<EntityStore> {
   @Nonnull
   private final String reputationGroupId;

   @Nonnull
   public static ComponentType<EntityStore, ReputationGroupComponent> getComponentType() {
      return ReputationPlugin.get().getReputationGroupComponentType();
   }

   public ReputationGroupComponent(@Nonnull String reputationGroupId) {
      this.reputationGroupId = reputationGroupId;
   }

   @Nonnull
   public String getReputationGroupId() {
      return this.reputationGroupId;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new ReputationGroupComponent(this.reputationGroupId);
   }
}
