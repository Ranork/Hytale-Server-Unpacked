package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ServerCameraSettings {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 154;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 154;
   public static final int MAX_SIZE = 154;
   public float positionLerpSpeed = 1.0F;
   public float rotationLerpSpeed = 1.0F;
   public float distance;
   public float speedModifier = 1.0F;
   public boolean allowPitchControls;
   public boolean displayCursor;
   public boolean displayReticle;
   @Nonnull
   public MouseInputTargetType mouseInputTargetType = MouseInputTargetType.Any;
   public boolean sendMouseMotion;
   public boolean skipCharacterPhysics;
   public boolean isFirstPerson = true;
   @Nonnull
   public MovementForceRotationType movementForceRotationType = MovementForceRotationType.AttachedToHead;
   @Nullable
   public Direction movementForceRotation;
   @Nonnull
   public AttachedToType attachedToType = AttachedToType.LocalPlayer;
   public int attachedToEntityId;
   public boolean eyeOffset;
   @Nonnull
   public PositionDistanceOffsetType positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset;
   @Nullable
   public Position positionOffset;
   @Nullable
   public Direction rotationOffset;
   @Nonnull
   public PositionType positionType = PositionType.AttachedToPlusOffset;
   @Nullable
   public Position position;
   @Nonnull
   public RotationType rotationType = RotationType.AttachedToPlusOffset;
   @Nullable
   public Direction rotation;
   @Nonnull
   public CanMoveType canMoveType = CanMoveType.AttachedToLocalPlayer;
   @Nonnull
   public ApplyMovementType applyMovementType = ApplyMovementType.CharacterController;
   @Nullable
   public Vector3f movementMultiplier;
   @Nonnull
   public ApplyLookType applyLookType = ApplyLookType.LocalPlayerLookOrientation;
   @Nullable
   public Vector2f lookMultiplier;
   @Nonnull
   public MouseInputType mouseInputType = MouseInputType.LookAtTarget;
   @Nullable
   public Vector3f planeNormal;

   public ServerCameraSettings() {
   }

   public ServerCameraSettings(
      float positionLerpSpeed,
      float rotationLerpSpeed,
      float distance,
      float speedModifier,
      boolean allowPitchControls,
      boolean displayCursor,
      boolean displayReticle,
      @Nonnull MouseInputTargetType mouseInputTargetType,
      boolean sendMouseMotion,
      boolean skipCharacterPhysics,
      boolean isFirstPerson,
      @Nonnull MovementForceRotationType movementForceRotationType,
      @Nullable Direction movementForceRotation,
      @Nonnull AttachedToType attachedToType,
      int attachedToEntityId,
      boolean eyeOffset,
      @Nonnull PositionDistanceOffsetType positionDistanceOffsetType,
      @Nullable Position positionOffset,
      @Nullable Direction rotationOffset,
      @Nonnull PositionType positionType,
      @Nullable Position position,
      @Nonnull RotationType rotationType,
      @Nullable Direction rotation,
      @Nonnull CanMoveType canMoveType,
      @Nonnull ApplyMovementType applyMovementType,
      @Nullable Vector3f movementMultiplier,
      @Nonnull ApplyLookType applyLookType,
      @Nullable Vector2f lookMultiplier,
      @Nonnull MouseInputType mouseInputType,
      @Nullable Vector3f planeNormal
   ) {
      this.positionLerpSpeed = positionLerpSpeed;
      this.rotationLerpSpeed = rotationLerpSpeed;
      this.distance = distance;
      this.speedModifier = speedModifier;
      this.allowPitchControls = allowPitchControls;
      this.displayCursor = displayCursor;
      this.displayReticle = displayReticle;
      this.mouseInputTargetType = mouseInputTargetType;
      this.sendMouseMotion = sendMouseMotion;
      this.skipCharacterPhysics = skipCharacterPhysics;
      this.isFirstPerson = isFirstPerson;
      this.movementForceRotationType = movementForceRotationType;
      this.movementForceRotation = movementForceRotation;
      this.attachedToType = attachedToType;
      this.attachedToEntityId = attachedToEntityId;
      this.eyeOffset = eyeOffset;
      this.positionDistanceOffsetType = positionDistanceOffsetType;
      this.positionOffset = positionOffset;
      this.rotationOffset = rotationOffset;
      this.positionType = positionType;
      this.position = position;
      this.rotationType = rotationType;
      this.rotation = rotation;
      this.canMoveType = canMoveType;
      this.applyMovementType = applyMovementType;
      this.movementMultiplier = movementMultiplier;
      this.applyLookType = applyLookType;
      this.lookMultiplier = lookMultiplier;
      this.mouseInputType = mouseInputType;
      this.planeNormal = planeNormal;
   }

   public ServerCameraSettings(@Nonnull ServerCameraSettings other) {
      this.positionLerpSpeed = other.positionLerpSpeed;
      this.rotationLerpSpeed = other.rotationLerpSpeed;
      this.distance = other.distance;
      this.speedModifier = other.speedModifier;
      this.allowPitchControls = other.allowPitchControls;
      this.displayCursor = other.displayCursor;
      this.displayReticle = other.displayReticle;
      this.mouseInputTargetType = other.mouseInputTargetType;
      this.sendMouseMotion = other.sendMouseMotion;
      this.skipCharacterPhysics = other.skipCharacterPhysics;
      this.isFirstPerson = other.isFirstPerson;
      this.movementForceRotationType = other.movementForceRotationType;
      this.movementForceRotation = other.movementForceRotation;
      this.attachedToType = other.attachedToType;
      this.attachedToEntityId = other.attachedToEntityId;
      this.eyeOffset = other.eyeOffset;
      this.positionDistanceOffsetType = other.positionDistanceOffsetType;
      this.positionOffset = other.positionOffset;
      this.rotationOffset = other.rotationOffset;
      this.positionType = other.positionType;
      this.position = other.position;
      this.rotationType = other.rotationType;
      this.rotation = other.rotation;
      this.canMoveType = other.canMoveType;
      this.applyMovementType = other.applyMovementType;
      this.movementMultiplier = other.movementMultiplier;
      this.applyLookType = other.applyLookType;
      this.lookMultiplier = other.lookMultiplier;
      this.mouseInputType = other.mouseInputType;
      this.planeNormal = other.planeNormal;
   }

   @Nonnull
   public static ServerCameraSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      ServerCameraSettings obj = new ServerCameraSettings();
      byte nullBits = buf.getByte(offset);
      obj.positionLerpSpeed = buf.getFloatLE(offset + 1);
      obj.rotationLerpSpeed = buf.getFloatLE(offset + 5);
      obj.distance = buf.getFloatLE(offset + 9);
      obj.speedModifier = buf.getFloatLE(offset + 13);
      obj.allowPitchControls = buf.getByte(offset + 17) != 0;
      obj.displayCursor = buf.getByte(offset + 18) != 0;
      obj.displayReticle = buf.getByte(offset + 19) != 0;
      obj.mouseInputTargetType = MouseInputTargetType.fromValue(buf.getByte(offset + 20));
      obj.sendMouseMotion = buf.getByte(offset + 21) != 0;
      obj.skipCharacterPhysics = buf.getByte(offset + 22) != 0;
      obj.isFirstPerson = buf.getByte(offset + 23) != 0;
      obj.movementForceRotationType = MovementForceRotationType.fromValue(buf.getByte(offset + 24));
      if ((nullBits & 1) != 0) {
         obj.movementForceRotation = Direction.deserialize(buf, offset + 25);
      }

      obj.attachedToType = AttachedToType.fromValue(buf.getByte(offset + 37));
      obj.attachedToEntityId = buf.getIntLE(offset + 38);
      obj.eyeOffset = buf.getByte(offset + 42) != 0;
      obj.positionDistanceOffsetType = PositionDistanceOffsetType.fromValue(buf.getByte(offset + 43));
      if ((nullBits & 2) != 0) {
         obj.positionOffset = Position.deserialize(buf, offset + 44);
      }

      if ((nullBits & 4) != 0) {
         obj.rotationOffset = Direction.deserialize(buf, offset + 68);
      }

      obj.positionType = PositionType.fromValue(buf.getByte(offset + 80));
      if ((nullBits & 8) != 0) {
         obj.position = Position.deserialize(buf, offset + 81);
      }

      obj.rotationType = RotationType.fromValue(buf.getByte(offset + 105));
      if ((nullBits & 16) != 0) {
         obj.rotation = Direction.deserialize(buf, offset + 106);
      }

      obj.canMoveType = CanMoveType.fromValue(buf.getByte(offset + 118));
      obj.applyMovementType = ApplyMovementType.fromValue(buf.getByte(offset + 119));
      if ((nullBits & 32) != 0) {
         obj.movementMultiplier = Vector3f.deserialize(buf, offset + 120);
      }

      obj.applyLookType = ApplyLookType.fromValue(buf.getByte(offset + 132));
      if ((nullBits & 64) != 0) {
         obj.lookMultiplier = Vector2f.deserialize(buf, offset + 133);
      }

      obj.mouseInputType = MouseInputType.fromValue(buf.getByte(offset + 141));
      if ((nullBits & 128) != 0) {
         obj.planeNormal = Vector3f.deserialize(buf, offset + 142);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 154;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.movementForceRotation != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.positionOffset != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.rotationOffset != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.position != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.movementMultiplier != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.lookMultiplier != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.planeNormal != null) {
         nullBits = (byte)(nullBits | 128);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.positionLerpSpeed);
      buf.writeFloatLE(this.rotationLerpSpeed);
      buf.writeFloatLE(this.distance);
      buf.writeFloatLE(this.speedModifier);
      buf.writeByte(this.allowPitchControls ? 1 : 0);
      buf.writeByte(this.displayCursor ? 1 : 0);
      buf.writeByte(this.displayReticle ? 1 : 0);
      buf.writeByte(this.mouseInputTargetType.getValue());
      buf.writeByte(this.sendMouseMotion ? 1 : 0);
      buf.writeByte(this.skipCharacterPhysics ? 1 : 0);
      buf.writeByte(this.isFirstPerson ? 1 : 0);
      buf.writeByte(this.movementForceRotationType.getValue());
      if (this.movementForceRotation != null) {
         this.movementForceRotation.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeByte(this.attachedToType.getValue());
      buf.writeIntLE(this.attachedToEntityId);
      buf.writeByte(this.eyeOffset ? 1 : 0);
      buf.writeByte(this.positionDistanceOffsetType.getValue());
      if (this.positionOffset != null) {
         this.positionOffset.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      if (this.rotationOffset != null) {
         this.rotationOffset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeByte(this.positionType.getValue());
      if (this.position != null) {
         this.position.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      buf.writeByte(this.rotationType.getValue());
      if (this.rotation != null) {
         this.rotation.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeByte(this.canMoveType.getValue());
      buf.writeByte(this.applyMovementType.getValue());
      if (this.movementMultiplier != null) {
         this.movementMultiplier.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeByte(this.applyLookType.getValue());
      if (this.lookMultiplier != null) {
         this.lookMultiplier.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      buf.writeByte(this.mouseInputType.getValue());
      if (this.planeNormal != null) {
         this.planeNormal.serialize(buf);
      } else {
         buf.writeZero(12);
      }
   }

   public int computeSize() {
      return 154;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 154 ? ValidationResult.error("Buffer too small: expected at least 154 bytes") : ValidationResult.OK;
   }

   public ServerCameraSettings clone() {
      ServerCameraSettings copy = new ServerCameraSettings();
      copy.positionLerpSpeed = this.positionLerpSpeed;
      copy.rotationLerpSpeed = this.rotationLerpSpeed;
      copy.distance = this.distance;
      copy.speedModifier = this.speedModifier;
      copy.allowPitchControls = this.allowPitchControls;
      copy.displayCursor = this.displayCursor;
      copy.displayReticle = this.displayReticle;
      copy.mouseInputTargetType = this.mouseInputTargetType;
      copy.sendMouseMotion = this.sendMouseMotion;
      copy.skipCharacterPhysics = this.skipCharacterPhysics;
      copy.isFirstPerson = this.isFirstPerson;
      copy.movementForceRotationType = this.movementForceRotationType;
      copy.movementForceRotation = this.movementForceRotation != null ? this.movementForceRotation.clone() : null;
      copy.attachedToType = this.attachedToType;
      copy.attachedToEntityId = this.attachedToEntityId;
      copy.eyeOffset = this.eyeOffset;
      copy.positionDistanceOffsetType = this.positionDistanceOffsetType;
      copy.positionOffset = this.positionOffset != null ? this.positionOffset.clone() : null;
      copy.rotationOffset = this.rotationOffset != null ? this.rotationOffset.clone() : null;
      copy.positionType = this.positionType;
      copy.position = this.position != null ? this.position.clone() : null;
      copy.rotationType = this.rotationType;
      copy.rotation = this.rotation != null ? this.rotation.clone() : null;
      copy.canMoveType = this.canMoveType;
      copy.applyMovementType = this.applyMovementType;
      copy.movementMultiplier = this.movementMultiplier != null ? this.movementMultiplier.clone() : null;
      copy.applyLookType = this.applyLookType;
      copy.lookMultiplier = this.lookMultiplier != null ? this.lookMultiplier.clone() : null;
      copy.mouseInputType = this.mouseInputType;
      copy.planeNormal = this.planeNormal != null ? this.planeNormal.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ServerCameraSettings other)
            ? false
            : this.positionLerpSpeed == other.positionLerpSpeed
               && this.rotationLerpSpeed == other.rotationLerpSpeed
               && this.distance == other.distance
               && this.speedModifier == other.speedModifier
               && this.allowPitchControls == other.allowPitchControls
               && this.displayCursor == other.displayCursor
               && this.displayReticle == other.displayReticle
               && Objects.equals(this.mouseInputTargetType, other.mouseInputTargetType)
               && this.sendMouseMotion == other.sendMouseMotion
               && this.skipCharacterPhysics == other.skipCharacterPhysics
               && this.isFirstPerson == other.isFirstPerson
               && Objects.equals(this.movementForceRotationType, other.movementForceRotationType)
               && Objects.equals(this.movementForceRotation, other.movementForceRotation)
               && Objects.equals(this.attachedToType, other.attachedToType)
               && this.attachedToEntityId == other.attachedToEntityId
               && this.eyeOffset == other.eyeOffset
               && Objects.equals(this.positionDistanceOffsetType, other.positionDistanceOffsetType)
               && Objects.equals(this.positionOffset, other.positionOffset)
               && Objects.equals(this.rotationOffset, other.rotationOffset)
               && Objects.equals(this.positionType, other.positionType)
               && Objects.equals(this.position, other.position)
               && Objects.equals(this.rotationType, other.rotationType)
               && Objects.equals(this.rotation, other.rotation)
               && Objects.equals(this.canMoveType, other.canMoveType)
               && Objects.equals(this.applyMovementType, other.applyMovementType)
               && Objects.equals(this.movementMultiplier, other.movementMultiplier)
               && Objects.equals(this.applyLookType, other.applyLookType)
               && Objects.equals(this.lookMultiplier, other.lookMultiplier)
               && Objects.equals(this.mouseInputType, other.mouseInputType)
               && Objects.equals(this.planeNormal, other.planeNormal);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.positionLerpSpeed,
         this.rotationLerpSpeed,
         this.distance,
         this.speedModifier,
         this.allowPitchControls,
         this.displayCursor,
         this.displayReticle,
         this.mouseInputTargetType,
         this.sendMouseMotion,
         this.skipCharacterPhysics,
         this.isFirstPerson,
         this.movementForceRotationType,
         this.movementForceRotation,
         this.attachedToType,
         this.attachedToEntityId,
         this.eyeOffset,
         this.positionDistanceOffsetType,
         this.positionOffset,
         this.rotationOffset,
         this.positionType,
         this.position,
         this.rotationType,
         this.rotation,
         this.canMoveType,
         this.applyMovementType,
         this.movementMultiplier,
         this.applyLookType,
         this.lookMultiplier,
         this.mouseInputType,
         this.planeNormal
      );
   }
}
