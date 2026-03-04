package com.hypixel.hytale.server.core.client;

import com.hypixel.hytale.protocol.packets.setup.ClientFeature;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import javax.annotation.Nonnull;

public class ClientFeatureHandler {
   public static void register(@Nonnull ClientFeature feature) {
      for (World world : Universe.get().getWorlds().values()) {
         world.registerFeature(feature, true);
      }
   }

   public static void unregister(@Nonnull ClientFeature feature) {
      for (World world : Universe.get().getWorlds().values()) {
         world.registerFeature(feature, false);
      }
   }
}
