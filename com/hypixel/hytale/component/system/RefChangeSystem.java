package com.hypixel.hytale.component.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class RefChangeSystem<ECS_TYPE, T extends Component<ECS_TYPE>> extends System<ECS_TYPE> implements QuerySystem<ECS_TYPE> {
   @Nonnull
   public abstract ComponentType<ECS_TYPE, T> componentType();

   public abstract void onComponentAdded(@Nonnull Ref<ECS_TYPE> var1, @Nonnull T var2, @Nonnull Store<ECS_TYPE> var3, @Nonnull CommandBuffer<ECS_TYPE> var4);

   public abstract void onComponentSet(
      @Nonnull Ref<ECS_TYPE> var1, @Nullable T var2, @Nonnull T var3, @Nonnull Store<ECS_TYPE> var4, @Nonnull CommandBuffer<ECS_TYPE> var5
   );

   public abstract void onComponentRemoved(@Nonnull Ref<ECS_TYPE> var1, @Nonnull T var2, @Nonnull Store<ECS_TYPE> var3, @Nonnull CommandBuffer<ECS_TYPE> var4);
}
