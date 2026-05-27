package com.hypixel.hytale.builtin.asseteditor;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorAuthorization;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorInitialize;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorUpdateJsonAsset;
import com.hypixel.hytale.server.core.io.handlers.IPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.SubPacketHandler;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class AssetEditorGamePacketHandler implements SubPacketHandler {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final IPacketHandler packetHandler;

   public AssetEditorGamePacketHandler(IPacketHandler packetHandler) {
      this.packetHandler = packetHandler;
   }

   @Override
   public void registerHandlers() {
      if (AssetEditorPlugin.get().isDisabled()) {
         this.packetHandler.registerNoOpHandlers(302);
         this.packetHandler.registerNoOpHandlers(325);
      } else {
         this.packetHandler.registerHandler(302, p -> this.handle((AssetEditorInitialize)p));
         this.packetHandler.registerHandler(323, p -> this.handle((AssetEditorUpdateJsonAsset)p));
      }
   }

   public void handle(AssetEditorInitialize packet) {
      PlayerRef playerRef = this.packetHandler.getPlayerRef();
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         if (this.lacksPermission(playerRef, false)) {
            this.packetHandler.getPlayerRef().getPacketHandler().write(new AssetEditorAuthorization(false));
         } else {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            world.execute(() -> {
               this.packetHandler.getPlayerRef().getPacketHandler().write(new AssetEditorAuthorization(true));
               AssetEditorPlugin.get().handleInitializeEditor(ref, store);
            });
         }
      }
   }

   @Deprecated
   public void handle(@Nonnull AssetEditorUpdateJsonAsset packet) {
      PlayerRef playerRef = this.packetHandler.getPlayerRef();
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         if (!this.lacksPermission(playerRef, true)) {
            CompletableFuture.runAsync(
               () -> {
                  LOGGER.at(Level.INFO).log("%s updating json asset at %s", this.packetHandler.getPlayerRef().getUsername(), packet.path);
                  EditorClient mockClient = new EditorClient(playerRef);
                  AssetEditorPlugin.get()
                     .handleJsonAssetUpdate(
                        mockClient, packet.path != null ? new AssetPath(packet.path) : null, packet.assetType, packet.assetIndex, packet.commands, packet.token
                     );
               }
            );
         }
      }
   }

   private boolean lacksPermission(@Nonnull PlayerRef playerRef, boolean shouldShowDenialMessage) {
      if (!playerRef.hasPermission(HytalePermissions.ASSET_EDITOR)) {
         if (shouldShowDenialMessage) {
            playerRef.sendMessage(Messages.USAGE_DENIED);
         }

         return true;
      } else {
         return false;
      }
   }
}
