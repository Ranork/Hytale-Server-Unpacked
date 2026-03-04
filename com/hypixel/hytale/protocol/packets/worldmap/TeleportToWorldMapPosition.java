package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class TeleportToWorldMapPosition implements Packet, ToServerPacket {
   public static final int PACKET_ID = 245;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public int x;
   public int y;

   @Override
   public int getId() {
      return 245;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public TeleportToWorldMapPosition() {
   }

   public TeleportToWorldMapPosition(int x, int y) {
      this.x = x;
      this.y = y;
   }

   public TeleportToWorldMapPosition(@Nonnull TeleportToWorldMapPosition other) {
      this.x = other.x;
      this.y = other.y;
   }

   @Nonnull
   public static TeleportToWorldMapPosition deserialize(@Nonnull ByteBuf buf, int offset) {
      TeleportToWorldMapPosition obj = new TeleportToWorldMapPosition();
      obj.x = buf.getIntLE(offset + 0);
      obj.y = buf.getIntLE(offset + 4);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.y);
   }

   @Override
   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public TeleportToWorldMapPosition clone() {
      TeleportToWorldMapPosition copy = new TeleportToWorldMapPosition();
      copy.x = this.x;
      copy.y = this.y;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof TeleportToWorldMapPosition other) ? false : this.x == other.x && this.y == other.y;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y);
   }
}
