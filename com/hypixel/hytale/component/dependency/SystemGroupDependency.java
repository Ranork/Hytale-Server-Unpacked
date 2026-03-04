package com.hypixel.hytale.component.dependency;

import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.system.ISystem;
import javax.annotation.Nonnull;

public class SystemGroupDependency<ECS_TYPE> extends Dependency<ECS_TYPE> {
   @Nonnull
   private final SystemGroup<ECS_TYPE> group;

   public SystemGroupDependency(@Nonnull Order order, @Nonnull SystemGroup<ECS_TYPE> group) {
      this(order, group, OrderPriority.NORMAL);
   }

   public SystemGroupDependency(@Nonnull Order order, @Nonnull SystemGroup<ECS_TYPE> group, int priority) {
      super(order, priority);
      this.group = group;
   }

   public SystemGroupDependency(@Nonnull Order order, @Nonnull SystemGroup<ECS_TYPE> group, @Nonnull OrderPriority priority) {
      super(order, priority);
      this.group = group;
   }

   @Nonnull
   public SystemGroup<ECS_TYPE> getGroup() {
      return this.group;
   }

   @Override
   public void validate(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
      if (!registry.hasSystemGroup(this.group)) {
         throw new IllegalArgumentException("System dependency isn't registered: " + this.group);
      }
   }

   @Override
   public void resolveGraphEdge(@Nonnull ComponentRegistry<ECS_TYPE> registry, @Nonnull ISystem<ECS_TYPE> thisSystem, @Nonnull DependencyGraph<ECS_TYPE> graph) {
      switch (this.order) {
         case BEFORE:
            for (ISystem<ECS_TYPE> systemx : graph.getSystems()) {
               if (this.group.equals(systemx.getGroup())) {
                  graph.addEdge(thisSystem, systemx, -this.priority);
               }
            }
            break;
         case AFTER:
            for (ISystem<ECS_TYPE> system : graph.getSystems()) {
               if (this.group.equals(system.getGroup())) {
                  graph.addEdge(system, thisSystem, this.priority);
               }
            }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "SystemGroupDependency{group=" + this.group + "} " + super.toString();
   }
}
