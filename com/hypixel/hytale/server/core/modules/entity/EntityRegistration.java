package com.hypixel.hytale.server.core.modules.entity;

import com.hypixel.hytale.registry.Registration;
import com.hypixel.hytale.server.core.entity.Entity;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;

public class EntityRegistration extends Registration {
   private final Class<? extends Entity> entityClass;

   public EntityRegistration(Class<? extends Entity> entityClass, BooleanSupplier isEnabled, Runnable unregister) {
      super(isEnabled, unregister);
      this.entityClass = entityClass;
   }

   public EntityRegistration(@Nonnull EntityRegistration registration, BooleanSupplier isEnabled, Runnable unregister) {
      super(isEnabled, unregister);
      this.entityClass = registration.entityClass;
   }

   public Class<? extends Entity> getEntityClass() {
      return this.entityClass;
   }

   @Nonnull
   @Override
   public String toString() {
      return "EntityRegistration{entityClass=" + this.entityClass + ", " + super.toString() + "}";
   }
}
