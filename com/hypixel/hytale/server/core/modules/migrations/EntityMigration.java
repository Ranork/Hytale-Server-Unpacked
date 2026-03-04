package com.hypixel.hytale.server.core.modules.migrations;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import java.util.function.IntFunction;

public abstract class EntityMigration<T> implements Migration {
   private Class<T> tClass;
   private IntFunction<ExtraInfo> extraInfoSupplier;

   public EntityMigration(Class<T> tClass, IntFunction<ExtraInfo> extraInfoSupplier) {
      this.tClass = tClass;
      this.extraInfoSupplier = extraInfoSupplier;
   }

   @Override
   public final void run(WorldChunk chunk) {
      throw new UnsupportedOperationException("Not implemented!");
   }

   protected abstract boolean migrate(T var1);
}
