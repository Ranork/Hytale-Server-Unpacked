package com.hypixel.hytale.protocol.packets.setup;

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
      if (buf.readableBytes() - offset < 4) {
         throw ProtocolException.bufferTooSmall("SetTimeDilation", 4, buf.readableBytes() - offset);
      } else {
         SetTimeDilation obj = new SetTimeDilation();
         obj.timeDilation = buf.getFloatLE(offset + 0);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 4L;
   }

   public static float getTimeDilation(MemorySegment mem) {
      return getTimeDilation(mem, 0);
   }

   public static float getTimeDilation(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static SetTimeDilation toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SetTimeDilation toObject(MemorySegment mem, int offset) {
      if (offset + 4 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SetTimeDilation", offset + 4, (int)mem.byteSize());
      } else {
         return new SetTimeDilation(mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.timeDilation);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.timeDilation);
      return 4;
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
