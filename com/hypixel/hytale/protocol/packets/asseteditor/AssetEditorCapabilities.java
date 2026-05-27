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

public class AssetEditorCapabilities implements Packet, ToClientPacket {
   public static final int PACKET_ID = 304;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 5;
   public boolean canDiscardAssets;
   public boolean canEditAssets;
   public boolean canCreateAssetPacks;
   public boolean canEditAssetPacks;
   public boolean canDeleteAssetPacks;

   @Override
   public int getId() {
      return 304;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorCapabilities() {
   }

   public AssetEditorCapabilities(
      boolean canDiscardAssets, boolean canEditAssets, boolean canCreateAssetPacks, boolean canEditAssetPacks, boolean canDeleteAssetPacks
   ) {
      this.canDiscardAssets = canDiscardAssets;
      this.canEditAssets = canEditAssets;
      this.canCreateAssetPacks = canCreateAssetPacks;
      this.canEditAssetPacks = canEditAssetPacks;
      this.canDeleteAssetPacks = canDeleteAssetPacks;
   }

   public AssetEditorCapabilities(@Nonnull AssetEditorCapabilities other) {
      this.canDiscardAssets = other.canDiscardAssets;
      this.canEditAssets = other.canEditAssets;
      this.canCreateAssetPacks = other.canCreateAssetPacks;
      this.canEditAssetPacks = other.canEditAssetPacks;
      this.canDeleteAssetPacks = other.canDeleteAssetPacks;
   }

   @Nonnull
   public static AssetEditorCapabilities deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("AssetEditorCapabilities", 5, buf.readableBytes() - offset);
      } else {
         AssetEditorCapabilities obj = new AssetEditorCapabilities();
         obj.canDiscardAssets = buf.getByte(offset + 0) != 0;
         obj.canEditAssets = buf.getByte(offset + 1) != 0;
         obj.canCreateAssetPacks = buf.getByte(offset + 2) != 0;
         obj.canEditAssetPacks = buf.getByte(offset + 3) != 0;
         obj.canDeleteAssetPacks = buf.getByte(offset + 4) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 5;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static boolean getCanDiscardAssets(MemorySegment mem) {
      return getCanDiscardAssets(mem, 0);
   }

   public static boolean getCanDiscardAssets(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0));
   }

   public static boolean getCanEditAssets(MemorySegment mem) {
      return getCanEditAssets(mem, 0);
   }

   public static boolean getCanEditAssets(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static boolean getCanCreateAssetPacks(MemorySegment mem) {
      return getCanCreateAssetPacks(mem, 0);
   }

   public static boolean getCanCreateAssetPacks(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2));
   }

   public static boolean getCanEditAssetPacks(MemorySegment mem) {
      return getCanEditAssetPacks(mem, 0);
   }

   public static boolean getCanEditAssetPacks(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 3));
   }

   public static boolean getCanDeleteAssetPacks(MemorySegment mem) {
      return getCanDeleteAssetPacks(mem, 0);
   }

   public static boolean getCanDeleteAssetPacks(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4));
   }

   public static AssetEditorCapabilities toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorCapabilities toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorCapabilities", offset + 5, (int)mem.byteSize());
      } else {
         return new AssetEditorCapabilities(
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 3)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.canDiscardAssets ? 1 : 0);
      buf.writeByte(this.canEditAssets ? 1 : 0);
      buf.writeByte(this.canCreateAssetPacks ? 1 : 0);
      buf.writeByte(this.canEditAssetPacks ? 1 : 0);
      buf.writeByte(this.canDeleteAssetPacks ? 1 : 0);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BOOL, offset + 0, this.canDiscardAssets);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.canEditAssets);
      mem.set(PacketIO.PROTO_BOOL, offset + 2, this.canCreateAssetPacks);
      mem.set(PacketIO.PROTO_BOOL, offset + 3, this.canEditAssetPacks);
      mem.set(PacketIO.PROTO_BOOL, offset + 4, this.canDeleteAssetPacks);
      return 5;
   }

   @Override
   public int computeSize() {
      return 5;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 5 ? ValidationResult.error("Buffer too small: expected at least 5 bytes") : ValidationResult.OK;
   }

   public AssetEditorCapabilities clone() {
      AssetEditorCapabilities copy = new AssetEditorCapabilities();
      copy.canDiscardAssets = this.canDiscardAssets;
      copy.canEditAssets = this.canEditAssets;
      copy.canCreateAssetPacks = this.canCreateAssetPacks;
      copy.canEditAssetPacks = this.canEditAssetPacks;
      copy.canDeleteAssetPacks = this.canDeleteAssetPacks;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorCapabilities other)
            ? false
            : this.canDiscardAssets == other.canDiscardAssets
               && this.canEditAssets == other.canEditAssets
               && this.canCreateAssetPacks == other.canCreateAssetPacks
               && this.canEditAssetPacks == other.canEditAssetPacks
               && this.canDeleteAssetPacks == other.canDeleteAssetPacks;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.canDiscardAssets, this.canEditAssets, this.canCreateAssetPacks, this.canEditAssetPacks, this.canDeleteAssetPacks);
   }
}
