package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
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
      AssetEditorRebuildCaches obj = new AssetEditorRebuildCaches();
      obj.blockTextures = buf.getByte(offset + 0) != 0;
      obj.models = buf.getByte(offset + 1) != 0;
      obj.modelTextures = buf.getByte(offset + 2) != 0;
      obj.mapGeometry = buf.getByte(offset + 3) != 0;
      obj.itemIcons = buf.getByte(offset + 4) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 5;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.blockTextures ? 1 : 0);
      buf.writeByte(this.models ? 1 : 0);
      buf.writeByte(this.modelTextures ? 1 : 0);
      buf.writeByte(this.mapGeometry ? 1 : 0);
      buf.writeByte(this.itemIcons ? 1 : 0);
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
