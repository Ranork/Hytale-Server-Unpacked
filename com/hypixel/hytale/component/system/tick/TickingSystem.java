package com.hypixel.hytale.component.system.tick;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.System;
import javax.annotation.Nonnull;

public abstract class TickingSystem<ECS_TYPE> extends System<ECS_TYPE> implements TickableSystem<ECS_TYPE> {
   @Override
   public abstract void tick(float var1, int var2, @Nonnull Store<ECS_TYPE> var3);
}
