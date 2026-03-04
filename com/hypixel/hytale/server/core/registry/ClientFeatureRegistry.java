package com.hypixel.hytale.server.core.registry;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.function.consumer.BooleanConsumer;
import com.hypixel.hytale.protocol.packets.setup.ClientFeature;
import com.hypixel.hytale.protocol.packets.setup.ServerTags;
import com.hypixel.hytale.registry.Registry;
import com.hypixel.hytale.server.core.client.ClientFeatureHandler;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.universe.Universe;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;

public class ClientFeatureRegistry extends Registry<ClientFeatureRegistration> {
   public ClientFeatureRegistry(@Nonnull List<BooleanConsumer> registrations, BooleanSupplier precondition, String preconditionMessage, PluginBase plugin) {
      super(registrations, precondition, preconditionMessage, ClientFeatureRegistration::new);
   }

   public ClientFeatureRegistration register(ClientFeature feature) {
      ClientFeatureHandler.register(feature);
      return super.register(new ClientFeatureRegistration(feature));
   }

   public void registerClientTag(@Nonnull String tag) {
      if (AssetRegistry.registerClientTag(tag)) {
         ServerTags packet = new ServerTags(AssetRegistry.getClientTags());
         Universe.get().broadcastPacketNoCache(packet);
      }
   }
}
