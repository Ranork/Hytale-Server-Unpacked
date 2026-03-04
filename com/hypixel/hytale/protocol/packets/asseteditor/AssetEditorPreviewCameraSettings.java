package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorPreviewCameraSettings {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 29;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 29;
   public static final int MAX_SIZE = 29;
   public float modelScale;
   @Nullable
   public Vector3f cameraPosition;
   @Nullable
   public Vector3f cameraOrientation;

   public AssetEditorPreviewCameraSettings() {
   }

   public AssetEditorPreviewCameraSettings(float modelScale, @Nullable Vector3f cameraPosition, @Nullable Vector3f cameraOrientation) {
      this.modelScale = modelScale;
      this.cameraPosition = cameraPosition;
      this.cameraOrientation = cameraOrientation;
   }

   public AssetEditorPreviewCameraSettings(@Nonnull AssetEditorPreviewCameraSettings other) {
      this.modelScale = other.modelScale;
      this.cameraPosition = other.cameraPosition;
      this.cameraOrientation = other.cameraOrientation;
   }

   @Nonnull
   public static AssetEditorPreviewCameraSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorPreviewCameraSettings obj = new AssetEditorPreviewCameraSettings();
      byte nullBits = buf.getByte(offset);
      obj.modelScale = buf.getFloatLE(offset + 1);
      if ((nullBits & 1) != 0) {
         obj.cameraPosition = Vector3f.deserialize(buf, offset + 5);
      }

      if ((nullBits & 2) != 0) {
         obj.cameraOrientation = Vector3f.deserialize(buf, offset + 17);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 29;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.cameraPosition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.cameraOrientation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.modelScale);
      if (this.cameraPosition != null) {
         this.cameraPosition.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.cameraOrientation != null) {
         this.cameraOrientation.serialize(buf);
      } else {
         buf.writeZero(12);
      }
   }

   public int computeSize() {
      return 29;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 29 ? ValidationResult.error("Buffer too small: expected at least 29 bytes") : ValidationResult.OK;
   }

   public AssetEditorPreviewCameraSettings clone() {
      AssetEditorPreviewCameraSettings copy = new AssetEditorPreviewCameraSettings();
      copy.modelScale = this.modelScale;
      copy.cameraPosition = this.cameraPosition != null ? this.cameraPosition.clone() : null;
      copy.cameraOrientation = this.cameraOrientation != null ? this.cameraOrientation.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorPreviewCameraSettings other)
            ? false
            : this.modelScale == other.modelScale
               && Objects.equals(this.cameraPosition, other.cameraPosition)
               && Objects.equals(this.cameraOrientation, other.cameraOrientation);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.modelScale, this.cameraPosition, this.cameraOrientation);
   }
}
