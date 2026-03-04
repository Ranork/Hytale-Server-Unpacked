package com.hypixel.hytale.component.dependency;

import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.system.ISystem;
import javax.annotation.Nonnull;

public class SystemDependency<ECS_TYPE, T extends ISystem<ECS_TYPE>> extends Dependency<ECS_TYPE> {
   @Nonnull
   private final Class<T> systemClass;

   public SystemDependency(@Nonnull Order order, @Nonnull Class<T> systemClass) {
      this(order, systemClass, OrderPriority.NORMAL);
   }

   public SystemDependency(@Nonnull Order order, @Nonnull Class<T> systemClass, int priority) {
      super(order, priority);
      this.systemClass = systemClass;
   }

   public SystemDependency(@Nonnull Order order, @Nonnull Class<T> systemClass, @Nonnull OrderPriority priority) {
      super(order, priority);
      this.systemClass = systemClass;
   }

   @Nonnull
   public Class<T> getSystemClass() {
      return this.systemClass;
   }

   @Override
   public void validate(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
      if (!registry.hasSystemClass(this.systemClass)) {
         throw new IllegalArgumentException("SystemType dependency isn't registered: " + this.systemClass);
      }
   }

   @Override
   public void resolveGraphEdge(@Nonnull ComponentRegistry<ECS_TYPE> registry, @Nonnull ISystem<ECS_TYPE> thisSystem, @Nonnull DependencyGraph<ECS_TYPE> graph) {
      switch (this.order) {
         case BEFORE:
            for (ISystem<ECS_TYPE> systemx : graph.getSystems()) {
               if (this.systemClass.equals(systemx.getClass())) {
                  graph.addEdge(thisSystem, systemx, -this.priority);
               }
            }
            break;
         case AFTER:
            for (ISystem<ECS_TYPE> system : graph.getSystems()) {
               if (this.systemClass.equals(system.getClass())) {
                  graph.addEdge(system, thisSystem, this.priority);
               }
            }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "SystemDependency{systemClass=" + this.systemClass + "} " + super.toString();
   }
}
