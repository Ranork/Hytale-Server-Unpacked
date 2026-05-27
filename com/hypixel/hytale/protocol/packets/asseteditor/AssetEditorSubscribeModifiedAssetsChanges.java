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

public class AssetEditorSubscribeModifiedAssetsChanges implements Packet, ToServerPacket {
   public static final int PACKET_ID = 341;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   public boolean subscribe;

   @Override
   public int getId() {
      return 341;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorSubscribeModifiedAssetsChanges() {
   }

   public AssetEditorSubscribeModifiedAssetsChanges(boolean subscribe) {
      this.subscribe = subscribe;
   }

   public AssetEditorSubscribeModifiedAssetsChanges(@Nonnull AssetEditorSubscribeModifiedAssetsChanges other) {
      this.subscribe = other.subscribe;
   }

   @Nonnull
   public static AssetEditorSubscribeModifiedAssetsChanges deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("AssetEditorSubscribeModifiedAssetsChanges", 1, buf.readableBytes() - offset);
      } else {
         AssetEditorSubscribeModifiedAssetsChanges obj = new AssetEditorSubscribeModifiedAssetsChanges();
         obj.subscribe = buf.getByte(offset + 0) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   public static boolean getSubscribe(MemorySegment mem) {
      return getSubscribe(mem, 0);
   }

   public static boolean getSubscribe(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0));
   }

   public static AssetEditorSubscribeModifiedAssetsChanges toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorSubscribeModifiedAssetsChanges toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorSubscribeModifiedAssetsChanges", offset + 1, (int)mem.byteSize());
      } else {
         return new AssetEditorSubscribeModifiedAssetsChanges(mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.subscribe ? 1 : 0);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BOOL, offset + 0, this.subscribe);
      return 1;
   }

   @Override
   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public AssetEditorSubscribeModifiedAssetsChanges clone() {
      AssetEditorSubscribeModifiedAssetsChanges copy = new AssetEditorSubscribeModifiedAssetsChanges();
      copy.subscribe = this.subscribe;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorSubscribeModifiedAssetsChanges other ? this.subscribe == other.subscribe : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.subscribe);
   }
}
