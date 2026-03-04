package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.ModelTransform;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderToolSetEntityTransform implements Packet, ToServerPacket {
   public static final int PACKET_ID = 402;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 54;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 54;
   public static final int MAX_SIZE = 54;
   public int entityId;
   @Nullable
   public ModelTransform modelTransform;

   @Override
   public int getId() {
      return 402;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolSetEntityTransform() {
   }

   public BuilderToolSetEntityTransform(int entityId, @Nullable ModelTransform modelTransform) {
      this.entityId = entityId;
      this.modelTransform = modelTransform;
   }

   public BuilderToolSetEntityTransform(@Nonnull BuilderToolSetEntityTransform other) {
      this.entityId = other.entityId;
      this.modelTransform = other.modelTransform;
   }

   @Nonnull
   public static BuilderToolSetEntityTransform deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolSetEntityTransform obj = new BuilderToolSetEntityTransform();
      byte nullBits = buf.getByte(offset);
      obj.entityId = buf.getIntLE(offset + 1);
      if ((nullBits & 1) != 0) {
         obj.modelTransform = ModelTransform.deserialize(buf, offset + 5);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 54;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.modelTransform != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.entityId);
      if (this.modelTransform != null) {
         this.modelTransform.serialize(buf);
      } else {
         buf.writeZero(49);
      }
   }

   @Override
   public int computeSize() {
      return 54;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 54 ? ValidationResult.error("Buffer too small: expected at least 54 bytes") : ValidationResult.OK;
   }

   public BuilderToolSetEntityTransform clone() {
      BuilderToolSetEntityTransform copy = new BuilderToolSetEntityTransform();
      copy.entityId = this.entityId;
      copy.modelTransform = this.modelTransform != null ? this.modelTransform.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolSetEntityTransform other)
            ? false
            : this.entityId == other.entityId && Objects.equals(this.modelTransform, other.modelTransform);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entityId, this.modelTransform);
   }
}
