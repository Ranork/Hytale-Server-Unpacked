package com.hypixel.hytale.component.system;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.query.Query;
import javax.annotation.Nullable;

public interface QuerySystem<ECS_TYPE> extends ISystem<ECS_TYPE> {
   default boolean test(ComponentRegistry<ECS_TYPE> componentRegistry, Archetype<ECS_TYPE> archetype) {
      return this.getQuery().test(archetype);
   }

   @Nullable
   Query<ECS_TYPE> getQuery();
}
