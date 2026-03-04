package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MountedUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 48;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 48;
   public static final int MAX_SIZE = 48;
   public int mountedToEntity;
   @Nullable
   public Vector3f attachmentOffset;
   @Nonnull
   public MountController controller = MountController.Minecart;
   @Nullable
   public BlockMount block;

   public MountedUpdate() {
   }

   public MountedUpdate(int mountedToEntity, @Nullable Vector3f attachmentOffset, @Nonnull MountController controller, @Nullable BlockMount block) {
      this.mountedToEntity = mountedToEntity;
      this.attachmentOffset = attachmentOffset;
      this.controller = controller;
      this.block = block;
   }

   public MountedUpdate(@Nonnull MountedUpdate other) {
      this.mountedToEntity = other.mountedToEntity;
      this.attachmentOffset = other.attachmentOffset;
      this.controller = other.controller;
      this.block = other.block;
   }

   @Nonnull
   public static MountedUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      MountedUpdate obj = new MountedUpdate();
      byte nullBits = buf.getByte(offset);
      obj.mountedToEntity = buf.getIntLE(offset + 1);
      if ((nullBits & 1) != 0) {
         obj.attachmentOffset = Vector3f.deserialize(buf, offset + 5);
      }

      obj.controller = MountController.fromValue(buf.getByte(offset + 17));
      if ((nullBits & 2) != 0) {
         obj.block = BlockMount.deserialize(buf, offset + 18);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 48;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.attachmentOffset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.block != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.mountedToEntity);
      if (this.attachmentOffset != null) {
         this.attachmentOffset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeByte(this.controller.getValue());
      if (this.block != null) {
         this.block.serialize(buf);
      } else {
         buf.writeZero(30);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      return 48;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 48 ? ValidationResult.error("Buffer too small: expected at least 48 bytes") : ValidationResult.OK;
   }

   public MountedUpdate clone() {
      MountedUpdate copy = new MountedUpdate();
      copy.mountedToEntity = this.mountedToEntity;
      copy.attachmentOffset = this.attachmentOffset != null ? this.attachmentOffset.clone() : null;
      copy.controller = this.controller;
      copy.block = this.block != null ? this.block.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MountedUpdate other)
            ? false
            : this.mountedToEntity == other.mountedToEntity
               && Objects.equals(this.attachmentOffset, other.attachmentOffset)
               && Objects.equals(this.controller, other.controller)
               && Objects.equals(this.block, other.block);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.mountedToEntity, this.attachmentOffset, this.controller, this.block);
   }
}
