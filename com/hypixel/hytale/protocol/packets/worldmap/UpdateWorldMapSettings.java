package com.hypixel.hytale.protocol.packets.worldmap;

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

public class UpdateWorldMapSettings implements Packet, ToClientPacket {
   public static final int PACKET_ID = 240;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 19;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 19;
   public static final int MAX_SIZE = 19;
   public boolean enabled = true;
   public boolean allowTeleportToCoordinates;
   public boolean allowTeleportToMarkers;
   public boolean allowShowOnMapToggle;
   public boolean allowCompassTrackingToggle;
   public boolean allowCreatingMapMarkers;
   public boolean allowRemovingOtherPlayersMarkers;
   public float defaultScale = 32.0F;
   public float minScale = 2.0F;
   public float maxScale = 256.0F;

   @Override
   public int getId() {
      return 240;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateWorldMapSettings() {
   }

   public UpdateWorldMapSettings(
      boolean enabled,
      boolean allowTeleportToCoordinates,
      boolean allowTeleportToMarkers,
      boolean allowShowOnMapToggle,
      boolean allowCompassTrackingToggle,
      boolean allowCreatingMapMarkers,
      boolean allowRemovingOtherPlayersMarkers,
      float defaultScale,
      float minScale,
      float maxScale
   ) {
      this.enabled = enabled;
      this.allowTeleportToCoordinates = allowTeleportToCoordinates;
      this.allowTeleportToMarkers = allowTeleportToMarkers;
      this.allowShowOnMapToggle = allowShowOnMapToggle;
      this.allowCompassTrackingToggle = allowCompassTrackingToggle;
      this.allowCreatingMapMarkers = allowCreatingMapMarkers;
      this.allowRemovingOtherPlayersMarkers = allowRemovingOtherPlayersMarkers;
      this.defaultScale = defaultScale;
      this.minScale = minScale;
      this.maxScale = maxScale;
   }

   public UpdateWorldMapSettings(@Nonnull UpdateWorldMapSettings other) {
      this.enabled = other.enabled;
      this.allowTeleportToCoordinates = other.allowTeleportToCoordinates;
      this.allowTeleportToMarkers = other.allowTeleportToMarkers;
      this.allowShowOnMapToggle = other.allowShowOnMapToggle;
      this.allowCompassTrackingToggle = other.allowCompassTrackingToggle;
      this.allowCreatingMapMarkers = other.allowCreatingMapMarkers;
      this.allowRemovingOtherPlayersMarkers = other.allowRemovingOtherPlayersMarkers;
      this.defaultScale = other.defaultScale;
      this.minScale = other.minScale;
      this.maxScale = other.maxScale;
   }

   @Nonnull
   public static UpdateWorldMapSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 19) {
         throw ProtocolException.bufferTooSmall("UpdateWorldMapSettings", 19, buf.readableBytes() - offset);
      } else {
         UpdateWorldMapSettings obj = new UpdateWorldMapSettings();
         obj.enabled = buf.getByte(offset + 0) != 0;
         obj.allowTeleportToCoordinates = buf.getByte(offset + 1) != 0;
         obj.allowTeleportToMarkers = buf.getByte(offset + 2) != 0;
         obj.allowShowOnMapToggle = buf.getByte(offset + 3) != 0;
         obj.allowCompassTrackingToggle = buf.getByte(offset + 4) != 0;
         obj.allowCreatingMapMarkers = buf.getByte(offset + 5) != 0;
         obj.allowRemovingOtherPlayersMarkers = buf.getByte(offset + 6) != 0;
         obj.defaultScale = buf.getFloatLE(offset + 7);
         obj.minScale = buf.getFloatLE(offset + 11);
         obj.maxScale = buf.getFloatLE(offset + 15);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 19;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 19L;
   }

   public static boolean getEnabled(MemorySegment mem) {
      return getEnabled(mem, 0);
   }

   public static boolean getEnabled(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0));
   }

   public static boolean getAllowTeleportToCoordinates(MemorySegment mem) {
      return getAllowTeleportToCoordinates(mem, 0);
   }

   public static boolean getAllowTeleportToCoordinates(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static boolean getAllowTeleportToMarkers(MemorySegment mem) {
      return getAllowTeleportToMarkers(mem, 0);
   }

   public static boolean getAllowTeleportToMarkers(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2));
   }

   public static boolean getAllowShowOnMapToggle(MemorySegment mem) {
      return getAllowShowOnMapToggle(mem, 0);
   }

   public static boolean getAllowShowOnMapToggle(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 3));
   }

   public static boolean getAllowCompassTrackingToggle(MemorySegment mem) {
      return getAllowCompassTrackingToggle(mem, 0);
   }

   public static boolean getAllowCompassTrackingToggle(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4));
   }

   public static boolean getAllowCreatingMapMarkers(MemorySegment mem) {
      return getAllowCreatingMapMarkers(mem, 0);
   }

   public static boolean getAllowCreatingMapMarkers(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5));
   }

   public static boolean getAllowRemovingOtherPlayersMarkers(MemorySegment mem) {
      return getAllowRemovingOtherPlayersMarkers(mem, 0);
   }

   public static boolean getAllowRemovingOtherPlayersMarkers(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 6));
   }

   public static float getDefaultScale(MemorySegment mem) {
      return getDefaultScale(mem, 0);
   }

   public static float getDefaultScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 7));
   }

   public static float getMinScale(MemorySegment mem) {
      return getMinScale(mem, 0);
   }

   public static float getMinScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 11));
   }

   public static float getMaxScale(MemorySegment mem) {
      return getMaxScale(mem, 0);
   }

   public static float getMaxScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 15));
   }

   public static UpdateWorldMapSettings toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateWorldMapSettings toObject(MemorySegment mem, int offset) {
      if (offset + 19 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateWorldMapSettings", offset + 19, (int)mem.byteSize());
      } else {
         return new UpdateWorldMapSettings(
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 3)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 6)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 7)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 11)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 15))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.enabled ? 1 : 0);
      buf.writeByte(this.allowTeleportToCoordinates ? 1 : 0);
      buf.writeByte(this.allowTeleportToMarkers ? 1 : 0);
      buf.writeByte(this.allowShowOnMapToggle ? 1 : 0);
      buf.writeByte(this.allowCompassTrackingToggle ? 1 : 0);
      buf.writeByte(this.allowCreatingMapMarkers ? 1 : 0);
      buf.writeByte(this.allowRemovingOtherPlayersMarkers ? 1 : 0);
      buf.writeFloatLE(this.defaultScale);
      buf.writeFloatLE(this.minScale);
      buf.writeFloatLE(this.maxScale);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BOOL, offset + 0, this.enabled);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.allowTeleportToCoordinates);
      mem.set(PacketIO.PROTO_BOOL, offset + 2, this.allowTeleportToMarkers);
      mem.set(PacketIO.PROTO_BOOL, offset + 3, this.allowShowOnMapToggle);
      mem.set(PacketIO.PROTO_BOOL, offset + 4, this.allowCompassTrackingToggle);
      mem.set(PacketIO.PROTO_BOOL, offset + 5, this.allowCreatingMapMarkers);
      mem.set(PacketIO.PROTO_BOOL, offset + 6, this.allowRemovingOtherPlayersMarkers);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 7), this.defaultScale);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 11), this.minScale);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 15), this.maxScale);
      return 19;
   }

   @Override
   public int computeSize() {
      return 19;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 19 ? ValidationResult.error("Buffer too small: expected at least 19 bytes") : ValidationResult.OK;
   }

   public UpdateWorldMapSettings clone() {
      UpdateWorldMapSettings copy = new UpdateWorldMapSettings();
      copy.enabled = this.enabled;
      copy.allowTeleportToCoordinates = this.allowTeleportToCoordinates;
      copy.allowTeleportToMarkers = this.allowTeleportToMarkers;
      copy.allowShowOnMapToggle = this.allowShowOnMapToggle;
      copy.allowCompassTrackingToggle = this.allowCompassTrackingToggle;
      copy.allowCreatingMapMarkers = this.allowCreatingMapMarkers;
      copy.allowRemovingOtherPlayersMarkers = this.allowRemovingOtherPlayersMarkers;
      copy.defaultScale = this.defaultScale;
      copy.minScale = this.minScale;
      copy.maxScale = this.maxScale;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateWorldMapSettings other)
            ? false
            : this.enabled == other.enabled
               && this.allowTeleportToCoordinates == other.allowTeleportToCoordinates
               && this.allowTeleportToMarkers == other.allowTeleportToMarkers
               && this.allowShowOnMapToggle == other.allowShowOnMapToggle
               && this.allowCompassTrackingToggle == other.allowCompassTrackingToggle
               && this.allowCreatingMapMarkers == other.allowCreatingMapMarkers
               && this.allowRemovingOtherPlayersMarkers == other.allowRemovingOtherPlayersMarkers
               && this.defaultScale == other.defaultScale
               && this.minScale == other.minScale
               && this.maxScale == other.maxScale;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.enabled,
         this.allowTeleportToCoordinates,
         this.allowTeleportToMarkers,
         this.allowShowOnMapToggle,
         this.allowCompassTrackingToggle,
         this.allowCreatingMapMarkers,
         this.allowRemovingOtherPlayersMarkers,
         this.defaultScale,
         this.minScale,
         this.maxScale
      );
   }
}
