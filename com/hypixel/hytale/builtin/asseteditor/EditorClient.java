package com.hypixel.hytale.builtin.asseteditor;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorPopupNotification;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorPopupNotificationType;
import com.hypixel.hytale.protocol.packets.asseteditor.FailureReply;
import com.hypixel.hytale.protocol.packets.asseteditor.SuccessReply;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.permissions.PermissionHolder;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EditorClient implements PermissionHolder {
   private String language;
   private final UUID uuid;
   private final String username;
   @Nullable
   private final PlayerAuthentication auth;
   private final PacketHandler packetHandler;

   public EditorClient(String language, @Nonnull PlayerAuthentication auth, PacketHandler packetHandler) {
      this.language = language;
      this.uuid = auth.getUuid();
      this.username = auth.getUsername();
      this.auth = auth;
      this.packetHandler = packetHandler;
   }

   public EditorClient(String language, UUID uuid, String username, PacketHandler packetHandler) {
      this.language = language;
      this.uuid = uuid;
      this.username = username;
      this.auth = null;
      this.packetHandler = packetHandler;
   }

   @Deprecated
   public EditorClient(@Nonnull PlayerRef playerRef) {
      this.language = playerRef.getLanguage();
      this.uuid = playerRef.getUuid();
      this.username = playerRef.getUsername();
      this.auth = null;
      this.packetHandler = playerRef.getPacketHandler();
   }

   public String getLanguage() {
      return this.language;
   }

   public void setLanguage(String language) {
      this.language = language;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public String getUsername() {
      return this.username;
   }

   @Nullable
   public PlayerAuthentication getAuth() {
      return this.auth;
   }

   public PacketHandler getPacketHandler() {
      return this.packetHandler;
   }

   @Nullable
   public PlayerRef tryGetPlayer() {
      return Universe.get().getPlayer(this.uuid);
   }

   @Override
   public boolean hasPermission(@Nonnull String id) {
      return PermissionsModule.get().hasPermission(this.uuid, id);
   }

   @Override
   public boolean hasPermission(@Nonnull String id, boolean def) {
      return PermissionsModule.get().hasPermission(this.uuid, id, def);
   }

   public void sendPopupNotification(AssetEditorPopupNotificationType type, @Nonnull Message message) {
      FormattedMessage msg = message.getFormattedMessage();
      this.getPacketHandler().write(new AssetEditorPopupNotification(type, msg));
   }

   public void sendSuccessReply(int token) {
      this.sendSuccessReply(token, null);
   }

   public void sendSuccessReply(int token, @Nullable Message message) {
      FormattedMessage msg = message != null ? message.getFormattedMessage() : null;
      this.getPacketHandler().write(new SuccessReply(token, msg));
   }

   public void sendFailureReply(int token, @Nonnull Message message) {
      FormattedMessage msg = message.getFormattedMessage();
      this.getPacketHandler().write(new FailureReply(token, msg));
   }
}
