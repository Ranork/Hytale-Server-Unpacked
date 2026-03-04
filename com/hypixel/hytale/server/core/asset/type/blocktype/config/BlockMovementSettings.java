package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class BlockMovementSettings implements NetworkSerializable<com.hypixel.hytale.protocol.BlockMovementSettings> {
   public static final BuilderCodec<BlockMovementSettings> CODEC = BuilderCodec.builder(BlockMovementSettings.class, BlockMovementSettings::new)
      .append(
         new KeyedCodec<>("IsClimbable", Codec.BOOLEAN),
         (blockMovementSettings, o) -> blockMovementSettings.isClimbable = o,
         blockMovementSettings -> blockMovementSettings.isClimbable
      )
      .add()
      .append(
         new KeyedCodec<>("IsBouncy", Codec.BOOLEAN),
         (blockMovementSettings, o) -> blockMovementSettings.isBouncy = o,
         blockMovementSettings -> blockMovementSettings.isBouncy
      )
      .add()
      .append(
         new KeyedCodec<>("BounceVelocity", Codec.FLOAT),
         (blockMovementSettings, o) -> blockMovementSettings.bounceVelocity = o,
         blockMovementSettings -> blockMovementSettings.bounceVelocity
      )
      .add()
      .append(
         new KeyedCodec<>("ClimbUpSpeedMultiplier", Codec.FLOAT),
         (blockMovementSettings, o) -> blockMovementSettings.climbUpSpeedMultiplier = o,
         blockMovementSettings -> blockMovementSettings.climbUpSpeedMultiplier
      )
      .add()
      .append(
         new KeyedCodec<>("ClimbDownSpeedMultiplier", Codec.FLOAT),
         (blockMovementSettings, o) -> blockMovementSettings.climbDownSpeedMultiplier = o,
         blockMovementSettings -> blockMovementSettings.climbDownSpeedMultiplier
      )
      .add()
      .append(
         new KeyedCodec<>("ClimbLateralSpeedMultiplier", Codec.FLOAT),
         (blockMovementSettings, o) -> blockMovementSettings.climbLateralSpeedMultiplier = o,
         blockMovementSettings -> blockMovementSettings.climbLateralSpeedMultiplier
      )
      .add()
      .append(
         new KeyedCodec<>("Drag", Codec.FLOAT),
         (blockMovementSettings, o) -> blockMovementSettings.drag = o,
         blockMovementSettings -> blockMovementSettings.drag
      )
      .add()
      .append(
         new KeyedCodec<>("Friction", Codec.FLOAT),
         (blockMovementSettings, o) -> blockMovementSettings.friction = o,
         blockMovementSettings -> blockMovementSettings.friction
      )
      .add()
      .append(
         new KeyedCodec<>("TerminalVelocityModifier", Codec.FLOAT),
         (blockMovementSettings, o) -> blockMovementSettings.terminalVelocityModifier = o,
         blockMovementSettings -> blockMovementSettings.terminalVelocityModifier
      )
      .add()
      .append(
         new KeyedCodec<>("HorizontalSpeedMultiplier", Codec.FLOAT),
         (blockMovementSettings, o) -> blockMovementSettings.horizontalSpeedMultiplier = o,
         blockMovementSettings -> blockMovementSettings.horizontalSpeedMultiplier
      )
      .add()
      .append(
         new KeyedCodec<>("JumpForceMultiplier", Codec.FLOAT),
         (blockMovementSettings, o) -> blockMovementSettings.jumpForceMultiplier = o,
         blockMovementSettings -> blockMovementSettings.jumpForceMultiplier
      )
      .add()
      .build();
   private boolean isClimbable;
   private boolean isBouncy;
   private float bounceVelocity;
   private float drag = 0.82F;
   private float friction = 0.18F;
   private float climbUpSpeedMultiplier = 1.0F;
   private float climbDownSpeedMultiplier = 1.0F;
   private float climbLateralSpeedMultiplier = 1.0F;
   private float terminalVelocityModifier = 1.0F;
   private float horizontalSpeedMultiplier = 1.0F;
   private float jumpForceMultiplier = 1.0F;

   public BlockMovementSettings(
      boolean isClimbable,
      boolean isBouncy,
      float bounceVelocity,
      float drag,
      float friction,
      float climbUpSpeed,
      float climbDownSpeed,
      float climbLateralSpeedMultiplier,
      float terminalVelocityModifier,
      float horizontalSpeedMultiplier,
      float jumpForceMultiplier
   ) {
      this.isClimbable = isClimbable;
      this.isBouncy = isBouncy;
      this.bounceVelocity = bounceVelocity;
      this.climbUpSpeedMultiplier = climbUpSpeed;
      this.climbDownSpeedMultiplier = climbDownSpeed;
      this.climbLateralSpeedMultiplier = climbLateralSpeedMultiplier;
      this.drag = drag;
      this.friction = friction;
      this.terminalVelocityModifier = terminalVelocityModifier;
      this.horizontalSpeedMultiplier = horizontalSpeedMultiplier;
      this.jumpForceMultiplier = jumpForceMultiplier;
   }

   protected BlockMovementSettings() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.BlockMovementSettings toPacket() {
      com.hypixel.hytale.protocol.BlockMovementSettings packet = new com.hypixel.hytale.protocol.BlockMovementSettings();
      packet.isClimbable = this.isClimbable;
      packet.isBouncy = this.isBouncy;
      packet.bounceVelocity = this.bounceVelocity;
      packet.climbUpSpeedMultiplier = this.climbUpSpeedMultiplier;
      packet.climbDownSpeedMultiplier = this.climbDownSpeedMultiplier;
      packet.climbLateralSpeedMultiplier = this.climbLateralSpeedMultiplier;
      packet.drag = this.drag;
      packet.friction = this.friction;
      packet.terminalVelocityModifier = this.terminalVelocityModifier;
      packet.horizontalSpeedMultiplier = this.horizontalSpeedMultiplier;
      packet.jumpForceMultiplier = this.jumpForceMultiplier;
      return packet;
   }

   public boolean isClimbable() {
      return this.isClimbable;
   }

   public boolean isBouncy() {
      return this.isBouncy;
   }

   public float getBounceVelocity() {
      return this.bounceVelocity;
   }

   public float getDrag() {
      return this.drag;
   }

   public float getFriction() {
      return this.friction;
   }

   public float getClimbUpSpeedMultiplier() {
      return this.climbUpSpeedMultiplier;
   }

   public float getClimbDownSpeedMultiplier() {
      return this.climbDownSpeedMultiplier;
   }

   public float getClimbLateralSpeedMultiplier() {
      return this.climbLateralSpeedMultiplier;
   }

   public float getTerminalVelocityModifier() {
      return this.terminalVelocityModifier;
   }

   public float getHorizontalSpeedMultiplier() {
      return this.horizontalSpeedMultiplier;
   }

   public float jumpForceMultiplier() {
      return this.jumpForceMultiplier;
   }

   @Nonnull
   @Override
   public String toString() {
      return "BlockMovementSettings{isClimbable="
         + this.isClimbable
         + "isBouncy="
         + this.isBouncy
         + "bounceSpeed="
         + this.bounceVelocity
         + ", climbUpSpeedMultiplier="
         + this.climbUpSpeedMultiplier
         + ", climbDownSpeedMultiplier="
         + this.climbDownSpeedMultiplier
         + ", climbLateralSpeedMultiplier="
         + this.climbLateralSpeedMultiplier
         + ", drag="
         + this.drag
         + ", friction="
         + this.friction
         + ", terminalVelocityModifier="
         + this.terminalVelocityModifier
         + ", horizontalSpeedMultiplier="
         + this.horizontalSpeedMultiplier
         + ", jumpForceMultiplier="
         + this.jumpForceMultiplier
         + "}";
   }
}
