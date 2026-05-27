package com.hypixel.hytale.protocol.packets.assets;

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

public class UntrackObjective implements Packet, ToClientPacket {
   public static final int PACKET_ID = 70;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 16;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 16;
   public static final int MAX_SIZE = 16;
   @Nonnull
   public UUID objectiveUuid = new UUID(0L, 0L);

   @Override
   public int getId() {
      return 70;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UntrackObjective() {
   }

   public UntrackObjective(@Nonnull UUID objectiveUuid) {
      this.objectiveUuid = objectiveUuid;
   }

   public UntrackObjective(@Nonnull UntrackObjective other) {
      this.objectiveUuid = other.objectiveUuid;
   }

   @Nonnull
   public static UntrackObjective deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 16) {
         throw ProtocolException.bufferTooSmall("UntrackObjective", 16, buf.readableBytes() - offset);
      } else {
         UntrackObjective obj = new UntrackObjective();
         obj.objectiveUuid = PacketIO.readUUID(buf, offset + 0);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 16;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 16L;
   }

   public static UUID getObjectiveUuid(MemorySegment mem) {
      return getObjectiveUuid(mem, 0);
   }

   public static UUID getObjectiveUuid(MemorySegment mem, int offset) {
      return PacketIO.readUUID(mem, offset + 0);
   }

   public static UntrackObjective toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UntrackObjective toObject(MemorySegment mem, int offset) {
      if (offset + 16 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UntrackObjective", offset + 16, (int)mem.byteSize());
      } else {
         return new UntrackObjective(PacketIO.readUUID(mem, offset + 0));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      PacketIO.writeUUID(buf, this.objectiveUuid);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      PacketIO.writeUUID(mem, offset + 0, this.objectiveUuid);
      return 16;
   }

   @Override
   public int computeSize() {
      return 16;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 16 ? ValidationResult.error("Buffer too small: expected at least 16 bytes") : ValidationResult.OK;
   }

   public UntrackObjective clone() {
      UntrackObjective copy = new UntrackObjective();
      copy.objectiveUuid = this.objectiveUuid;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UntrackObjective other ? Objects.equals(this.objectiveUuid, other.objectiveUuid) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.objectiveUuid);
   }
}
