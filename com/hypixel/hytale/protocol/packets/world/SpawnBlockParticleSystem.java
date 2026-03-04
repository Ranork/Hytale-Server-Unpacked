package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.BlockParticleEvent;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpawnBlockParticleSystem implements Packet, ToClientPacket {
   public static final int PACKET_ID = 153;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 30;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 30;
   public static final int MAX_SIZE = 30;
   public int blockId;
   @Nonnull
   public BlockParticleEvent particleType = BlockParticleEvent.Walk;
   @Nullable
   public Position position;

   @Override
   public int getId() {
      return 153;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SpawnBlockParticleSystem() {
   }

   public SpawnBlockParticleSystem(int blockId, @Nonnull BlockParticleEvent particleType, @Nullable Position position) {
      this.blockId = blockId;
      this.particleType = particleType;
      this.position = position;
   }

   public SpawnBlockParticleSystem(@Nonnull SpawnBlockParticleSystem other) {
      this.blockId = other.blockId;
      this.particleType = other.particleType;
      this.position = other.position;
   }

   @Nonnull
   public static SpawnBlockParticleSystem deserialize(@Nonnull ByteBuf buf, int offset) {
      SpawnBlockParticleSystem obj = new SpawnBlockParticleSystem();
      byte nullBits = buf.getByte(offset);
      obj.blockId = buf.getIntLE(offset + 1);
      obj.particleType = BlockParticleEvent.fromValue(buf.getByte(offset + 5));
      if ((nullBits & 1) != 0) {
         obj.position = Position.deserialize(buf, offset + 6);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 30;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.blockId);
      buf.writeByte(this.particleType.getValue());
      if (this.position != null) {
         this.position.serialize(buf);
      } else {
         buf.writeZero(24);
      }
   }

   @Override
   public int computeSize() {
      return 30;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 30 ? ValidationResult.error("Buffer too small: expected at least 30 bytes") : ValidationResult.OK;
   }

   public SpawnBlockParticleSystem clone() {
      SpawnBlockParticleSystem copy = new SpawnBlockParticleSystem();
      copy.blockId = this.blockId;
      copy.particleType = this.particleType;
      copy.position = this.position != null ? this.position.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SpawnBlockParticleSystem other)
            ? false
            : this.blockId == other.blockId && Objects.equals(this.particleType, other.particleType) && Objects.equals(this.position, other.position);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.blockId, this.particleType, this.position);
   }
}
