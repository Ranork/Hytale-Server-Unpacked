package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SetClientId implements Packet, ToClientPacket {
   public static final int PACKET_ID = 100;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public int clientId;

   @Override
   public int getId() {
      return 100;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SetClientId() {
   }

   public SetClientId(int clientId) {
      this.clientId = clientId;
   }

   public SetClientId(@Nonnull SetClientId other) {
      this.clientId = other.clientId;
   }

   @Nonnull
   public static SetClientId deserialize(@Nonnull ByteBuf buf, int offset) {
      SetClientId obj = new SetClientId();
      obj.clientId = buf.getIntLE(offset + 0);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.clientId);
   }

   @Override
   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public SetClientId clone() {
      SetClientId copy = new SetClientId();
      copy.clientId = this.clientId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof SetClientId other ? this.clientId == other.clientId : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.clientId);
   }
}
