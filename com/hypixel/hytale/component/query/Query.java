package com.hypixel.hytale.component.query;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import javax.annotation.Nonnull;

public interface Query<ECS_TYPE> {
   @Nonnull
   static <ECS_TYPE> AnyQuery<ECS_TYPE> any() {
      return (AnyQuery<ECS_TYPE>)AnyQuery.INSTANCE;
   }

   @Nonnull
   static <ECS_TYPE> NotQuery<ECS_TYPE> not(@Nonnull Query<ECS_TYPE> query) {
      return new NotQuery<>(query);
   }

   @Nonnull
   @SafeVarargs
   static <ECS_TYPE> AndQuery<ECS_TYPE> and(@Nonnull Query<ECS_TYPE>... queries) {
      return new AndQuery<>(queries);
   }

   @Nonnull
   @SafeVarargs
   static <ECS_TYPE> OrQuery<ECS_TYPE> or(@Nonnull Query<ECS_TYPE>... queries) {
      return new OrQuery<>(queries);
   }

   boolean test(Archetype<ECS_TYPE> var1);

   boolean requiresComponentType(ComponentType<ECS_TYPE, ?> var1);

   void validateRegistry(@Nonnull ComponentRegistry<ECS_TYPE> var1);

   void validate();
}
