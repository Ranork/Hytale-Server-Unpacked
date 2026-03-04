package com.hypixel.hytale.builtin.asseteditor.event;

import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import com.hypixel.hytale.server.core.io.PacketHandler;
import javax.annotation.Nonnull;

public class AssetEditorClientDisconnectEvent extends EditorClientEvent<Void> {
   private final PacketHandler.DisconnectReason disconnectReason;

   public AssetEditorClientDisconnectEvent(EditorClient editorClient, PacketHandler.DisconnectReason disconnectReason) {
      super(editorClient);
      this.disconnectReason = disconnectReason;
   }

   public PacketHandler.DisconnectReason getDisconnectReason() {
      return this.disconnectReason;
   }

   @Nonnull
   @Override
   public String toString() {
      return "AssetEditorClientDisconnectedEvent{disconnectReason=" + this.disconnectReason + "}" + super.toString();
   }
}
