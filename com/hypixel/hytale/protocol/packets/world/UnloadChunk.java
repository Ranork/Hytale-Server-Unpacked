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

public class UnloadChunk implements Packet, ToClientPacket {
   public static final int PACKET_ID = 135;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public int chunkX;
   public int chunkZ;

   @Override
   public int getId() {
      return 135;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Chunks;
   }

   public UnloadChunk() {
   }

   public UnloadChunk(int chunkX, int chunkZ) {
      this.chunkX = chunkX;
      this.chunkZ = chunkZ;
   }

   public UnloadChunk(@Nonnull UnloadChunk other) {
      this.chunkX = other.chunkX;
      this.chunkZ = other.chunkZ;
   }

   @Nonnull
   public static UnloadChunk deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 8) {
         throw ProtocolException.bufferTooSmall("UnloadChunk", 8, buf.readableBytes() - offset);
      } else {
         UnloadChunk obj = new UnloadChunk();
         obj.chunkX = buf.getIntLE(offset + 0);
         obj.chunkZ = buf.getIntLE(offset + 4);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 8L;
   }

   public static int getChunkX(MemorySegment mem) {
      return getChunkX(mem, 0);
   }

   public static int getChunkX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getChunkZ(MemorySegment mem) {
      return getChunkZ(mem, 0);
   }

   public static int getChunkZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static UnloadChunk toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UnloadChunk toObject(MemorySegment mem, int offset) {
      if (offset + 8 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UnloadChunk", offset + 8, (int)mem.byteSize());
      } else {
         return new UnloadChunk(mem.get(PacketIO.PROTO_INT, (long)(offset + 0)), mem.get(PacketIO.PROTO_INT, (long)(offset + 4)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.chunkX);
      buf.writeIntLE(this.chunkZ);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.chunkX);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.chunkZ);
      return 8;
   }

   @Override
   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public UnloadChunk clone() {
      UnloadChunk copy = new UnloadChunk();
      copy.chunkX = this.chunkX;
      copy.chunkZ = this.chunkZ;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UnloadChunk other) ? false : this.chunkX == other.chunkX && this.chunkZ == other.chunkZ;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.chunkX, this.chunkZ);
   }
}
