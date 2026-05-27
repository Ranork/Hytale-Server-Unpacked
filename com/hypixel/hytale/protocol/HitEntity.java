package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HitEntity {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 8192010;
   public int next;
   @Nullable
   public EntityMatcher[] matchers;

   public HitEntity() {
   }

   public HitEntity(int next, @Nullable EntityMatcher[] matchers) {
      this.next = next;
      this.matchers = matchers;
   }

   public HitEntity(@Nonnull HitEntity other) {
      this.next = other.next;
      this.matchers = other.matchers;
   }

   @Nonnull
   public static HitEntity deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("HitEntity", 5, buf.readableBytes() - offset);
      } else {
         HitEntity obj = new HitEntity();
         byte nullBits = buf.getByte(offset);
         obj.next = buf.getIntLE(offset + 1);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int matchersCount = VarInt.peek(buf, pos);
            if (matchersCount < 0) {
               throw ProtocolException.invalidVarInt("Matchers");
            }

            int matchersVarLen = VarInt.size(matchersCount);
            if (matchersCount > 4096000) {
               throw ProtocolException.arrayTooLong("Matchers", matchersCount, 4096000);
            }

            if (pos + matchersVarLen + matchersCount * 2L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Matchers", pos + matchersVarLen + matchersCount * 2, buf.readableBytes());
            }

            pos += matchersVarLen;
            obj.matchers = new EntityMatcher[matchersCount];

            for (int i = 0; i < matchersCount; i++) {
               obj.matchers[i] = EntityMatcher.deserialize(buf, pos);
               pos += EntityMatcher.computeBytesConsumed(buf, pos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos += EntityMatcher.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static int getNext(MemorySegment mem) {
      return getNext(mem, 0);
   }

   public static int getNext(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static EntityMatcher[] getMatchers(MemorySegment mem) {
      return getMatchers(mem, 0);
   }

   @Nullable
   public static EntityMatcher[] getMatchers(MemorySegment mem, int offset) {
      if (!hasMatchers(mem, offset)) {
         return null;
      } else {
         int off = offset + 5;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Matchers", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Matchers", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 2L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Matchers", off + lenOffset + len * 2, (int)mem.byteSize());
            } else {
               off += lenOffset;
               EntityMatcher[] data = new EntityMatcher[len];

               for (int i = 0; i < len; i++) {
                  data[i] = EntityMatcher.toObject(mem, off + i * 2);
               }

               return data;
            }
         }
      }
   }

   public static boolean hasMatchers(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static HitEntity toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static HitEntity toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("HitEntity", offset + 5, (int)mem.byteSize());
      } else {
         EntityMatcher[] matchers = null;
         if (hasMatchers(mem, offset)) {
            int off = offset + 5;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Matchers", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Matchers", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 2L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Matchers", off + lenOffset + len * 2, (int)mem.byteSize());
            }

            off += lenOffset;
            matchers = new EntityMatcher[len];

            for (int i = 0; i < len; i++) {
               matchers[i] = EntityMatcher.toObject(mem, off + i * 2);
            }
         }

         return new HitEntity(mem.get(PacketIO.PROTO_INT, (long)(offset + 1)), matchers);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.matchers != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.next);
      if (this.matchers != null) {
         if (this.matchers.length > 4096000) {
            throw ProtocolException.arrayTooLong("Matchers", this.matchers.length, 4096000);
         }

         VarInt.write(buf, this.matchers.length);

         for (EntityMatcher item : this.matchers) {
            item.serialize(buf);
         }
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.matchers != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.next);
      int varOffset = offset + 5;
      if (this.matchers != null) {
         if (this.matchers.length > 4096000) {
            throw ProtocolException.arrayTooLong("Matchers", this.matchers.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.matchers.length);
         int matchersValueOffset = 0;

         for (int i = 0; i < this.matchers.length; i++) {
            matchersValueOffset += this.matchers[i].serialize(mem, varOffset + matchersValueOffset);
         }

         varOffset += matchersValueOffset;
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 5;
      if (this.matchers != null) {
         size += VarInt.size(this.matchers.length) + this.matchers.length * 2;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int matchersCount = VarInt.peek(buffer, pos);
            if (matchersCount < 0) {
               return ValidationResult.error("Invalid array count for Matchers");
            }

            if (matchersCount > 4096000) {
               return ValidationResult.error("Matchers exceeds max length 4096000");
            }

            pos += VarInt.size(matchersCount);
            pos += matchersCount * 2;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Matchers");
            }
         }

         return ValidationResult.OK;
      }
   }

   public HitEntity clone() {
      HitEntity copy = new HitEntity();
      copy.next = this.next;
      copy.matchers = this.matchers != null ? Arrays.stream(this.matchers).map(e -> e.clone()).toArray(EntityMatcher[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof HitEntity other) ? false : this.next == other.next && Arrays.equals((Object[])this.matchers, (Object[])other.matchers);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.next);
      return 31 * result + Arrays.hashCode((Object[])this.matchers);
   }
}
