package com.hypixel.hytale.builtin.asseteditor;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorAuthorization;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorInitialize;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorUpdateJsonAsset;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.io.handlers.IPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.SubPacketHandler;
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
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         world.execute(() -> {
            Player playerComponent = store.getComponent(ref, Player.getComponentType());

            assert playerComponent != null;

            if (this.lacksPermission(playerComponent, false)) {
               this.packetHandler.getPlayerRef().getPacketHandler().write(new AssetEditorAuthorization(false));
            } else {
               this.packetHandler.getPlayerRef().getPacketHandler().write(new AssetEditorAuthorization(true));
               AssetEditorPlugin.get().handleInitializeEditor(ref, store);
            }
         });
      } else {
         throw new RuntimeException("Unable to process AssetEditorInitialize packet. Player ref is invalid!");
      }
   }

   @Deprecated
   public void handle(@Nonnull AssetEditorUpdateJsonAsset packet) {
      PlayerRef playerRef = this.packetHandler.getPlayerRef();
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         world.execute(
            () -> {
               Player playerComponent = store.getComponent(ref, Player.getComponentType());
               if (!this.lacksPermission(playerComponent, true)) {
                  CompletableFuture.runAsync(
                     () -> {
                        LOGGER.at(Level.INFO).log("%s updating json asset at %s", this.packetHandler.getPlayerRef().getUsername(), packet.path);
                        EditorClient mockClient = new EditorClient(playerRef);
                        AssetEditorPlugin.get()
                           .handleJsonAssetUpdate(
                              mockClient,
                              packet.path != null ? new AssetPath(packet.path) : null,
                              packet.assetType,
                              packet.assetIndex,
                              packet.commands,
                              packet.token
                           );
                     }
                  );
               }
            }
         );
      } else {
         throw new RuntimeException("Unable to process AssetEditorUpdateJsonAsset packet. Player ref is invalid!");
      }
   }

   private boolean lacksPermission(@Nonnull Player player, boolean shouldShowDenialMessage) {
      if (!player.hasPermission("hytale.editor.asset")) {
         if (shouldShowDenialMessage) {
            player.sendMessage(Messages.USAGE_DENIED);
         }

         return true;
      } else {
         return false;
      }
   }
}
