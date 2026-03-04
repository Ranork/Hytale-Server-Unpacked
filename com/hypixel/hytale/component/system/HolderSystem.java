package com.hypixel.hytale.component.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import javax.annotation.Nonnull;

public abstract class HolderSystem<ECS_TYPE> extends System<ECS_TYPE> implements QuerySystem<ECS_TYPE> {
   public abstract void onEntityAdd(@Nonnull Holder<ECS_TYPE> var1, @Nonnull AddReason var2, @Nonnull Store<ECS_TYPE> var3);

   public abstract void onEntityRemoved(@Nonnull Holder<ECS_TYPE> var1, @Nonnull RemoveReason var2, @Nonnull Store<ECS_TYPE> var3);
}
