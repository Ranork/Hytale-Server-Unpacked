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

public class BlockBreakingDecal {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String[] stageTextures;

   public BlockBreakingDecal() {
   }

   public BlockBreakingDecal(@Nullable String[] stageTextures) {
      this.stageTextures = stageTextures;
   }

   public BlockBreakingDecal(@Nonnull BlockBreakingDecal other) {
      this.stageTextures = other.stageTextures;
   }

   @Nonnull
   public static BlockBreakingDecal deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("BlockBreakingDecal", 1, buf.readableBytes() - offset);
      } else {
         BlockBreakingDecal obj = new BlockBreakingDecal();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int stageTexturesCount = VarInt.peek(buf, pos);
            if (stageTexturesCount < 0) {
               throw ProtocolException.invalidVarInt("StageTextures");
            }

            int stageTexturesVarLen = VarInt.size(stageTexturesCount);
            if (stageTexturesCount > 4096000) {
               throw ProtocolException.arrayTooLong("StageTextures", stageTexturesCount, 4096000);
            }

            if (pos + stageTexturesVarLen + stageTexturesCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("StageTextures", pos + stageTexturesVarLen + stageTexturesCount * 1, buf.readableBytes());
            }

            pos += stageTexturesVarLen;
            obj.stageTextures = new String[stageTexturesCount];

            for (int i = 0; i < stageTexturesCount; i++) {
               int strLen = VarInt.peek(buf, pos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("stageTextures[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("stageTextures[" + i + "]", strLen, 4096000);
               }

               if (pos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("stageTextures[" + i + "]", pos + strVarLen + strLen, buf.readableBytes());
               }

               obj.stageTextures[i] = PacketIO.readVarString(buf, pos);
               pos += strVarLen + strLen;
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.size(sl) + sl;
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static String[] getStageTextures(MemorySegment mem) {
      return getStageTextures(mem, 0);
   }

   @Nullable
   public static String[] getStageTextures(MemorySegment mem, int offset) {
      if (!hasStageTextures(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("StageTextures", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("StageTextures", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("StageTextures", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("StageTextures", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   public static boolean hasStageTextures(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static BlockBreakingDecal toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockBreakingDecal toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockBreakingDecal", offset + 1, (int)mem.byteSize());
      } else {
         String[] stageTextures = null;
         if (hasStageTextures(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("StageTextures", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("StageTextures", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("StageTextures", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            stageTextures = new String[len];

            for (int i = 0; i < len; i++) {
               long sp = VarInt.getWithLength(mem, off);
               int n = (int)sp + (int)(sp >>> 32);
               stageTextures[i] = PacketIO.readVarString("StageTextures", mem, off, 16384000, PacketIO.UTF8);
               off += n;
            }
         }

         return new BlockBreakingDecal(stageTextures);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.stageTextures != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.stageTextures != null) {
         if (this.stageTextures.length > 4096000) {
            throw ProtocolException.arrayTooLong("StageTextures", this.stageTextures.length, 4096000);
         }

         VarInt.write(buf, this.stageTextures.length);

         for (String item : this.stageTextures) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.stageTextures != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.stageTextures != null) {
         if (this.stageTextures.length > 4096000) {
            throw ProtocolException.arrayTooLong("StageTextures", this.stageTextures.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.stageTextures.length);
         int stageTexturesValueOffset = 0;

         for (int i = 0; i < this.stageTextures.length; i++) {
            stageTexturesValueOffset += PacketIO.writeVarString(mem, varOffset + stageTexturesValueOffset, this.stageTextures[i], 16384000);
         }

         varOffset += stageTexturesValueOffset;
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 1;
      if (this.stageTextures != null) {
         int stageTexturesSize = 0;

         for (String elem : this.stageTextures) {
            stageTexturesSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.stageTextures.length) + stageTexturesSize;
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
            int stageTexturesCount = VarInt.peek(buffer, pos);
            if (stageTexturesCount < 0) {
               return ValidationResult.error("Invalid array count for StageTextures");
            }

            if (stageTexturesCount > 4096000) {
               return ValidationResult.error("StageTextures exceeds max length 4096000");
            }

            pos += VarInt.size(stageTexturesCount);

            for (int i = 0; i < stageTexturesCount; i++) {
               int strLen = VarInt.peek(buffer, pos);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in StageTextures");
               }

               pos += VarInt.size(strLen);
               pos += strLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in StageTextures");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public BlockBreakingDecal clone() {
      BlockBreakingDecal copy = new BlockBreakingDecal();
      copy.stageTextures = this.stageTextures != null ? Arrays.copyOf(this.stageTextures, this.stageTextures.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof BlockBreakingDecal other ? Arrays.equals((Object[])this.stageTextures, (Object[])other.stageTextures) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.stageTextures);
   }
}
