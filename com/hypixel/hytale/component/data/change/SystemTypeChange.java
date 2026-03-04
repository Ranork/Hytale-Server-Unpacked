package com.hypixel.hytale.component.data.change;

import com.hypixel.hytale.component.SystemType;
import com.hypixel.hytale.component.system.ISystem;
import javax.annotation.Nonnull;

public class SystemTypeChange<ECS_TYPE, T extends ISystem<ECS_TYPE>> implements DataChange {
   private final ChangeType type;
   private final SystemType<ECS_TYPE, T> systemType;

   public SystemTypeChange(ChangeType type, SystemType<ECS_TYPE, T> systemType) {
      this.type = type;
      this.systemType = systemType;
   }

   public ChangeType getType() {
      return this.type;
   }

   public SystemType<ECS_TYPE, T> getSystemType() {
      return this.systemType;
   }

   @Nonnull
   @Override
   public String toString() {
      return "SystemTypeChange{type=" + this.type + ", systemType=" + this.systemType + "}";
   }
}
