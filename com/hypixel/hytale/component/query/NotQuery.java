package com.hypixel.hytale.component.query;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import java.util.Objects;
import javax.annotation.Nonnull;

public class NotQuery<ECS_TYPE> implements Query<ECS_TYPE> {
   @Nonnull
   private final Query<ECS_TYPE> query;

   public NotQuery(@Nonnull Query<ECS_TYPE> query) {
      this.query = query;
      Objects.requireNonNull(query, "Sub-query for NotQuery cannot be null");
   }

   @Override
   public boolean test(Archetype<ECS_TYPE> archetype) {
      return !this.query.test(archetype);
   }

   @Override
   public boolean requiresComponentType(ComponentType<ECS_TYPE, ?> componentType) {
      return this.query.requiresComponentType(componentType);
   }

   @Override
   public void validateRegistry(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
      this.query.validateRegistry(registry);
   }

   @Override
   public void validate() {
      this.query.validate();
   }
}
