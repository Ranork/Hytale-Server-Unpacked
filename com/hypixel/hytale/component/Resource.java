package com.hypixel.hytale.component;

import javax.annotation.Nullable;

public interface Resource<ECS_TYPE> extends Cloneable {
   Resource[] EMPTY_ARRAY = new Resource[0];

   @Nullable
   Resource<ECS_TYPE> clone();
}
