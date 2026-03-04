package com.hypixel.hytale.protocol.packets.setup;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SetTimeDilation implements Packet, ToClientPacket {
   public static final int PACKET_ID = 30;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public float timeDilation;

   @Override
   public int getId() {
      return 30;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SetTimeDilation() {
   }

   public SetTimeDilation(float timeDilation) {
      this.timeDilation = timeDilation;
   }

   public SetTimeDilation(@Nonnull SetTimeDilation other) {
      this.timeDilation = other.timeDilation;
   }

   @Nonnull
   public static SetTimeDilation deserialize(@Nonnull ByteBuf buf, int offset) {
      SetTimeDilation obj = new SetTimeDilation();
      obj.timeDilation = buf.getFloatLE(offset + 0);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.timeDilation);
   }

   @Override
   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public SetTimeDilation clone() {
      SetTimeDilation copy = new SetTimeDilation();
      copy.timeDilation = this.timeDilation;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof SetTimeDilation other ? this.timeDilation == other.timeDilation : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.timeDilation);
   }
}
