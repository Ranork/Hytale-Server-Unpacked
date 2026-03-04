package com.hypixel.hytale.component.system;

import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.DependencyGraph;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ISystem<ECS_TYPE> {
   ISystem[] EMPTY_ARRAY = new ISystem[0];

   default void onSystemRegistered() {
   }

   default void onSystemUnregistered() {
   }

   @Nullable
   default SystemGroup<ECS_TYPE> getGroup() {
      return null;
   }

   @Nonnull
   default Set<Dependency<ECS_TYPE>> getDependencies() {
      return Collections.emptySet();
   }

   static <ECS_TYPE> void calculateOrder(@Nonnull ComponentRegistry<ECS_TYPE> registry, @Nonnull ISystem<ECS_TYPE>[] sortedSystems, int systemSize) {
      DependencyGraph<ECS_TYPE> graph = new DependencyGraph<>(Arrays.copyOf(sortedSystems, systemSize));
      graph.resolveEdges(registry);
      graph.sort(sortedSystems);
   }
}
