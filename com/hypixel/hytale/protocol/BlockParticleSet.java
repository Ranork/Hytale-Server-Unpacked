package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3fc;

public class BlockParticleSet {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 32;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 40;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public Color color;
   public float scale;
   @Nullable
   public Vector3fc positionOffset;
   @Nullable
   public Direction rotationOffset;
   @Nullable
   public Map<BlockParticleEvent, String> particleSystemIds;

   public BlockParticleSet() {
   }

   public BlockParticleSet(
      @Nullable String id,
      @Nullable Color color,
      float scale,
      @Nullable Vector3fc positionOffset,
      @Nullable Direction rotationOffset,
      @Nullable Map<BlockParticleEvent, String> particleSystemIds
   ) {
      this.id = id;
      this.color = color;
      this.scale = scale;
      this.positionOffset = positionOffset;
      this.rotationOffset = rotationOffset;
      this.particleSystemIds = particleSystemIds;
   }

   public BlockParticleSet(@Nonnull BlockParticleSet other) {
      this.id = other.id;
      this.color = other.color;
      this.scale = other.scale;
      this.positionOffset = other.positionOffset;
      this.rotationOffset = other.rotationOffset;
      this.particleSystemIds = other.particleSystemIds;
   }

   @Nonnull
   public static BlockParticleSet deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 40) {
         throw ProtocolException.bufferTooSmall("BlockParticleSet", 40, buf.readableBytes() - offset);
      } else {
         BlockParticleSet obj = new BlockParticleSet();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.color = Color.deserialize(buf, offset + 1);
         }

         obj.scale = buf.getFloatLE(offset + 4);
         if ((nullBits & 2) != 0) {
            obj.positionOffset = PacketIO.readVector3f(buf, offset + 8);
         }

         if ((nullBits & 4) != 0) {
            obj.rotationOffset = Direction.deserialize(buf, offset + 20);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 32);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 40) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 40 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            }

            int idVarIntLen = VarInt.size(idLen);
            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("Id", idLen, 4096000);
            }

            if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
            }

            obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 16) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 36);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 40) {
               throw ProtocolException.invalidOffset("ParticleSystemIds", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 40 + varPosBase1;
            int particleSystemIdsCount = VarInt.peek(buf, varPos1);
            if (particleSystemIdsCount < 0) {
               throw ProtocolException.invalidVarInt("ParticleSystemIds");
            }

            int varIntLen = VarInt.size(particleSystemIdsCount);
            if (particleSystemIdsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ParticleSystemIds", particleSystemIdsCount, 4096000);
            }

            obj.particleSystemIds = new HashMap<>(particleSystemIdsCount);
            int dictPos = varPos1 + varIntLen;

            for (int i = 0; i < particleSystemIdsCount; i++) {
               BlockParticleEvent key = BlockParticleEvent.fromValue(buf.getByte(dictPos));
               int valLen = VarInt.peek(buf, ++dictPos);
               if (valLen < 0) {
                  throw ProtocolException.invalidVarInt("val");
               }

               int valVarLen = VarInt.size(valLen);
               if (valLen > 4096000) {
                  throw ProtocolException.stringTooLong("val", valLen, 4096000);
               }

               if (dictPos + valVarLen + valLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("val", dictPos + valVarLen + valLen, buf.readableBytes());
               }

               String val = PacketIO.readVarString(buf, dictPos);
               dictPos += valVarLen + valLen;
               if (obj.particleSystemIds.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("particleSystemIds", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 40;
      if ((nullBits & 8) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 32);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 40) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 40 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 36);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 40) {
            throw ProtocolException.invalidOffset("ParticleSystemIds", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 40 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, ++pos1);
            pos1 += VarInt.size(sl) + sl;
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 40L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 32, 40, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static Color getColor(MemorySegment mem) {
      return getColor(mem, 0);
   }

   @Nullable
   public static Color getColor(MemorySegment mem, int offset) {
      return hasColor(mem, offset) ? Color.toObject(mem, offset + 1) : null;
   }

   public static float getScale(MemorySegment mem) {
      return getScale(mem, 0);
   }

   public static float getScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   @Nullable
   public static Vector3fc getPositionOffset(MemorySegment mem) {
      return getPositionOffset(mem, 0);
   }

   @Nullable
   public static Vector3fc getPositionOffset(MemorySegment mem, int offset) {
      return hasPositionOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 8) : null;
   }

   @Nullable
   public static Direction getRotationOffset(MemorySegment mem) {
      return getRotationOffset(mem, 0);
   }

   @Nullable
   public static Direction getRotationOffset(MemorySegment mem, int offset) {
      return hasRotationOffset(mem, offset) ? Direction.toObject(mem, offset + 20) : null;
   }

   @Nullable
   public static Map<BlockParticleEvent, String> getParticleSystemIds(MemorySegment mem) {
      return getParticleSystemIds(mem, 0);
   }

   @Nullable
   public static Map<BlockParticleEvent, String> getParticleSystemIds(MemorySegment mem, int offset) {
      if (!hasParticleSystemIds(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 36, 40, "ParticleSystemIds");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ParticleSystemIds", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ParticleSystemIds", len, 4096000);
         } else {
            Map<BlockParticleEvent, String> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               BlockParticleEvent key = BlockParticleEvent.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               long valuePacked = VarInt.getWithLength(mem, ++off);
               int nvalue = (int)valuePacked + (int)(valuePacked >>> 32);
               String value = PacketIO.readVarString("value", mem, off, 16384000, PacketIO.UTF8);
               off += nvalue;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ParticleSystemIds", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasColor(MemorySegment mem, int offset) {
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

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasParticleSystemIds(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static BlockParticleSet toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockParticleSet toObject(MemorySegment mem, int offset) {
      if (offset + 40 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockParticleSet", offset + 40, (int)mem.byteSize());
      } else {
         Map<BlockParticleEvent, String> particleSystemIds = null;
         if (hasParticleSystemIds(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 36, 40, "ParticleSystemIds");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("ParticleSystemIds", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ParticleSystemIds", len, 4096000);
            }

            particleSystemIds = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               BlockParticleEvent key = BlockParticleEvent.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               long valuePacked = VarInt.getWithLength(mem, ++off);
               int nvalue = (int)valuePacked + (int)(valuePacked >>> 32);
               String value = PacketIO.readVarString("value", mem, off, 16384000, PacketIO.UTF8);
               off += nvalue;
               if (particleSystemIds.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ParticleSystemIds", key);
               }
            }
         }

         return new BlockParticleSet(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 32, 40, "Id"), 4096000, PacketIO.UTF8) : null,
            hasColor(mem, offset) ? Color.toObject(mem, offset + 1) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)),
            hasPositionOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 8) : null,
            hasRotationOffset(mem, offset) ? Direction.toObject(mem, offset + 20) : null,
            particleSystemIds
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.color != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.positionOffset != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.rotationOffset != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.particleSystemIds != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      if (this.color != null) {
         this.color.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      buf.writeFloatLE(this.scale);
      if (this.positionOffset != null) {
         PacketIO.writeVector3f(buf, this.positionOffset);
      } else {
         buf.writeZero(12);
      }

      if (this.rotationOffset != null) {
         this.rotationOffset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int particleSystemIdsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.particleSystemIds != null) {
         buf.setIntLE(particleSystemIdsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.particleSystemIds.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ParticleSystemIds", this.particleSystemIds.size(), 4096000);
         }

         VarInt.write(buf, this.particleSystemIds.size());

         for (Entry<BlockParticleEvent, String> e : this.particleSystemIds.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            PacketIO.writeVarString(buf, e.getValue(), 4096000);
         }
      } else {
         buf.setIntLE(particleSystemIdsOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.color != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.positionOffset != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.rotationOffset != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.particleSystemIds != null) {
         nullBits = (byte)(nullBits | 16);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.color != null) {
         this.color.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 3L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.scale);
      if (this.positionOffset != null) {
         PacketIO.writeVector3f(mem, offset + 8, this.positionOffset);
      } else {
         mem.asSlice(offset + 8, 12L).fill((byte)0);
      }

      if (this.rotationOffset != null) {
         this.rotationOffset.serialize(mem, offset + 20);
      } else {
         mem.asSlice(offset + 20, 12L).fill((byte)0);
      }

      int varOffset = offset + 40;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 32), varOffset - offset - 40);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 32), -1);
      }

      if (this.particleSystemIds != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 36), varOffset - offset - 40);
         if (this.particleSystemIds.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ParticleSystemIds", this.particleSystemIds.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.particleSystemIds.size());

         for (Entry<BlockParticleEvent, String> e : this.particleSystemIds.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            varOffset = ++varOffset + PacketIO.writeVarString(mem, varOffset, e.getValue(), 16384000);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 36), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 40;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.particleSystemIds != null) {
         int particleSystemIdsSize = 0;

         for (Entry<BlockParticleEvent, String> kvp : this.particleSystemIds.entrySet()) {
            particleSystemIdsSize += 1 + PacketIO.stringSize(kvp.getValue());
         }

         size += VarInt.size(this.particleSystemIds.size()) + particleSystemIdsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 40) {
         return ValidationResult.error("Buffer too small: expected at least 40 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 8) != 0) {
            int idOffset = buffer.getIntLE(offset + 32);
            if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 40) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 40 + idOffset;
            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.size(idLen);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 16) != 0) {
            int particleSystemIdsOffset = buffer.getIntLE(offset + 36);
            if (particleSystemIdsOffset < 0 || particleSystemIdsOffset > buffer.writerIndex() - offset - 40) {
               return ValidationResult.error("Invalid offset for ParticleSystemIds");
            }

            int posx = offset + 40 + particleSystemIdsOffset;
            int particleSystemIdsCount = VarInt.peek(buffer, posx);
            if (particleSystemIdsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ParticleSystemIds");
            }

            if (particleSystemIdsCount > 4096000) {
               return ValidationResult.error("ParticleSystemIds exceeds max length 4096000");
            }

            posx += VarInt.size(particleSystemIdsCount);

            for (int i = 0; i < particleSystemIdsCount; i++) {
               int v = buffer.getByte(posx) & 255;
               if (v >= 10) {
                  return ValidationResult.error("Invalid BlockParticleEvent value for key");
               }

               v = VarInt.peek(buffer, ++posx);
               if (v < 0) {
                  return ValidationResult.error("Invalid string length for value");
               }

               if (v > 4096000) {
                  return ValidationResult.error("value exceeds max length 4096000");
               }

               posx += VarInt.size(v);
               posx += v;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public BlockParticleSet clone() {
      BlockParticleSet copy = new BlockParticleSet();
      copy.id = this.id;
      copy.color = this.color != null ? this.color.clone() : null;
      copy.scale = this.scale;
      copy.positionOffset = this.positionOffset;
      copy.rotationOffset = this.rotationOffset != null ? this.rotationOffset.clone() : null;
      copy.particleSystemIds = this.particleSystemIds != null ? new HashMap<>(this.particleSystemIds) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockParticleSet other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.color, other.color)
               && this.scale == other.scale
               && Objects.equals(this.positionOffset, other.positionOffset)
               && Objects.equals(this.rotationOffset, other.rotationOffset)
               && Objects.equals(this.particleSystemIds, other.particleSystemIds);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.color, this.scale, this.positionOffset, this.rotationOffset, this.particleSystemIds);
   }
}
