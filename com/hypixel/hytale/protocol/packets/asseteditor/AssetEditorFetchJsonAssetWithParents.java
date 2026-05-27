package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorFetchJsonAssetWithParents implements Packet, ToServerPacket {
   public static final int PACKET_ID = 311;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 32768025;
   public int token;
   @Nullable
   public AssetPath path;
   public boolean isFromOpenedTab;

   @Override
   public int getId() {
      return 311;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorFetchJsonAssetWithParents() {
   }

   public AssetEditorFetchJsonAssetWithParents(int token, @Nullable AssetPath path, boolean isFromOpenedTab) {
      this.token = token;
      this.path = path;
      this.isFromOpenedTab = isFromOpenedTab;
   }

   public AssetEditorFetchJsonAssetWithParents(@Nonnull AssetEditorFetchJsonAssetWithParents other) {
      this.token = other.token;
      this.path = other.path;
      this.isFromOpenedTab = other.isFromOpenedTab;
   }

   @Nonnull
   public static AssetEditorFetchJsonAssetWithParents deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("AssetEditorFetchJsonAssetWithParents", 6, buf.readableBytes() - offset);
      } else {
         AssetEditorFetchJsonAssetWithParents obj = new AssetEditorFetchJsonAssetWithParents();
         byte nullBits = buf.getByte(offset);
         obj.token = buf.getIntLE(offset + 1);
         obj.isFromOpenedTab = buf.getByte(offset + 5) != 0;
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            obj.path = AssetPath.deserialize(buf, pos);
            pos += AssetPath.computeBytesConsumed(buf, pos);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         pos += AssetPath.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 6L;
   }

   public static int getToken(MemorySegment mem) {
      return getToken(mem, 0);
   }

   public static int getToken(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static AssetPath getPath(MemorySegment mem) {
      return getPath(mem, 0);
   }

   @Nullable
   public static AssetPath getPath(MemorySegment mem, int offset) {
      return hasPath(mem, offset) ? AssetPath.toObject(mem, offset + 6) : null;
   }

   public static boolean getIsFromOpenedTab(MemorySegment mem) {
      return getIsFromOpenedTab(mem, 0);
   }

   public static boolean getIsFromOpenedTab(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5));
   }

   public static boolean hasPath(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AssetEditorFetchJsonAssetWithParents toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorFetchJsonAssetWithParents toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorFetchJsonAssetWithParents", offset + 6, (int)mem.byteSize());
      } else {
         return new AssetEditorFetchJsonAssetWithParents(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasPath(mem, offset) ? AssetPath.toObject(mem, offset + 6) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.path != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.token);
      buf.writeByte(this.isFromOpenedTab ? 1 : 0);
      if (this.path != null) {
         this.path.serialize(buf);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.path != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.token);
      mem.set(PacketIO.PROTO_BOOL, offset + 5, this.isFromOpenedTab);
      int varOffset = offset + 6;
      if (this.path != null) {
         varOffset += this.path.serialize(mem, varOffset);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.path != null) {
         size += this.path.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 6) {
         return ValidationResult.error("Buffer too small: expected at least 6 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            ValidationResult pathResult = AssetPath.validateStructure(buffer, pos);
            if (!pathResult.isValid()) {
               return ValidationResult.error("Invalid Path: " + pathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorFetchJsonAssetWithParents clone() {
      AssetEditorFetchJsonAssetWithParents copy = new AssetEditorFetchJsonAssetWithParents();
      copy.token = this.token;
      copy.path = this.path != null ? this.path.clone() : null;
      copy.isFromOpenedTab = this.isFromOpenedTab;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorFetchJsonAssetWithParents other)
            ? false
            : this.token == other.token && Objects.equals(this.path, other.path) && this.isFromOpenedTab == other.isFromOpenedTab;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.token, this.path, this.isFromOpenedTab);
   }
}
