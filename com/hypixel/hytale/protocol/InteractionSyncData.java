package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3fc;

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
   public Vector3fc raycastNormal;
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
      @Nullable Vector3fc raycastNormal,
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
      if (buf.readableBytes() - offset < 165) {
         throw ProtocolException.bufferTooSmall("InteractionSyncData", 165, buf.readableBytes() - offset);
      } else {
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
            obj.raycastNormal = PacketIO.readVector3f(buf, offset + 123);
         }

         obj.movementDirection = MovementDirection.fromValue(buf.getByte(offset + 135));
         obj.applyForceState = ApplyForceState.fromValue(buf.getByte(offset + 136));
         obj.nextLabel = buf.getIntLE(offset + 137);
         if ((nullBits[0] & 64) != 0) {
            obj.generatedUUID = PacketIO.readUUID(buf, offset + 141);
         }

         if ((nullBits[0] & 128) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 157);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 165) {
               throw ProtocolException.invalidOffset("ForkCounts", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 165 + varPosBase0;
            int forkCountsCount = VarInt.peek(buf, varPos0);
            if (forkCountsCount < 0) {
               throw ProtocolException.invalidVarInt("ForkCounts");
            }

            int varIntLen = VarInt.size(forkCountsCount);
            if (forkCountsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ForkCounts", forkCountsCount, 4096000);
            }

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
            int varPosBase1 = buf.getIntLE(offset + 161);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 165) {
               throw ProtocolException.invalidOffset("HitEntities", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 165 + varPosBase1;
            int hitEntitiesCount = VarInt.peek(buf, varPos1);
            if (hitEntitiesCount < 0) {
               throw ProtocolException.invalidVarInt("HitEntities");
            }

            int varIntLen = VarInt.size(hitEntitiesCount);
            if (hitEntitiesCount > 4096000) {
               throw ProtocolException.arrayTooLong("HitEntities", hitEntitiesCount, 4096000);
            }

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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      int maxEnd = 165;
      if ((nullBits[0] & 128) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 157);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 165) {
            throw ProtocolException.invalidOffset("ForkCounts", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 165 + fieldOffset0;
         int dictLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos0 = ++pos0 + 4;
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 161);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 165) {
            throw ProtocolException.invalidOffset("HitEntities", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 165 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += SelectedHitEntity.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 165L;
   }

   public static InteractionState getState(MemorySegment mem) {
      return getState(mem, 0);
   }

   public static InteractionState getState(MemorySegment mem, int offset) {
      return InteractionState.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2)));
   }

   public static float getProgress(MemorySegment mem) {
      return getProgress(mem, 0);
   }

   public static float getProgress(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 3));
   }

   public static int getOperationCounter(MemorySegment mem) {
      return getOperationCounter(mem, 0);
   }

   public static int getOperationCounter(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 7));
   }

   public static int getRootInteraction(MemorySegment mem) {
      return getRootInteraction(mem, 0);
   }

   public static int getRootInteraction(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 11));
   }

   public static int getTotalForks(MemorySegment mem) {
      return getTotalForks(mem, 0);
   }

   public static int getTotalForks(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 15));
   }

   public static int getEntityId(MemorySegment mem) {
      return getEntityId(mem, 0);
   }

   public static int getEntityId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 19));
   }

   public static int getEnteredRootInteraction(MemorySegment mem) {
      return getEnteredRootInteraction(mem, 0);
   }

   public static int getEnteredRootInteraction(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 23));
   }

   @Nullable
   public static BlockPosition getBlockPosition(MemorySegment mem) {
      return getBlockPosition(mem, 0);
   }

   @Nullable
   public static BlockPosition getBlockPosition(MemorySegment mem, int offset) {
      return hasBlockPosition(mem, offset) ? BlockPosition.toObject(mem, offset + 27) : null;
   }

   public static BlockFace getBlockFace(MemorySegment mem) {
      return getBlockFace(mem, 0);
   }

   public static BlockFace getBlockFace(MemorySegment mem, int offset) {
      return BlockFace.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 39)));
   }

   @Nullable
   public static BlockRotation getBlockRotation(MemorySegment mem) {
      return getBlockRotation(mem, 0);
   }

   @Nullable
   public static BlockRotation getBlockRotation(MemorySegment mem, int offset) {
      return hasBlockRotation(mem, offset) ? BlockRotation.toObject(mem, offset + 40) : null;
   }

   public static int getPlacedBlockId(MemorySegment mem) {
      return getPlacedBlockId(mem, 0);
   }

   public static int getPlacedBlockId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 43));
   }

   public static float getChargeValue(MemorySegment mem) {
      return getChargeValue(mem, 0);
   }

   public static float getChargeValue(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 47));
   }

   @Nullable
   public static Map<InteractionType, Integer> getForkCounts(MemorySegment mem) {
      return getForkCounts(mem, 0);
   }

   @Nullable
   public static Map<InteractionType, Integer> getForkCounts(MemorySegment mem, int offset) {
      if (!hasForkCounts(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 157, 165, "ForkCounts");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ForkCounts", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ForkCounts", len, 4096000);
         } else {
            Map<InteractionType, Integer> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               InteractionType key = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               int value = mem.get(PacketIO.PROTO_INT, (long)(++off));
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ForkCounts", key);
               }
            }

            return data;
         }
      }
   }

   public static int getChainingIndex(MemorySegment mem) {
      return getChainingIndex(mem, 0);
   }

   public static int getChainingIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 51));
   }

   public static int getFlagIndex(MemorySegment mem) {
      return getFlagIndex(mem, 0);
   }

   public static int getFlagIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 55));
   }

   @Nullable
   public static SelectedHitEntity[] getHitEntities(MemorySegment mem) {
      return getHitEntities(mem, 0);
   }

   @Nullable
   public static SelectedHitEntity[] getHitEntities(MemorySegment mem, int offset) {
      if (!hasHitEntities(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 161, 165, "HitEntities");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("HitEntities", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("HitEntities", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 53L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("HitEntities", off + lenOffset + len * 53, (int)mem.byteSize());
            } else {
               off += lenOffset;
               SelectedHitEntity[] data = new SelectedHitEntity[len];

               for (int i = 0; i < len; i++) {
                  data[i] = SelectedHitEntity.toObject(mem, off + i * 53);
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static Position getAttackerPos(MemorySegment mem) {
      return getAttackerPos(mem, 0);
   }

   @Nullable
   public static Position getAttackerPos(MemorySegment mem, int offset) {
      return hasAttackerPos(mem, offset) ? Position.toObject(mem, offset + 59) : null;
   }

   @Nullable
   public static Direction getAttackerRot(MemorySegment mem) {
      return getAttackerRot(mem, 0);
   }

   @Nullable
   public static Direction getAttackerRot(MemorySegment mem, int offset) {
      return hasAttackerRot(mem, offset) ? Direction.toObject(mem, offset + 83) : null;
   }

   @Nullable
   public static Position getRaycastHit(MemorySegment mem) {
      return getRaycastHit(mem, 0);
   }

   @Nullable
   public static Position getRaycastHit(MemorySegment mem, int offset) {
      return hasRaycastHit(mem, offset) ? Position.toObject(mem, offset + 95) : null;
   }

   public static float getRaycastDistance(MemorySegment mem) {
      return getRaycastDistance(mem, 0);
   }

   public static float getRaycastDistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 119));
   }

   @Nullable
   public static Vector3fc getRaycastNormal(MemorySegment mem) {
      return getRaycastNormal(mem, 0);
   }

   @Nullable
   public static Vector3fc getRaycastNormal(MemorySegment mem, int offset) {
      return hasRaycastNormal(mem, offset) ? PacketIO.readVector3f(mem, offset + 123) : null;
   }

   public static MovementDirection getMovementDirection(MemorySegment mem) {
      return getMovementDirection(mem, 0);
   }

   public static MovementDirection getMovementDirection(MemorySegment mem, int offset) {
      return MovementDirection.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 135)));
   }

   public static ApplyForceState getApplyForceState(MemorySegment mem) {
      return getApplyForceState(mem, 0);
   }

   public static ApplyForceState getApplyForceState(MemorySegment mem, int offset) {
      return ApplyForceState.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 136)));
   }

   public static int getNextLabel(MemorySegment mem) {
      return getNextLabel(mem, 0);
   }

   public static int getNextLabel(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 137));
   }

   @Nullable
   public static UUID getGeneratedUUID(MemorySegment mem) {
      return getGeneratedUUID(mem, 0);
   }

   @Nullable
   public static UUID getGeneratedUUID(MemorySegment mem, int offset) {
      return hasGeneratedUUID(mem, offset) ? PacketIO.readUUID(mem, offset + 141) : null;
   }

   public static boolean hasBlockPosition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasBlockRotation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasAttackerPos(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasAttackerRot(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasRaycastHit(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasRaycastNormal(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasGeneratedUUID(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasForkCounts(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   public static boolean hasHitEntities(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 1) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static InteractionSyncData toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InteractionSyncData toObject(MemorySegment mem, int offset) {
      if (offset + 165 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InteractionSyncData", offset + 165, (int)mem.byteSize());
      } else {
         Map<InteractionType, Integer> forkCounts = null;
         if (hasForkCounts(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 157, 165, "ForkCounts");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("ForkCounts", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ForkCounts", len, 4096000);
            }

            forkCounts = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               InteractionType key = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               int value = mem.get(PacketIO.PROTO_INT, (long)(++off));
               off += 4;
               if (forkCounts.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ForkCounts", key);
               }
            }
         }

         SelectedHitEntity[] hitEntities = null;
         if (hasHitEntities(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 161, 165, "HitEntities");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("HitEntities", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("HitEntities", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx * 53L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("HitEntities", offx + lenOffset + lenx * 53, (int)mem.byteSize());
            }

            offx += lenOffset;
            hitEntities = new SelectedHitEntity[lenx];

            for (int ix = 0; ix < lenx; ix++) {
               hitEntities[ix] = SelectedHitEntity.toObject(mem, offx + ix * 53);
            }
         }

         return new InteractionSyncData(
            InteractionState.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2))),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 3)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 7)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 11)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 15)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 19)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 23)),
            hasBlockPosition(mem, offset) ? BlockPosition.toObject(mem, offset + 27) : null,
            BlockFace.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 39))),
            hasBlockRotation(mem, offset) ? BlockRotation.toObject(mem, offset + 40) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 43)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 47)),
            forkCounts,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 51)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 55)),
            hitEntities,
            hasAttackerPos(mem, offset) ? Position.toObject(mem, offset + 59) : null,
            hasAttackerRot(mem, offset) ? Direction.toObject(mem, offset + 83) : null,
            hasRaycastHit(mem, offset) ? Position.toObject(mem, offset + 95) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 119)),
            hasRaycastNormal(mem, offset) ? PacketIO.readVector3f(mem, offset + 123) : null,
            MovementDirection.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 135))),
            ApplyForceState.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 136))),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 137)),
            hasGeneratedUUID(mem, offset) ? PacketIO.readUUID(mem, offset + 141) : null
         );
      }
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
         PacketIO.writeVector3f(buf, this.raycastNormal);
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.blockPosition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.blockRotation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.attackerPos != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.attackerRot != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.raycastHit != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.raycastNormal != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.generatedUUID != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.forkCounts != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      nullBits = 0;
      if (this.hitEntities != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), (byte)this.state.getValue());
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 3), this.progress);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 7), this.operationCounter);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 11), this.rootInteraction);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 15), this.totalForks);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 19), this.entityId);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 23), this.enteredRootInteraction);
      if (this.blockPosition != null) {
         this.blockPosition.serialize(mem, offset + 27);
      } else {
         mem.asSlice(offset + 27, 12L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 39), (byte)this.blockFace.getValue());
      if (this.blockRotation != null) {
         this.blockRotation.serialize(mem, offset + 40);
      } else {
         mem.asSlice(offset + 40, 3L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_INT, (long)(offset + 43), this.placedBlockId);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 47), this.chargeValue);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 51), this.chainingIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 55), this.flagIndex);
      if (this.attackerPos != null) {
         this.attackerPos.serialize(mem, offset + 59);
      } else {
         mem.asSlice(offset + 59, 24L).fill((byte)0);
      }

      if (this.attackerRot != null) {
         this.attackerRot.serialize(mem, offset + 83);
      } else {
         mem.asSlice(offset + 83, 12L).fill((byte)0);
      }

      if (this.raycastHit != null) {
         this.raycastHit.serialize(mem, offset + 95);
      } else {
         mem.asSlice(offset + 95, 24L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 119), this.raycastDistance);
      if (this.raycastNormal != null) {
         PacketIO.writeVector3f(mem, offset + 123, this.raycastNormal);
      } else {
         mem.asSlice(offset + 123, 12L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 135), (byte)this.movementDirection.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 136), (byte)this.applyForceState.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 137), this.nextLabel);
      if (this.generatedUUID != null) {
         PacketIO.writeUUID(mem, offset + 141, this.generatedUUID);
      } else {
         mem.asSlice(offset + 141, 16L).fill((byte)0);
      }

      int varOffset = offset + 165;
      if (this.forkCounts != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 157), varOffset - offset - 165);
         if (this.forkCounts.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ForkCounts", this.forkCounts.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.forkCounts.size());

         for (Entry<InteractionType, Integer> e : this.forkCounts.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            mem.set(PacketIO.PROTO_INT, (long)(++varOffset), e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 157), -1);
      }

      if (this.hitEntities != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 161), varOffset - offset - 165);
         if (this.hitEntities.length > 4096000) {
            throw ProtocolException.arrayTooLong("HitEntities", this.hitEntities.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.hitEntities.length);
         int hitEntitiesValueOffset = 0;

         for (int i = 0; i < this.hitEntities.length; i++) {
            hitEntitiesValueOffset += this.hitEntities[i].serialize(mem, varOffset + hitEntitiesValueOffset);
         }

         varOffset += hitEntitiesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 161), -1);
      }

      return varOffset - offset;
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
         int v = buffer.getByte(offset + 2) & 255;
         if (v >= 5) {
            return ValidationResult.error("Invalid InteractionState value for State");
         } else {
            v = buffer.getByte(offset + 39) & 255;
            if (v >= 7) {
               return ValidationResult.error("Invalid BlockFace value for BlockFace");
            } else {
               v = buffer.getByte(offset + 135) & 255;
               if (v >= 9) {
                  return ValidationResult.error("Invalid MovementDirection value for MovementDirection");
               } else {
                  v = buffer.getByte(offset + 136) & 255;
                  if (v >= 4) {
                     return ValidationResult.error("Invalid ApplyForceState value for ApplyForceState");
                  } else {
                     if ((nullBits[0] & 128) != 0) {
                        v = buffer.getIntLE(offset + 157);
                        if (v < 0 || v > buffer.writerIndex() - offset - 165) {
                           return ValidationResult.error("Invalid offset for ForkCounts");
                        }

                        int pos = offset + 165 + v;
                        int forkCountsCount = VarInt.peek(buffer, pos);
                        if (forkCountsCount < 0) {
                           return ValidationResult.error("Invalid dictionary count for ForkCounts");
                        }

                        if (forkCountsCount > 4096000) {
                           return ValidationResult.error("ForkCounts exceeds max length 4096000");
                        }

                        pos += VarInt.size(forkCountsCount);

                        for (int i = 0; i < forkCountsCount; i++) {
                           int vx = buffer.getByte(pos) & 255;
                           if (vx >= 25) {
                              return ValidationResult.error("Invalid InteractionType value for key");
                           }

                           pos = ++pos + 4;
                           if (pos > buffer.writerIndex()) {
                              return ValidationResult.error("Buffer overflow reading value");
                           }
                        }
                     }

                     if ((nullBits[1] & 1) != 0) {
                        v = buffer.getIntLE(offset + 161);
                        if (v < 0 || v > buffer.writerIndex() - offset - 165) {
                           return ValidationResult.error("Invalid offset for HitEntities");
                        }

                        int posx = offset + 165 + v;
                        int hitEntitiesCount = VarInt.peek(buffer, posx);
                        if (hitEntitiesCount < 0) {
                           return ValidationResult.error("Invalid array count for HitEntities");
                        }

                        if (hitEntitiesCount > 4096000) {
                           return ValidationResult.error("HitEntities exceeds max length 4096000");
                        }

                        posx += VarInt.size(hitEntitiesCount);
                        posx += hitEntitiesCount * 53;
                        if (posx > buffer.writerIndex()) {
                           return ValidationResult.error("Buffer overflow reading HitEntities");
                        }
                     }

                     return ValidationResult.OK;
                  }
               }
            }
         }
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
      copy.raycastNormal = this.raycastNormal;
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
