package com.hypixel.hytale.component;

import com.hypixel.hytale.component.system.ISystem;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SystemType<ECS_TYPE, T extends ISystem<ECS_TYPE>> implements Comparable<SystemType<ECS_TYPE, ?>> {
   @Nonnull
   public static final SystemType[] EMPTY_ARRAY = new SystemType[0];
   @Nonnull
   private final ComponentRegistry<ECS_TYPE> registry;
   @Nonnull
   private final Class<? super T> tClass;
   private final int index;
   private boolean invalidated;

   protected SystemType(@Nonnull ComponentRegistry<ECS_TYPE> registry, @Nonnull Class<? super T> tClass, int index) {
      this.registry = registry;
      this.tClass = tClass;
      this.index = index;
   }

   @Nonnull
   public ComponentRegistry<ECS_TYPE> getRegistry() {
      return this.registry;
   }

   public Class<? super T> getTypeClass() {
      return this.tClass;
   }

   public boolean isType(@Nonnull ISystem<ECS_TYPE> system) {
      return this.tClass.isAssignableFrom(system.getClass());
   }

   public int getIndex() {
      return this.index;
   }

   public void validateRegistry(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
      if (!this.registry.equals(registry)) {
         throw new IllegalArgumentException("SystemType is for a different registry! " + this);
      }
   }

   public void validate() {
      if (this.invalidated) {
         throw new IllegalStateException("SystemType is invalid!");
      }
   }

   protected void invalidate() {
      this.invalidated = true;
   }

   protected boolean isValid() {
      return !this.invalidated;
   }

   public int compareTo(@Nonnull SystemType<ECS_TYPE, ?> o) {
      return Integer.compare(this.index, o.getIndex());
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SystemType<?, ?> that = (SystemType<?, ?>)o;
         return this.index != that.index ? false : this.registry.equals(that.registry);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.registry.hashCode();
      return 31 * result + this.index;
   }

   @Nonnull
   @Override
   public String toString() {
      return "SystemType{registry=" + this.registry.getClass() + "@" + this.registry.hashCode() + ", typeClass=" + this.tClass + ", index=" + this.index + "}";
   }
}
