package com.hypixel.hytale.protocol.packets.setup;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
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

public class ServerTags implements Packet, ToClientPacket {
   public static final int PACKET_ID = 34;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public Map<String, Integer> tags;

   @Override
   public int getId() {
      return 34;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ServerTags() {
   }

   public ServerTags(@Nullable Map<String, Integer> tags) {
      this.tags = tags;
   }

   public ServerTags(@Nonnull ServerTags other) {
      this.tags = other.tags;
   }

   @Nonnull
   public static ServerTags deserialize(@Nonnull ByteBuf buf, int offset) {
      ServerTags obj = new ServerTags();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int tagsCount = VarInt.peek(buf, pos);
         if (tagsCount < 0) {
            throw ProtocolException.negativeLength("Tags", tagsCount);
         }

         if (tagsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Tags", tagsCount, 4096000);
         }

         pos += VarInt.size(tagsCount);
         obj.tags = new HashMap<>(tagsCount);

         for (int i = 0; i < tagsCount; i++) {
            int keyLen = VarInt.peek(buf, pos);
            if (keyLen < 0) {
               throw ProtocolException.negativeLength("key", keyLen);
            }

            if (keyLen > 4096000) {
               throw ProtocolException.stringTooLong("key", keyLen, 4096000);
            }

            int keyVarLen = VarInt.length(buf, pos);
            String key = PacketIO.readVarString(buf, pos);
            pos += keyVarLen + keyLen;
            int val = buf.getIntLE(pos);
            pos += 4;
            if (obj.tags.put(key, val) != null) {
               throw ProtocolException.duplicateKey("tags", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.length(buf, pos) + sl;
            pos += 4;
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.tags != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.tags != null) {
         if (this.tags.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Tags", this.tags.size(), 4096000);
         }

         VarInt.write(buf, this.tags.size());

         for (Entry<String, Integer> e : this.tags.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            buf.writeIntLE(e.getValue());
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.tags != null) {
         int tagsSize = 0;

         for (Entry<String, Integer> kvp : this.tags.entrySet()) {
            tagsSize += PacketIO.stringSize(kvp.getKey()) + 4;
         }

         size += VarInt.size(this.tags.size()) + tagsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int tagsCount = VarInt.peek(buffer, pos);
            if (tagsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Tags");
            }

            if (tagsCount > 4096000) {
               return ValidationResult.error("Tags exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < tagsCount; i++) {
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

               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public ServerTags clone() {
      ServerTags copy = new ServerTags();
      copy.tags = this.tags != null ? new HashMap<>(this.tags) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof ServerTags other ? Objects.equals(this.tags, other.tags) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.tags);
   }
}
