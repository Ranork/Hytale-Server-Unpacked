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

public class AssetEditorUpdateWeatherPreviewLock implements Packet, ToServerPacket {
   public static final int PACKET_ID = 354;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   public boolean locked;

   @Override
   public int getId() {
      return 354;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorUpdateWeatherPreviewLock() {
   }

   public AssetEditorUpdateWeatherPreviewLock(boolean locked) {
      this.locked = locked;
   }

   public AssetEditorUpdateWeatherPreviewLock(@Nonnull AssetEditorUpdateWeatherPreviewLock other) {
      this.locked = other.locked;
   }

   @Nonnull
   public static AssetEditorUpdateWeatherPreviewLock deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("AssetEditorUpdateWeatherPreviewLock", 1, buf.readableBytes() - offset);
      } else {
         AssetEditorUpdateWeatherPreviewLock obj = new AssetEditorUpdateWeatherPreviewLock();
         obj.locked = buf.getByte(offset + 0) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   public static boolean getLocked(MemorySegment mem) {
      return getLocked(mem, 0);
   }

   public static boolean getLocked(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0));
   }

   public static AssetEditorUpdateWeatherPreviewLock toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorUpdateWeatherPreviewLock toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorUpdateWeatherPreviewLock", offset + 1, (int)mem.byteSize());
      } else {
         return new AssetEditorUpdateWeatherPreviewLock(mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.locked ? 1 : 0);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BOOL, offset + 0, this.locked);
      return 1;
   }

   @Override
   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public AssetEditorUpdateWeatherPreviewLock clone() {
      AssetEditorUpdateWeatherPreviewLock copy = new AssetEditorUpdateWeatherPreviewLock();
      copy.locked = this.locked;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorUpdateWeatherPreviewLock other ? this.locked == other.locked : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.locked);
   }
}
