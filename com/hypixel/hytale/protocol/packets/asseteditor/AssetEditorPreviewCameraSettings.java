package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.joml.Vector3fc;

public class AssetEditorPreviewCameraSettings {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 28;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 28;
   public static final int MAX_SIZE = 28;
   public float modelScale;
   @Nonnull
   public Vector3fc cameraPosition = PacketIO.ZERO_VECTOR3;
   @Nonnull
   public Vector3fc cameraOrientation = PacketIO.ZERO_VECTOR3;

   public AssetEditorPreviewCameraSettings() {
   }

   public AssetEditorPreviewCameraSettings(float modelScale, @Nonnull Vector3fc cameraPosition, @Nonnull Vector3fc cameraOrientation) {
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
      if (buf.readableBytes() - offset < 28) {
         throw ProtocolException.bufferTooSmall("AssetEditorPreviewCameraSettings", 28, buf.readableBytes() - offset);
      } else {
         AssetEditorPreviewCameraSettings obj = new AssetEditorPreviewCameraSettings();
         obj.modelScale = buf.getFloatLE(offset + 0);
         obj.cameraPosition = PacketIO.readVector3f(buf, offset + 4);
         obj.cameraOrientation = PacketIO.readVector3f(buf, offset + 16);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 28;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 28L;
   }

   public static float getModelScale(MemorySegment mem) {
      return getModelScale(mem, 0);
   }

   public static float getModelScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static Vector3fc getCameraPosition(MemorySegment mem) {
      return getCameraPosition(mem, 0);
   }

   public static Vector3fc getCameraPosition(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 4);
   }

   public static Vector3fc getCameraOrientation(MemorySegment mem) {
      return getCameraOrientation(mem, 0);
   }

   public static Vector3fc getCameraOrientation(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 16);
   }

   public static AssetEditorPreviewCameraSettings toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorPreviewCameraSettings toObject(MemorySegment mem, int offset) {
      if (offset + 28 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorPreviewCameraSettings", offset + 28, (int)mem.byteSize());
      } else {
         return new AssetEditorPreviewCameraSettings(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)), PacketIO.readVector3f(mem, offset + 4), PacketIO.readVector3f(mem, offset + 16)
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.modelScale);
      PacketIO.writeVector3f(buf, this.cameraPosition);
      PacketIO.writeVector3f(buf, this.cameraOrientation);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.modelScale);
      PacketIO.writeVector3f(mem, offset + 4, this.cameraPosition);
      PacketIO.writeVector3f(mem, offset + 16, this.cameraOrientation);
      return 28;
   }

   public int computeSize() {
      return 28;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 28 ? ValidationResult.error("Buffer too small: expected at least 28 bytes") : ValidationResult.OK;
   }

   public AssetEditorPreviewCameraSettings clone() {
      AssetEditorPreviewCameraSettings copy = new AssetEditorPreviewCameraSettings();
      copy.modelScale = this.modelScale;
      copy.cameraPosition = this.cameraPosition;
      copy.cameraOrientation = this.cameraOrientation;
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
