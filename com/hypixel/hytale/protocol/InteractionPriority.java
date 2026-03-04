package com.hypixel.hytale.protocol;

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

public class InteractionPriority {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 20480006;
   @Nullable
   public Map<PrioritySlot, Integer> values;

   public InteractionPriority() {
   }

   public InteractionPriority(@Nullable Map<PrioritySlot, Integer> values) {
      this.values = values;
   }

   public InteractionPriority(@Nonnull InteractionPriority other) {
      this.values = other.values;
   }

   @Nonnull
   public static InteractionPriority deserialize(@Nonnull ByteBuf buf, int offset) {
      InteractionPriority obj = new InteractionPriority();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int valuesCount = VarInt.peek(buf, pos);
         if (valuesCount < 0) {
            throw ProtocolException.negativeLength("Values", valuesCount);
         }

         if (valuesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Values", valuesCount, 4096000);
         }

         pos += VarInt.size(valuesCount);
         obj.values = new HashMap<>(valuesCount);

         for (int i = 0; i < valuesCount; i++) {
            PrioritySlot key = PrioritySlot.fromValue(buf.getByte(pos));
            int val = buf.getIntLE(++pos);
            pos += 4;
            if (obj.values.put(key, val) != null) {
               throw ProtocolException.duplicateKey("values", key);
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
            pos = ++pos + 4;
         }
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.values != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.values != null) {
         if (this.values.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Values", this.values.size(), 4096000);
         }

         VarInt.write(buf, this.values.size());

         for (Entry<PrioritySlot, Integer> e : this.values.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            buf.writeIntLE(e.getValue());
         }
      }
   }

   public int computeSize() {
      int size = 1;
      if (this.values != null) {
         size += VarInt.size(this.values.size()) + this.values.size() * 5;
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
            int valuesCount = VarInt.peek(buffer, pos);
            if (valuesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Values");
            }

            if (valuesCount > 4096000) {
               return ValidationResult.error("Values exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < valuesCount; i++) {
               pos = ++pos + 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public InteractionPriority clone() {
      InteractionPriority copy = new InteractionPriority();
      copy.values = this.values != null ? new HashMap<>(this.values) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof InteractionPriority other ? Objects.equals(this.values, other.values) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.values);
   }
}
