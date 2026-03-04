package com.hypixel.hytale.server.core.command.commands.player.camera;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.math.iterator.BlockIterator;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.MouseButtonState;
import com.hypixel.hytale.protocol.MouseButtonType;
import com.hypixel.hytale.protocol.MouseInputType;
import com.hypixel.hytale.protocol.MovementForceRotationType;
import com.hypixel.hytale.protocol.PositionDistanceOffsetType;
import com.hypixel.hytale.protocol.RotationType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.player.CameraManager;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;

public class CameraDemo {
   public static final CameraDemo INSTANCE = new CameraDemo();
   private final EventRegistry eventRegistry = new EventRegistry(
      new CopyOnWriteArrayList<>(), () -> this.isActive, "CameraDemo is not active!", HytaleServer.get().getEventBus()
   );
   private final ServerCameraSettings cameraSettings = createServerCameraSettings();
   private boolean isActive;

   public void activate() {
      if (!this.isActive) {
         this.eventRegistry.enable();
         this.isActive = true;
         this.eventRegistry.register(PlayerConnectEvent.class, event -> this.onAddNewPlayer(event.getPlayerRef()));
         this.eventRegistry.register(PlayerMouseButtonEvent.class, this::onPlayerMouseButton);
         this.eventRegistry.registerGlobal(PlayerInteractEvent.class, event -> event.setCancelled(true));
         Universe.get().getPlayers().forEach(this::onAddNewPlayer);
      }
   }

   public void deactivate() {
      if (this.isActive) {
         this.eventRegistry.shutdownAndCleanup(false);
         Universe.get().getPlayers().forEach(p -> {
            CameraManager cameraManager = p.getComponent(CameraManager.getComponentType());
            if (cameraManager != null) {
               cameraManager.resetCamera(p);
            }
         });
         this.isActive = false;
      }
   }

   private void onAddNewPlayer(@Nonnull PlayerRef player) {
      player.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, true, this.cameraSettings));
   }

   private void onPlayerMouseButton(@Nonnull PlayerMouseButtonEvent event) {
      if (event.getMouseButton().state == MouseButtonState.Released) {
         Ref<EntityStore> ref = event.getPlayerRef();
         if (ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            Vector3i targetBlock = event.getTargetBlock();
            CameraManager cameraManagerComponent = store.getComponent(ref, CameraManager.getComponentType());

            assert cameraManagerComponent != null;

            Vector3i lastTargetBlock = cameraManagerComponent.getLastMouseButtonPressedPosition(event.getMouseButton().mouseButtonType);
            if (event.getMouseButton().mouseButtonType == MouseButtonType.Middle) {
               if (event.getItemInHand() != null && event.getItemInHand().hasBlockType() && targetBlock != null) {
                  String key = event.getItemInHand().getId();
                  int blockId = BlockType.getAssetMap().getIndex(key);
                  if (blockId == Integer.MIN_VALUE) {
                     throw new IllegalArgumentException("Unknown key! " + key);
                  }

                  if (!lastTargetBlock.equals(targetBlock)) {
                     BlockIterator.iterateFromTo(lastTargetBlock, targetBlock, (xx, y, zx, px, py, pz, qx, qy, qz) -> {
                        world.getChunk(ChunkUtil.indexChunkFromBlock(xx, zx)).setBlock(xx, y, zx, blockId);
                        return true;
                     });
                  } else {
                     int x = targetBlock.getX();
                     int z = targetBlock.getZ();
                     world.getChunk(ChunkUtil.indexChunkFromBlock(x, z)).setBlock(x, targetBlock.getY(), z, blockId);
                  }
               }
            } else if (event.getMouseButton().mouseButtonType == MouseButtonType.Right) {
               if (!lastTargetBlock.equals(targetBlock)) {
                  BlockIterator.iterateFromTo(lastTargetBlock, targetBlock, (xx, y, zx, px, py, pz, qx, qy, qz) -> {
                     world.getChunk(ChunkUtil.indexChunkFromBlock(xx, zx)).setBlock(xx, y, zx, 0);
                     return true;
                  });
               } else {
                  int x = targetBlock.getX();
                  int z = targetBlock.getZ();
                  world.getChunk(ChunkUtil.indexChunkFromBlock(x, z)).setBlock(x, targetBlock.getY(), z, 0);
               }
            } else if (event.getMouseButton().mouseButtonType == MouseButtonType.Left
               && event.getItemInHand() != null
               && event.getItemInHand().hasBlockType()
               && targetBlock != null) {
               String keyx = event.getItemInHand().getId();
               int blockIdx = BlockType.getAssetMap().getIndex(keyx);
               if (blockIdx == Integer.MIN_VALUE) {
                  throw new IllegalArgumentException("Unknown key! " + keyx);
               }

               if (!lastTargetBlock.equals(targetBlock)) {
                  BlockIterator.iterateFromTo(
                     lastTargetBlock.getX(),
                     lastTargetBlock.getY() + 1,
                     lastTargetBlock.getZ(),
                     targetBlock.getX(),
                     targetBlock.getY() + 1,
                     targetBlock.getZ(),
                     (xx, y, zx, px, py, pz, qx, qy, qz) -> {
                        world.getChunk(ChunkUtil.indexChunkFromBlock(xx, zx)).setBlock(xx, y, zx, blockId);
                        return true;
                     }
                  );
               } else {
                  int x = targetBlock.getX();
                  int z = targetBlock.getZ();
                  world.getChunk(ChunkUtil.indexChunkFromBlock(x, z)).setBlock(x, targetBlock.getY() + 1, z, blockIdx);
               }
            }
         }
      }
   }

   @Nonnull
   private static ServerCameraSettings createServerCameraSettings() {
      ServerCameraSettings cameraSettings = new ServerCameraSettings();
      cameraSettings.positionLerpSpeed = 0.2F;
      cameraSettings.rotationLerpSpeed = 0.2F;
      cameraSettings.distance = 20.0F;
      cameraSettings.displayCursor = true;
      cameraSettings.sendMouseMotion = true;
      cameraSettings.isFirstPerson = false;
      cameraSettings.movementForceRotationType = MovementForceRotationType.Custom;
      cameraSettings.eyeOffset = true;
      cameraSettings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset;
      cameraSettings.rotationType = RotationType.Custom;
      cameraSettings.rotation = new Direction(0.0F, (float) (-Math.PI / 2), 0.0F);
      cameraSettings.mouseInputType = MouseInputType.LookAtPlane;
      cameraSettings.planeNormal = new Vector3f(0.0F, 1.0F, 0.0F);
      return cameraSettings;
   }
}
