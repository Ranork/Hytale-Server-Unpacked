package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.EntityStatType;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
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

public class UpdateEntityStatTypes implements Packet, ToClientPacket {
   public static final int PACKET_ID = 72;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   public int maxId;
   @Nullable
   public Map<Integer, EntityStatType> types;

   @Override
   public int getId() {
      return 72;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateEntityStatTypes() {
   }

   public UpdateEntityStatTypes(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, EntityStatType> types) {
      this.type = type;
      this.maxId = maxId;
      this.types = types;
   }

   public UpdateEntityStatTypes(@Nonnull UpdateEntityStatTypes other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.types = other.types;
   }

   @Nonnull
   public static UpdateEntityStatTypes deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateEntityStatTypes obj = new UpdateEntityStatTypes();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int typesCount = VarInt.peek(buf, pos);
         if (typesCount < 0) {
            throw ProtocolException.negativeLength("Types", typesCount);
         }

         if (typesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Types", typesCount, 4096000);
         }

         pos += VarInt.size(typesCount);
         obj.types = new HashMap<>(typesCount);

         for (int i = 0; i < typesCount; i++) {
            int key = buf.getIntLE(pos);
            pos += 4;
            EntityStatType val = EntityStatType.deserialize(buf, pos);
            pos += EntityStatType.computeBytesConsumed(buf, pos);
            if (obj.types.put(key, val) != null) {
               throw ProtocolException.duplicateKey("types", key);
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
            pos += EntityStatType.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.types != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.types != null) {
         if (this.types.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Types", this.types.size(), 4096000);
         }

         VarInt.write(buf, this.types.size());

         for (Entry<Integer, EntityStatType> e : this.types.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.types != null) {
         int typesSize = 0;

         for (Entry<Integer, EntityStatType> kvp : this.types.entrySet()) {
            typesSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.types.size()) + typesSize;
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
            int typesCount = VarInt.peek(buffer, pos);
            if (typesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Types");
            }

            if (typesCount > 4096000) {
               return ValidationResult.error("Types exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < typesCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += EntityStatType.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateEntityStatTypes clone() {
      UpdateEntityStatTypes copy = new UpdateEntityStatTypes();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.types != null) {
         Map<Integer, EntityStatType> m = new HashMap<>();

         for (Entry<Integer, EntityStatType> e : this.types.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.types = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateEntityStatTypes other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.types, other.types);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.types);
   }
}
