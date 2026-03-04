package com.hypixel.hytale.builtin.instances.removal;

import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public interface RemovalCondition {
   @Nonnull
   CodecMapCodec<RemovalCondition> CODEC = new CodecMapCodec<>("Type");
   @Nonnull
   RemovalCondition[] EMPTY = new RemovalCondition[0];

   boolean shouldRemoveWorld(@Nonnull Store<ChunkStore> var1);
}
