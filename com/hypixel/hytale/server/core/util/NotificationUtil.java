// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package com.hypixel.hytale.server.core.util;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.protocol.packets.interface_.Notification;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Iterator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NotificationUtil {
   public NotificationUtil() {
   }

   public static void sendNotificationToUniverse(@Nonnull Message message, @Nullable Message secondaryMessage, @Nullable String icon, @Nullable ItemWithAllMetadata item, @Nonnull NotificationStyle style) {
      Iterator var5 = Universe.get().getWorlds().values().iterator();

      while(var5.hasNext()) {
         World world = (World)var5.next();
         world.execute(() -> {
            Store<EntityStore> store = world.getEntityStore().getStore();
            sendNotificationToWorld(message, secondaryMessage, icon, item, style, store);
         });
      }

   }

   public static void sendNotificationToUniverse(@Nonnull String message) {
      sendNotificationToUniverse(Message.raw(message), (Message)null, (String)null, (ItemWithAllMetadata)null, NotificationStyle.Default);
   }

   public static void sendNotificationToUniverse(@Nonnull String message, @Nonnull String secondaryMessage) {
      sendNotificationToUniverse(Message.raw(message), Message.raw(secondaryMessage), (String)null, (ItemWithAllMetadata)null, NotificationStyle.Default);
   }

   public static void sendNotificationToUniverse(@Nonnull String message, @Nonnull NotificationStyle style) {
      sendNotificationToUniverse(Message.raw(message), (Message)null, (String)null, (ItemWithAllMetadata)null, style);
   }

   public static void sendNotificationToUniverse(@Nonnull Message message) {
      sendNotificationToUniverse(message, (Message)null, (String)null, (ItemWithAllMetadata)null, NotificationStyle.Default);
   }

   public static void sendNotificationToUniverse(@Nonnull Message message, @Nonnull NotificationStyle style) {
      sendNotificationToUniverse(message, (Message)null, (String)null, (ItemWithAllMetadata)null, style);
   }

   public static void sendNotificationToUniverse(@Nonnull Message message, @Nullable String icon, @Nonnull NotificationStyle style) {
      sendNotificationToUniverse(message, (Message)null, icon, (ItemWithAllMetadata)null, style);
   }

   public static void sendNotificationToUniverse(@Nonnull Message message, @Nullable ItemWithAllMetadata item, @Nonnull NotificationStyle style) {
      sendNotificationToUniverse(message, (Message)null, (String)null, item, style);
   }

   public static void sendNotificationToUniverse(@Nonnull Message message, @Nullable Message secondaryMessage, @Nullable String icon) {
      sendNotificationToUniverse(message, secondaryMessage, icon, (ItemWithAllMetadata)null, NotificationStyle.Default);
   }

   public static void sendNotificationToUniverse(@Nonnull Message message, @Nullable Message secondaryMessage, @Nullable ItemWithAllMetadata item) {
      sendNotificationToUniverse(message, secondaryMessage, (String)null, item, NotificationStyle.Default);
   }

   public static void sendNotificationToUniverse(@Nonnull Message message, @Nullable Message secondaryMessage, @Nonnull NotificationStyle style) {
      sendNotificationToUniverse(message, secondaryMessage, (String)null, (ItemWithAllMetadata)null, style);
   }

   public static void sendNotificationToUniverse(@Nonnull Message message, @Nullable Message secondaryMessage, @Nullable String icon, @Nonnull NotificationStyle style) {
      sendNotificationToUniverse(message, secondaryMessage, icon, (ItemWithAllMetadata)null, style);
   }

   public static void sendNotificationToUniverse(@Nonnull Message message, @Nullable Message secondaryMessage, @Nullable ItemWithAllMetadata item, @Nonnull NotificationStyle style) {
      sendNotificationToUniverse(message, secondaryMessage, (String)null, item, style);
   }

   public static void sendNotificationToWorld(@Nonnull Message message, @Nullable Message secondaryMessage, @Nullable String icon, @Nullable ItemWithAllMetadata item, @Nonnull NotificationStyle style, @Nonnull Store<EntityStore> store) {
      World world = ((EntityStore)store.getExternalData()).getWorld();
      Iterator var7 = world.getPlayerRefs().iterator();

      while(var7.hasNext()) {
         PlayerRef playerRefComponent = (PlayerRef)var7.next();
         sendNotification(playerRefComponent.getPacketHandler(), message, secondaryMessage, icon, item, style);
      }

   }

   public static void sendNotification(@Nonnull PacketHandler handler, @Nonnull Message message, @Nullable Message secondaryMessage, @Nullable String icon, @Nullable ItemWithAllMetadata item, @Nonnull NotificationStyle style) {
      Objects.requireNonNull(message, "Notification message can't be null!");
      Objects.requireNonNull(style, "Notification style can't be null!");
      handler.writeNoCache(new Notification(message.getFormattedMessage(), secondaryMessage != null ? secondaryMessage.getFormattedMessage() : null, icon, item, style));
   }

   public static void sendNotification(@Nonnull PacketHandler handler, @Nonnull String message) {
      sendNotification(handler, Message.raw(message), (Message)null, (String)null, (ItemWithAllMetadata)null, NotificationStyle.Default);
   }

   public static void sendNotification(@Nonnull PacketHandler handler, @Nonnull Message message, @Nonnull String icon) {
      sendNotification(handler, message, (Message)null, icon, (ItemWithAllMetadata)null, NotificationStyle.Default);
   }

   public static void sendNotification(@Nonnull PacketHandler handler, @Nonnull Message message, @Nonnull String icon, @Nonnull NotificationStyle notificationStyle) {
      sendNotification(handler, message, (Message)null, icon, (ItemWithAllMetadata)null, notificationStyle);
   }

   public static void sendNotification(@Nonnull PacketHandler handler, @Nonnull String message, @Nonnull NotificationStyle style) {
      sendNotification(handler, Message.raw(message), (Message)null, (String)null, (ItemWithAllMetadata)null, style);
   }

   public static void sendNotification(@Nonnull PacketHandler handler, @Nonnull Message message) {
      sendNotification(handler, message, (Message)null, (String)null, (ItemWithAllMetadata)null, NotificationStyle.Default);
   }

   public static void sendNotification(@Nonnull PacketHandler handler, Message message, NotificationStyle style) {
      sendNotification(handler, message, (Message)null, (String)null, (ItemWithAllMetadata)null, style);
   }

   public static void sendNotification(@Nonnull PacketHandler handler, @Nonnull String message, @Nonnull String secondaryMessage) {
      sendNotification(handler, Message.raw(message), Message.raw(secondaryMessage), (String)null, (ItemWithAllMetadata)null, NotificationStyle.Default);
   }

   public static void sendNotification(@Nonnull PacketHandler handler, @Nonnull Message message, @Nullable Message secondaryMessage, @Nullable String icon) {
      sendNotification(handler, message, secondaryMessage, icon, (ItemWithAllMetadata)null, NotificationStyle.Default);
   }

   public static void sendNotification(@Nonnull PacketHandler handler, Message message, Message secondaryMessage) {
      sendNotification(handler, message, secondaryMessage, (String)null, (ItemWithAllMetadata)null, NotificationStyle.Default);
   }

   public static void sendNotification(@Nonnull PacketHandler handler, Message message, Message secondaryMessage, ItemWithAllMetadata item) {
      sendNotification(handler, message, secondaryMessage, (String)null, item, NotificationStyle.Default);
   }

   public static void sendNotification(@Nonnull PacketHandler handler, Message message, Message secondaryMessage, @Nonnull NotificationStyle style) {
      sendNotification(handler, message, secondaryMessage, (String)null, (ItemWithAllMetadata)null, style);
   }

   public static void sendNotification(@Nonnull PacketHandler handler, Message message, Message secondaryMessage, String icon, @Nonnull NotificationStyle style) {
      sendNotification(handler, message, secondaryMessage, icon, (ItemWithAllMetadata)null, style);
   }

   public static void sendNotification(@Nonnull PacketHandler handler, Message message, Message secondaryMessage, ItemWithAllMetadata item, @Nonnull NotificationStyle style) {
      sendNotification(handler, message, secondaryMessage, (String)null, item, style);
   }
}
