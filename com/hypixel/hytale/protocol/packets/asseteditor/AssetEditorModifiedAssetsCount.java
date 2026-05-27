package com.hypixel.hytale.protocol.packets.asseteditor;

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

public class AssetEditorModifiedAssetsCount implements Packet, ToClientPacket {
   public static final int PACKET_ID = 340;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public int count;

   @Override
   public int getId() {
      return 340;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorModifiedAssetsCount() {
   }

   public AssetEditorModifiedAssetsCount(int count) {
      this.count = count;
   }

   public AssetEditorModifiedAssetsCount(@Nonnull AssetEditorModifiedAssetsCount other) {
      this.count = other.count;
   }

   @Nonnull
   public static AssetEditorModifiedAssetsCount deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 4) {
         throw ProtocolException.bufferTooSmall("AssetEditorModifiedAssetsCount", 4, buf.readableBytes() - offset);
      } else {
         AssetEditorModifiedAssetsCount obj = new AssetEditorModifiedAssetsCount();
         obj.count = buf.getIntLE(offset + 0);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 4L;
   }

   public static int getCount(MemorySegment mem) {
      return getCount(mem, 0);
   }

   public static int getCount(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static AssetEditorModifiedAssetsCount toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorModifiedAssetsCount toObject(MemorySegment mem, int offset) {
      if (offset + 4 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorModifiedAssetsCount", offset + 4, (int)mem.byteSize());
      } else {
         return new AssetEditorModifiedAssetsCount(mem.get(PacketIO.PROTO_INT, (long)(offset + 0)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.count);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.count);
      return 4;
   }

   @Override
   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public AssetEditorModifiedAssetsCount clone() {
      AssetEditorModifiedAssetsCount copy = new AssetEditorModifiedAssetsCount();
      copy.count = this.count;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorModifiedAssetsCount other ? this.count == other.count : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.count);
   }
}
