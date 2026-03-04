package com.hypixel.hytale.component.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import javax.annotation.Nonnull;

public abstract class RefSystem<ECS_TYPE> extends System<ECS_TYPE> implements QuerySystem<ECS_TYPE> {
   public abstract void onEntityAdded(
      @Nonnull Ref<ECS_TYPE> var1, @Nonnull AddReason var2, @Nonnull Store<ECS_TYPE> var3, @Nonnull CommandBuffer<ECS_TYPE> var4
   );

   public abstract void onEntityRemove(
      @Nonnull Ref<ECS_TYPE> var1, @Nonnull RemoveReason var2, @Nonnull Store<ECS_TYPE> var3, @Nonnull CommandBuffer<ECS_TYPE> var4
   );
}
