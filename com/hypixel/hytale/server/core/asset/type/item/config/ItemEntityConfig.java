package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import javax.annotation.Nonnull;

public class ItemEntityConfig implements NetworkSerializable<com.hypixel.hytale.protocol.ItemEntityConfig> {
   public static final String DEFAULT_PARTICLE_SYSTEM_ID = "Item";
   public static final ItemEntityConfig DEFAULT = new ItemEntityConfig("Item", null, true);
   public static final ItemEntityConfig DEFAULT_BLOCK = new ItemEntityConfig(null, null, true);
   public static final BuilderCodec<ItemEntityConfig> CODEC = BuilderCodec.builder(ItemEntityConfig.class, ItemEntityConfig::new)
      .appendInherited(
         new KeyedCodec<>("Physics", PhysicsValues.CODEC),
         (itemEntityConfig, physicsValues) -> itemEntityConfig.physicsValues = physicsValues,
         itemEntityConfig -> itemEntityConfig.physicsValues,
         (o, p) -> o.physicsValues = p.physicsValues
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("PickupRadius", Codec.FLOAT),
         (itemEntityConfig, box) -> itemEntityConfig.pickupRadius = box,
         itemEntityConfig -> itemEntityConfig.pickupRadius,
         (o, p) -> o.pickupRadius = p.pickupRadius
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Lifetime", Codec.FLOAT),
         (itemEntityConfig, v) -> itemEntityConfig.ttl = v,
         itemEntityConfig -> itemEntityConfig.ttl,
         (o, p) -> o.ttl = p.ttl
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("ParticleSystemId", Codec.STRING),
         (o, i) -> o.particleSystemId = i,
         o -> o.particleSystemId,
         (o, p) -> o.particleSystemId = p.particleSystemId
      )
      .addValidator(ParticleSystem.VALIDATOR_CACHE.getValidator())
      .add()
      .appendInherited(
         new KeyedCodec<>("ParticleColor", ProtocolCodecs.COLOR),
         (o, i) -> o.particleColor = i,
         o -> o.particleColor,
         (o, p) -> o.particleColor = p.particleColor
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ShowItemParticles", Codec.BOOLEAN),
         (o, i) -> o.showItemParticles = i,
         o -> o.showItemParticles,
         (o, p) -> o.showItemParticles = p.showItemParticles
      )
      .add()
      .build();
   protected PhysicsValues physicsValues = new PhysicsValues(5.0, 0.5, false);
   protected float pickupRadius = 1.75F;
   protected Float ttl;
   protected String particleSystemId = "Item";
   protected Color particleColor;
   protected boolean showItemParticles = true;

   public ItemEntityConfig() {
   }

   public ItemEntityConfig(String particleSystemId, Color particleColor, boolean showItemParticles) {
      this.particleSystemId = particleSystemId;
      this.particleColor = particleColor;
      this.showItemParticles = showItemParticles;
   }

   public PhysicsValues getPhysicsValues() {
      return this.physicsValues;
   }

   public float getPickupRadius() {
      return this.pickupRadius;
   }

   public Float getTtl() {
      return this.ttl;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ItemEntityConfig toPacket() {
      com.hypixel.hytale.protocol.ItemEntityConfig packet = new com.hypixel.hytale.protocol.ItemEntityConfig();
      packet.particleSystemId = this.particleSystemId;
      packet.particleColor = this.particleColor;
      packet.showItemParticles = this.showItemParticles;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemEntityConfig{physicsValues=" + this.physicsValues + ", pickupRadius=" + this.pickupRadius + ", ttl=" + this.ttl + "}";
   }
}
