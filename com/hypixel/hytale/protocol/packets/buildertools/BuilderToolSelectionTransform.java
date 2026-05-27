package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.BlockPosition;
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
import javax.annotation.Nullable;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public class BuilderToolSelectionTransform implements Packet, ToServerPacket {
   public static final int PACKET_ID = 405;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 80;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 80;
   public static final int MAX_SIZE = 80;
   @Nullable
   public Quaternionfc rotation;
   @Nullable
   public BlockPosition translationOffset;
   @Nullable
   public BlockPosition initialSelectionMin;
   @Nullable
   public BlockPosition initialSelectionMax;
   @Nonnull
   public Vector3fc initialRotationOrigin = PacketIO.ZERO_VECTOR3;
   public boolean cutOriginal;
   public boolean applyTransformationToSelectionMinMax;
   public boolean isExitingTransformMode;
   @Nullable
   public BlockPosition initialPastePointForClipboardPaste;

   @Override
   public int getId() {
      return 405;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolSelectionTransform() {
   }

   public BuilderToolSelectionTransform(
      @Nullable Quaternionfc rotation,
      @Nullable BlockPosition translationOffset,
      @Nullable BlockPosition initialSelectionMin,
      @Nullable BlockPosition initialSelectionMax,
      @Nonnull Vector3fc initialRotationOrigin,
      boolean cutOriginal,
      boolean applyTransformationToSelectionMinMax,
      boolean isExitingTransformMode,
      @Nullable BlockPosition initialPastePointForClipboardPaste
   ) {
      this.rotation = rotation;
      this.translationOffset = translationOffset;
      this.initialSelectionMin = initialSelectionMin;
      this.initialSelectionMax = initialSelectionMax;
      this.initialRotationOrigin = initialRotationOrigin;
      this.cutOriginal = cutOriginal;
      this.applyTransformationToSelectionMinMax = applyTransformationToSelectionMinMax;
      this.isExitingTransformMode = isExitingTransformMode;
      this.initialPastePointForClipboardPaste = initialPastePointForClipboardPaste;
   }

   public BuilderToolSelectionTransform(@Nonnull BuilderToolSelectionTransform other) {
      this.rotation = other.rotation;
      this.translationOffset = other.translationOffset;
      this.initialSelectionMin = other.initialSelectionMin;
      this.initialSelectionMax = other.initialSelectionMax;
      this.initialRotationOrigin = other.initialRotationOrigin;
      this.cutOriginal = other.cutOriginal;
      this.applyTransformationToSelectionMinMax = other.applyTransformationToSelectionMinMax;
      this.isExitingTransformMode = other.isExitingTransformMode;
      this.initialPastePointForClipboardPaste = other.initialPastePointForClipboardPaste;
   }

   @Nonnull
   public static BuilderToolSelectionTransform deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 80) {
         throw ProtocolException.bufferTooSmall("BuilderToolSelectionTransform", 80, buf.readableBytes() - offset);
      } else {
         BuilderToolSelectionTransform obj = new BuilderToolSelectionTransform();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.rotation = PacketIO.readQuaternionf(buf, offset + 1);
         }

         if ((nullBits & 2) != 0) {
            obj.translationOffset = BlockPosition.deserialize(buf, offset + 17);
         }

         if ((nullBits & 4) != 0) {
            obj.initialSelectionMin = BlockPosition.deserialize(buf, offset + 29);
         }

         if ((nullBits & 8) != 0) {
            obj.initialSelectionMax = BlockPosition.deserialize(buf, offset + 41);
         }

         obj.initialRotationOrigin = PacketIO.readVector3f(buf, offset + 53);
         obj.cutOriginal = buf.getByte(offset + 65) != 0;
         obj.applyTransformationToSelectionMinMax = buf.getByte(offset + 66) != 0;
         obj.isExitingTransformMode = buf.getByte(offset + 67) != 0;
         if ((nullBits & 16) != 0) {
            obj.initialPastePointForClipboardPaste = BlockPosition.deserialize(buf, offset + 68);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 80;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 80L;
   }

   @Nullable
   public static Quaternionfc getRotation(MemorySegment mem) {
      return getRotation(mem, 0);
   }

   @Nullable
   public static Quaternionfc getRotation(MemorySegment mem, int offset) {
      return hasRotation(mem, offset) ? PacketIO.readQuaternionf(mem, offset + 1) : null;
   }

   @Nullable
   public static BlockPosition getTranslationOffset(MemorySegment mem) {
      return getTranslationOffset(mem, 0);
   }

   @Nullable
   public static BlockPosition getTranslationOffset(MemorySegment mem, int offset) {
      return hasTranslationOffset(mem, offset) ? BlockPosition.toObject(mem, offset + 17) : null;
   }

   @Nullable
   public static BlockPosition getInitialSelectionMin(MemorySegment mem) {
      return getInitialSelectionMin(mem, 0);
   }

   @Nullable
   public static BlockPosition getInitialSelectionMin(MemorySegment mem, int offset) {
      return hasInitialSelectionMin(mem, offset) ? BlockPosition.toObject(mem, offset + 29) : null;
   }

   @Nullable
   public static BlockPosition getInitialSelectionMax(MemorySegment mem) {
      return getInitialSelectionMax(mem, 0);
   }

   @Nullable
   public static BlockPosition getInitialSelectionMax(MemorySegment mem, int offset) {
      return hasInitialSelectionMax(mem, offset) ? BlockPosition.toObject(mem, offset + 41) : null;
   }

   public static Vector3fc getInitialRotationOrigin(MemorySegment mem) {
      return getInitialRotationOrigin(mem, 0);
   }

   public static Vector3fc getInitialRotationOrigin(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 53);
   }

   public static boolean getCutOriginal(MemorySegment mem) {
      return getCutOriginal(mem, 0);
   }

   public static boolean getCutOriginal(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 65));
   }

   public static boolean getApplyTransformationToSelectionMinMax(MemorySegment mem) {
      return getApplyTransformationToSelectionMinMax(mem, 0);
   }

   public static boolean getApplyTransformationToSelectionMinMax(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 66));
   }

   public static boolean getIsExitingTransformMode(MemorySegment mem) {
      return getIsExitingTransformMode(mem, 0);
   }

   public static boolean getIsExitingTransformMode(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 67));
   }

   @Nullable
   public static BlockPosition getInitialPastePointForClipboardPaste(MemorySegment mem) {
      return getInitialPastePointForClipboardPaste(mem, 0);
   }

   @Nullable
   public static BlockPosition getInitialPastePointForClipboardPaste(MemorySegment mem, int offset) {
      return hasInitialPastePointForClipboardPaste(mem, offset) ? BlockPosition.toObject(mem, offset + 68) : null;
   }

   public static boolean hasRotation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasTranslationOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasInitialSelectionMin(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasInitialSelectionMax(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasInitialPastePointForClipboardPaste(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static BuilderToolSelectionTransform toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolSelectionTransform toObject(MemorySegment mem, int offset) {
      if (offset + 80 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolSelectionTransform", offset + 80, (int)mem.byteSize());
      } else {
         return new BuilderToolSelectionTransform(
            hasRotation(mem, offset) ? PacketIO.readQuaternionf(mem, offset + 1) : null,
            hasTranslationOffset(mem, offset) ? BlockPosition.toObject(mem, offset + 17) : null,
            hasInitialSelectionMin(mem, offset) ? BlockPosition.toObject(mem, offset + 29) : null,
            hasInitialSelectionMax(mem, offset) ? BlockPosition.toObject(mem, offset + 41) : null,
            PacketIO.readVector3f(mem, offset + 53),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 65)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 66)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 67)),
            hasInitialPastePointForClipboardPaste(mem, offset) ? BlockPosition.toObject(mem, offset + 68) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.translationOffset != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.initialSelectionMin != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.initialSelectionMax != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.initialPastePointForClipboardPaste != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      if (this.rotation != null) {
         PacketIO.writeQuaternionf(buf, this.rotation);
      } else {
         buf.writeZero(16);
      }

      if (this.translationOffset != null) {
         this.translationOffset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.initialSelectionMin != null) {
         this.initialSelectionMin.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.initialSelectionMax != null) {
         this.initialSelectionMax.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      PacketIO.writeVector3f(buf, this.initialRotationOrigin);
      buf.writeByte(this.cutOriginal ? 1 : 0);
      buf.writeByte(this.applyTransformationToSelectionMinMax ? 1 : 0);
      buf.writeByte(this.isExitingTransformMode ? 1 : 0);
      if (this.initialPastePointForClipboardPaste != null) {
         this.initialPastePointForClipboardPaste.serialize(buf);
      } else {
         buf.writeZero(12);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.translationOffset != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.initialSelectionMin != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.initialSelectionMax != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.initialPastePointForClipboardPaste != null) {
         nullBits = (byte)(nullBits | 16);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.rotation != null) {
         PacketIO.writeQuaternionf(mem, offset + 1, this.rotation);
      } else {
         mem.asSlice(offset + 1, 16L).fill((byte)0);
      }

      if (this.translationOffset != null) {
         this.translationOffset.serialize(mem, offset + 17);
      } else {
         mem.asSlice(offset + 17, 12L).fill((byte)0);
      }

      if (this.initialSelectionMin != null) {
         this.initialSelectionMin.serialize(mem, offset + 29);
      } else {
         mem.asSlice(offset + 29, 12L).fill((byte)0);
      }

      if (this.initialSelectionMax != null) {
         this.initialSelectionMax.serialize(mem, offset + 41);
      } else {
         mem.asSlice(offset + 41, 12L).fill((byte)0);
      }

      PacketIO.writeVector3f(mem, offset + 53, this.initialRotationOrigin);
      mem.set(PacketIO.PROTO_BOOL, offset + 65, this.cutOriginal);
      mem.set(PacketIO.PROTO_BOOL, offset + 66, this.applyTransformationToSelectionMinMax);
      mem.set(PacketIO.PROTO_BOOL, offset + 67, this.isExitingTransformMode);
      if (this.initialPastePointForClipboardPaste != null) {
         this.initialPastePointForClipboardPaste.serialize(mem, offset + 68);
      } else {
         mem.asSlice(offset + 68, 12L).fill((byte)0);
      }

      return 80;
   }

   @Override
   public int computeSize() {
      return 80;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 80) {
         return ValidationResult.error("Buffer too small: expected at least 80 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public BuilderToolSelectionTransform clone() {
      BuilderToolSelectionTransform copy = new BuilderToolSelectionTransform();
      copy.rotation = this.rotation;
      copy.translationOffset = this.translationOffset != null ? this.translationOffset.clone() : null;
      copy.initialSelectionMin = this.initialSelectionMin != null ? this.initialSelectionMin.clone() : null;
      copy.initialSelectionMax = this.initialSelectionMax != null ? this.initialSelectionMax.clone() : null;
      copy.initialRotationOrigin = this.initialRotationOrigin;
      copy.cutOriginal = this.cutOriginal;
      copy.applyTransformationToSelectionMinMax = this.applyTransformationToSelectionMinMax;
      copy.isExitingTransformMode = this.isExitingTransformMode;
      copy.initialPastePointForClipboardPaste = this.initialPastePointForClipboardPaste != null ? this.initialPastePointForClipboardPaste.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolSelectionTransform other)
            ? false
            : Objects.equals(this.rotation, other.rotation)
               && Objects.equals(this.translationOffset, other.translationOffset)
               && Objects.equals(this.initialSelectionMin, other.initialSelectionMin)
               && Objects.equals(this.initialSelectionMax, other.initialSelectionMax)
               && Objects.equals(this.initialRotationOrigin, other.initialRotationOrigin)
               && this.cutOriginal == other.cutOriginal
               && this.applyTransformationToSelectionMinMax == other.applyTransformationToSelectionMinMax
               && this.isExitingTransformMode == other.isExitingTransformMode
               && Objects.equals(this.initialPastePointForClipboardPaste, other.initialPastePointForClipboardPaste);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.rotation,
         this.translationOffset,
         this.initialSelectionMin,
         this.initialSelectionMax,
         this.initialRotationOrigin,
         this.cutOriginal,
         this.applyTransformationToSelectionMinMax,
         this.isExitingTransformMode,
         this.initialPastePointForClipboardPaste
      );
   }
}
