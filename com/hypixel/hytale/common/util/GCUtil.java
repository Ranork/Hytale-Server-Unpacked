package com.hypixel.hytale.common.util;

import com.sun.management.GarbageCollectionNotificationInfo;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.management.NotificationEmitter;
import javax.management.openmbean.CompositeData;

public class GCUtil {
   public static void register(@Nonnull Consumer<GarbageCollectionNotificationInfo> consumer) {
      for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
         NotificationEmitter emitter = (NotificationEmitter)gcBean;
         emitter.addNotificationListener((notification, handback) -> {
            if (notification.getType().equals("com.sun.management.gc.notification")) {
               GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData)notification.getUserData());
               consumer.accept(info);
            }
         }, null, null);
      }
   }
}
