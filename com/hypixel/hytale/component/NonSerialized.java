package com.hypixel.hytale.component;

import javax.annotation.Nonnull;

public class NonSerialized<ECS_TYPE> implements Component<ECS_TYPE> {
   @Nonnull
   private static final NonSerialized<?> INSTANCE = new NonSerialized();

   public static <ECS_TYPE> NonSerialized<ECS_TYPE> get() {
      return (NonSerialized<ECS_TYPE>)INSTANCE;
   }

   @Override
   public Component<ECS_TYPE> clone() {
      return get();
   }
}
