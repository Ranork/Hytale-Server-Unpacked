package com.hypixel.hytale.server.core.asset.type.ambiencefx.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.server.core.asset.type.physicalmaterial.config.PhysicalMaterial;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class AmbienceFXPhysicalMaterial implements NetworkSerializable<com.hypixel.hytale.protocol.AmbienceFXPhysicalMaterial> {
   public static final BuilderCodec<AmbienceFXPhysicalMaterial> CODEC = BuilderCodec.builder(AmbienceFXPhysicalMaterial.class, AmbienceFXPhysicalMaterial::new)
      .append(
         new KeyedCodec<>("PhysicalMaterialId", Codec.STRING),
         (ambienceFXPhysicalMaterial, s) -> ambienceFXPhysicalMaterial.physicalMaterialId = s,
         ambienceFXPhysicalMaterial -> ambienceFXPhysicalMaterial.physicalMaterialId
      )
      .addValidator(Validators.nonNull())
      .addValidator(PhysicalMaterial.VALIDATOR_CACHE.getValidator())
      .add()
      .<Rangef>append(
         new KeyedCodec<>("Percent", ProtocolCodecs.RANGEF),
         (ambienceFXPhysicalMaterial, o) -> ambienceFXPhysicalMaterial.percent = o,
         ambienceFXPhysicalMaterial -> ambienceFXPhysicalMaterial.percent
      )
      .addValidator(Validators.nonNull())
      .add()
      .afterDecode(AmbienceFXPhysicalMaterial::processConfig)
      .build();
   public static final Rangef DEFAULT_PERCENT = new Rangef(0.0F, 0.0F);
   protected String physicalMaterialId;
   protected transient int physicalMaterialIndex;
   protected Rangef percent = DEFAULT_PERCENT;

   public AmbienceFXPhysicalMaterial(String physicalMaterialId, Rangef percent) {
      this.physicalMaterialId = physicalMaterialId;
      this.percent = percent;
   }

   protected AmbienceFXPhysicalMaterial() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.AmbienceFXPhysicalMaterial toPacket() {
      com.hypixel.hytale.protocol.AmbienceFXPhysicalMaterial packet = new com.hypixel.hytale.protocol.AmbienceFXPhysicalMaterial();
      packet.physicalMaterialIndex = this.physicalMaterialIndex;
      packet.percent = this.percent;
      return packet;
   }

   public String getPhysicalMaterialId() {
      return this.physicalMaterialId;
   }

   public Rangef getPercent() {
      return this.percent;
   }

   protected void processConfig() {
      this.physicalMaterialIndex = PhysicalMaterial.getAssetMap().getIndex(this.physicalMaterialId);
   }

   @Nonnull
   @Override
   public String toString() {
      return "AmbienceFXPhysicalMaterial{physicalMaterialId='"
         + this.physicalMaterialId
         + "', physicalMaterialIndex="
         + this.physicalMaterialIndex
         + ", percent="
         + this.percent
         + "}";
   }
}
