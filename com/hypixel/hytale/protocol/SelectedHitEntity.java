package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SelectedHitEntity {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 53;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 53;
   public static final int MAX_SIZE = 53;
   public int networkId;
   @Nullable
   public Vector3f hitLocation;
   @Nullable
   public Position position;
   @Nullable
   public Direction bodyRotation;

   public SelectedHitEntity() {
   }

   public SelectedHitEntity(int networkId, @Nullable Vector3f hitLocation, @Nullable Position position, @Nullable Direction bodyRotation) {
      this.networkId = networkId;
      this.hitLocation = hitLocation;
      this.position = position;
      this.bodyRotation = bodyRotation;
   }

   public SelectedHitEntity(@Nonnull SelectedHitEntity other) {
      this.networkId = other.networkId;
      this.hitLocation = other.hitLocation;
      this.position = other.position;
      this.bodyRotation = other.bodyRotation;
   }

   @Nonnull
   public static SelectedHitEntity deserialize(@Nonnull ByteBuf buf, int offset) {
      SelectedHitEntity obj = new SelectedHitEntity();
      byte nullBits = buf.getByte(offset);
      obj.networkId = buf.getIntLE(offset + 1);
      if ((nullBits & 1) != 0) {
         obj.hitLocation = Vector3f.deserialize(buf, offset + 5);
      }

      if ((nullBits & 2) != 0) {
         obj.position = Position.deserialize(buf, offset + 17);
      }

      if ((nullBits & 4) != 0) {
         obj.bodyRotation = Direction.deserialize(buf, offset + 41);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 53;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.hitLocation != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.position != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.bodyRotation != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.networkId);
      if (this.hitLocation != null) {
         this.hitLocation.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.position != null) {
         this.position.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      if (this.bodyRotation != null) {
         this.bodyRotation.serialize(buf);
      } else {
         buf.writeZero(12);
      }
   }

   public int computeSize() {
      return 53;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 53 ? ValidationResult.error("Buffer too small: expected at least 53 bytes") : ValidationResult.OK;
   }

   public SelectedHitEntity clone() {
      SelectedHitEntity copy = new SelectedHitEntity();
      copy.networkId = this.networkId;
      copy.hitLocation = this.hitLocation != null ? this.hitLocation.clone() : null;
      copy.position = this.position != null ? this.position.clone() : null;
      copy.bodyRotation = this.bodyRotation != null ? this.bodyRotation.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SelectedHitEntity other)
            ? false
            : this.networkId == other.networkId
               && Objects.equals(this.hitLocation, other.hitLocation)
               && Objects.equals(this.position, other.position)
               && Objects.equals(this.bodyRotation, other.bodyRotation);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.networkId, this.hitLocation, this.position, this.bodyRotation);
   }
}
