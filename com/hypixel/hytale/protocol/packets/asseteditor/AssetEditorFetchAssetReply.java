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

public class AssetEditorFetchAssetReply implements Packet, ToClientPacket {
   public static final int PACKET_ID = 312;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 4096010;
   public int token;
   @Nullable
   public byte[] contents;

   @Override
   public int getId() {
      return 312;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorFetchAssetReply() {
   }

   public AssetEditorFetchAssetReply(int token, @Nullable byte[] contents) {
      this.token = token;
      this.contents = contents;
   }

   public AssetEditorFetchAssetReply(@Nonnull AssetEditorFetchAssetReply other) {
      this.token = other.token;
      this.contents = other.contents;
   }

   @Nonnull
   public static AssetEditorFetchAssetReply deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("AssetEditorFetchAssetReply", 5, buf.readableBytes() - offset);
      } else {
         AssetEditorFetchAssetReply obj = new AssetEditorFetchAssetReply();
         byte nullBits = buf.getByte(offset);
         obj.token = buf.getIntLE(offset + 1);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int contentsCount = VarInt.peek(buf, pos);
            if (contentsCount < 0) {
               throw ProtocolException.invalidVarInt("Contents");
            }

            int contentsVarLen = VarInt.size(contentsCount);
            if (contentsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Contents", contentsCount, 4096000);
            }

            if (pos + contentsVarLen + contentsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Contents", pos + contentsVarLen + contentsCount * 1, buf.readableBytes());
            }

            pos += contentsVarLen;
            obj.contents = new byte[contentsCount];

            for (int i = 0; i < contentsCount; i++) {
               obj.contents[i] = buf.getByte(pos + i * 1);
            }

            pos += contentsCount * 1;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen) + arrLen * 1;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static int getToken(MemorySegment mem) {
      return getToken(mem, 0);
   }

   public static int getToken(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static byte[] getContents(MemorySegment mem) {
      return getContents(mem, 0);
   }

   @Nullable
   public static byte[] getContents(MemorySegment mem, int offset) {
      if (!hasContents(mem, offset)) {
         return null;
      } else {
         int off = offset + 5;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Contents", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Contents", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Contents", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] data = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static boolean hasContents(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AssetEditorFetchAssetReply toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorFetchAssetReply toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorFetchAssetReply", offset + 5, (int)mem.byteSize());
      } else {
         byte[] contents = null;
         if (hasContents(mem, offset)) {
            int off = offset + 5;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Contents", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Contents", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Contents", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            contents = new byte[len];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, contents, 0, len);
         }

         return new AssetEditorFetchAssetReply(mem.get(PacketIO.PROTO_INT, (long)(offset + 1)), contents);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.contents != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.token);
      if (this.contents != null) {
         if (this.contents.length > 4096000) {
            throw ProtocolException.arrayTooLong("Contents", this.contents.length, 4096000);
         }

         VarInt.write(buf, this.contents.length);

         for (byte item : this.contents) {
            buf.writeByte(item);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.contents != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.token);
      int varOffset = offset + 5;
      if (this.contents != null) {
         if (this.contents.length > 4096000) {
            throw ProtocolException.arrayTooLong("Contents", this.contents.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.contents.length);
         MemorySegment.copy(this.contents, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.contents.length);
         varOffset += this.contents.length * 1;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 5;
      if (this.contents != null) {
         size += VarInt.size(this.contents.length) + this.contents.length * 1;
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
            int contentsCount = VarInt.peek(buffer, pos);
            if (contentsCount < 0) {
               return ValidationResult.error("Invalid array count for Contents");
            }

            if (contentsCount > 4096000) {
               return ValidationResult.error("Contents exceeds max length 4096000");
            }

            pos += VarInt.size(contentsCount);
            pos += contentsCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Contents");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorFetchAssetReply clone() {
      AssetEditorFetchAssetReply copy = new AssetEditorFetchAssetReply();
      copy.token = this.token;
      copy.contents = this.contents != null ? Arrays.copyOf(this.contents, this.contents.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorFetchAssetReply other) ? false : this.token == other.token && Arrays.equals(this.contents, other.contents);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.token);
      return 31 * result + Arrays.hashCode(this.contents);
   }
}
