package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdatePortal implements Packet, ToClientPacket {
   public static final int PACKET_ID = 229;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 16384020;
   @Nullable
   public PortalState state;
   @Nullable
   public PortalDef definition;

   @Override
   public int getId() {
      return 229;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdatePortal() {
   }

   public UpdatePortal(@Nullable PortalState state, @Nullable PortalDef definition) {
      this.state = state;
      this.definition = definition;
   }

   public UpdatePortal(@Nonnull UpdatePortal other) {
      this.state = other.state;
      this.definition = other.definition;
   }

   @Nonnull
   public static UpdatePortal deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdatePortal obj = new UpdatePortal();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.state = PortalState.deserialize(buf, offset + 1);
      }

      int pos = offset + 6;
      if ((nullBits & 2) != 0) {
         obj.definition = PortalDef.deserialize(buf, pos);
         pos += PortalDef.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 6;
      if ((nullBits & 2) != 0) {
         pos += PortalDef.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.state != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.definition != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      if (this.state != null) {
         this.state.serialize(buf);
      } else {
         buf.writeZero(5);
      }

      if (this.definition != null) {
         this.definition.serialize(buf);
      }
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.definition != null) {
         size += this.definition.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 6) {
         return ValidationResult.error("Buffer too small: expected at least 6 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 6;
         if ((nullBits & 2) != 0) {
            ValidationResult definitionResult = PortalDef.validateStructure(buffer, pos);
            if (!definitionResult.isValid()) {
               return ValidationResult.error("Invalid Definition: " + definitionResult.error());
            }

            pos += PortalDef.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public UpdatePortal clone() {
      UpdatePortal copy = new UpdatePortal();
      copy.state = this.state != null ? this.state.clone() : null;
      copy.definition = this.definition != null ? this.definition.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdatePortal other) ? false : Objects.equals(this.state, other.state) && Objects.equals(this.definition, other.definition);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.state, this.definition);
   }
}
