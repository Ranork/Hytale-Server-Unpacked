package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionSyncData {
   public static final int NULLABLE_BIT_FIELD_SIZE = 2;
   public static final int FIXED_BLOCK_SIZE = 157;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 165;
   public static final int MAX_SIZE = 237568175;
   @Nonnull
   public InteractionState state = InteractionState.Finished;
   public float progress;
   public int operationCounter;
   public int rootInteraction;
   public int totalForks;
   public int entityId;
   public int enteredRootInteraction = Integer.MIN_VALUE;
   @Nullable
   public BlockPosition blockPosition;
   @Nonnull
   public BlockFace blockFace = BlockFace.None;
   @Nullable
   public BlockRotation blockRotation;
   public int placedBlockId = Integer.MIN_VALUE;
   public float chargeValue = -1.0F;
   @Nullable
   public Map<InteractionType, Integer> forkCounts;
   public int chainingIndex = -1;
   public int flagIndex = -1;
   @Nullable
   public SelectedHitEntity[] hitEntities;
   @Nullable
   public Position attackerPos;
   @Nullable
   public Direction attackerRot;
   @Nullable
   public Position raycastHit;
   public float raycastDistance;
   @Nullable
   public Vector3f raycastNormal;
   @Nonnull
   public MovementDirection movementDirection = MovementDirection.None;
   @Nonnull
   public ApplyForceState applyForceState = ApplyForceState.Waiting;
   public int nextLabel;
   @Nullable
   public UUID generatedUUID = null;

   public InteractionSyncData() {
   }

   public InteractionSyncData(
      @Nonnull InteractionState state,
      float progress,
      int operationCounter,
      int rootInteraction,
      int totalForks,
      int entityId,
      int enteredRootInteraction,
      @Nullable BlockPosition blockPosition,
      @Nonnull BlockFace blockFace,
      @Nullable BlockRotation blockRotation,
      int placedBlockId,
      float chargeValue,
      @Nullable Map<InteractionType, Integer> forkCounts,
      int chainingIndex,
      int flagIndex,
      @Nullable SelectedHitEntity[] hitEntities,
      @Nullable Position attackerPos,
      @Nullable Direction attackerRot,
      @Nullable Position raycastHit,
      float raycastDistance,
      @Nullable Vector3f raycastNormal,
      @Nonnull MovementDirection movementDirection,
      @Nonnull ApplyForceState applyForceState,
      int nextLabel,
      @Nullable UUID generatedUUID
   ) {
      this.state = state;
      this.progress = progress;
      this.operationCounter = operationCounter;
      this.rootInteraction = rootInteraction;
      this.totalForks = totalForks;
      this.entityId = entityId;
      this.enteredRootInteraction = enteredRootInteraction;
      this.blockPosition = blockPosition;
      this.blockFace = blockFace;
      this.blockRotation = blockRotation;
      this.placedBlockId = placedBlockId;
      this.chargeValue = chargeValue;
      this.forkCounts = forkCounts;
      this.chainingIndex = chainingIndex;
      this.flagIndex = flagIndex;
      this.hitEntities = hitEntities;
      this.attackerPos = attackerPos;
      this.attackerRot = attackerRot;
      this.raycastHit = raycastHit;
      this.raycastDistance = raycastDistance;
      this.raycastNormal = raycastNormal;
      this.movementDirection = movementDirection;
      this.applyForceState = applyForceState;
      this.nextLabel = nextLabel;
      this.generatedUUID = generatedUUID;
   }

   public InteractionSyncData(@Nonnull InteractionSyncData other) {
      this.state = other.state;
      this.progress = other.progress;
      this.operationCounter = other.operationCounter;
      this.rootInteraction = other.rootInteraction;
      this.totalForks = other.totalForks;
      this.entityId = other.entityId;
      this.enteredRootInteraction = other.enteredRootInteraction;
      this.blockPosition = other.blockPosition;
      this.blockFace = other.blockFace;
      this.blockRotation = other.blockRotation;
      this.placedBlockId = other.placedBlockId;
      this.chargeValue = other.chargeValue;
      this.forkCounts = other.forkCounts;
      this.chainingIndex = other.chainingIndex;
      this.flagIndex = other.flagIndex;
      this.hitEntities = other.hitEntities;
      this.attackerPos = other.attackerPos;
      this.attackerRot = other.attackerRot;
      this.raycastHit = other.raycastHit;
      this.raycastDistance = other.raycastDistance;
      this.raycastNormal = other.raycastNormal;
      this.movementDirection = other.movementDirection;
      this.applyForceState = other.applyForceState;
      this.nextLabel = other.nextLabel;
      this.generatedUUID = other.generatedUUID;
   }

   @Nonnull
   public static InteractionSyncData deserialize(@Nonnull ByteBuf buf, int offset) {
      InteractionSyncData obj = new InteractionSyncData();
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      obj.state = InteractionState.fromValue(buf.getByte(offset + 2));
      obj.progress = buf.getFloatLE(offset + 3);
      obj.operationCounter = buf.getIntLE(offset + 7);
      obj.rootInteraction = buf.getIntLE(offset + 11);
      obj.totalForks = buf.getIntLE(offset + 15);
      obj.entityId = buf.getIntLE(offset + 19);
      obj.enteredRootInteraction = buf.getIntLE(offset + 23);
      if ((nullBits[0] & 1) != 0) {
         obj.blockPosition = BlockPosition.deserialize(buf, offset + 27);
      }

      obj.blockFace = BlockFace.fromValue(buf.getByte(offset + 39));
      if ((nullBits[0] & 2) != 0) {
         obj.blockRotation = BlockRotation.deserialize(buf, offset + 40);
      }

      obj.placedBlockId = buf.getIntLE(offset + 43);
      obj.chargeValue = buf.getFloatLE(offset + 47);
      obj.chainingIndex = buf.getIntLE(offset + 51);
      obj.flagIndex = buf.getIntLE(offset + 55);
      if ((nullBits[0] & 4) != 0) {
         obj.attackerPos = Position.deserialize(buf, offset + 59);
      }

      if ((nullBits[0] & 8) != 0) {
         obj.attackerRot = Direction.deserialize(buf, offset + 83);
      }

      if ((nullBits[0] & 16) != 0) {
         obj.raycastHit = Position.deserialize(buf, offset + 95);
      }

      obj.raycastDistance = buf.getFloatLE(offset + 119);
      if ((nullBits[0] & 32) != 0) {
         obj.raycastNormal = Vector3f.deserialize(buf, offset + 123);
      }

      obj.movementDirection = MovementDirection.fromValue(buf.getByte(offset + 135));
      obj.applyForceState = ApplyForceState.fromValue(buf.getByte(offset + 136));
      obj.nextLabel = buf.getIntLE(offset + 137);
      if ((nullBits[0] & 64) != 0) {
         obj.generatedUUID = PacketIO.readUUID(buf, offset + 141);
      }

      if ((nullBits[0] & 128) != 0) {
         int varPos0 = offset + 165 + buf.getIntLE(offset + 157);
         int forkCountsCount = VarInt.peek(buf, varPos0);
         if (forkCountsCount < 0) {
            throw ProtocolException.negativeLength("ForkCounts", forkCountsCount);
         }

         if (forkCountsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ForkCounts", forkCountsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
         obj.forkCounts = new HashMap<>(forkCountsCount);
         int dictPos = varPos0 + varIntLen;

         for (int i = 0; i < forkCountsCount; i++) {
            InteractionType key = InteractionType.fromValue(buf.getByte(dictPos));
            int val = buf.getIntLE(++dictPos);
            dictPos += 4;
            if (obj.forkCounts.put(key, val) != null) {
               throw ProtocolException.duplicateKey("forkCounts", key);
            }
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int varPos1 = offset + 165 + buf.getIntLE(offset + 161);
         int hitEntitiesCount = VarInt.peek(buf, varPos1);
         if (hitEntitiesCount < 0) {
            throw ProtocolException.negativeLength("HitEntities", hitEntitiesCount);
         }

         if (hitEntitiesCount > 4096000) {
            throw ProtocolException.arrayTooLong("HitEntities", hitEntitiesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + hitEntitiesCount * 53L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("HitEntities", varPos1 + varIntLen + hitEntitiesCount * 53, buf.readableBytes());
         }

         obj.hitEntities = new SelectedHitEntity[hitEntitiesCount];
         int elemPos = varPos1 + varIntLen;

         for (int ix = 0; ix < hitEntitiesCount; ix++) {
            obj.hitEntities[ix] = SelectedHitEntity.deserialize(buf, elemPos);
            elemPos += SelectedHitEntity.computeBytesConsumed(buf, elemPos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      int maxEnd = 165;
      if ((nullBits[0] & 128) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 157);
         int pos0 = offset + 165 + fieldOffset0;
         int dictLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0);

         for (int i = 0; i < dictLen; i++) {
            pos0 = ++pos0 + 4;
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 161);
         int pos1 = offset + 165 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += SelectedHitEntity.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte[] nullBits = new byte[2];
      if (this.blockPosition != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.blockRotation != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.attackerPos != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.attackerRot != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.raycastHit != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.raycastNormal != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.generatedUUID != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.forkCounts != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.hitEntities != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      buf.writeBytes(nullBits);
      buf.writeByte(this.state.getValue());
      buf.writeFloatLE(this.progress);
      buf.writeIntLE(this.operationCounter);
      buf.writeIntLE(this.rootInteraction);
      buf.writeIntLE(this.totalForks);
      buf.writeIntLE(this.entityId);
      buf.writeIntLE(this.enteredRootInteraction);
      if (this.blockPosition != null) {
         this.blockPosition.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeByte(this.blockFace.getValue());
      if (this.blockRotation != null) {
         this.blockRotation.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      buf.writeIntLE(this.placedBlockId);
      buf.writeFloatLE(this.chargeValue);
      buf.writeIntLE(this.chainingIndex);
      buf.writeIntLE(this.flagIndex);
      if (this.attackerPos != null) {
         this.attackerPos.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      if (this.attackerRot != null) {
         this.attackerRot.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.raycastHit != null) {
         this.raycastHit.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      buf.writeFloatLE(this.raycastDistance);
      if (this.raycastNormal != null) {
         this.raycastNormal.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeByte(this.movementDirection.getValue());
      buf.writeByte(this.applyForceState.getValue());
      buf.writeIntLE(this.nextLabel);
      if (this.generatedUUID != null) {
         PacketIO.writeUUID(buf, this.generatedUUID);
      } else {
         buf.writeZero(16);
      }

      int forkCountsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int hitEntitiesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.forkCounts != null) {
         buf.setIntLE(forkCountsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.forkCounts.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ForkCounts", this.forkCounts.size(), 4096000);
         }

         VarInt.write(buf, this.forkCounts.size());

         for (Entry<InteractionType, Integer> e : this.forkCounts.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            buf.writeIntLE(e.getValue());
         }
      } else {
         buf.setIntLE(forkCountsOffsetSlot, -1);
      }

      if (this.hitEntities != null) {
         buf.setIntLE(hitEntitiesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.hitEntities.length > 4096000) {
            throw ProtocolException.arrayTooLong("HitEntities", this.hitEntities.length, 4096000);
         }

         VarInt.write(buf, this.hitEntities.length);

         for (SelectedHitEntity item : this.hitEntities) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(hitEntitiesOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 165;
      if (this.forkCounts != null) {
         size += VarInt.size(this.forkCounts.size()) + this.forkCounts.size() * 5;
      }

      if (this.hitEntities != null) {
         size += VarInt.size(this.hitEntities.length) + this.hitEntities.length * 53;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 165) {
         return ValidationResult.error("Buffer too small: expected at least 165 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 2);
         if ((nullBits[0] & 128) != 0) {
            int forkCountsOffset = buffer.getIntLE(offset + 157);
            if (forkCountsOffset < 0) {
               return ValidationResult.error("Invalid offset for ForkCounts");
            }

            int pos = offset + 165 + forkCountsOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ForkCounts");
            }

            int forkCountsCount = VarInt.peek(buffer, pos);
            if (forkCountsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ForkCounts");
            }

            if (forkCountsCount > 4096000) {
               return ValidationResult.error("ForkCounts exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < forkCountsCount; i++) {
               pos = ++pos + 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         if ((nullBits[1] & 1) != 0) {
            int hitEntitiesOffset = buffer.getIntLE(offset + 161);
            if (hitEntitiesOffset < 0) {
               return ValidationResult.error("Invalid offset for HitEntities");
            }

            int posx = offset + 165 + hitEntitiesOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for HitEntities");
            }

            int hitEntitiesCount = VarInt.peek(buffer, posx);
            if (hitEntitiesCount < 0) {
               return ValidationResult.error("Invalid array count for HitEntities");
            }

            if (hitEntitiesCount > 4096000) {
               return ValidationResult.error("HitEntities exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += hitEntitiesCount * 53;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading HitEntities");
            }
         }

         return ValidationResult.OK;
      }
   }

   public InteractionSyncData clone() {
      InteractionSyncData copy = new InteractionSyncData();
      copy.state = this.state;
      copy.progress = this.progress;
      copy.operationCounter = this.operationCounter;
      copy.rootInteraction = this.rootInteraction;
      copy.totalForks = this.totalForks;
      copy.entityId = this.entityId;
      copy.enteredRootInteraction = this.enteredRootInteraction;
      copy.blockPosition = this.blockPosition != null ? this.blockPosition.clone() : null;
      copy.blockFace = this.blockFace;
      copy.blockRotation = this.blockRotation != null ? this.blockRotation.clone() : null;
      copy.placedBlockId = this.placedBlockId;
      copy.chargeValue = this.chargeValue;
      copy.forkCounts = this.forkCounts != null ? new HashMap<>(this.forkCounts) : null;
      copy.chainingIndex = this.chainingIndex;
      copy.flagIndex = this.flagIndex;
      copy.hitEntities = this.hitEntities != null ? Arrays.stream(this.hitEntities).map(e -> e.clone()).toArray(SelectedHitEntity[]::new) : null;
      copy.attackerPos = this.attackerPos != null ? this.attackerPos.clone() : null;
      copy.attackerRot = this.attackerRot != null ? this.attackerRot.clone() : null;
      copy.raycastHit = this.raycastHit != null ? this.raycastHit.clone() : null;
      copy.raycastDistance = this.raycastDistance;
      copy.raycastNormal = this.raycastNormal != null ? this.raycastNormal.clone() : null;
      copy.movementDirection = this.movementDirection;
      copy.applyForceState = this.applyForceState;
      copy.nextLabel = this.nextLabel;
      copy.generatedUUID = this.generatedUUID;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InteractionSyncData other)
            ? false
            : Objects.equals(this.state, other.state)
               && this.progress == other.progress
               && this.operationCounter == other.operationCounter
               && this.rootInteraction == other.rootInteraction
               && this.totalForks == other.totalForks
               && this.entityId == other.entityId
               && this.enteredRootInteraction == other.enteredRootInteraction
               && Objects.equals(this.blockPosition, other.blockPosition)
               && Objects.equals(this.blockFace, other.blockFace)
               && Objects.equals(this.blockRotation, other.blockRotation)
               && this.placedBlockId == other.placedBlockId
               && this.chargeValue == other.chargeValue
               && Objects.equals(this.forkCounts, other.forkCounts)
               && this.chainingIndex == other.chainingIndex
               && this.flagIndex == other.flagIndex
               && Arrays.equals((Object[])this.hitEntities, (Object[])other.hitEntities)
               && Objects.equals(this.attackerPos, other.attackerPos)
               && Objects.equals(this.attackerRot, other.attackerRot)
               && Objects.equals(this.raycastHit, other.raycastHit)
               && this.raycastDistance == other.raycastDistance
               && Objects.equals(this.raycastNormal, other.raycastNormal)
               && Objects.equals(this.movementDirection, other.movementDirection)
               && Objects.equals(this.applyForceState, other.applyForceState)
               && this.nextLabel == other.nextLabel
               && Objects.equals(this.generatedUUID, other.generatedUUID);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.state);
      result = 31 * result + Float.hashCode(this.progress);
      result = 31 * result + Integer.hashCode(this.operationCounter);
      result = 31 * result + Integer.hashCode(this.rootInteraction);
      result = 31 * result + Integer.hashCode(this.totalForks);
      result = 31 * result + Integer.hashCode(this.entityId);
      result = 31 * result + Integer.hashCode(this.enteredRootInteraction);
      result = 31 * result + Objects.hashCode(this.blockPosition);
      result = 31 * result + Objects.hashCode(this.blockFace);
      result = 31 * result + Objects.hashCode(this.blockRotation);
      result = 31 * result + Integer.hashCode(this.placedBlockId);
      result = 31 * result + Float.hashCode(this.chargeValue);
      result = 31 * result + Objects.hashCode(this.forkCounts);
      result = 31 * result + Integer.hashCode(this.chainingIndex);
      result = 31 * result + Integer.hashCode(this.flagIndex);
      result = 31 * result + Arrays.hashCode((Object[])this.hitEntities);
      result = 31 * result + Objects.hashCode(this.attackerPos);
      result = 31 * result + Objects.hashCode(this.attackerRot);
      result = 31 * result + Objects.hashCode(this.raycastHit);
      result = 31 * result + Float.hashCode(this.raycastDistance);
      result = 31 * result + Objects.hashCode(this.raycastNormal);
      result = 31 * result + Objects.hashCode(this.movementDirection);
      result = 31 * result + Objects.hashCode(this.applyForceState);
      result = 31 * result + Integer.hashCode(this.nextLabel);
      return 31 * result + Objects.hashCode(this.generatedUUID);
   }
}
