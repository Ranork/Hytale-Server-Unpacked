package com.hypixel.hytale.component.query;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import javax.annotation.Nonnull;

public class OrQuery<ECS_TYPE> implements Query<ECS_TYPE> {
   @Nonnull
   private final Query<ECS_TYPE>[] queries;

   public OrQuery(@Nonnull Query<ECS_TYPE>... queries) {
      this.queries = queries;
      if (queries.length == 0) {
         throw new IllegalArgumentException("At least one query must be provided for OrQuery");
      } else {
         for (int i = 0; i < queries.length; i++) {
            if (queries[i] == null) {
               throw new NullPointerException("Query at index " + i + " for OrQuery cannot be null");
            }
         }
      }
   }

   @Override
   public boolean test(Archetype<ECS_TYPE> archetype) {
      for (Query<ECS_TYPE> query : this.queries) {
         if (query.test(archetype)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean requiresComponentType(ComponentType<ECS_TYPE, ?> componentType) {
      for (Query<ECS_TYPE> query : this.queries) {
         if (query.requiresComponentType(componentType)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void validateRegistry(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
      for (Query<ECS_TYPE> query : this.queries) {
         query.validateRegistry(registry);
      }
   }

   @Override
   public void validate() {
      for (Query<ECS_TYPE> query : this.queries) {
         query.validate();
      }
   }
}
