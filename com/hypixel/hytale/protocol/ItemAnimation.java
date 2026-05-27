package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemAnimation {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 5;
   public static final int VARIABLE_BLOCK_START = 32;
   public static final int MAX_SIZE = 81920057;
   @Nullable
   public String thirdPerson;
   @Nullable
   public String thirdPersonMoving;
   @Nullable
   public String thirdPersonFace;
   @Nullable
   public String firstPerson;
   @Nullable
   public String firstPersonOverride;
   public boolean keepPreviousFirstPersonAnimation;
   public float speed;
   public float blendingDuration = 0.2F;
   public boolean looping;
   public boolean clipsGeometry;

   public ItemAnimation() {
   }

   public ItemAnimation(
      @Nullable String thirdPerson,
      @Nullable String thirdPersonMoving,
      @Nullable String thirdPersonFace,
      @Nullable String firstPerson,
      @Nullable String firstPersonOverride,
      boolean keepPreviousFirstPersonAnimation,
      float speed,
      float blendingDuration,
      boolean looping,
      boolean clipsGeometry
   ) {
      this.thirdPerson = thirdPerson;
      this.thirdPersonMoving = thirdPersonMoving;
      this.thirdPersonFace = thirdPersonFace;
      this.firstPerson = firstPerson;
      this.firstPersonOverride = firstPersonOverride;
      this.keepPreviousFirstPersonAnimation = keepPreviousFirstPersonAnimation;
      this.speed = speed;
      this.blendingDuration = blendingDuration;
      this.looping = looping;
      this.clipsGeometry = clipsGeometry;
   }

   public ItemAnimation(@Nonnull ItemAnimation other) {
      this.thirdPerson = other.thirdPerson;
      this.thirdPersonMoving = other.thirdPersonMoving;
      this.thirdPersonFace = other.thirdPersonFace;
      this.firstPerson = other.firstPerson;
      this.firstPersonOverride = other.firstPersonOverride;
      this.keepPreviousFirstPersonAnimation = other.keepPreviousFirstPersonAnimation;
      this.speed = other.speed;
      this.blendingDuration = other.blendingDuration;
      this.looping = other.looping;
      this.clipsGeometry = other.clipsGeometry;
   }

   @Nonnull
   public static ItemAnimation deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 32) {
         throw ProtocolException.bufferTooSmall("ItemAnimation", 32, buf.readableBytes() - offset);
      } else {
         ItemAnimation obj = new ItemAnimation();
         byte nullBits = buf.getByte(offset);
         obj.keepPreviousFirstPersonAnimation = buf.getByte(offset + 1) != 0;
         obj.speed = buf.getFloatLE(offset + 2);
         obj.blendingDuration = buf.getFloatLE(offset + 6);
         obj.looping = buf.getByte(offset + 10) != 0;
         obj.clipsGeometry = buf.getByte(offset + 11) != 0;
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 12);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 32) {
               throw ProtocolException.invalidOffset("ThirdPerson", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 32 + varPosBase0;
            int thirdPersonLen = VarInt.peek(buf, varPos0);
            if (thirdPersonLen < 0) {
               throw ProtocolException.invalidVarInt("ThirdPerson");
            }

            int thirdPersonVarIntLen = VarInt.size(thirdPersonLen);
            if (thirdPersonLen > 4096000) {
               throw ProtocolException.stringTooLong("ThirdPerson", thirdPersonLen, 4096000);
            }

            if (varPos0 + thirdPersonVarIntLen + thirdPersonLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ThirdPerson", varPos0 + thirdPersonVarIntLen + thirdPersonLen, buf.readableBytes());
            }

            obj.thirdPerson = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 16);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 32) {
               throw ProtocolException.invalidOffset("ThirdPersonMoving", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 32 + varPosBase1;
            int thirdPersonMovingLen = VarInt.peek(buf, varPos1);
            if (thirdPersonMovingLen < 0) {
               throw ProtocolException.invalidVarInt("ThirdPersonMoving");
            }

            int thirdPersonMovingVarIntLen = VarInt.size(thirdPersonMovingLen);
            if (thirdPersonMovingLen > 4096000) {
               throw ProtocolException.stringTooLong("ThirdPersonMoving", thirdPersonMovingLen, 4096000);
            }

            if (varPos1 + thirdPersonMovingVarIntLen + thirdPersonMovingLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ThirdPersonMoving", varPos1 + thirdPersonMovingVarIntLen + thirdPersonMovingLen, buf.readableBytes());
            }

            obj.thirdPersonMoving = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 20);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 32) {
               throw ProtocolException.invalidOffset("ThirdPersonFace", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 32 + varPosBase2;
            int thirdPersonFaceLen = VarInt.peek(buf, varPos2);
            if (thirdPersonFaceLen < 0) {
               throw ProtocolException.invalidVarInt("ThirdPersonFace");
            }

            int thirdPersonFaceVarIntLen = VarInt.size(thirdPersonFaceLen);
            if (thirdPersonFaceLen > 4096000) {
               throw ProtocolException.stringTooLong("ThirdPersonFace", thirdPersonFaceLen, 4096000);
            }

            if (varPos2 + thirdPersonFaceVarIntLen + thirdPersonFaceLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ThirdPersonFace", varPos2 + thirdPersonFaceVarIntLen + thirdPersonFaceLen, buf.readableBytes());
            }

            obj.thirdPersonFace = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 24);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 32) {
               throw ProtocolException.invalidOffset("FirstPerson", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 32 + varPosBase3;
            int firstPersonLen = VarInt.peek(buf, varPos3);
            if (firstPersonLen < 0) {
               throw ProtocolException.invalidVarInt("FirstPerson");
            }

            int firstPersonVarIntLen = VarInt.size(firstPersonLen);
            if (firstPersonLen > 4096000) {
               throw ProtocolException.stringTooLong("FirstPerson", firstPersonLen, 4096000);
            }

            if (varPos3 + firstPersonVarIntLen + firstPersonLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("FirstPerson", varPos3 + firstPersonVarIntLen + firstPersonLen, buf.readableBytes());
            }

            obj.firstPerson = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
         }

         if ((nullBits & 16) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 28);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 32) {
               throw ProtocolException.invalidOffset("FirstPersonOverride", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 32 + varPosBase4;
            int firstPersonOverrideLen = VarInt.peek(buf, varPos4);
            if (firstPersonOverrideLen < 0) {
               throw ProtocolException.invalidVarInt("FirstPersonOverride");
            }

            int firstPersonOverrideVarIntLen = VarInt.size(firstPersonOverrideLen);
            if (firstPersonOverrideLen > 4096000) {
               throw ProtocolException.stringTooLong("FirstPersonOverride", firstPersonOverrideLen, 4096000);
            }

            if (varPos4 + firstPersonOverrideVarIntLen + firstPersonOverrideLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall(
                  "FirstPersonOverride", varPos4 + firstPersonOverrideVarIntLen + firstPersonOverrideLen, buf.readableBytes()
               );
            }

            obj.firstPersonOverride = PacketIO.readVarString(buf, varPos4, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 32;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 12);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 32) {
            throw ProtocolException.invalidOffset("ThirdPerson", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 32 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 16);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 32) {
            throw ProtocolException.invalidOffset("ThirdPersonMoving", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 32 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 20);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 32) {
            throw ProtocolException.invalidOffset("ThirdPersonFace", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 32 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 24);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 32) {
            throw ProtocolException.invalidOffset("FirstPerson", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 32 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(sl) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 28);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 32) {
            throw ProtocolException.invalidOffset("FirstPersonOverride", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 32 + fieldOffset4;
         int sl = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(sl) + sl;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 32L;
   }

   @Nullable
   public static String getThirdPerson(MemorySegment mem) {
      return getThirdPerson(mem, 0);
   }

   @Nullable
   public static String getThirdPerson(MemorySegment mem, int offset) {
      return hasThirdPerson(mem, offset)
         ? PacketIO.readVarString("ThirdPerson", mem, offset + getValidatedOffset(mem, offset, 12, 32, "ThirdPerson"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getThirdPersonMoving(MemorySegment mem) {
      return getThirdPersonMoving(mem, 0);
   }

   @Nullable
   public static String getThirdPersonMoving(MemorySegment mem, int offset) {
      return hasThirdPersonMoving(mem, offset)
         ? PacketIO.readVarString("ThirdPersonMoving", mem, offset + getValidatedOffset(mem, offset, 16, 32, "ThirdPersonMoving"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getThirdPersonFace(MemorySegment mem) {
      return getThirdPersonFace(mem, 0);
   }

   @Nullable
   public static String getThirdPersonFace(MemorySegment mem, int offset) {
      return hasThirdPersonFace(mem, offset)
         ? PacketIO.readVarString("ThirdPersonFace", mem, offset + getValidatedOffset(mem, offset, 20, 32, "ThirdPersonFace"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getFirstPerson(MemorySegment mem) {
      return getFirstPerson(mem, 0);
   }

   @Nullable
   public static String getFirstPerson(MemorySegment mem, int offset) {
      return hasFirstPerson(mem, offset)
         ? PacketIO.readVarString("FirstPerson", mem, offset + getValidatedOffset(mem, offset, 24, 32, "FirstPerson"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getFirstPersonOverride(MemorySegment mem) {
      return getFirstPersonOverride(mem, 0);
   }

   @Nullable
   public static String getFirstPersonOverride(MemorySegment mem, int offset) {
      return hasFirstPersonOverride(mem, offset)
         ? PacketIO.readVarString("FirstPersonOverride", mem, offset + getValidatedOffset(mem, offset, 28, 32, "FirstPersonOverride"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean getKeepPreviousFirstPersonAnimation(MemorySegment mem) {
      return getKeepPreviousFirstPersonAnimation(mem, 0);
   }

   public static boolean getKeepPreviousFirstPersonAnimation(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static float getSpeed(MemorySegment mem) {
      return getSpeed(mem, 0);
   }

   public static float getSpeed(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 2));
   }

   public static float getBlendingDuration(MemorySegment mem) {
      return getBlendingDuration(mem, 0);
   }

   public static float getBlendingDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 6));
   }

   public static boolean getLooping(MemorySegment mem) {
      return getLooping(mem, 0);
   }

   public static boolean getLooping(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 10));
   }

   public static boolean getClipsGeometry(MemorySegment mem) {
      return getClipsGeometry(mem, 0);
   }

   public static boolean getClipsGeometry(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 11));
   }

   public static boolean hasThirdPerson(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasThirdPersonMoving(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasThirdPersonFace(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasFirstPerson(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasFirstPersonOverride(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static ItemAnimation toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemAnimation toObject(MemorySegment mem, int offset) {
      if (offset + 32 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemAnimation", offset + 32, (int)mem.byteSize());
      } else {
         return new ItemAnimation(
            hasThirdPerson(mem, offset)
               ? PacketIO.readVarString("ThirdPerson", mem, offset + getValidatedOffset(mem, offset, 12, 32, "ThirdPerson"), 4096000, PacketIO.UTF8)
               : null,
            hasThirdPersonMoving(mem, offset)
               ? PacketIO.readVarString("ThirdPersonMoving", mem, offset + getValidatedOffset(mem, offset, 16, 32, "ThirdPersonMoving"), 4096000, PacketIO.UTF8)
               : null,
            hasThirdPersonFace(mem, offset)
               ? PacketIO.readVarString("ThirdPersonFace", mem, offset + getValidatedOffset(mem, offset, 20, 32, "ThirdPersonFace"), 4096000, PacketIO.UTF8)
               : null,
            hasFirstPerson(mem, offset)
               ? PacketIO.readVarString("FirstPerson", mem, offset + getValidatedOffset(mem, offset, 24, 32, "FirstPerson"), 4096000, PacketIO.UTF8)
               : null,
            hasFirstPersonOverride(mem, offset)
               ? PacketIO.readVarString(
                  "FirstPersonOverride", mem, offset + getValidatedOffset(mem, offset, 28, 32, "FirstPersonOverride"), 4096000, PacketIO.UTF8
               )
               : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 2)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 6)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 10)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 11))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.thirdPerson != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.thirdPersonMoving != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.thirdPersonFace != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.firstPerson != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.firstPersonOverride != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.keepPreviousFirstPersonAnimation ? 1 : 0);
      buf.writeFloatLE(this.speed);
      buf.writeFloatLE(this.blendingDuration);
      buf.writeByte(this.looping ? 1 : 0);
      buf.writeByte(this.clipsGeometry ? 1 : 0);
      int thirdPersonOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int thirdPersonMovingOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int thirdPersonFaceOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int firstPersonOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int firstPersonOverrideOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.thirdPerson != null) {
         buf.setIntLE(thirdPersonOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.thirdPerson, 4096000);
      } else {
         buf.setIntLE(thirdPersonOffsetSlot, -1);
      }

      if (this.thirdPersonMoving != null) {
         buf.setIntLE(thirdPersonMovingOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.thirdPersonMoving, 4096000);
      } else {
         buf.setIntLE(thirdPersonMovingOffsetSlot, -1);
      }

      if (this.thirdPersonFace != null) {
         buf.setIntLE(thirdPersonFaceOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.thirdPersonFace, 4096000);
      } else {
         buf.setIntLE(thirdPersonFaceOffsetSlot, -1);
      }

      if (this.firstPerson != null) {
         buf.setIntLE(firstPersonOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.firstPerson, 4096000);
      } else {
         buf.setIntLE(firstPersonOffsetSlot, -1);
      }

      if (this.firstPersonOverride != null) {
         buf.setIntLE(firstPersonOverrideOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.firstPersonOverride, 4096000);
      } else {
         buf.setIntLE(firstPersonOverrideOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.thirdPerson != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.thirdPersonMoving != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.thirdPersonFace != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.firstPerson != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.firstPersonOverride != null) {
         nullBits = (byte)(nullBits | 16);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.keepPreviousFirstPersonAnimation);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 2), this.speed);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 6), this.blendingDuration);
      mem.set(PacketIO.PROTO_BOOL, offset + 10, this.looping);
      mem.set(PacketIO.PROTO_BOOL, offset + 11, this.clipsGeometry);
      int varOffset = offset + 32;
      if (this.thirdPerson != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 12), varOffset - offset - 32);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.thirdPerson, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 12), -1);
      }

      if (this.thirdPersonMoving != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 16), varOffset - offset - 32);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.thirdPersonMoving, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 16), -1);
      }

      if (this.thirdPersonFace != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 20), varOffset - offset - 32);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.thirdPersonFace, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 20), -1);
      }

      if (this.firstPerson != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 24), varOffset - offset - 32);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.firstPerson, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 24), -1);
      }

      if (this.firstPersonOverride != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 28), varOffset - offset - 32);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.firstPersonOverride, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 28), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 32;
      if (this.thirdPerson != null) {
         size += PacketIO.stringSize(this.thirdPerson);
      }

      if (this.thirdPersonMoving != null) {
         size += PacketIO.stringSize(this.thirdPersonMoving);
      }

      if (this.thirdPersonFace != null) {
         size += PacketIO.stringSize(this.thirdPersonFace);
      }

      if (this.firstPerson != null) {
         size += PacketIO.stringSize(this.firstPerson);
      }

      if (this.firstPersonOverride != null) {
         size += PacketIO.stringSize(this.firstPersonOverride);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 32) {
         return ValidationResult.error("Buffer too small: expected at least 32 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int thirdPersonOffset = buffer.getIntLE(offset + 12);
            if (thirdPersonOffset < 0 || thirdPersonOffset > buffer.writerIndex() - offset - 32) {
               return ValidationResult.error("Invalid offset for ThirdPerson");
            }

            int pos = offset + 32 + thirdPersonOffset;
            int thirdPersonLen = VarInt.peek(buffer, pos);
            if (thirdPersonLen < 0) {
               return ValidationResult.error("Invalid string length for ThirdPerson");
            }

            if (thirdPersonLen > 4096000) {
               return ValidationResult.error("ThirdPerson exceeds max length 4096000");
            }

            pos += VarInt.size(thirdPersonLen);
            pos += thirdPersonLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ThirdPerson");
            }
         }

         if ((nullBits & 2) != 0) {
            int thirdPersonMovingOffset = buffer.getIntLE(offset + 16);
            if (thirdPersonMovingOffset < 0 || thirdPersonMovingOffset > buffer.writerIndex() - offset - 32) {
               return ValidationResult.error("Invalid offset for ThirdPersonMoving");
            }

            int posx = offset + 32 + thirdPersonMovingOffset;
            int thirdPersonMovingLen = VarInt.peek(buffer, posx);
            if (thirdPersonMovingLen < 0) {
               return ValidationResult.error("Invalid string length for ThirdPersonMoving");
            }

            if (thirdPersonMovingLen > 4096000) {
               return ValidationResult.error("ThirdPersonMoving exceeds max length 4096000");
            }

            posx += VarInt.size(thirdPersonMovingLen);
            posx += thirdPersonMovingLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ThirdPersonMoving");
            }
         }

         if ((nullBits & 4) != 0) {
            int thirdPersonFaceOffset = buffer.getIntLE(offset + 20);
            if (thirdPersonFaceOffset < 0 || thirdPersonFaceOffset > buffer.writerIndex() - offset - 32) {
               return ValidationResult.error("Invalid offset for ThirdPersonFace");
            }

            int posxx = offset + 32 + thirdPersonFaceOffset;
            int thirdPersonFaceLen = VarInt.peek(buffer, posxx);
            if (thirdPersonFaceLen < 0) {
               return ValidationResult.error("Invalid string length for ThirdPersonFace");
            }

            if (thirdPersonFaceLen > 4096000) {
               return ValidationResult.error("ThirdPersonFace exceeds max length 4096000");
            }

            posxx += VarInt.size(thirdPersonFaceLen);
            posxx += thirdPersonFaceLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ThirdPersonFace");
            }
         }

         if ((nullBits & 8) != 0) {
            int firstPersonOffset = buffer.getIntLE(offset + 24);
            if (firstPersonOffset < 0 || firstPersonOffset > buffer.writerIndex() - offset - 32) {
               return ValidationResult.error("Invalid offset for FirstPerson");
            }

            int posxxx = offset + 32 + firstPersonOffset;
            int firstPersonLen = VarInt.peek(buffer, posxxx);
            if (firstPersonLen < 0) {
               return ValidationResult.error("Invalid string length for FirstPerson");
            }

            if (firstPersonLen > 4096000) {
               return ValidationResult.error("FirstPerson exceeds max length 4096000");
            }

            posxxx += VarInt.size(firstPersonLen);
            posxxx += firstPersonLen;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FirstPerson");
            }
         }

         if ((nullBits & 16) != 0) {
            int firstPersonOverrideOffset = buffer.getIntLE(offset + 28);
            if (firstPersonOverrideOffset < 0 || firstPersonOverrideOffset > buffer.writerIndex() - offset - 32) {
               return ValidationResult.error("Invalid offset for FirstPersonOverride");
            }

            int posxxxx = offset + 32 + firstPersonOverrideOffset;
            int firstPersonOverrideLen = VarInt.peek(buffer, posxxxx);
            if (firstPersonOverrideLen < 0) {
               return ValidationResult.error("Invalid string length for FirstPersonOverride");
            }

            if (firstPersonOverrideLen > 4096000) {
               return ValidationResult.error("FirstPersonOverride exceeds max length 4096000");
            }

            posxxxx += VarInt.size(firstPersonOverrideLen);
            posxxxx += firstPersonOverrideLen;
            if (posxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FirstPersonOverride");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemAnimation clone() {
      ItemAnimation copy = new ItemAnimation();
      copy.thirdPerson = this.thirdPerson;
      copy.thirdPersonMoving = this.thirdPersonMoving;
      copy.thirdPersonFace = this.thirdPersonFace;
      copy.firstPerson = this.firstPerson;
      copy.firstPersonOverride = this.firstPersonOverride;
      copy.keepPreviousFirstPersonAnimation = this.keepPreviousFirstPersonAnimation;
      copy.speed = this.speed;
      copy.blendingDuration = this.blendingDuration;
      copy.looping = this.looping;
      copy.clipsGeometry = this.clipsGeometry;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemAnimation other)
            ? false
            : Objects.equals(this.thirdPerson, other.thirdPerson)
               && Objects.equals(this.thirdPersonMoving, other.thirdPersonMoving)
               && Objects.equals(this.thirdPersonFace, other.thirdPersonFace)
               && Objects.equals(this.firstPerson, other.firstPerson)
               && Objects.equals(this.firstPersonOverride, other.firstPersonOverride)
               && this.keepPreviousFirstPersonAnimation == other.keepPreviousFirstPersonAnimation
               && this.speed == other.speed
               && this.blendingDuration == other.blendingDuration
               && this.looping == other.looping
               && this.clipsGeometry == other.clipsGeometry;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.thirdPerson,
         this.thirdPersonMoving,
         this.thirdPersonFace,
         this.firstPerson,
         this.firstPersonOverride,
         this.keepPreviousFirstPersonAnimation,
         this.speed,
         this.blendingDuration,
         this.looping,
         this.clipsGeometry
      );
   }
}
