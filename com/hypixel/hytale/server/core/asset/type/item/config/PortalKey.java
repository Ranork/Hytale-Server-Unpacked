package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.portalworld.PortalType;

public class PortalKey {
   public static final BuilderCodec<PortalKey> CODEC = BuilderCodec.builder(PortalKey.class, PortalKey::new)
      .appendInherited(
         new KeyedCodec<>("PortalType", Codec.STRING),
         (portalKey, o) -> portalKey.portalTypeId = o,
         portalKey -> portalKey.portalTypeId,
         (portalKey, parent) -> portalKey.portalTypeId = parent.portalTypeId
      )
      .documentation("The ID of of the PortalType that this key opens.")
      .addValidator(Validators.nonNull())
      .addValidator(PortalType.VALIDATOR_CACHE.getValidator())
      .add()
      .appendInherited(
         new KeyedCodec<>("TimeLimitSeconds", Codec.INTEGER),
         (portalKey, o) -> portalKey.timeLimitSeconds = o,
         portalKey -> portalKey.timeLimitSeconds,
         (portalKey, parent) -> portalKey.timeLimitSeconds = parent.timeLimitSeconds
      )
      .add()
      .build();
   private String portalTypeId;
   private int timeLimitSeconds = -1;

   public String getPortalTypeId() {
      return this.portalTypeId;
   }

   public int getTimeLimitSeconds() {
      return this.timeLimitSeconds;
   }

   @Override
   public String toString() {
      return "PortalKey{instanceId='" + this.portalTypeId + "', timeLimitSeconds=" + this.timeLimitSeconds + "}";
   }
}
