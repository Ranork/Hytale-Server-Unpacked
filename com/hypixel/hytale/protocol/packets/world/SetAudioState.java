package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.StateTransition;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SetAudioState implements Packet, ToClientPacket {
   public static final int PACKET_ID = 168;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 24;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 24;
   public static final int MAX_SIZE = 24;
   public int audioStateIndex;
   public int valueIndex;
   @Nullable
   public StateTransition transitionOverride;

   @Override
   public int getId() {
      return 168;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SetAudioState() {
   }

   public SetAudioState(int audioStateIndex, int valueIndex, @Nullable StateTransition transitionOverride) {
      this.audioStateIndex = audioStateIndex;
      this.valueIndex = valueIndex;
      this.transitionOverride = transitionOverride;
   }

   public SetAudioState(@Nonnull SetAudioState other) {
      this.audioStateIndex = other.audioStateIndex;
      this.valueIndex = other.valueIndex;
      this.transitionOverride = other.transitionOverride;
   }

   @Nonnull
   public static SetAudioState deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 24) {
         throw ProtocolException.bufferTooSmall("SetAudioState", 24, buf.readableBytes() - offset);
      } else {
         SetAudioState obj = new SetAudioState();
         byte nullBits = buf.getByte(offset);
         obj.audioStateIndex = buf.getIntLE(offset + 1);
         obj.valueIndex = buf.getIntLE(offset + 5);
         if ((nullBits & 1) != 0) {
            obj.transitionOverride = StateTransition.deserialize(buf, offset + 9);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 24;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 24L;
   }

   public static int getAudioStateIndex(MemorySegment mem) {
      return getAudioStateIndex(mem, 0);
   }

   public static int getAudioStateIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getValueIndex(MemorySegment mem) {
      return getValueIndex(mem, 0);
   }

   public static int getValueIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   @Nullable
   public static StateTransition getTransitionOverride(MemorySegment mem) {
      return getTransitionOverride(mem, 0);
   }

   @Nullable
   public static StateTransition getTransitionOverride(MemorySegment mem, int offset) {
      return hasTransitionOverride(mem, offset) ? StateTransition.toObject(mem, offset + 9) : null;
   }

   public static boolean hasTransitionOverride(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static SetAudioState toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SetAudioState toObject(MemorySegment mem, int offset) {
      if (offset + 24 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SetAudioState", offset + 24, (int)mem.byteSize());
      } else {
         return new SetAudioState(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            hasTransitionOverride(mem, offset) ? StateTransition.toObject(mem, offset + 9) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.transitionOverride != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.audioStateIndex);
      buf.writeIntLE(this.valueIndex);
      if (this.transitionOverride != null) {
         this.transitionOverride.serialize(buf);
      } else {
         buf.writeZero(15);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.transitionOverride != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.audioStateIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.valueIndex);
      if (this.transitionOverride != null) {
         this.transitionOverride.serialize(mem, offset + 9);
      } else {
         mem.asSlice(offset + 9, 15L).fill((byte)0);
      }

      return 24;
   }

   @Override
   public int computeSize() {
      return 24;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 24) {
         return ValidationResult.error("Buffer too small: expected at least 24 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public SetAudioState clone() {
      SetAudioState copy = new SetAudioState();
      copy.audioStateIndex = this.audioStateIndex;
      copy.valueIndex = this.valueIndex;
      copy.transitionOverride = this.transitionOverride != null ? this.transitionOverride.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SetAudioState other)
            ? false
            : this.audioStateIndex == other.audioStateIndex
               && this.valueIndex == other.valueIndex
               && Objects.equals(this.transitionOverride, other.transitionOverride);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.audioStateIndex, this.valueIndex, this.transitionOverride);
   }
}
