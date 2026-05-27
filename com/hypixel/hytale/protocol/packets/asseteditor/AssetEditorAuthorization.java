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

public class AssetEditorAuthorization implements Packet, ToClientPacket {
   public static final int PACKET_ID = 303;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   public boolean canUse;

   @Override
   public int getId() {
      return 303;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorAuthorization() {
   }

   public AssetEditorAuthorization(boolean canUse) {
      this.canUse = canUse;
   }

   public AssetEditorAuthorization(@Nonnull AssetEditorAuthorization other) {
      this.canUse = other.canUse;
   }

   @Nonnull
   public static AssetEditorAuthorization deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("AssetEditorAuthorization", 1, buf.readableBytes() - offset);
      } else {
         AssetEditorAuthorization obj = new AssetEditorAuthorization();
         obj.canUse = buf.getByte(offset + 0) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   public static boolean getCanUse(MemorySegment mem) {
      return getCanUse(mem, 0);
   }

   public static boolean getCanUse(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0));
   }

   public static AssetEditorAuthorization toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorAuthorization toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorAuthorization", offset + 1, (int)mem.byteSize());
      } else {
         return new AssetEditorAuthorization(mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.canUse ? 1 : 0);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BOOL, offset + 0, this.canUse);
      return 1;
   }

   @Override
   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public AssetEditorAuthorization clone() {
      AssetEditorAuthorization copy = new AssetEditorAuthorization();
      copy.canUse = this.canUse;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorAuthorization other ? this.canUse == other.canUse : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.canUse);
   }
}
