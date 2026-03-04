package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.RepulsionConfig;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
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

public class UpdateRepulsionConfig implements Packet, ToClientPacket {
   public static final int PACKET_ID = 75;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 65536011;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   public int maxId;
   @Nullable
   public Map<Integer, RepulsionConfig> repulsionConfigs;

   @Override
   public int getId() {
      return 75;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateRepulsionConfig() {
   }

   public UpdateRepulsionConfig(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, RepulsionConfig> repulsionConfigs) {
      this.type = type;
      this.maxId = maxId;
      this.repulsionConfigs = repulsionConfigs;
   }

   public UpdateRepulsionConfig(@Nonnull UpdateRepulsionConfig other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.repulsionConfigs = other.repulsionConfigs;
   }

   @Nonnull
   public static UpdateRepulsionConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateRepulsionConfig obj = new UpdateRepulsionConfig();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int repulsionConfigsCount = VarInt.peek(buf, pos);
         if (repulsionConfigsCount < 0) {
            throw ProtocolException.negativeLength("RepulsionConfigs", repulsionConfigsCount);
         }

         if (repulsionConfigsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("RepulsionConfigs", repulsionConfigsCount, 4096000);
         }

         pos += VarInt.size(repulsionConfigsCount);
         obj.repulsionConfigs = new HashMap<>(repulsionConfigsCount);

         for (int i = 0; i < repulsionConfigsCount; i++) {
            int key = buf.getIntLE(pos);
            pos += 4;
            RepulsionConfig val = RepulsionConfig.deserialize(buf, pos);
            pos += RepulsionConfig.computeBytesConsumed(buf, pos);
            if (obj.repulsionConfigs.put(key, val) != null) {
               throw ProtocolException.duplicateKey("repulsionConfigs", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < dictLen; i++) {
            pos += 4;
            pos += RepulsionConfig.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.repulsionConfigs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.repulsionConfigs != null) {
         if (this.repulsionConfigs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("RepulsionConfigs", this.repulsionConfigs.size(), 4096000);
         }

         VarInt.write(buf, this.repulsionConfigs.size());

         for (Entry<Integer, RepulsionConfig> e : this.repulsionConfigs.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.repulsionConfigs != null) {
         size += VarInt.size(this.repulsionConfigs.size()) + this.repulsionConfigs.size() * 16;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 6) {
         return ValidationResult.error("Buffer too small: expected at least 6 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int repulsionConfigsCount = VarInt.peek(buffer, pos);
            if (repulsionConfigsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for RepulsionConfigs");
            }

            if (repulsionConfigsCount > 4096000) {
               return ValidationResult.error("RepulsionConfigs exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < repulsionConfigsCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += 12;
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateRepulsionConfig clone() {
      UpdateRepulsionConfig copy = new UpdateRepulsionConfig();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.repulsionConfigs != null) {
         Map<Integer, RepulsionConfig> m = new HashMap<>();

         for (Entry<Integer, RepulsionConfig> e : this.repulsionConfigs.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.repulsionConfigs = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateRepulsionConfig other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.repulsionConfigs, other.repulsionConfigs);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.repulsionConfigs);
   }
}
