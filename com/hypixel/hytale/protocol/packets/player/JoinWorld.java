package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;

public class JoinWorld implements Packet, ToClientPacket {
   public static final int PACKET_ID = 104;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 18;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 18;
   public static final int MAX_SIZE = 18;
   public boolean clearWorld;
   public boolean fadeInOut;
   @Nonnull
   public UUID worldUuid = new UUID(0L, 0L);

   @Override
   public int getId() {
      return 104;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public JoinWorld() {
   }

   public JoinWorld(boolean clearWorld, boolean fadeInOut, @Nonnull UUID worldUuid) {
      this.clearWorld = clearWorld;
      this.fadeInOut = fadeInOut;
      this.worldUuid = worldUuid;
   }

   public JoinWorld(@Nonnull JoinWorld other) {
      this.clearWorld = other.clearWorld;
      this.fadeInOut = other.fadeInOut;
      this.worldUuid = other.worldUuid;
   }

   @Nonnull
   public static JoinWorld deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 18) {
         throw ProtocolException.bufferTooSmall("JoinWorld", 18, buf.readableBytes() - offset);
      } else {
         JoinWorld obj = new JoinWorld();
         obj.clearWorld = buf.getByte(offset + 0) != 0;
         obj.fadeInOut = buf.getByte(offset + 1) != 0;
         obj.worldUuid = PacketIO.readUUID(buf, offset + 2);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 18;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 18L;
   }

   public static boolean getClearWorld(MemorySegment mem) {
      return getClearWorld(mem, 0);
   }

   public static boolean getClearWorld(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0));
   }

   public static boolean getFadeInOut(MemorySegment mem) {
      return getFadeInOut(mem, 0);
   }

   public static boolean getFadeInOut(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static UUID getWorldUuid(MemorySegment mem) {
      return getWorldUuid(mem, 0);
   }

   public static UUID getWorldUuid(MemorySegment mem, int offset) {
      return PacketIO.readUUID(mem, offset + 2);
   }

   public static JoinWorld toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static JoinWorld toObject(MemorySegment mem, int offset) {
      if (offset + 18 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("JoinWorld", offset + 18, (int)mem.byteSize());
      } else {
         return new JoinWorld(
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0)), mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)), PacketIO.readUUID(mem, offset + 2)
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.clearWorld ? 1 : 0);
      buf.writeByte(this.fadeInOut ? 1 : 0);
      PacketIO.writeUUID(buf, this.worldUuid);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BOOL, offset + 0, this.clearWorld);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.fadeInOut);
      PacketIO.writeUUID(mem, offset + 2, this.worldUuid);
      return 18;
   }

   @Override
   public int computeSize() {
      return 18;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 18 ? ValidationResult.error("Buffer too small: expected at least 18 bytes") : ValidationResult.OK;
   }

   public JoinWorld clone() {
      JoinWorld copy = new JoinWorld();
      copy.clearWorld = this.clearWorld;
      copy.fadeInOut = this.fadeInOut;
      copy.worldUuid = this.worldUuid;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof JoinWorld other)
            ? false
            : this.clearWorld == other.clearWorld && this.fadeInOut == other.fadeInOut && Objects.equals(this.worldUuid, other.worldUuid);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.clearWorld, this.fadeInOut, this.worldUuid);
   }
}
