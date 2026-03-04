package com.hypixel.hytale.component.dependency;

import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.SystemType;
import com.hypixel.hytale.component.system.ISystem;
import javax.annotation.Nonnull;

public class SystemTypeDependency<ECS_TYPE, T extends ISystem<ECS_TYPE>> extends Dependency<ECS_TYPE> {
   @Nonnull
   private final SystemType<ECS_TYPE, T> systemType;

   public SystemTypeDependency(@Nonnull Order order, @Nonnull SystemType<ECS_TYPE, T> systemType) {
      this(order, systemType, OrderPriority.NORMAL);
   }

   public SystemTypeDependency(@Nonnull Order order, @Nonnull SystemType<ECS_TYPE, T> systemType, int priority) {
      super(order, priority);
      this.systemType = systemType;
   }

   public SystemTypeDependency(@Nonnull Order order, @Nonnull SystemType<ECS_TYPE, T> systemType, @Nonnull OrderPriority priority) {
      super(order, priority);
      this.systemType = systemType;
   }

   @Nonnull
   public SystemType<ECS_TYPE, T> getSystemType() {
      return this.systemType;
   }

   @Override
   public void validate(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
      if (!registry.hasSystemType(this.systemType)) {
         throw new IllegalArgumentException("SystemType dependency isn't registered: " + this.systemType);
      }
   }

   @Override
   public void resolveGraphEdge(@Nonnull ComponentRegistry<ECS_TYPE> registry, @Nonnull ISystem<ECS_TYPE> thisSystem, @Nonnull DependencyGraph<ECS_TYPE> graph) {
      switch (this.order) {
         case BEFORE:
            for (ISystem<ECS_TYPE> systemx : graph.getSystems()) {
               if (this.systemType.isType(systemx)) {
                  graph.addEdge(thisSystem, systemx, -this.priority);
               }
            }
            break;
         case AFTER:
            for (ISystem<ECS_TYPE> system : graph.getSystems()) {
               if (this.systemType.isType(system)) {
                  graph.addEdge(system, thisSystem, this.priority);
               }
            }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "SystemTypeDependency{systemType=" + this.systemType + "} " + super.toString();
   }
}
