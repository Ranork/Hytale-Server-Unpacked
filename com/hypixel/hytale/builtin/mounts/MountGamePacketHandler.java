package com.hypixel.hytale.builtin.mounts;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.protocol.packets.interaction.DismountNPC;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.io.handlers.IPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.SubPacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class MountGamePacketHandler implements SubPacketHandler {
   private final IPacketHandler packetHandler;

   public MountGamePacketHandler(IPacketHandler packetHandler) {
      this.packetHandler = packetHandler;
   }

   @Override
   public void registerHandlers() {
      this.packetHandler.registerHandler(294, protoPacket -> this.handle((DismountNPC)protoPacket));
   }

   public void handle(DismountNPC packet) {
      PlayerRef playerRef = this.packetHandler.getPlayerRef();
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         Store<EntityStore> store = ref.getStore();
         EntityStore entityStore = store.getExternalData();
         World world = entityStore.getWorld();
         world.execute(() -> {
            Player playerComponent = store.getComponent(ref, Player.getComponentType());

            assert playerComponent != null;

            MountedComponent mounted = store.getComponent(ref, MountedComponent.getComponentType());
            if (mounted == null) {
               MountPlugin.checkDismountNpc(store, ref, playerComponent);
            } else {
               if (mounted.getControllerType() == MountController.BlockMount) {
                  store.tryRemoveComponent(ref, MountedComponent.getComponentType());
               }
            }
         });
      } else {
         throw new RuntimeException("Unable to process DismountNPC packet. Player ref is invalid!");
      }
   }
}
