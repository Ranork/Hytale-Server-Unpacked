package com.hypixel.hytale.component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Component<ECS_TYPE> extends Cloneable {
   @Nonnull
   Component[] EMPTY_ARRAY = new Component[0];

   @Nullable
   Component<ECS_TYPE> clone();

   @Nullable
   default Component<ECS_TYPE> cloneSerializable() {
      return this.clone();
   }
}
