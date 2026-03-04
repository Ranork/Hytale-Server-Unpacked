package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AnimationSet {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public Animation[] animations;
   @Nullable
   public Rangef nextAnimationDelay;

   public AnimationSet() {
   }

   public AnimationSet(@Nullable String id, @Nullable Animation[] animations, @Nullable Rangef nextAnimationDelay) {
      this.id = id;
      this.animations = animations;
      this.nextAnimationDelay = nextAnimationDelay;
   }

   public AnimationSet(@Nonnull AnimationSet other) {
      this.id = other.id;
      this.animations = other.animations;
      this.nextAnimationDelay = other.nextAnimationDelay;
   }

   @Nonnull
   public static AnimationSet deserialize(@Nonnull ByteBuf buf, int offset) {
      AnimationSet obj = new AnimationSet();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.nextAnimationDelay = Rangef.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         int varPos0 = offset + 17 + buf.getIntLE(offset + 9);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 4) != 0) {
         int varPos1 = offset + 17 + buf.getIntLE(offset + 13);
         int animationsCount = VarInt.peek(buf, varPos1);
         if (animationsCount < 0) {
            throw ProtocolException.negativeLength("Animations", animationsCount);
         }

         if (animationsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Animations", animationsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + animationsCount * 22L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Animations", varPos1 + varIntLen + animationsCount * 22, buf.readableBytes());
         }

         obj.animations = new Animation[animationsCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < animationsCount; i++) {
            obj.animations[i] = Animation.deserialize(buf, elemPos);
            elemPos += Animation.computeBytesConsumed(buf, elemPos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 17;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 9);
         int pos0 = offset + 17 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 13);
         int pos1 = offset + 17 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += Animation.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.nextAnimationDelay != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.animations != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      if (this.nextAnimationDelay != null) {
         this.nextAnimationDelay.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int animationsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.animations != null) {
         buf.setIntLE(animationsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.animations.length > 4096000) {
            throw ProtocolException.arrayTooLong("Animations", this.animations.length, 4096000);
         }

         VarInt.write(buf, this.animations.length);

         for (Animation item : this.animations) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(animationsOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 17;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.animations != null) {
         int animationsSize = 0;

         for (Animation elem : this.animations) {
            animationsSize += elem.computeSize();
         }

         size += VarInt.size(this.animations.length) + animationsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 17) {
         return ValidationResult.error("Buffer too small: expected at least 17 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 2) != 0) {
            int idOffset = buffer.getIntLE(offset + 9);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 17 + idOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Id");
            }

            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 4) != 0) {
            int animationsOffset = buffer.getIntLE(offset + 13);
            if (animationsOffset < 0) {
               return ValidationResult.error("Invalid offset for Animations");
            }

            int posx = offset + 17 + animationsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Animations");
            }

            int animationsCount = VarInt.peek(buffer, posx);
            if (animationsCount < 0) {
               return ValidationResult.error("Invalid array count for Animations");
            }

            if (animationsCount > 4096000) {
               return ValidationResult.error("Animations exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < animationsCount; i++) {
               ValidationResult structResult = Animation.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid Animation in Animations[" + i + "]: " + structResult.error());
               }

               posx += Animation.computeBytesConsumed(buffer, posx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public AnimationSet clone() {
      AnimationSet copy = new AnimationSet();
      copy.id = this.id;
      copy.animations = this.animations != null ? Arrays.stream(this.animations).map(e -> e.clone()).toArray(Animation[]::new) : null;
      copy.nextAnimationDelay = this.nextAnimationDelay != null ? this.nextAnimationDelay.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AnimationSet other)
            ? false
            : Objects.equals(this.id, other.id)
               && Arrays.equals((Object[])this.animations, (Object[])other.animations)
               && Objects.equals(this.nextAnimationDelay, other.nextAnimationDelay);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Arrays.hashCode((Object[])this.animations);
      return 31 * result + Objects.hashCode(this.nextAnimationDelay);
   }
}
