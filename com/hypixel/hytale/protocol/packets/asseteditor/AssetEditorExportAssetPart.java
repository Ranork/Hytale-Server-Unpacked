package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorExportAssetPart implements Packet, ToClientPacket {
   public static final int PACKET_ID = 344;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 4096006;
   @Nullable
   public byte[] part;

   @Override
   public int getId() {
      return 344;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorExportAssetPart() {
   }

   public AssetEditorExportAssetPart(@Nullable byte[] part) {
      this.part = part;
   }

   public AssetEditorExportAssetPart(@Nonnull AssetEditorExportAssetPart other) {
      this.part = other.part;
   }

   @Nonnull
   public static AssetEditorExportAssetPart deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("AssetEditorExportAssetPart", 1, buf.readableBytes() - offset);
      } else {
         AssetEditorExportAssetPart obj = new AssetEditorExportAssetPart();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int partCount = VarInt.peek(buf, pos);
            if (partCount < 0) {
               throw ProtocolException.invalidVarInt("Part");
            }

            int partVarLen = VarInt.size(partCount);
            if (partCount > 4096000) {
               throw ProtocolException.arrayTooLong("Part", partCount, 4096000);
            }

            if (pos + partVarLen + partCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Part", pos + partVarLen + partCount * 1, buf.readableBytes());
            }

            pos += partVarLen;
            obj.part = new byte[partCount];

            for (int i = 0; i < partCount; i++) {
               obj.part[i] = buf.getByte(pos + i * 1);
            }

            pos += partCount * 1;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen) + arrLen * 1;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static byte[] getPart(MemorySegment mem) {
      return getPart(mem, 0);
   }

   @Nullable
   public static byte[] getPart(MemorySegment mem, int offset) {
      if (!hasPart(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Part", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Part", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Part", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] data = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static boolean hasPart(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AssetEditorExportAssetPart toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorExportAssetPart toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorExportAssetPart", offset + 1, (int)mem.byteSize());
      } else {
         byte[] part = null;
         if (hasPart(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Part", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Part", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Part", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            part = new byte[len];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, part, 0, len);
         }

         return new AssetEditorExportAssetPart(part);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.part != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.part != null) {
         if (this.part.length > 4096000) {
            throw ProtocolException.arrayTooLong("Part", this.part.length, 4096000);
         }

         VarInt.write(buf, this.part.length);

         for (byte item : this.part) {
            buf.writeByte(item);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.part != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.part != null) {
         if (this.part.length > 4096000) {
            throw ProtocolException.arrayTooLong("Part", this.part.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.part.length);
         MemorySegment.copy(this.part, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.part.length);
         varOffset += this.part.length * 1;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.part != null) {
         size += VarInt.size(this.part.length) + this.part.length * 1;
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
            int partCount = VarInt.peek(buffer, pos);
            if (partCount < 0) {
               return ValidationResult.error("Invalid array count for Part");
            }

            if (partCount > 4096000) {
               return ValidationResult.error("Part exceeds max length 4096000");
            }

            pos += VarInt.size(partCount);
            pos += partCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Part");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorExportAssetPart clone() {
      AssetEditorExportAssetPart copy = new AssetEditorExportAssetPart();
      copy.part = this.part != null ? Arrays.copyOf(this.part, this.part.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorExportAssetPart other ? Arrays.equals(this.part, other.part) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode(this.part);
   }
}
