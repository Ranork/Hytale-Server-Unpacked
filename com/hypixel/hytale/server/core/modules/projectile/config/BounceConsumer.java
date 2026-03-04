package com.hypixel.hytale.server.core.modules.projectile.config;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

@FunctionalInterface
public interface BounceConsumer {
   void onBounce(@Nonnull Ref<EntityStore> var1, @Nonnull Vector3d var2, @Nonnull CommandBuffer<EntityStore> var3);
}
