package com.hypixel.hytale.server.core.modules.entity;

import com.hypixel.hytale.codec.DirectDecodeCodec;
import com.hypixel.hytale.function.consumer.BooleanConsumer;
import com.hypixel.hytale.registry.Registry;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityRegistry extends Registry<EntityRegistration> {
   public EntityRegistry(@Nonnull List<BooleanConsumer> registrations, BooleanSupplier precondition, String preconditionMessage) {
      super(registrations, precondition, preconditionMessage, EntityRegistration::new);
   }

   @Nullable
   public <T extends Entity> EntityRegistration registerEntity(
      @Nonnull String key, @Nonnull Class<T> clazz, Function<World, T> constructor, DirectDecodeCodec<T> codec
   ) {
      this.checkPrecondition();
      return this.register(EntityModule.get().registerEntity(key, clazz, constructor, codec));
   }
}
