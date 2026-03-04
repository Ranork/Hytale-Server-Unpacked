package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.io.NetworkSerializable;

public class ItemGlider implements NetworkSerializable<com.hypixel.hytale.protocol.ItemGlider> {
   public static final BuilderCodec<ItemGlider> CODEC = BuilderCodec.builder(ItemGlider.class, ItemGlider::new)
      .appendInherited(
         new KeyedCodec<>("TerminalVelocity", Codec.FLOAT),
         (o, i) -> o.terminalVelocity = i,
         o -> o.terminalVelocity,
         (o, p) -> o.terminalVelocity = p.terminalVelocity
      )
      .documentation("The maximum speed the player can fall while gliding.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("FallSpeedMultiplier", Codec.FLOAT),
         (o, i) -> o.fallSpeedMultiplier = i,
         o -> o.fallSpeedMultiplier,
         (o, p) -> o.fallSpeedMultiplier = p.fallSpeedMultiplier
      )
      .documentation("The rate at which the fall speed is incremented.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("HorizontalSpeedMultiplier", Codec.FLOAT),
         (o, i) -> o.horizontalSpeedMultiplier = i,
         o -> o.horizontalSpeedMultiplier,
         (o, p) -> o.horizontalSpeedMultiplier = p.horizontalSpeedMultiplier
      )
      .documentation("The rate at which the horizontal move speed is incremented.")
      .add()
      .<Float>appendInherited(new KeyedCodec<>("Speed", Codec.FLOAT), (o, i) -> o.speed = i, o -> o.speed, (o, p) -> o.speed = p.speed)
      .documentation("The horizontal movement speed of the glider.")
      .add()
      .build();
   protected float terminalVelocity;
   protected float fallSpeedMultiplier;
   protected float horizontalSpeedMultiplier;
   protected float speed;

   public float getTerminalVelocity() {
      return this.terminalVelocity;
   }

   public float getFallSpeedMultiplier() {
      return this.fallSpeedMultiplier;
   }

   public float getHorizontalSpeedMultiplier() {
      return this.horizontalSpeedMultiplier;
   }

   public float getSpeed() {
      return this.speed;
   }

   public com.hypixel.hytale.protocol.ItemGlider toPacket() {
      com.hypixel.hytale.protocol.ItemGlider packet = new com.hypixel.hytale.protocol.ItemGlider();
      packet.terminalVelocity = this.terminalVelocity;
      packet.fallSpeedMultiplier = this.fallSpeedMultiplier;
      packet.horizontalSpeedMultiplier = this.horizontalSpeedMultiplier;
      packet.speed = this.speed;
      return packet;
   }
}
