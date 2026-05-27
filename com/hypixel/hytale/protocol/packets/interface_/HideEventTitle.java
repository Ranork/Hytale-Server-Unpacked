package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class HideEventTitle implements Packet, ToClientPacket {
   public static final int PACKET_ID = 215;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public float fadeOutDuration;

   @Override
   public int getId() {
      return 215;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public HideEventTitle() {
   }

   public HideEventTitle(float fadeOutDuration) {
      this.fadeOutDuration = fadeOutDuration;
   }

   public HideEventTitle(@Nonnull HideEventTitle other) {
      this.fadeOutDuration = other.fadeOutDuration;
   }

   @Nonnull
   public static HideEventTitle deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 4) {
         throw ProtocolException.bufferTooSmall("HideEventTitle", 4, buf.readableBytes() - offset);
      } else {
         HideEventTitle obj = new HideEventTitle();
         obj.fadeOutDuration = buf.getFloatLE(offset + 0);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 4L;
   }

   public static float getFadeOutDuration(MemorySegment mem) {
      return getFadeOutDuration(mem, 0);
   }

   public static float getFadeOutDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static HideEventTitle toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static HideEventTitle toObject(MemorySegment mem, int offset) {
      if (offset + 4 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("HideEventTitle", offset + 4, (int)mem.byteSize());
      } else {
         return new HideEventTitle(mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.fadeOutDuration);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.fadeOutDuration);
      return 4;
   }

   @Override
   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public HideEventTitle clone() {
      HideEventTitle copy = new HideEventTitle();
      copy.fadeOutDuration = this.fadeOutDuration;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof HideEventTitle other ? this.fadeOutDuration == other.fadeOutDuration : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.fadeOutDuration);
   }
}
