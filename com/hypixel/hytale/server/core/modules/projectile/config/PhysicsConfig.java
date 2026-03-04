package com.hypixel.hytale.server.core.modules.projectile.config;

import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface PhysicsConfig extends NetworkSerializable<com.hypixel.hytale.protocol.PhysicsConfig> {
   @Nonnull
   CodecMapCodec<PhysicsConfig> CODEC = new CodecMapCodec<>("Type");

   void apply(
      @Nonnull Holder<EntityStore> var1, @Nullable Ref<EntityStore> var2, @Nonnull Vector3d var3, @Nonnull ComponentAccessor<EntityStore> var4, boolean var5
   );

   default double getGravity() {
      return 0.0;
   }
}
