package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ProjectileConfig;
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

public class UpdateProjectileConfigs implements Packet, ToClientPacket {
   public static final int PACKET_ID = 85;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 10;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   @Nullable
   public Map<String, ProjectileConfig> configs;
   @Nullable
   public String[] removedConfigs;

   @Override
   public int getId() {
      return 85;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateProjectileConfigs() {
   }

   public UpdateProjectileConfigs(@Nonnull UpdateType type, @Nullable Map<String, ProjectileConfig> configs, @Nullable String[] removedConfigs) {
      this.type = type;
      this.configs = configs;
      this.removedConfigs = removedConfigs;
   }

   public UpdateProjectileConfigs(@Nonnull UpdateProjectileConfigs other) {
      this.type = other.type;
      this.configs = other.configs;
      this.removedConfigs = other.removedConfigs;
   }

   @Nonnull
   public static UpdateProjectileConfigs deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateProjectileConfigs obj = new UpdateProjectileConfigs();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 10 + buf.getIntLE(offset + 2);
         int configsCount = VarInt.peek(buf, varPos0);
         if (configsCount < 0) {
            throw ProtocolException.negativeLength("Configs", configsCount);
         }

         if (configsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Configs", configsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
         obj.configs = new HashMap<>(configsCount);
         int dictPos = varPos0 + varIntLen;

         for (int i = 0; i < configsCount; i++) {
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
            ProjectileConfig val = ProjectileConfig.deserialize(buf, dictPos);
            dictPos += ProjectileConfig.computeBytesConsumed(buf, dictPos);
            if (obj.configs.put(key, val) != null) {
               throw ProtocolException.duplicateKey("configs", key);
            }
         }
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 10 + buf.getIntLE(offset + 6);
         int removedConfigsCount = VarInt.peek(buf, varPos1);
         if (removedConfigsCount < 0) {
            throw ProtocolException.negativeLength("RemovedConfigs", removedConfigsCount);
         }

         if (removedConfigsCount > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedConfigs", removedConfigsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + removedConfigsCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("RemovedConfigs", varPos1 + varIntLen + removedConfigsCount * 1, buf.readableBytes());
         }

         obj.removedConfigs = new String[removedConfigsCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < removedConfigsCount; i++) {
            int strLen = VarInt.peek(buf, elemPos);
            if (strLen < 0) {
               throw ProtocolException.negativeLength("removedConfigs[" + i + "]", strLen);
            }

            if (strLen > 4096000) {
               throw ProtocolException.stringTooLong("removedConfigs[" + i + "]", strLen, 4096000);
            }

            int strVarLen = VarInt.length(buf, elemPos);
            obj.removedConfigs[i] = PacketIO.readVarString(buf, elemPos);
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
            pos0 += ProjectileConfig.computeBytesConsumed(buf, pos0);
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
      if (this.configs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.removedConfigs != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      int configsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int removedConfigsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.configs != null) {
         buf.setIntLE(configsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.configs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Configs", this.configs.size(), 4096000);
         }

         VarInt.write(buf, this.configs.size());

         for (Entry<String, ProjectileConfig> e : this.configs.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(configsOffsetSlot, -1);
      }

      if (this.removedConfigs != null) {
         buf.setIntLE(removedConfigsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.removedConfigs.length > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedConfigs", this.removedConfigs.length, 4096000);
         }

         VarInt.write(buf, this.removedConfigs.length);

         for (String item : this.removedConfigs) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(removedConfigsOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 10;
      if (this.configs != null) {
         int configsSize = 0;

         for (Entry<String, ProjectileConfig> kvp : this.configs.entrySet()) {
            configsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.configs.size()) + configsSize;
      }

      if (this.removedConfigs != null) {
         int removedConfigsSize = 0;

         for (String elem : this.removedConfigs) {
            removedConfigsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.removedConfigs.length) + removedConfigsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 10) {
         return ValidationResult.error("Buffer too small: expected at least 10 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int configsOffset = buffer.getIntLE(offset + 2);
            if (configsOffset < 0) {
               return ValidationResult.error("Invalid offset for Configs");
            }

            int pos = offset + 10 + configsOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Configs");
            }

            int configsCount = VarInt.peek(buffer, pos);
            if (configsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Configs");
            }

            if (configsCount > 4096000) {
               return ValidationResult.error("Configs exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < configsCount; i++) {
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

               pos += ProjectileConfig.computeBytesConsumed(buffer, pos);
            }
         }

         if ((nullBits & 2) != 0) {
            int removedConfigsOffset = buffer.getIntLE(offset + 6);
            if (removedConfigsOffset < 0) {
               return ValidationResult.error("Invalid offset for RemovedConfigs");
            }

            int posx = offset + 10 + removedConfigsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for RemovedConfigs");
            }

            int removedConfigsCount = VarInt.peek(buffer, posx);
            if (removedConfigsCount < 0) {
               return ValidationResult.error("Invalid array count for RemovedConfigs");
            }

            if (removedConfigsCount > 4096000) {
               return ValidationResult.error("RemovedConfigs exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < removedConfigsCount; i++) {
               int strLen = VarInt.peek(buffer, posx);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in RemovedConfigs");
               }

               posx += VarInt.length(buffer, posx);
               posx += strLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in RemovedConfigs");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateProjectileConfigs clone() {
      UpdateProjectileConfigs copy = new UpdateProjectileConfigs();
      copy.type = this.type;
      if (this.configs != null) {
         Map<String, ProjectileConfig> m = new HashMap<>();

         for (Entry<String, ProjectileConfig> e : this.configs.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.configs = m;
      }

      copy.removedConfigs = this.removedConfigs != null ? Arrays.copyOf(this.removedConfigs, this.removedConfigs.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateProjectileConfigs other)
            ? false
            : Objects.equals(this.type, other.type)
               && Objects.equals(this.configs, other.configs)
               && Arrays.equals((Object[])this.removedConfigs, (Object[])other.removedConfigs);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.configs);
      return 31 * result + Arrays.hashCode((Object[])this.removedConfigs);
   }
}
