package com.hypixel.hytale.protocol.packets.window;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SendWindowAction implements Packet, ToServerPacket {
   public static final int PACKET_ID = 203;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 32768027;
   public int id;
   @Nonnull
   public WindowAction action;

   @Override
   public int getId() {
      return 203;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SendWindowAction() {
   }

   public SendWindowAction(int id, @Nonnull WindowAction action) {
      this.id = id;
      this.action = action;
   }

   public SendWindowAction(@Nonnull SendWindowAction other) {
      this.id = other.id;
      this.action = other.action;
   }

   @Nonnull
   public static SendWindowAction deserialize(@Nonnull ByteBuf buf, int offset) {
      SendWindowAction obj = new SendWindowAction();
      obj.id = buf.getIntLE(offset + 0);
      int pos = offset + 4;
      obj.action = WindowAction.deserialize(buf, pos);
      pos += WindowAction.computeBytesConsumed(buf, pos);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 4;
      pos += WindowAction.computeBytesConsumed(buf, pos);
      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.id);
      this.action.serializeWithTypeId(buf);
   }

   @Override
   public int computeSize() {
      int size = 4;
      return size + this.action.computeSizeWithTypeId();
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 4) {
         return ValidationResult.error("Buffer too small: expected at least 4 bytes");
      } else {
         int pos = offset + 4;
         ValidationResult actionResult = WindowAction.validateStructure(buffer, pos);
         if (!actionResult.isValid()) {
            return ValidationResult.error("Invalid Action: " + actionResult.error());
         } else {
            pos += WindowAction.computeBytesConsumed(buffer, pos);
            return ValidationResult.OK;
         }
      }
   }

   public SendWindowAction clone() {
      SendWindowAction copy = new SendWindowAction();
      copy.id = this.id;
      copy.action = this.action;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SendWindowAction other) ? false : this.id == other.id && Objects.equals(this.action, other.action);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.action);
   }
}
