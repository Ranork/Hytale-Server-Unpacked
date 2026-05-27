package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MapChunk {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 20480037;
   public int chunkX;
   public int chunkZ;
   @Nullable
   public MapImage image;

   public MapChunk() {
   }

   public MapChunk(int chunkX, int chunkZ, @Nullable MapImage image) {
      this.chunkX = chunkX;
      this.chunkZ = chunkZ;
      this.image = image;
   }

   public MapChunk(@Nonnull MapChunk other) {
      this.chunkX = other.chunkX;
      this.chunkZ = other.chunkZ;
      this.image = other.image;
   }

   @Nonnull
   public static MapChunk deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("MapChunk", 9, buf.readableBytes() - offset);
      } else {
         MapChunk obj = new MapChunk();
         byte nullBits = buf.getByte(offset);
         obj.chunkX = buf.getIntLE(offset + 1);
         obj.chunkZ = buf.getIntLE(offset + 5);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            obj.image = MapImage.deserialize(buf, pos);
            pos += MapImage.computeBytesConsumed(buf, pos);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         pos += MapImage.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   public static int getChunkX(MemorySegment mem) {
      return getChunkX(mem, 0);
   }

   public static int getChunkX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getChunkZ(MemorySegment mem) {
      return getChunkZ(mem, 0);
   }

   public static int getChunkZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   @Nullable
   public static MapImage getImage(MemorySegment mem) {
      return getImage(mem, 0);
   }

   @Nullable
   public static MapImage getImage(MemorySegment mem, int offset) {
      return hasImage(mem, offset) ? MapImage.toObject(mem, offset + 9) : null;
   }

   public static boolean hasImage(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static MapChunk toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static MapChunk toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("MapChunk", offset + 9, (int)mem.byteSize());
      } else {
         return new MapChunk(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            hasImage(mem, offset) ? MapImage.toObject(mem, offset + 9) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.image != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.chunkX);
      buf.writeIntLE(this.chunkZ);
      if (this.image != null) {
         this.image.serialize(buf);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.image != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.chunkX);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.chunkZ);
      int varOffset = offset + 9;
      if (this.image != null) {
         varOffset += this.image.serialize(mem, varOffset);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 9;
      if (this.image != null) {
         size += this.image.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            ValidationResult imageResult = MapImage.validateStructure(buffer, pos);
            if (!imageResult.isValid()) {
               return ValidationResult.error("Invalid Image: " + imageResult.error());
            }

            pos += MapImage.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public MapChunk clone() {
      MapChunk copy = new MapChunk();
      copy.chunkX = this.chunkX;
      copy.chunkZ = this.chunkZ;
      copy.image = this.image != null ? this.image.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MapChunk other)
            ? false
            : this.chunkX == other.chunkX && this.chunkZ == other.chunkZ && Objects.equals(this.image, other.image);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.chunkX, this.chunkZ, this.image);
   }
}
