package com.hypixel.hytale.component.query;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import javax.annotation.Nonnull;

public class AndQuery<ECS_TYPE> implements Query<ECS_TYPE> {
   @Nonnull
   private final Query<ECS_TYPE>[] queries;

   @SafeVarargs
   public AndQuery(@Nonnull Query<ECS_TYPE>... queries) {
      this.queries = queries;

      for (int i = 0; i < queries.length; i++) {
         Query<ECS_TYPE> query = queries[i];
         if (query == null) {
            throw new IllegalArgumentException("Query in AndQuery cannot be null (Index: " + i + ")");
         }
      }
   }

   @Override
   public boolean test(Archetype<ECS_TYPE> archetype) {
      for (Query<ECS_TYPE> query : this.queries) {
         if (!query.test(archetype)) {
            return false;
         }
      }

      return true;
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
