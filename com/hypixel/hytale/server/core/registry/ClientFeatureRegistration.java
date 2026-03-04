package com.hypixel.hytale.server.core.registry;

import com.hypixel.hytale.protocol.packets.setup.ClientFeature;
import com.hypixel.hytale.registry.Registration;
import com.hypixel.hytale.server.core.client.ClientFeatureHandler;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;

public class ClientFeatureRegistration extends Registration {
   private final ClientFeature feature;

   public ClientFeatureRegistration(@Nonnull ClientFeatureRegistration registration, BooleanSupplier isEnabled, Runnable unregister) {
      this(registration.feature, isEnabled, unregister);
   }

   public ClientFeatureRegistration(ClientFeature feature) {
      super(() -> true, () -> ClientFeatureHandler.unregister(feature));
      this.feature = feature;
   }

   public ClientFeatureRegistration(ClientFeature feature, BooleanSupplier isEnabled, Runnable unregister) {
      super(isEnabled, unregister);
      this.feature = feature;
   }

   public ClientFeature getFeature() {
      return this.feature;
   }
}
