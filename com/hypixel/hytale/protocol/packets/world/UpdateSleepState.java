package com.hypixel.hytale.protocol.packets.world;

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
import javax.annotation.Nullable;

public class UpdateSleepState implements Packet, ToClientPacket {
   public static final int PACKET_ID = 157;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 36;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 36;
   public static final int MAX_SIZE = 65536050;
   public boolean grayFade;
   public boolean sleepUi;
   @Nullable
   public SleepClock clock;
   @Nullable
   public SleepMultiplayer multiplayer;

   @Override
   public int getId() {
      return 157;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateSleepState() {
   }

   public UpdateSleepState(boolean grayFade, boolean sleepUi, @Nullable SleepClock clock, @Nullable SleepMultiplayer multiplayer) {
      this.grayFade = grayFade;
      this.sleepUi = sleepUi;
      this.clock = clock;
      this.multiplayer = multiplayer;
   }

   public UpdateSleepState(@Nonnull UpdateSleepState other) {
      this.grayFade = other.grayFade;
      this.sleepUi = other.sleepUi;
      this.clock = other.clock;
      this.multiplayer = other.multiplayer;
   }

   @Nonnull
   public static UpdateSleepState deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 36) {
         throw ProtocolException.bufferTooSmall("UpdateSleepState", 36, buf.readableBytes() - offset);
      } else {
         UpdateSleepState obj = new UpdateSleepState();
         byte nullBits = buf.getByte(offset);
         obj.grayFade = buf.getByte(offset + 1) != 0;
         obj.sleepUi = buf.getByte(offset + 2) != 0;
         if ((nullBits & 1) != 0) {
            obj.clock = SleepClock.deserialize(buf, offset + 3);
         }

         int pos = offset + 36;
         if ((nullBits & 2) != 0) {
            obj.multiplayer = SleepMultiplayer.deserialize(buf, pos);
            pos += SleepMultiplayer.computeBytesConsumed(buf, pos);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 36;
      if ((nullBits & 2) != 0) {
         pos += SleepMultiplayer.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 36L;
   }

   public static boolean getGrayFade(MemorySegment mem) {
      return getGrayFade(mem, 0);
   }

   public static boolean getGrayFade(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static boolean getSleepUi(MemorySegment mem) {
      return getSleepUi(mem, 0);
   }

   public static boolean getSleepUi(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2));
   }

   @Nullable
   public static SleepClock getClock(MemorySegment mem) {
      return getClock(mem, 0);
   }

   @Nullable
   public static SleepClock getClock(MemorySegment mem, int offset) {
      return hasClock(mem, offset) ? SleepClock.toObject(mem, offset + 3) : null;
   }

   @Nullable
   public static SleepMultiplayer getMultiplayer(MemorySegment mem) {
      return getMultiplayer(mem, 0);
   }

   @Nullable
   public static SleepMultiplayer getMultiplayer(MemorySegment mem, int offset) {
      return hasMultiplayer(mem, offset) ? SleepMultiplayer.toObject(mem, offset + 36) : null;
   }

   public static boolean hasClock(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasMultiplayer(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static UpdateSleepState toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateSleepState toObject(MemorySegment mem, int offset) {
      if (offset + 36 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateSleepState", offset + 36, (int)mem.byteSize());
      } else {
         return new UpdateSleepState(
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2)),
            hasClock(mem, offset) ? SleepClock.toObject(mem, offset + 3) : null,
            hasMultiplayer(mem, offset) ? SleepMultiplayer.toObject(mem, offset + 36) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.clock != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.multiplayer != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.grayFade ? 1 : 0);
      buf.writeByte(this.sleepUi ? 1 : 0);
      if (this.clock != null) {
         this.clock.serialize(buf);
      } else {
         buf.writeZero(33);
      }

      if (this.multiplayer != null) {
         this.multiplayer.serialize(buf);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.clock != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.multiplayer != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.grayFade);
      mem.set(PacketIO.PROTO_BOOL, offset + 2, this.sleepUi);
      if (this.clock != null) {
         this.clock.serialize(mem, offset + 3);
      } else {
         mem.asSlice(offset + 3, 33L).fill((byte)0);
      }

      int varOffset = offset + 36;
      if (this.multiplayer != null) {
         varOffset += this.multiplayer.serialize(mem, varOffset);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 36;
      if (this.multiplayer != null) {
         size += this.multiplayer.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 36) {
         return ValidationResult.error("Buffer too small: expected at least 36 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 36;
         if ((nullBits & 2) != 0) {
            ValidationResult multiplayerResult = SleepMultiplayer.validateStructure(buffer, pos);
            if (!multiplayerResult.isValid()) {
               return ValidationResult.error("Invalid Multiplayer: " + multiplayerResult.error());
            }

            pos += SleepMultiplayer.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public UpdateSleepState clone() {
      UpdateSleepState copy = new UpdateSleepState();
      copy.grayFade = this.grayFade;
      copy.sleepUi = this.sleepUi;
      copy.clock = this.clock != null ? this.clock.clone() : null;
      copy.multiplayer = this.multiplayer != null ? this.multiplayer.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateSleepState other)
            ? false
            : this.grayFade == other.grayFade
               && this.sleepUi == other.sleepUi
               && Objects.equals(this.clock, other.clock)
               && Objects.equals(this.multiplayer, other.multiplayer);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.grayFade, this.sleepUi, this.clock, this.multiplayer);
   }
}
