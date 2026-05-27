package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class ActiveAnimationsUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 0;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public String[] activeAnimations = new String[0];

   public ActiveAnimationsUpdate() {
   }

   public ActiveAnimationsUpdate(@Nonnull String[] activeAnimations) {
      this.activeAnimations = activeAnimations;
   }

   public ActiveAnimationsUpdate(@Nonnull ActiveAnimationsUpdate other) {
      this.activeAnimations = other.activeAnimations;
   }

   @Nonnull
   public static ActiveAnimationsUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      ActiveAnimationsUpdate obj = new ActiveAnimationsUpdate();
      int pos = offset + 0;
      int activeAnimationsCount = VarInt.peek(buf, pos);
      if (activeAnimationsCount < 0) {
         throw ProtocolException.invalidVarInt("ActiveAnimations");
      } else {
         int activeAnimationsVarLen = VarInt.size(activeAnimationsCount);
         if (activeAnimationsCount > 4096000) {
            throw ProtocolException.arrayTooLong("ActiveAnimations", activeAnimationsCount, 4096000);
         } else {
            pos += activeAnimationsVarLen;
            int activeAnimationsBitfieldSize = (activeAnimationsCount + 7) / 8;
            if (pos + activeAnimationsBitfieldSize > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ActiveAnimations", pos + activeAnimationsBitfieldSize, buf.readableBytes());
            } else {
               byte[] activeAnimationsBitfield = PacketIO.readBytes(buf, pos, activeAnimationsBitfieldSize);
               pos += activeAnimationsBitfieldSize;
               obj.activeAnimations = new String[activeAnimationsCount];

               for (int i = 0; i < activeAnimationsCount; i++) {
                  if ((activeAnimationsBitfield[i / 8] & 1 << i % 8) != 0) {
                     int strLen = VarInt.peek(buf, pos);
                     if (strLen < 0) {
                        throw ProtocolException.invalidVarInt("activeAnimations[" + i + "]");
                     }

                     int strVarLen = VarInt.size(strLen);
                     if (strLen > 4096000) {
                        throw ProtocolException.stringTooLong("activeAnimations[" + i + "]", strLen, 4096000);
                     }

                     if (pos + strVarLen + strLen > buf.readableBytes()) {
                        throw ProtocolException.bufferTooSmall("activeAnimations[" + i + "]", pos + strVarLen + strLen, buf.readableBytes());
                     }

                     obj.activeAnimations[i] = PacketIO.readVarString(buf, pos);
                     pos += strVarLen + strLen;
                  }
               }

               return obj;
            }
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 0;
      int arrLen = VarInt.peek(buf, pos);
      pos += VarInt.size(arrLen);
      int bitfieldSize = (arrLen + 7) / 8;
      byte[] bitfield = PacketIO.readBytes(buf, pos, bitfieldSize);
      pos += bitfieldSize;

      for (int i = 0; i < arrLen; i++) {
         if ((bitfield[i / 8] & 1 << i % 8) != 0) {
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.size(sl) + sl;
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 0L;
   }

   public static String[] getActiveAnimations(MemorySegment mem) {
      return getActiveAnimations(mem, 0);
   }

   public static String[] getActiveAnimations(MemorySegment mem, int offset) {
      int off = offset + 0;
      long packed = VarInt.getWithLength(mem, off);
      int len = (int)packed;
      if (len < 0) {
         throw ProtocolException.negativeLength("ActiveAnimations", len);
      } else if (len > 4096000) {
         throw ProtocolException.arrayTooLong("ActiveAnimations", len, 4096000);
      } else {
         int lenOffset = (int)(packed >>> 32);
         if (off + lenOffset + (len + 7) / 8 > mem.byteSize()) {
            throw ProtocolException.bufferTooSmall("ActiveAnimations", off + lenOffset + (len + 7) / 8, (int)mem.byteSize());
         } else {
            off += lenOffset;
            String[] data = new String[len];
            int bitfieldSize = (len + 7) / 8;
            int bitfieldOff = off;
            off += bitfieldSize;
            if (off + bitfieldSize > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ActiveAnimations", off + bitfieldSize, (int)mem.byteSize());
            } else {
               int i = 0;

               while (i < len) {
                  byte bits = mem.get(PacketIO.PROTO_BYTE, (long)(bitfieldOff + i / 8));

                  for (int batchEnd = Math.min(len, (i & -8) + 8); i < batchEnd; i++) {
                     if ((bits & 1 << (i & 7)) != 0) {
                        long sp = VarInt.getWithLength(mem, off);
                        int n = (int)sp + (int)(sp >>> 32);
                        data[i] = PacketIO.readVarString("ActiveAnimations", mem, off, 16384000, PacketIO.UTF8);
                        off += n;
                     }
                  }
               }

               return data;
            }
         }
      }
   }

   public static ActiveAnimationsUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ActiveAnimationsUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 0 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ActiveAnimationsUpdate", offset + 0, (int)mem.byteSize());
      } else {
         int off = offset + 0;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ActiveAnimations", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("ActiveAnimations", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + (len + 7) / 8 > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ActiveAnimations", off + lenOffset + (len + 7) / 8, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] activeAnimations = new String[len];
               int bitfieldSize = (len + 7) / 8;
               int bitfieldOff = off;
               off += bitfieldSize;
               if (off + bitfieldSize > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("ActiveAnimations", off + bitfieldSize, (int)mem.byteSize());
               } else {
                  int i = 0;

                  while (i < len) {
                     byte bits = mem.get(PacketIO.PROTO_BYTE, (long)(bitfieldOff + i / 8));

                     for (int batchEnd = Math.min(len, (i & -8) + 8); i < batchEnd; i++) {
                        if ((bits & 1 << (i & 7)) != 0) {
                           long sp = VarInt.getWithLength(mem, off);
                           int n = (int)sp + (int)(sp >>> 32);
                           activeAnimations[i] = PacketIO.readVarString("ActiveAnimations", mem, off, 16384000, PacketIO.UTF8);
                           off += n;
                        }
                     }
                  }

                  return new ActiveAnimationsUpdate(activeAnimations);
               }
            }
         }
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      if (this.activeAnimations.length > 4096000) {
         throw ProtocolException.arrayTooLong("ActiveAnimations", this.activeAnimations.length, 4096000);
      } else {
         VarInt.write(buf, this.activeAnimations.length);
         int activeAnimationsBitfieldSize = (this.activeAnimations.length + 7) / 8;
         byte[] activeAnimationsBitfield = new byte[activeAnimationsBitfieldSize];

         for (int i = 0; i < this.activeAnimations.length; i++) {
            if (this.activeAnimations[i] != null) {
               activeAnimationsBitfield[i / 8] = (byte)(activeAnimationsBitfield[i / 8] | (byte)(1 << i % 8));
            }
         }

         buf.writeBytes(activeAnimationsBitfield);

         for (int ix = 0; ix < this.activeAnimations.length; ix++) {
            if (this.activeAnimations[ix] != null) {
               PacketIO.writeVarString(buf, this.activeAnimations[ix], 4096000);
            }
         }

         return buf.writerIndex() - startPos;
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      int varOffset = offset + 0;
      if (this.activeAnimations.length > 4096000) {
         throw ProtocolException.arrayTooLong("ActiveAnimations", this.activeAnimations.length, 4096000);
      } else {
         varOffset += VarInt.set(mem, varOffset, this.activeAnimations.length);
         int activeAnimationsBitfieldSize = (this.activeAnimations.length + 7) / 8;

         for (int bi = 0; bi < activeAnimationsBitfieldSize; bi++) {
            byte bits = 0;

            for (int j = bi * 8; j < Math.min(this.activeAnimations.length, bi * 8 + 8); j++) {
               if (this.activeAnimations[j] != null) {
                  bits |= (byte)(1 << (j & 7));
               }
            }

            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + bi), bits);
         }

         varOffset += activeAnimationsBitfieldSize;

         for (int i = 0; i < this.activeAnimations.length; i++) {
            if (this.activeAnimations[i] != null) {
               varOffset += PacketIO.writeVarString(mem, varOffset, this.activeAnimations[i], 16384000);
            }
         }

         return varOffset - offset;
      }
   }

   @Override
   public int computeSize() {
      int size = 0;
      int activeAnimationsSize = 0;

      for (String elem : this.activeAnimations) {
         if (elem != null) {
            activeAnimationsSize += PacketIO.stringSize(elem);
         }
      }

      return size + VarInt.size(this.activeAnimations.length) + (this.activeAnimations.length + 7) / 8 + activeAnimationsSize;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 0) {
         return ValidationResult.error("Buffer too small: expected at least 0 bytes");
      } else {
         int pos = offset + 0;
         int activeAnimationsCount = VarInt.peek(buffer, pos);
         if (activeAnimationsCount < 0) {
            return ValidationResult.error("Invalid array count for ActiveAnimations");
         } else if (activeAnimationsCount > 4096000) {
            return ValidationResult.error("ActiveAnimations exceeds max length 4096000");
         } else {
            pos += VarInt.size(activeAnimationsCount);
            int activeAnimationsBitfieldSize = (activeAnimationsCount + 7) / 8;
            if (pos + activeAnimationsBitfieldSize > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading bitfield for ActiveAnimations");
            } else {
               byte[] activeAnimationsBitfield = PacketIO.readBytes(buffer, pos, activeAnimationsBitfieldSize);
               pos += activeAnimationsBitfieldSize;

               for (int i = 0; i < activeAnimationsCount; i++) {
                  if ((activeAnimationsBitfield[i / 8] & 1 << i % 8) != 0) {
                     int strLen = VarInt.peek(buffer, pos);
                     if (strLen < 0) {
                        return ValidationResult.error("Invalid string length in activeAnimations");
                     }

                     pos += VarInt.size(strLen);
                     pos += strLen;
                     if (pos > buffer.writerIndex()) {
                        return ValidationResult.error("Buffer overflow reading string in activeAnimations");
                     }
                  }
               }

               return ValidationResult.OK;
            }
         }
      }
   }

   public ActiveAnimationsUpdate clone() {
      ActiveAnimationsUpdate copy = new ActiveAnimationsUpdate();
      copy.activeAnimations = Arrays.copyOf(this.activeAnimations, this.activeAnimations.length);
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof ActiveAnimationsUpdate other ? Arrays.equals((Object[])this.activeAnimations, (Object[])other.activeAnimations) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.activeAnimations);
   }
}
