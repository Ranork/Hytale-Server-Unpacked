package com.hypixel.hytale.server.core.modules.entity;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

@Deprecated
public class AllLegacyLivingEntityTypesQuery implements Query<EntityStore> {
   @Nonnull
   public static final AllLegacyLivingEntityTypesQuery INSTANCE = new AllLegacyLivingEntityTypesQuery();

   @Override
   public boolean test(@Nonnull Archetype<EntityStore> archetype) {
      return EntityUtils.hasLivingEntity(archetype);
   }

   @Override
   public boolean requiresComponentType(ComponentType<EntityStore, ?> componentType) {
      return false;
   }

   @Override
   public void validateRegistry(@Nonnull ComponentRegistry<EntityStore> registry) {
   }

   @Override
   public void validate() {
   }
}
