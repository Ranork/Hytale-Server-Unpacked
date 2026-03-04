package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionCamera {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 29;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 29;
   public static final int MAX_SIZE = 29;
   public float time;
   @Nullable
   public Vector3f position;
   @Nullable
   public Direction rotation;

   public InteractionCamera() {
   }

   public InteractionCamera(float time, @Nullable Vector3f position, @Nullable Direction rotation) {
      this.time = time;
      this.position = position;
      this.rotation = rotation;
   }

   public InteractionCamera(@Nonnull InteractionCamera other) {
      this.time = other.time;
      this.position = other.position;
      this.rotation = other.rotation;
   }

   @Nonnull
   public static InteractionCamera deserialize(@Nonnull ByteBuf buf, int offset) {
      InteractionCamera obj = new InteractionCamera();
      byte nullBits = buf.getByte(offset);
      obj.time = buf.getFloatLE(offset + 1);
      if ((nullBits & 1) != 0) {
         obj.position = Vector3f.deserialize(buf, offset + 5);
      }

      if ((nullBits & 2) != 0) {
         obj.rotation = Direction.deserialize(buf, offset + 17);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 29;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.time);
      if (this.position != null) {
         this.position.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.rotation != null) {
         this.rotation.serialize(buf);
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

   public InteractionCamera clone() {
      InteractionCamera copy = new InteractionCamera();
      copy.time = this.time;
      copy.position = this.position != null ? this.position.clone() : null;
      copy.rotation = this.rotation != null ? this.rotation.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InteractionCamera other)
            ? false
            : this.time == other.time && Objects.equals(this.position, other.position) && Objects.equals(this.rotation, other.rotation);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.time, this.position, this.rotation);
   }
}
