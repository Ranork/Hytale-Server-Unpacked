package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      HitEntity obj = new HitEntity();
      byte nullBits = buf.getByte(offset);
      obj.next = buf.getIntLE(offset + 1);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int matchersCount = VarInt.peek(buf, pos);
         if (matchersCount < 0) {
            throw ProtocolException.negativeLength("Matchers", matchersCount);
         }

         if (matchersCount > 4096000) {
            throw ProtocolException.arrayTooLong("Matchers", matchersCount, 4096000);
         }

         int matchersVarLen = VarInt.size(matchersCount);
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < arrLen; i++) {
            pos += EntityMatcher.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
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

            pos += VarInt.length(buffer, pos);
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
