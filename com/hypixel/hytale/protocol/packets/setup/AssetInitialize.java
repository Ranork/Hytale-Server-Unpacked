package com.hypixel.hytale.protocol.packets.setup;

import com.hypixel.hytale.protocol.Asset;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class AssetInitialize implements Packet, ToClientPacket {
   public static final int PACKET_ID = 24;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 2121;
   @Nonnull
   public Asset asset = new Asset();
   public int size;

   @Override
   public int getId() {
      return 24;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetInitialize() {
   }

   public AssetInitialize(@Nonnull Asset asset, int size) {
      this.asset = asset;
      this.size = size;
   }

   public AssetInitialize(@Nonnull AssetInitialize other) {
      this.asset = other.asset;
      this.size = other.size;
   }

   @Nonnull
   public static AssetInitialize deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 4) {
         throw ProtocolException.bufferTooSmall("AssetInitialize", 4, buf.readableBytes() - offset);
      } else {
         AssetInitialize obj = new AssetInitialize();
         obj.size = buf.getIntLE(offset + 0);
         int pos = offset + 4;
         obj.asset = Asset.deserialize(buf, pos);
         pos += Asset.computeBytesConsumed(buf, pos);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 4;
      pos += Asset.computeBytesConsumed(buf, pos);
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 4L;
   }

   public static Asset getAsset(MemorySegment mem) {
      return getAsset(mem, 0);
   }

   public static Asset getAsset(MemorySegment mem, int offset) {
      return Asset.toObject(mem, offset + 4);
   }

   public static int getSize(MemorySegment mem) {
      return getSize(mem, 0);
   }

   public static int getSize(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static AssetInitialize toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetInitialize toObject(MemorySegment mem, int offset) {
      if (offset + 4 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetInitialize", offset + 4, (int)mem.byteSize());
      } else {
         return new AssetInitialize(Asset.toObject(mem, offset + 4), mem.get(PacketIO.PROTO_INT, (long)(offset + 0)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.size);
      this.asset.serialize(buf);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.size);
      int varOffset = offset + 4;
      varOffset += this.asset.serialize(mem, varOffset);
      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 4;
      return size + this.asset.computeSize();
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 4) {
         return ValidationResult.error("Buffer too small: expected at least 4 bytes");
      } else {
         int pos = offset + 4;
         ValidationResult assetResult = Asset.validateStructure(buffer, pos);
         if (!assetResult.isValid()) {
            return ValidationResult.error("Invalid Asset: " + assetResult.error());
         } else {
            pos += Asset.computeBytesConsumed(buffer, pos);
            return ValidationResult.OK;
         }
      }
   }

   public AssetInitialize clone() {
      AssetInitialize copy = new AssetInitialize();
      copy.asset = this.asset.clone();
      copy.size = this.size;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetInitialize other) ? false : Objects.equals(this.asset, other.asset) && this.size == other.size;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.asset, this.size);
   }
}
