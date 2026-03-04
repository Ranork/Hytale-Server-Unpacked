package com.hypixel.hytale.component.data.change;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import javax.annotation.Nonnull;

public class ComponentChange<ECS_TYPE, T extends Component<ECS_TYPE>> implements DataChange {
   private final ChangeType type;
   private final ComponentType<ECS_TYPE, T> componentType;

   public ComponentChange(ChangeType type, ComponentType<ECS_TYPE, T> componentType) {
      this.type = type;
      this.componentType = componentType;
   }

   public ChangeType getType() {
      return this.type;
   }

   public ComponentType<ECS_TYPE, T> getComponentType() {
      return this.componentType;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ComponentChange{type=" + this.type + ", componentType=" + this.componentType + "}";
   }
}
