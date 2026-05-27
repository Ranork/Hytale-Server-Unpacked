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
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("InteractionPriority", 1, buf.readableBytes() - offset);
      } else {
         InteractionPriority obj = new InteractionPriority();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int valuesCount = VarInt.peek(buf, pos);
            if (valuesCount < 0) {
               throw ProtocolException.invalidVarInt("Values");
            }

            int valuesVarLen = VarInt.size(valuesCount);
            if (valuesCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Values", valuesCount, 4096000);
            }

            pos += valuesVarLen;
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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos = ++pos + 4;
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static Map<PrioritySlot, Integer> getValues(MemorySegment mem) {
      return getValues(mem, 0);
   }

   @Nullable
   public static Map<PrioritySlot, Integer> getValues(MemorySegment mem, int offset) {
      if (!hasValues(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Values", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Values", len, 4096000);
         } else {
            Map<PrioritySlot, Integer> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               PrioritySlot key = PrioritySlot.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               int value = mem.get(PacketIO.PROTO_INT, (long)(++off));
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Values", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasValues(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static InteractionPriority toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InteractionPriority toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InteractionPriority", offset + 1, (int)mem.byteSize());
      } else {
         Map<PrioritySlot, Integer> values = null;
         if (hasValues(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Values", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Values", len, 4096000);
            }

            values = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               PrioritySlot key = PrioritySlot.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               int value = mem.get(PacketIO.PROTO_INT, (long)(++off));
               off += 4;
               if (values.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Values", key);
               }
            }
         }

         return new InteractionPriority(values);
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.values != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.values != null) {
         if (this.values.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Values", this.values.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.values.size());

         for (Entry<PrioritySlot, Integer> e : this.values.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            mem.set(PacketIO.PROTO_INT, (long)(++varOffset), e.getValue());
            varOffset += 4;
         }
      }

      return varOffset - offset;
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

            pos += VarInt.size(valuesCount);

            for (int i = 0; i < valuesCount; i++) {
               int v = buffer.getByte(pos) & 255;
               if (v >= 3) {
                  return ValidationResult.error("Invalid PrioritySlot value for key");
               }

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
