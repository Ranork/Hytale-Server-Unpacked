package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModelUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public Model model;
   public float entityScale;

   public ModelUpdate() {
   }

   public ModelUpdate(@Nullable Model model, float entityScale) {
      this.model = model;
      this.entityScale = entityScale;
   }

   public ModelUpdate(@Nonnull ModelUpdate other) {
      this.model = other.model;
      this.entityScale = other.entityScale;
   }

   @Nonnull
   public static ModelUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("ModelUpdate", 5, buf.readableBytes() - offset);
      } else {
         ModelUpdate obj = new ModelUpdate();
         byte nullBits = buf.getByte(offset);
         obj.entityScale = buf.getFloatLE(offset + 1);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            obj.model = Model.deserialize(buf, pos);
            pos += Model.computeBytesConsumed(buf, pos);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         pos += Model.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   @Nullable
   public static Model getModel(MemorySegment mem) {
      return getModel(mem, 0);
   }

   @Nullable
   public static Model getModel(MemorySegment mem, int offset) {
      return hasModel(mem, offset) ? Model.toObject(mem, offset + 5) : null;
   }

   public static float getEntityScale(MemorySegment mem) {
      return getEntityScale(mem, 0);
   }

   public static float getEntityScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static boolean hasModel(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static ModelUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ModelUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ModelUpdate", offset + 5, (int)mem.byteSize());
      } else {
         return new ModelUpdate(hasModel(mem, offset) ? Model.toObject(mem, offset + 5) : null, mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)));
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.model != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.entityScale);
      if (this.model != null) {
         this.model.serialize(buf);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.model != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.entityScale);
      int varOffset = offset + 5;
      if (this.model != null) {
         varOffset += this.model.serialize(mem, varOffset);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 5;
      if (this.model != null) {
         size += this.model.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            ValidationResult modelResult = Model.validateStructure(buffer, pos);
            if (!modelResult.isValid()) {
               return ValidationResult.error("Invalid Model: " + modelResult.error());
            }

            pos += Model.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public ModelUpdate clone() {
      ModelUpdate copy = new ModelUpdate();
      copy.model = this.model != null ? this.model.clone() : null;
      copy.entityScale = this.entityScale;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ModelUpdate other) ? false : Objects.equals(this.model, other.model) && this.entityScale == other.entityScale;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.model, this.entityScale);
   }
}
