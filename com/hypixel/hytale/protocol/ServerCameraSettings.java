package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector2fc;
import org.joml.Vector3fc;

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
   public Vector3fc movementMultiplier;
   @Nonnull
   public ApplyLookType applyLookType = ApplyLookType.LocalPlayerLookOrientation;
   @Nullable
   public Vector2fc lookMultiplier;
   @Nonnull
   public MouseInputType mouseInputType = MouseInputType.LookAtTarget;
   @Nullable
   public Vector3fc planeNormal;

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
      @Nullable Vector3fc movementMultiplier,
      @Nonnull ApplyLookType applyLookType,
      @Nullable Vector2fc lookMultiplier,
      @Nonnull MouseInputType mouseInputType,
      @Nullable Vector3fc planeNormal
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
      if (buf.readableBytes() - offset < 154) {
         throw ProtocolException.bufferTooSmall("ServerCameraSettings", 154, buf.readableBytes() - offset);
      } else {
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
            obj.movementMultiplier = PacketIO.readVector3f(buf, offset + 120);
         }

         obj.applyLookType = ApplyLookType.fromValue(buf.getByte(offset + 132));
         if ((nullBits & 64) != 0) {
            obj.lookMultiplier = PacketIO.readVector2f(buf, offset + 133);
         }

         obj.mouseInputType = MouseInputType.fromValue(buf.getByte(offset + 141));
         if ((nullBits & 128) != 0) {
            obj.planeNormal = PacketIO.readVector3f(buf, offset + 142);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 154;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 154L;
   }

   public static float getPositionLerpSpeed(MemorySegment mem) {
      return getPositionLerpSpeed(mem, 0);
   }

   public static float getPositionLerpSpeed(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static float getRotationLerpSpeed(MemorySegment mem) {
      return getRotationLerpSpeed(mem, 0);
   }

   public static float getRotationLerpSpeed(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   public static float getDistance(MemorySegment mem) {
      return getDistance(mem, 0);
   }

   public static float getDistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   public static float getSpeedModifier(MemorySegment mem) {
      return getSpeedModifier(mem, 0);
   }

   public static float getSpeedModifier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 13));
   }

   public static boolean getAllowPitchControls(MemorySegment mem) {
      return getAllowPitchControls(mem, 0);
   }

   public static boolean getAllowPitchControls(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 17));
   }

   public static boolean getDisplayCursor(MemorySegment mem) {
      return getDisplayCursor(mem, 0);
   }

   public static boolean getDisplayCursor(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 18));
   }

   public static boolean getDisplayReticle(MemorySegment mem) {
      return getDisplayReticle(mem, 0);
   }

   public static boolean getDisplayReticle(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 19));
   }

   public static MouseInputTargetType getMouseInputTargetType(MemorySegment mem) {
      return getMouseInputTargetType(mem, 0);
   }

   public static MouseInputTargetType getMouseInputTargetType(MemorySegment mem, int offset) {
      return MouseInputTargetType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 20)));
   }

   public static boolean getSendMouseMotion(MemorySegment mem) {
      return getSendMouseMotion(mem, 0);
   }

   public static boolean getSendMouseMotion(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 21));
   }

   public static boolean getSkipCharacterPhysics(MemorySegment mem) {
      return getSkipCharacterPhysics(mem, 0);
   }

   public static boolean getSkipCharacterPhysics(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 22));
   }

   public static boolean getIsFirstPerson(MemorySegment mem) {
      return getIsFirstPerson(mem, 0);
   }

   public static boolean getIsFirstPerson(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 23));
   }

   public static MovementForceRotationType getMovementForceRotationType(MemorySegment mem) {
      return getMovementForceRotationType(mem, 0);
   }

   public static MovementForceRotationType getMovementForceRotationType(MemorySegment mem, int offset) {
      return MovementForceRotationType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 24)));
   }

   @Nullable
   public static Direction getMovementForceRotation(MemorySegment mem) {
      return getMovementForceRotation(mem, 0);
   }

   @Nullable
   public static Direction getMovementForceRotation(MemorySegment mem, int offset) {
      return hasMovementForceRotation(mem, offset) ? Direction.toObject(mem, offset + 25) : null;
   }

   public static AttachedToType getAttachedToType(MemorySegment mem) {
      return getAttachedToType(mem, 0);
   }

   public static AttachedToType getAttachedToType(MemorySegment mem, int offset) {
      return AttachedToType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 37)));
   }

   public static int getAttachedToEntityId(MemorySegment mem) {
      return getAttachedToEntityId(mem, 0);
   }

   public static int getAttachedToEntityId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 38));
   }

   public static boolean getEyeOffset(MemorySegment mem) {
      return getEyeOffset(mem, 0);
   }

   public static boolean getEyeOffset(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 42));
   }

   public static PositionDistanceOffsetType getPositionDistanceOffsetType(MemorySegment mem) {
      return getPositionDistanceOffsetType(mem, 0);
   }

   public static PositionDistanceOffsetType getPositionDistanceOffsetType(MemorySegment mem, int offset) {
      return PositionDistanceOffsetType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 43)));
   }

   @Nullable
   public static Position getPositionOffset(MemorySegment mem) {
      return getPositionOffset(mem, 0);
   }

   @Nullable
   public static Position getPositionOffset(MemorySegment mem, int offset) {
      return hasPositionOffset(mem, offset) ? Position.toObject(mem, offset + 44) : null;
   }

   @Nullable
   public static Direction getRotationOffset(MemorySegment mem) {
      return getRotationOffset(mem, 0);
   }

   @Nullable
   public static Direction getRotationOffset(MemorySegment mem, int offset) {
      return hasRotationOffset(mem, offset) ? Direction.toObject(mem, offset + 68) : null;
   }

   public static PositionType getPositionType(MemorySegment mem) {
      return getPositionType(mem, 0);
   }

   public static PositionType getPositionType(MemorySegment mem, int offset) {
      return PositionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 80)));
   }

   @Nullable
   public static Position getPosition(MemorySegment mem) {
      return getPosition(mem, 0);
   }

   @Nullable
   public static Position getPosition(MemorySegment mem, int offset) {
      return hasPosition(mem, offset) ? Position.toObject(mem, offset + 81) : null;
   }

   public static RotationType getRotationType(MemorySegment mem) {
      return getRotationType(mem, 0);
   }

   public static RotationType getRotationType(MemorySegment mem, int offset) {
      return RotationType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 105)));
   }

   @Nullable
   public static Direction getRotation(MemorySegment mem) {
      return getRotation(mem, 0);
   }

   @Nullable
   public static Direction getRotation(MemorySegment mem, int offset) {
      return hasRotation(mem, offset) ? Direction.toObject(mem, offset + 106) : null;
   }

   public static CanMoveType getCanMoveType(MemorySegment mem) {
      return getCanMoveType(mem, 0);
   }

   public static CanMoveType getCanMoveType(MemorySegment mem, int offset) {
      return CanMoveType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 118)));
   }

   public static ApplyMovementType getApplyMovementType(MemorySegment mem) {
      return getApplyMovementType(mem, 0);
   }

   public static ApplyMovementType getApplyMovementType(MemorySegment mem, int offset) {
      return ApplyMovementType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 119)));
   }

   @Nullable
   public static Vector3fc getMovementMultiplier(MemorySegment mem) {
      return getMovementMultiplier(mem, 0);
   }

   @Nullable
   public static Vector3fc getMovementMultiplier(MemorySegment mem, int offset) {
      return hasMovementMultiplier(mem, offset) ? PacketIO.readVector3f(mem, offset + 120) : null;
   }

   public static ApplyLookType getApplyLookType(MemorySegment mem) {
      return getApplyLookType(mem, 0);
   }

   public static ApplyLookType getApplyLookType(MemorySegment mem, int offset) {
      return ApplyLookType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 132)));
   }

   @Nullable
   public static Vector2fc getLookMultiplier(MemorySegment mem) {
      return getLookMultiplier(mem, 0);
   }

   @Nullable
   public static Vector2fc getLookMultiplier(MemorySegment mem, int offset) {
      return hasLookMultiplier(mem, offset) ? PacketIO.readVector2f(mem, offset + 133) : null;
   }

   public static MouseInputType getMouseInputType(MemorySegment mem) {
      return getMouseInputType(mem, 0);
   }

   public static MouseInputType getMouseInputType(MemorySegment mem, int offset) {
      return MouseInputType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 141)));
   }

   @Nullable
   public static Vector3fc getPlaneNormal(MemorySegment mem) {
      return getPlaneNormal(mem, 0);
   }

   @Nullable
   public static Vector3fc getPlaneNormal(MemorySegment mem, int offset) {
      return hasPlaneNormal(mem, offset) ? PacketIO.readVector3f(mem, offset + 142) : null;
   }

   public static boolean hasMovementForceRotation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasPositionOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasRotationOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasPosition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasRotation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasMovementMultiplier(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasLookMultiplier(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasPlaneNormal(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   public static ServerCameraSettings toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ServerCameraSettings toObject(MemorySegment mem, int offset) {
      if (offset + 154 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ServerCameraSettings", offset + 154, (int)mem.byteSize());
      } else {
         return new ServerCameraSettings(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 13)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 17)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 18)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 19)),
            MouseInputTargetType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 20))),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 21)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 22)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 23)),
            MovementForceRotationType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 24))),
            hasMovementForceRotation(mem, offset) ? Direction.toObject(mem, offset + 25) : null,
            AttachedToType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 37))),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 38)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 42)),
            PositionDistanceOffsetType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 43))),
            hasPositionOffset(mem, offset) ? Position.toObject(mem, offset + 44) : null,
            hasRotationOffset(mem, offset) ? Direction.toObject(mem, offset + 68) : null,
            PositionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 80))),
            hasPosition(mem, offset) ? Position.toObject(mem, offset + 81) : null,
            RotationType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 105))),
            hasRotation(mem, offset) ? Direction.toObject(mem, offset + 106) : null,
            CanMoveType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 118))),
            ApplyMovementType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 119))),
            hasMovementMultiplier(mem, offset) ? PacketIO.readVector3f(mem, offset + 120) : null,
            ApplyLookType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 132))),
            hasLookMultiplier(mem, offset) ? PacketIO.readVector2f(mem, offset + 133) : null,
            MouseInputType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 141))),
            hasPlaneNormal(mem, offset) ? PacketIO.readVector3f(mem, offset + 142) : null
         );
      }
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
         PacketIO.writeVector3f(buf, this.movementMultiplier);
      } else {
         buf.writeZero(12);
      }

      buf.writeByte(this.applyLookType.getValue());
      if (this.lookMultiplier != null) {
         PacketIO.writeVector2f(buf, this.lookMultiplier);
      } else {
         buf.writeZero(8);
      }

      buf.writeByte(this.mouseInputType.getValue());
      if (this.planeNormal != null) {
         PacketIO.writeVector3f(buf, this.planeNormal);
      } else {
         buf.writeZero(12);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.positionLerpSpeed);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.rotationLerpSpeed);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.distance);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 13), this.speedModifier);
      mem.set(PacketIO.PROTO_BOOL, offset + 17, this.allowPitchControls);
      mem.set(PacketIO.PROTO_BOOL, offset + 18, this.displayCursor);
      mem.set(PacketIO.PROTO_BOOL, offset + 19, this.displayReticle);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 20), (byte)this.mouseInputTargetType.getValue());
      mem.set(PacketIO.PROTO_BOOL, offset + 21, this.sendMouseMotion);
      mem.set(PacketIO.PROTO_BOOL, offset + 22, this.skipCharacterPhysics);
      mem.set(PacketIO.PROTO_BOOL, offset + 23, this.isFirstPerson);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 24), (byte)this.movementForceRotationType.getValue());
      if (this.movementForceRotation != null) {
         this.movementForceRotation.serialize(mem, offset + 25);
      } else {
         mem.asSlice(offset + 25, 12L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 37), (byte)this.attachedToType.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 38), this.attachedToEntityId);
      mem.set(PacketIO.PROTO_BOOL, offset + 42, this.eyeOffset);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 43), (byte)this.positionDistanceOffsetType.getValue());
      if (this.positionOffset != null) {
         this.positionOffset.serialize(mem, offset + 44);
      } else {
         mem.asSlice(offset + 44, 24L).fill((byte)0);
      }

      if (this.rotationOffset != null) {
         this.rotationOffset.serialize(mem, offset + 68);
      } else {
         mem.asSlice(offset + 68, 12L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 80), (byte)this.positionType.getValue());
      if (this.position != null) {
         this.position.serialize(mem, offset + 81);
      } else {
         mem.asSlice(offset + 81, 24L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 105), (byte)this.rotationType.getValue());
      if (this.rotation != null) {
         this.rotation.serialize(mem, offset + 106);
      } else {
         mem.asSlice(offset + 106, 12L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 118), (byte)this.canMoveType.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 119), (byte)this.applyMovementType.getValue());
      if (this.movementMultiplier != null) {
         PacketIO.writeVector3f(mem, offset + 120, this.movementMultiplier);
      } else {
         mem.asSlice(offset + 120, 12L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 132), (byte)this.applyLookType.getValue());
      if (this.lookMultiplier != null) {
         PacketIO.writeVector2f(mem, offset + 133, this.lookMultiplier);
      } else {
         mem.asSlice(offset + 133, 8L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 141), (byte)this.mouseInputType.getValue());
      if (this.planeNormal != null) {
         PacketIO.writeVector3f(mem, offset + 142, this.planeNormal);
      } else {
         mem.asSlice(offset + 142, 12L).fill((byte)0);
      }

      return 154;
   }

   public int computeSize() {
      return 154;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 154) {
         return ValidationResult.error("Buffer too small: expected at least 154 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 20) & 255;
         if (v >= 4) {
            return ValidationResult.error("Invalid MouseInputTargetType value for MouseInputTargetType");
         } else {
            v = buffer.getByte(offset + 24) & 255;
            if (v >= 3) {
               return ValidationResult.error("Invalid MovementForceRotationType value for MovementForceRotationType");
            } else {
               v = buffer.getByte(offset + 37) & 255;
               if (v >= 3) {
                  return ValidationResult.error("Invalid AttachedToType value for AttachedToType");
               } else {
                  v = buffer.getByte(offset + 43) & 255;
                  if (v >= 3) {
                     return ValidationResult.error("Invalid PositionDistanceOffsetType value for PositionDistanceOffsetType");
                  } else {
                     v = buffer.getByte(offset + 80) & 255;
                     if (v >= 2) {
                        return ValidationResult.error("Invalid PositionType value for PositionType");
                     } else {
                        v = buffer.getByte(offset + 105) & 255;
                        if (v >= 2) {
                           return ValidationResult.error("Invalid RotationType value for RotationType");
                        } else {
                           v = buffer.getByte(offset + 118) & 255;
                           if (v >= 2) {
                              return ValidationResult.error("Invalid CanMoveType value for CanMoveType");
                           } else {
                              v = buffer.getByte(offset + 119) & 255;
                              if (v >= 2) {
                                 return ValidationResult.error("Invalid ApplyMovementType value for ApplyMovementType");
                              } else {
                                 v = buffer.getByte(offset + 132) & 255;
                                 if (v >= 2) {
                                    return ValidationResult.error("Invalid ApplyLookType value for ApplyLookType");
                                 } else {
                                    v = buffer.getByte(offset + 141) & 255;
                                    return v >= 4 ? ValidationResult.error("Invalid MouseInputType value for MouseInputType") : ValidationResult.OK;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
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
      copy.movementMultiplier = this.movementMultiplier;
      copy.applyLookType = this.applyLookType;
      copy.lookMultiplier = this.lookMultiplier;
      copy.mouseInputType = this.mouseInputType;
      copy.planeNormal = this.planeNormal;
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
