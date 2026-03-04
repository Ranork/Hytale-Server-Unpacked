package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ParticleSystem;
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

public class UpdateParticleSystems implements Packet, ToClientPacket {
   public static final int PACKET_ID = 49;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 10;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   @Nullable
   public Map<String, ParticleSystem> particleSystems;
   @Nullable
   public String[] removedParticleSystems;

   @Override
   public int getId() {
      return 49;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateParticleSystems() {
   }

   public UpdateParticleSystems(@Nonnull UpdateType type, @Nullable Map<String, ParticleSystem> particleSystems, @Nullable String[] removedParticleSystems) {
      this.type = type;
      this.particleSystems = particleSystems;
      this.removedParticleSystems = removedParticleSystems;
   }

   public UpdateParticleSystems(@Nonnull UpdateParticleSystems other) {
      this.type = other.type;
      this.particleSystems = other.particleSystems;
      this.removedParticleSystems = other.removedParticleSystems;
   }

   @Nonnull
   public static UpdateParticleSystems deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateParticleSystems obj = new UpdateParticleSystems();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 10 + buf.getIntLE(offset + 2);
         int particleSystemsCount = VarInt.peek(buf, varPos0);
         if (particleSystemsCount < 0) {
            throw ProtocolException.negativeLength("ParticleSystems", particleSystemsCount);
         }

         if (particleSystemsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ParticleSystems", particleSystemsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
         obj.particleSystems = new HashMap<>(particleSystemsCount);
         int dictPos = varPos0 + varIntLen;

         for (int i = 0; i < particleSystemsCount; i++) {
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
            ParticleSystem val = ParticleSystem.deserialize(buf, dictPos);
            dictPos += ParticleSystem.computeBytesConsumed(buf, dictPos);
            if (obj.particleSystems.put(key, val) != null) {
               throw ProtocolException.duplicateKey("particleSystems", key);
            }
         }
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 10 + buf.getIntLE(offset + 6);
         int removedParticleSystemsCount = VarInt.peek(buf, varPos1);
         if (removedParticleSystemsCount < 0) {
            throw ProtocolException.negativeLength("RemovedParticleSystems", removedParticleSystemsCount);
         }

         if (removedParticleSystemsCount > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedParticleSystems", removedParticleSystemsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + removedParticleSystemsCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("RemovedParticleSystems", varPos1 + varIntLen + removedParticleSystemsCount * 1, buf.readableBytes());
         }

         obj.removedParticleSystems = new String[removedParticleSystemsCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < removedParticleSystemsCount; i++) {
            int strLen = VarInt.peek(buf, elemPos);
            if (strLen < 0) {
               throw ProtocolException.negativeLength("removedParticleSystems[" + i + "]", strLen);
            }

            if (strLen > 4096000) {
               throw ProtocolException.stringTooLong("removedParticleSystems[" + i + "]", strLen, 4096000);
            }

            int strVarLen = VarInt.length(buf, elemPos);
            obj.removedParticleSystems[i] = PacketIO.readVarString(buf, elemPos);
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
            pos0 += ParticleSystem.computeBytesConsumed(buf, pos0);
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
      if (this.particleSystems != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.removedParticleSystems != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      int particleSystemsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int removedParticleSystemsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.particleSystems != null) {
         buf.setIntLE(particleSystemsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.particleSystems.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ParticleSystems", this.particleSystems.size(), 4096000);
         }

         VarInt.write(buf, this.particleSystems.size());

         for (Entry<String, ParticleSystem> e : this.particleSystems.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(particleSystemsOffsetSlot, -1);
      }

      if (this.removedParticleSystems != null) {
         buf.setIntLE(removedParticleSystemsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.removedParticleSystems.length > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedParticleSystems", this.removedParticleSystems.length, 4096000);
         }

         VarInt.write(buf, this.removedParticleSystems.length);

         for (String item : this.removedParticleSystems) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(removedParticleSystemsOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 10;
      if (this.particleSystems != null) {
         int particleSystemsSize = 0;

         for (Entry<String, ParticleSystem> kvp : this.particleSystems.entrySet()) {
            particleSystemsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.particleSystems.size()) + particleSystemsSize;
      }

      if (this.removedParticleSystems != null) {
         int removedParticleSystemsSize = 0;

         for (String elem : this.removedParticleSystems) {
            removedParticleSystemsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.removedParticleSystems.length) + removedParticleSystemsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 10) {
         return ValidationResult.error("Buffer too small: expected at least 10 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int particleSystemsOffset = buffer.getIntLE(offset + 2);
            if (particleSystemsOffset < 0) {
               return ValidationResult.error("Invalid offset for ParticleSystems");
            }

            int pos = offset + 10 + particleSystemsOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ParticleSystems");
            }

            int particleSystemsCount = VarInt.peek(buffer, pos);
            if (particleSystemsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ParticleSystems");
            }

            if (particleSystemsCount > 4096000) {
               return ValidationResult.error("ParticleSystems exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < particleSystemsCount; i++) {
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

               pos += ParticleSystem.computeBytesConsumed(buffer, pos);
            }
         }

         if ((nullBits & 2) != 0) {
            int removedParticleSystemsOffset = buffer.getIntLE(offset + 6);
            if (removedParticleSystemsOffset < 0) {
               return ValidationResult.error("Invalid offset for RemovedParticleSystems");
            }

            int posx = offset + 10 + removedParticleSystemsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for RemovedParticleSystems");
            }

            int removedParticleSystemsCount = VarInt.peek(buffer, posx);
            if (removedParticleSystemsCount < 0) {
               return ValidationResult.error("Invalid array count for RemovedParticleSystems");
            }

            if (removedParticleSystemsCount > 4096000) {
               return ValidationResult.error("RemovedParticleSystems exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < removedParticleSystemsCount; i++) {
               int strLen = VarInt.peek(buffer, posx);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in RemovedParticleSystems");
               }

               posx += VarInt.length(buffer, posx);
               posx += strLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in RemovedParticleSystems");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateParticleSystems clone() {
      UpdateParticleSystems copy = new UpdateParticleSystems();
      copy.type = this.type;
      if (this.particleSystems != null) {
         Map<String, ParticleSystem> m = new HashMap<>();

         for (Entry<String, ParticleSystem> e : this.particleSystems.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.particleSystems = m;
      }

      copy.removedParticleSystems = this.removedParticleSystems != null ? Arrays.copyOf(this.removedParticleSystems, this.removedParticleSystems.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateParticleSystems other)
            ? false
            : Objects.equals(this.type, other.type)
               && Objects.equals(this.particleSystems, other.particleSystems)
               && Arrays.equals((Object[])this.removedParticleSystems, (Object[])other.removedParticleSystems);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.particleSystems);
      return 31 * result + Arrays.hashCode((Object[])this.removedParticleSystems);
   }
}
