package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class AssetEditorRebuildCaches {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 5;
   public boolean blockTextures;
   public boolean models;
   public boolean modelTextures;
   public boolean mapGeometry;
   public boolean itemIcons;

   public AssetEditorRebuildCaches() {
   }

   public AssetEditorRebuildCaches(boolean blockTextures, boolean models, boolean modelTextures, boolean mapGeometry, boolean itemIcons) {
      this.blockTextures = blockTextures;
      this.models = models;
      this.modelTextures = modelTextures;
      this.mapGeometry = mapGeometry;
      this.itemIcons = itemIcons;
   }

   public AssetEditorRebuildCaches(@Nonnull AssetEditorRebuildCaches other) {
      this.blockTextures = other.blockTextures;
      this.models = other.models;
      this.modelTextures = other.modelTextures;
      this.mapGeometry = other.mapGeometry;
      this.itemIcons = other.itemIcons;
   }

   @Nonnull
   public static AssetEditorRebuildCaches deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("AssetEditorRebuildCaches", 5, buf.readableBytes() - offset);
      } else {
         AssetEditorRebuildCaches obj = new AssetEditorRebuildCaches();
         obj.blockTextures = buf.getByte(offset + 0) != 0;
         obj.models = buf.getByte(offset + 1) != 0;
         obj.modelTextures = buf.getByte(offset + 2) != 0;
         obj.mapGeometry = buf.getByte(offset + 3) != 0;
         obj.itemIcons = buf.getByte(offset + 4) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 5;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static boolean getBlockTextures(MemorySegment mem) {
      return getBlockTextures(mem, 0);
   }

   public static boolean getBlockTextures(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0));
   }

   public static boolean getModels(MemorySegment mem) {
      return getModels(mem, 0);
   }

   public static boolean getModels(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static boolean getModelTextures(MemorySegment mem) {
      return getModelTextures(mem, 0);
   }

   public static boolean getModelTextures(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2));
   }

   public static boolean getMapGeometry(MemorySegment mem) {
      return getMapGeometry(mem, 0);
   }

   public static boolean getMapGeometry(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 3));
   }

   public static boolean getItemIcons(MemorySegment mem) {
      return getItemIcons(mem, 0);
   }

   public static boolean getItemIcons(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4));
   }

   public static AssetEditorRebuildCaches toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorRebuildCaches toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorRebuildCaches", offset + 5, (int)mem.byteSize());
      } else {
         return new AssetEditorRebuildCaches(
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 3)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.blockTextures ? 1 : 0);
      buf.writeByte(this.models ? 1 : 0);
      buf.writeByte(this.modelTextures ? 1 : 0);
      buf.writeByte(this.mapGeometry ? 1 : 0);
      buf.writeByte(this.itemIcons ? 1 : 0);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BOOL, offset + 0, this.blockTextures);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.models);
      mem.set(PacketIO.PROTO_BOOL, offset + 2, this.modelTextures);
      mem.set(PacketIO.PROTO_BOOL, offset + 3, this.mapGeometry);
      mem.set(PacketIO.PROTO_BOOL, offset + 4, this.itemIcons);
      return 5;
   }

   public int computeSize() {
      return 5;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 5 ? ValidationResult.error("Buffer too small: expected at least 5 bytes") : ValidationResult.OK;
   }

   public AssetEditorRebuildCaches clone() {
      AssetEditorRebuildCaches copy = new AssetEditorRebuildCaches();
      copy.blockTextures = this.blockTextures;
      copy.models = this.models;
      copy.modelTextures = this.modelTextures;
      copy.mapGeometry = this.mapGeometry;
      copy.itemIcons = this.itemIcons;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorRebuildCaches other)
            ? false
            : this.blockTextures == other.blockTextures
               && this.models == other.models
               && this.modelTextures == other.modelTextures
               && this.mapGeometry == other.mapGeometry
               && this.itemIcons == other.itemIcons;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.blockTextures, this.models, this.modelTextures, this.mapGeometry, this.itemIcons);
   }
}
