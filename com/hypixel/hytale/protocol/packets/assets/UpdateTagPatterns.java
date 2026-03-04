package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.TagPattern;
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

public class UpdateTagPatterns implements Packet, ToClientPacket {
   public static final int PACKET_ID = 84;
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
   public Map<Integer, TagPattern> patterns;

   @Override
   public int getId() {
      return 84;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateTagPatterns() {
   }

   public UpdateTagPatterns(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, TagPattern> patterns) {
      this.type = type;
      this.maxId = maxId;
      this.patterns = patterns;
   }

   public UpdateTagPatterns(@Nonnull UpdateTagPatterns other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.patterns = other.patterns;
   }

   @Nonnull
   public static UpdateTagPatterns deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateTagPatterns obj = new UpdateTagPatterns();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int patternsCount = VarInt.peek(buf, pos);
         if (patternsCount < 0) {
            throw ProtocolException.negativeLength("Patterns", patternsCount);
         }

         if (patternsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Patterns", patternsCount, 4096000);
         }

         pos += VarInt.size(patternsCount);
         obj.patterns = new HashMap<>(patternsCount);

         for (int i = 0; i < patternsCount; i++) {
            int key = buf.getIntLE(pos);
            pos += 4;
            TagPattern val = TagPattern.deserialize(buf, pos);
            pos += TagPattern.computeBytesConsumed(buf, pos);
            if (obj.patterns.put(key, val) != null) {
               throw ProtocolException.duplicateKey("patterns", key);
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
            pos += TagPattern.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.patterns != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.patterns != null) {
         if (this.patterns.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Patterns", this.patterns.size(), 4096000);
         }

         VarInt.write(buf, this.patterns.size());

         for (Entry<Integer, TagPattern> e : this.patterns.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.patterns != null) {
         int patternsSize = 0;

         for (Entry<Integer, TagPattern> kvp : this.patterns.entrySet()) {
            patternsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.patterns.size()) + patternsSize;
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
            int patternsCount = VarInt.peek(buffer, pos);
            if (patternsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Patterns");
            }

            if (patternsCount > 4096000) {
               return ValidationResult.error("Patterns exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < patternsCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += TagPattern.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateTagPatterns clone() {
      UpdateTagPatterns copy = new UpdateTagPatterns();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.patterns != null) {
         Map<Integer, TagPattern> m = new HashMap<>();

         for (Entry<Integer, TagPattern> e : this.patterns.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.patterns = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateTagPatterns other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.patterns, other.patterns);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.patterns);
   }
}
