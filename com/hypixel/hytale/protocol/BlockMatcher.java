package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMatcher {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 3;
   public static final int MAX_SIZE = 32768026;
   @Nullable
   public BlockIdMatcher block;
   @Nonnull
   public BlockFace face = BlockFace.None;
   public boolean staticFace;

   public BlockMatcher() {
   }

   public BlockMatcher(@Nullable BlockIdMatcher block, @Nonnull BlockFace face, boolean staticFace) {
      this.block = block;
      this.face = face;
      this.staticFace = staticFace;
   }

   public BlockMatcher(@Nonnull BlockMatcher other) {
      this.block = other.block;
      this.face = other.face;
      this.staticFace = other.staticFace;
   }

   @Nonnull
   public static BlockMatcher deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 3) {
         throw ProtocolException.bufferTooSmall("BlockMatcher", 3, buf.readableBytes() - offset);
      } else {
         BlockMatcher obj = new BlockMatcher();
         byte nullBits = buf.getByte(offset);
         obj.face = BlockFace.fromValue(buf.getByte(offset + 1));
         obj.staticFace = buf.getByte(offset + 2) != 0;
         int pos = offset + 3;
         if ((nullBits & 1) != 0) {
            obj.block = BlockIdMatcher.deserialize(buf, pos);
            pos += BlockIdMatcher.computeBytesConsumed(buf, pos);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 3;
      if ((nullBits & 1) != 0) {
         pos += BlockIdMatcher.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 3L;
   }

   @Nullable
   public static BlockIdMatcher getBlock(MemorySegment mem) {
      return getBlock(mem, 0);
   }

   @Nullable
   public static BlockIdMatcher getBlock(MemorySegment mem, int offset) {
      return hasBlock(mem, offset) ? BlockIdMatcher.toObject(mem, offset + 3) : null;
   }

   public static BlockFace getFace(MemorySegment mem) {
      return getFace(mem, 0);
   }

   public static BlockFace getFace(MemorySegment mem, int offset) {
      return BlockFace.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static boolean getStaticFace(MemorySegment mem) {
      return getStaticFace(mem, 0);
   }

   public static boolean getStaticFace(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2));
   }

   public static boolean hasBlock(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static BlockMatcher toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockMatcher toObject(MemorySegment mem, int offset) {
      if (offset + 3 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockMatcher", offset + 3, (int)mem.byteSize());
      } else {
         return new BlockMatcher(
            hasBlock(mem, offset) ? BlockIdMatcher.toObject(mem, offset + 3) : null,
            BlockFace.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.block != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.face.getValue());
      buf.writeByte(this.staticFace ? 1 : 0);
      if (this.block != null) {
         this.block.serialize(buf);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.block != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.face.getValue());
      mem.set(PacketIO.PROTO_BOOL, offset + 2, this.staticFace);
      int varOffset = offset + 3;
      if (this.block != null) {
         varOffset += this.block.serialize(mem, varOffset);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 3;
      if (this.block != null) {
         size += this.block.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 3) {
         return ValidationResult.error("Buffer too small: expected at least 3 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 7) {
            return ValidationResult.error("Invalid BlockFace value for Face");
         } else {
            v = offset + 3;
            if ((nullBits & 1) != 0) {
               ValidationResult blockResult = BlockIdMatcher.validateStructure(buffer, v);
               if (!blockResult.isValid()) {
                  return ValidationResult.error("Invalid Block: " + blockResult.error());
               }

               v += BlockIdMatcher.computeBytesConsumed(buffer, v);
            }

            return ValidationResult.OK;
         }
      }
   }

   public BlockMatcher clone() {
      BlockMatcher copy = new BlockMatcher();
      copy.block = this.block != null ? this.block.clone() : null;
      copy.face = this.face;
      copy.staticFace = this.staticFace;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockMatcher other)
            ? false
            : Objects.equals(this.block, other.block) && Objects.equals(this.face, other.face) && this.staticFace == other.staticFace;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.block, this.face, this.staticFace);
   }
}
