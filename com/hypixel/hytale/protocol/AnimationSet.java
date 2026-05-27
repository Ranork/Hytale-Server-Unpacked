package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 17) {
         throw ProtocolException.bufferTooSmall("AnimationSet", 17, buf.readableBytes() - offset);
      } else {
         AnimationSet obj = new AnimationSet();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.nextAnimationDelay = Rangef.deserialize(buf, offset + 1);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 9);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 17 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            }

            int idVarIntLen = VarInt.size(idLen);
            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("Id", idLen, 4096000);
            }

            if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
            }

            obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 13);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("Animations", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 17 + varPosBase1;
            int animationsCount = VarInt.peek(buf, varPos1);
            if (animationsCount < 0) {
               throw ProtocolException.invalidVarInt("Animations");
            }

            int varIntLen = VarInt.size(animationsCount);
            if (animationsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Animations", animationsCount, 4096000);
            }

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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 17;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 9);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 17 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 13);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("Animations", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 17 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += Animation.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 17L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 9, 17, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static Animation[] getAnimations(MemorySegment mem) {
      return getAnimations(mem, 0);
   }

   @Nullable
   public static Animation[] getAnimations(MemorySegment mem, int offset) {
      if (!hasAnimations(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 13, 17, "Animations");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Animations", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Animations", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Animations", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               Animation[] data = new Animation[len];

               for (int i = 0; i < len; i++) {
                  data[i] = Animation.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static Rangef getNextAnimationDelay(MemorySegment mem) {
      return getNextAnimationDelay(mem, 0);
   }

   @Nullable
   public static Rangef getNextAnimationDelay(MemorySegment mem, int offset) {
      return hasNextAnimationDelay(mem, offset) ? Rangef.toObject(mem, offset + 1) : null;
   }

   public static boolean hasNextAnimationDelay(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasAnimations(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static AnimationSet toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AnimationSet toObject(MemorySegment mem, int offset) {
      if (offset + 17 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AnimationSet", offset + 17, (int)mem.byteSize());
      } else {
         Animation[] animations = null;
         if (hasAnimations(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 13, 17, "Animations");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Animations", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Animations", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Animations", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            animations = new Animation[len];

            for (int i = 0; i < len; i++) {
               animations[i] = Animation.toObject(mem, off);
               off += animations[i].computeSize();
            }
         }

         return new AnimationSet(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 9, 17, "Id"), 4096000, PacketIO.UTF8) : null,
            animations,
            hasNextAnimationDelay(mem, offset) ? Rangef.toObject(mem, offset + 1) : null
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.nextAnimationDelay != null) {
         this.nextAnimationDelay.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 8L).fill((byte)0);
      }

      int varOffset = offset + 17;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 17);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      if (this.animations != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 17);
         if (this.animations.length > 4096000) {
            throw ProtocolException.arrayTooLong("Animations", this.animations.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.animations.length);
         int animationsValueOffset = 0;

         for (int i = 0; i < this.animations.length; i++) {
            animationsValueOffset += this.animations[i].serialize(mem, varOffset + animationsValueOffset);
         }

         varOffset += animationsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      return varOffset - offset;
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
            if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 17 + idOffset;
            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.size(idLen);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 4) != 0) {
            int animationsOffset = buffer.getIntLE(offset + 13);
            if (animationsOffset < 0 || animationsOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for Animations");
            }

            int posx = offset + 17 + animationsOffset;
            int animationsCount = VarInt.peek(buffer, posx);
            if (animationsCount < 0) {
               return ValidationResult.error("Invalid array count for Animations");
            }

            if (animationsCount > 4096000) {
               return ValidationResult.error("Animations exceeds max length 4096000");
            }

            posx += VarInt.size(animationsCount);

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
