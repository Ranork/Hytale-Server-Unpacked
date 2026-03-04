package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
   public Vector3f positionOffset;
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
      @Nullable Vector3f positionOffset,
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
      BlockParticleSet obj = new BlockParticleSet();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.color = Color.deserialize(buf, offset + 1);
      }

      obj.scale = buf.getFloatLE(offset + 4);
      if ((nullBits & 2) != 0) {
         obj.positionOffset = Vector3f.deserialize(buf, offset + 8);
      }

      if ((nullBits & 4) != 0) {
         obj.rotationOffset = Direction.deserialize(buf, offset + 20);
      }

      if ((nullBits & 8) != 0) {
         int varPos0 = offset + 40 + buf.getIntLE(offset + 32);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 16) != 0) {
         int varPos1 = offset + 40 + buf.getIntLE(offset + 36);
         int particleSystemIdsCount = VarInt.peek(buf, varPos1);
         if (particleSystemIdsCount < 0) {
            throw ProtocolException.negativeLength("ParticleSystemIds", particleSystemIdsCount);
         }

         if (particleSystemIdsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ParticleSystemIds", particleSystemIdsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         obj.particleSystemIds = new HashMap<>(particleSystemIdsCount);
         int dictPos = varPos1 + varIntLen;

         for (int i = 0; i < particleSystemIdsCount; i++) {
            BlockParticleEvent key = BlockParticleEvent.fromValue(buf.getByte(dictPos));
            int valLen = VarInt.peek(buf, ++dictPos);
            if (valLen < 0) {
               throw ProtocolException.negativeLength("val", valLen);
            }

            if (valLen > 4096000) {
               throw ProtocolException.stringTooLong("val", valLen, 4096000);
            }

            int valVarLen = VarInt.length(buf, dictPos);
            String val = PacketIO.readVarString(buf, dictPos);
            dictPos += valVarLen + valLen;
            if (obj.particleSystemIds.put(key, val) != null) {
               throw ProtocolException.duplicateKey("particleSystemIds", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 40;
      if ((nullBits & 8) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 32);
         int pos0 = offset + 40 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 36);
         int pos1 = offset + 40 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, ++pos1);
            pos1 += VarInt.length(buf, pos1) + sl;
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
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
         this.positionOffset.serialize(buf);
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
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 40 + idOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Id");
            }

            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 16) != 0) {
            int particleSystemIdsOffset = buffer.getIntLE(offset + 36);
            if (particleSystemIdsOffset < 0) {
               return ValidationResult.error("Invalid offset for ParticleSystemIds");
            }

            int posx = offset + 40 + particleSystemIdsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ParticleSystemIds");
            }

            int particleSystemIdsCount = VarInt.peek(buffer, posx);
            if (particleSystemIdsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ParticleSystemIds");
            }

            if (particleSystemIdsCount > 4096000) {
               return ValidationResult.error("ParticleSystemIds exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < particleSystemIdsCount; i++) {
               int valueLen = VarInt.peek(buffer, ++posx);
               if (valueLen < 0) {
                  return ValidationResult.error("Invalid string length for value");
               }

               if (valueLen > 4096000) {
                  return ValidationResult.error("value exceeds max length 4096000");
               }

               posx += VarInt.length(buffer, posx);
               posx += valueLen;
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
      copy.positionOffset = this.positionOffset != null ? this.positionOffset.clone() : null;
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
