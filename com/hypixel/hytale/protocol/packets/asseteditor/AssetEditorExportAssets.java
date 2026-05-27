package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorExportAssets implements Packet, ToServerPacket {
   public static final int PACKET_ID = 342;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public AssetPath[] paths;

   @Override
   public int getId() {
      return 342;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorExportAssets() {
   }

   public AssetEditorExportAssets(@Nullable AssetPath[] paths) {
      this.paths = paths;
   }

   public AssetEditorExportAssets(@Nonnull AssetEditorExportAssets other) {
      this.paths = other.paths;
   }

   @Nonnull
   public static AssetEditorExportAssets deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("AssetEditorExportAssets", 1, buf.readableBytes() - offset);
      } else {
         AssetEditorExportAssets obj = new AssetEditorExportAssets();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int pathsCount = VarInt.peek(buf, pos);
            if (pathsCount < 0) {
               throw ProtocolException.invalidVarInt("Paths");
            }

            int pathsVarLen = VarInt.size(pathsCount);
            if (pathsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Paths", pathsCount, 4096000);
            }

            if (pos + pathsVarLen + pathsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Paths", pos + pathsVarLen + pathsCount * 1, buf.readableBytes());
            }

            pos += pathsVarLen;
            obj.paths = new AssetPath[pathsCount];

            for (int i = 0; i < pathsCount; i++) {
               obj.paths[i] = AssetPath.deserialize(buf, pos);
               pos += AssetPath.computeBytesConsumed(buf, pos);
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
            pos += AssetPath.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static AssetPath[] getPaths(MemorySegment mem) {
      return getPaths(mem, 0);
   }

   @Nullable
   public static AssetPath[] getPaths(MemorySegment mem, int offset) {
      if (!hasPaths(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Paths", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Paths", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Paths", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               AssetPath[] data = new AssetPath[len];

               for (int i = 0; i < len; i++) {
                  data[i] = AssetPath.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasPaths(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AssetEditorExportAssets toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorExportAssets toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorExportAssets", offset + 1, (int)mem.byteSize());
      } else {
         AssetPath[] paths = null;
         if (hasPaths(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Paths", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Paths", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Paths", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            paths = new AssetPath[len];

            for (int i = 0; i < len; i++) {
               paths[i] = AssetPath.toObject(mem, off);
               off += paths[i].computeSize();
            }
         }

         return new AssetEditorExportAssets(paths);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.paths != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.paths != null) {
         if (this.paths.length > 4096000) {
            throw ProtocolException.arrayTooLong("Paths", this.paths.length, 4096000);
         }

         VarInt.write(buf, this.paths.length);

         for (AssetPath item : this.paths) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.paths != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.paths != null) {
         if (this.paths.length > 4096000) {
            throw ProtocolException.arrayTooLong("Paths", this.paths.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.paths.length);
         int pathsValueOffset = 0;

         for (int i = 0; i < this.paths.length; i++) {
            pathsValueOffset += this.paths[i].serialize(mem, varOffset + pathsValueOffset);
         }

         varOffset += pathsValueOffset;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.paths != null) {
         int pathsSize = 0;

         for (AssetPath elem : this.paths) {
            pathsSize += elem.computeSize();
         }

         size += VarInt.size(this.paths.length) + pathsSize;
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
            int pathsCount = VarInt.peek(buffer, pos);
            if (pathsCount < 0) {
               return ValidationResult.error("Invalid array count for Paths");
            }

            if (pathsCount > 4096000) {
               return ValidationResult.error("Paths exceeds max length 4096000");
            }

            pos += VarInt.size(pathsCount);

            for (int i = 0; i < pathsCount; i++) {
               ValidationResult structResult = AssetPath.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid AssetPath in Paths[" + i + "]: " + structResult.error());
               }

               pos += AssetPath.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorExportAssets clone() {
      AssetEditorExportAssets copy = new AssetEditorExportAssets();
      copy.paths = this.paths != null ? Arrays.stream(this.paths).map(e -> e.clone()).toArray(AssetPath[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorExportAssets other ? Arrays.equals((Object[])this.paths, (Object[])other.paths) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.paths);
   }
}
