package com.hypixel.hytale.protocol.packets.camera;

import com.hypixel.hytale.protocol.AccumulationMode;
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

public class CameraShakeEffect implements Packet, ToClientPacket {
   public static final int PACKET_ID = 281;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 9;
   public int cameraShakeId;
   public float intensity;
   @Nonnull
   public AccumulationMode mode = AccumulationMode.Set;

   @Override
   public int getId() {
      return 281;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public CameraShakeEffect() {
   }

   public CameraShakeEffect(int cameraShakeId, float intensity, @Nonnull AccumulationMode mode) {
      this.cameraShakeId = cameraShakeId;
      this.intensity = intensity;
      this.mode = mode;
   }

   public CameraShakeEffect(@Nonnull CameraShakeEffect other) {
      this.cameraShakeId = other.cameraShakeId;
      this.intensity = other.intensity;
      this.mode = other.mode;
   }

   @Nonnull
   public static CameraShakeEffect deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("CameraShakeEffect", 9, buf.readableBytes() - offset);
      } else {
         CameraShakeEffect obj = new CameraShakeEffect();
         obj.cameraShakeId = buf.getIntLE(offset + 0);
         obj.intensity = buf.getFloatLE(offset + 4);
         obj.mode = AccumulationMode.fromValue(buf.getByte(offset + 8));
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 9;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   public static int getCameraShakeId(MemorySegment mem) {
      return getCameraShakeId(mem, 0);
   }

   public static int getCameraShakeId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static float getIntensity(MemorySegment mem) {
      return getIntensity(mem, 0);
   }

   public static float getIntensity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   public static AccumulationMode getMode(MemorySegment mem) {
      return getMode(mem, 0);
   }

   public static AccumulationMode getMode(MemorySegment mem, int offset) {
      return AccumulationMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 8)));
   }

   public static CameraShakeEffect toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CameraShakeEffect toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CameraShakeEffect", offset + 9, (int)mem.byteSize());
      } else {
         return new CameraShakeEffect(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)),
            AccumulationMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 8)))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.cameraShakeId);
      buf.writeFloatLE(this.intensity);
      buf.writeByte(this.mode.getValue());
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.cameraShakeId);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.intensity);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 8), (byte)this.mode.getValue());
      return 9;
   }

   @Override
   public int computeSize() {
      return 9;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         int v = buffer.getByte(offset + 8) & 255;
         return v >= 3 ? ValidationResult.error("Invalid AccumulationMode value for Mode") : ValidationResult.OK;
      }
   }

   public CameraShakeEffect clone() {
      CameraShakeEffect copy = new CameraShakeEffect();
      copy.cameraShakeId = this.cameraShakeId;
      copy.intensity = this.intensity;
      copy.mode = this.mode;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CameraShakeEffect other)
            ? false
            : this.cameraShakeId == other.cameraShakeId && this.intensity == other.intensity && Objects.equals(this.mode, other.mode);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.cameraShakeId, this.intensity, this.mode);
   }
}
