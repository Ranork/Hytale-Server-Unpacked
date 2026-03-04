package com.hypixel.hytale.component.dependency;

import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.system.ISystem;
import javax.annotation.Nonnull;

public abstract class Dependency<ECS_TYPE> {
   @Nonnull
   protected final Order order;
   protected final int priority;

   public Dependency(@Nonnull Order order, int priority) {
      this.order = order;
      this.priority = priority;
   }

   public Dependency(@Nonnull Order order, @Nonnull OrderPriority priority) {
      this.order = order;
      this.priority = priority.getValue();
   }

   @Nonnull
   public Order getOrder() {
      return this.order;
   }

   public int getPriority() {
      return this.priority;
   }

   public abstract void validate(@Nonnull ComponentRegistry<ECS_TYPE> var1);

   public abstract void resolveGraphEdge(@Nonnull ComponentRegistry<ECS_TYPE> var1, @Nonnull ISystem<ECS_TYPE> var2, @Nonnull DependencyGraph<ECS_TYPE> var3);

   @Nonnull
   @Override
   public String toString() {
      return "Dependency{order=" + this.order + ", priority=" + this.priority + "}";
   }
}
