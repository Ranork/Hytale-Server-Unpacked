package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.GameMode;
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

public class SetGameMode implements Packet, ToClientPacket {
   public static final int PACKET_ID = 101;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   @Nonnull
   public GameMode gameMode = GameMode.Adventure;

   @Override
   public int getId() {
      return 101;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SetGameMode() {
   }

   public SetGameMode(@Nonnull GameMode gameMode) {
      this.gameMode = gameMode;
   }

   public SetGameMode(@Nonnull SetGameMode other) {
      this.gameMode = other.gameMode;
   }

   @Nonnull
   public static SetGameMode deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("SetGameMode", 1, buf.readableBytes() - offset);
      } else {
         SetGameMode obj = new SetGameMode();
         obj.gameMode = GameMode.fromValue(buf.getByte(offset + 0));
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   public static GameMode getGameMode(MemorySegment mem) {
      return getGameMode(mem, 0);
   }

   public static GameMode getGameMode(MemorySegment mem, int offset) {
      return GameMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static SetGameMode toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SetGameMode toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SetGameMode", offset + 1, (int)mem.byteSize());
      } else {
         return new SetGameMode(GameMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.gameMode.getValue());
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.gameMode.getValue());
      return 1;
   }

   @Override
   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         return v >= 2 ? ValidationResult.error("Invalid GameMode value for GameMode") : ValidationResult.OK;
      }
   }

   public SetGameMode clone() {
      SetGameMode copy = new SetGameMode();
      copy.gameMode = this.gameMode;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof SetGameMode other ? Objects.equals(this.gameMode, other.gameMode) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.gameMode);
   }
}
