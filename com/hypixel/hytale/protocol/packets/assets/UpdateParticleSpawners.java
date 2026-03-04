package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ParticleSpawner;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateParticleSpawners implements Packet, ToClientPacket {
   public static final int PACKET_ID = 50;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 10;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   @Nullable
   public Map<String, ParticleSpawner> particleSpawners;
   @Nullable
   public String[] removedParticleSpawners;

   @Override
   public int getId() {
      return 50;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateParticleSpawners() {
   }

   public UpdateParticleSpawners(@Nonnull UpdateType type, @Nullable Map<String, ParticleSpawner> particleSpawners, @Nullable String[] removedParticleSpawners) {
      this.type = type;
      this.particleSpawners = particleSpawners;
      this.removedParticleSpawners = removedParticleSpawners;
   }

   public UpdateParticleSpawners(@Nonnull UpdateParticleSpawners other) {
      this.type = other.type;
      this.particleSpawners = other.particleSpawners;
      this.removedParticleSpawners = other.removedParticleSpawners;
   }

   @Nonnull
   public static UpdateParticleSpawners deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateParticleSpawners obj = new UpdateParticleSpawners();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 10 + buf.getIntLE(offset + 2);
         int particleSpawnersCount = VarInt.peek(buf, varPos0);
         if (particleSpawnersCount < 0) {
            throw ProtocolException.negativeLength("ParticleSpawners", particleSpawnersCount);
         }

         if (particleSpawnersCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ParticleSpawners", particleSpawnersCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
         obj.particleSpawners = new HashMap<>(particleSpawnersCount);
         int dictPos = varPos0 + varIntLen;

         for (int i = 0; i < particleSpawnersCount; i++) {
            int keyLen = VarInt.peek(buf, dictPos);
            if (keyLen < 0) {
               throw ProtocolException.negativeLength("key", keyLen);
            }

            if (keyLen > 4096000) {
               throw ProtocolException.stringTooLong("key", keyLen, 4096000);
            }

            int keyVarLen = VarInt.length(buf, dictPos);
            String key = PacketIO.readVarString(buf, dictPos);
            dictPos += keyVarLen + keyLen;
            ParticleSpawner val = ParticleSpawner.deserialize(buf, dictPos);
            dictPos += ParticleSpawner.computeBytesConsumed(buf, dictPos);
            if (obj.particleSpawners.put(key, val) != null) {
               throw ProtocolException.duplicateKey("particleSpawners", key);
            }
         }
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 10 + buf.getIntLE(offset + 6);
         int removedParticleSpawnersCount = VarInt.peek(buf, varPos1);
         if (removedParticleSpawnersCount < 0) {
            throw ProtocolException.negativeLength("RemovedParticleSpawners", removedParticleSpawnersCount);
         }

         if (removedParticleSpawnersCount > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedParticleSpawners", removedParticleSpawnersCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + removedParticleSpawnersCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("RemovedParticleSpawners", varPos1 + varIntLen + removedParticleSpawnersCount * 1, buf.readableBytes());
         }

         obj.removedParticleSpawners = new String[removedParticleSpawnersCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < removedParticleSpawnersCount; i++) {
            int strLen = VarInt.peek(buf, elemPos);
            if (strLen < 0) {
               throw ProtocolException.negativeLength("removedParticleSpawners[" + i + "]", strLen);
            }

            if (strLen > 4096000) {
               throw ProtocolException.stringTooLong("removedParticleSpawners[" + i + "]", strLen, 4096000);
            }

            int strVarLen = VarInt.length(buf, elemPos);
            obj.removedParticleSpawners[i] = PacketIO.readVarString(buf, elemPos);
            elemPos += strVarLen + strLen;
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 10;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 2);
         int pos0 = offset + 10 + fieldOffset0;
         int dictLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.length(buf, pos0) + sl;
            pos0 += ParticleSpawner.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 6);
         int pos1 = offset + 10 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos1);
            pos1 += VarInt.length(buf, pos1) + sl;
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.particleSpawners != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.removedParticleSpawners != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      int particleSpawnersOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int removedParticleSpawnersOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.particleSpawners != null) {
         buf.setIntLE(particleSpawnersOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.particleSpawners.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ParticleSpawners", this.particleSpawners.size(), 4096000);
         }

         VarInt.write(buf, this.particleSpawners.size());

         for (Entry<String, ParticleSpawner> e : this.particleSpawners.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(particleSpawnersOffsetSlot, -1);
      }

      if (this.removedParticleSpawners != null) {
         buf.setIntLE(removedParticleSpawnersOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.removedParticleSpawners.length > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedParticleSpawners", this.removedParticleSpawners.length, 4096000);
         }

         VarInt.write(buf, this.removedParticleSpawners.length);

         for (String item : this.removedParticleSpawners) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(removedParticleSpawnersOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 10;
      if (this.particleSpawners != null) {
         int particleSpawnersSize = 0;

         for (Entry<String, ParticleSpawner> kvp : this.particleSpawners.entrySet()) {
            particleSpawnersSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.particleSpawners.size()) + particleSpawnersSize;
      }

      if (this.removedParticleSpawners != null) {
         int removedParticleSpawnersSize = 0;

         for (String elem : this.removedParticleSpawners) {
            removedParticleSpawnersSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.removedParticleSpawners.length) + removedParticleSpawnersSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 10) {
         return ValidationResult.error("Buffer too small: expected at least 10 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int particleSpawnersOffset = buffer.getIntLE(offset + 2);
            if (particleSpawnersOffset < 0) {
               return ValidationResult.error("Invalid offset for ParticleSpawners");
            }

            int pos = offset + 10 + particleSpawnersOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ParticleSpawners");
            }

            int particleSpawnersCount = VarInt.peek(buffer, pos);
            if (particleSpawnersCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ParticleSpawners");
            }

            if (particleSpawnersCount > 4096000) {
               return ValidationResult.error("ParticleSpawners exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < particleSpawnersCount; i++) {
               int keyLen = VarInt.peek(buffer, pos);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               pos += VarInt.length(buffer, pos);
               pos += keyLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += ParticleSpawner.computeBytesConsumed(buffer, pos);
            }
         }

         if ((nullBits & 2) != 0) {
            int removedParticleSpawnersOffset = buffer.getIntLE(offset + 6);
            if (removedParticleSpawnersOffset < 0) {
               return ValidationResult.error("Invalid offset for RemovedParticleSpawners");
            }

            int posx = offset + 10 + removedParticleSpawnersOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for RemovedParticleSpawners");
            }

            int removedParticleSpawnersCount = VarInt.peek(buffer, posx);
            if (removedParticleSpawnersCount < 0) {
               return ValidationResult.error("Invalid array count for RemovedParticleSpawners");
            }

            if (removedParticleSpawnersCount > 4096000) {
               return ValidationResult.error("RemovedParticleSpawners exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < removedParticleSpawnersCount; i++) {
               int strLen = VarInt.peek(buffer, posx);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in RemovedParticleSpawners");
               }

               posx += VarInt.length(buffer, posx);
               posx += strLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in RemovedParticleSpawners");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateParticleSpawners clone() {
      UpdateParticleSpawners copy = new UpdateParticleSpawners();
      copy.type = this.type;
      if (this.particleSpawners != null) {
         Map<String, ParticleSpawner> m = new HashMap<>();

         for (Entry<String, ParticleSpawner> e : this.particleSpawners.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.particleSpawners = m;
      }

      copy.removedParticleSpawners = this.removedParticleSpawners != null
         ? Arrays.copyOf(this.removedParticleSpawners, this.removedParticleSpawners.length)
         : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateParticleSpawners other)
            ? false
            : Objects.equals(this.type, other.type)
               && Objects.equals(this.particleSpawners, other.particleSpawners)
               && Arrays.equals((Object[])this.removedParticleSpawners, (Object[])other.removedParticleSpawners);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.particleSpawners);
      return 31 * result + Arrays.hashCode((Object[])this.removedParticleSpawners);
   }
}
