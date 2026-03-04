package com.hypixel.hytale.server.core.io.handlers;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IWorldPacketHandler<T extends Packet> {
   void handle(@Nonnull T var1, @Nonnull PlayerRef var2, @Nonnull Ref<EntityStore> var3, @Nonnull World var4, @Nonnull Store<EntityStore> var5);

   static <T extends Packet> void registerHandler(@Nonnull IPacketHandler packetHandler, int packetId, @Nonnull IWorldPacketHandler<T> handler) {
      registerHandler(packetHandler, packetId, handler, null);
   }

   static <T extends Packet> void registerHandler(
      @Nonnull IPacketHandler packetHandler, int packetId, @Nonnull IWorldPacketHandler<T> handler, @Nullable Predicate<PlayerRef> precondition
   ) {
      packetHandler.registerHandler(packetId, packet -> {
         PlayerRef playerRef = packetHandler.getPlayerRef();
         Ref<EntityStore> ref = playerRef.getReference();
         if (ref != null && ref.isValid()) {
            if (precondition == null || precondition.test(playerRef)) {
               Store<EntityStore> store = ref.getStore();
               World world = store.getExternalData().getWorld();
               world.execute(() -> {
                  if (ref.isValid()) {
                     handler.handle((T)packet, playerRef, ref, world, store);
                  }
               });
            }
         }
      });
   }
}
