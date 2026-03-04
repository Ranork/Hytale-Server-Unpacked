package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateWorldMapSettings implements Packet, ToClientPacket {
   public static final int PACKET_ID = 240;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 20;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 20;
   public static final int MAX_SIZE = 1677721600;
   public boolean enabled = true;
   @Nullable
   public Map<Short, BiomeData> biomeDataMap;
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
      @Nullable Map<Short, BiomeData> biomeDataMap,
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
      this.biomeDataMap = biomeDataMap;
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
      this.biomeDataMap = other.biomeDataMap;
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
      UpdateWorldMapSettings obj = new UpdateWorldMapSettings();
      byte nullBits = buf.getByte(offset);
      obj.enabled = buf.getByte(offset + 1) != 0;
      obj.allowTeleportToCoordinates = buf.getByte(offset + 2) != 0;
      obj.allowTeleportToMarkers = buf.getByte(offset + 3) != 0;
      obj.allowShowOnMapToggle = buf.getByte(offset + 4) != 0;
      obj.allowCompassTrackingToggle = buf.getByte(offset + 5) != 0;
      obj.allowCreatingMapMarkers = buf.getByte(offset + 6) != 0;
      obj.allowRemovingOtherPlayersMarkers = buf.getByte(offset + 7) != 0;
      obj.defaultScale = buf.getFloatLE(offset + 8);
      obj.minScale = buf.getFloatLE(offset + 12);
      obj.maxScale = buf.getFloatLE(offset + 16);
      int pos = offset + 20;
      if ((nullBits & 1) != 0) {
         int biomeDataMapCount = VarInt.peek(buf, pos);
         if (biomeDataMapCount < 0) {
            throw ProtocolException.negativeLength("BiomeDataMap", biomeDataMapCount);
         }

         if (biomeDataMapCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("BiomeDataMap", biomeDataMapCount, 4096000);
         }

         pos += VarInt.size(biomeDataMapCount);
         obj.biomeDataMap = new HashMap<>(biomeDataMapCount);

         for (int i = 0; i < biomeDataMapCount; i++) {
            short key = buf.getShortLE(pos);
            pos += 2;
            BiomeData val = BiomeData.deserialize(buf, pos);
            pos += BiomeData.computeBytesConsumed(buf, pos);
            if (obj.biomeDataMap.put(key, val) != null) {
               throw ProtocolException.duplicateKey("biomeDataMap", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 20;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < dictLen; i++) {
            pos += 2;
            pos += BiomeData.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.biomeDataMap != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
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
      if (this.biomeDataMap != null) {
         if (this.biomeDataMap.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("BiomeDataMap", this.biomeDataMap.size(), 4096000);
         }

         VarInt.write(buf, this.biomeDataMap.size());

         for (Entry<Short, BiomeData> e : this.biomeDataMap.entrySet()) {
            buf.writeShortLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 20;
      if (this.biomeDataMap != null) {
         int biomeDataMapSize = 0;

         for (Entry<Short, BiomeData> kvp : this.biomeDataMap.entrySet()) {
            biomeDataMapSize += 2 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.biomeDataMap.size()) + biomeDataMapSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 20) {
         return ValidationResult.error("Buffer too small: expected at least 20 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 20;
         if ((nullBits & 1) != 0) {
            int biomeDataMapCount = VarInt.peek(buffer, pos);
            if (biomeDataMapCount < 0) {
               return ValidationResult.error("Invalid dictionary count for BiomeDataMap");
            }

            if (biomeDataMapCount > 4096000) {
               return ValidationResult.error("BiomeDataMap exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < biomeDataMapCount; i++) {
               pos += 2;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += BiomeData.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateWorldMapSettings clone() {
      UpdateWorldMapSettings copy = new UpdateWorldMapSettings();
      copy.enabled = this.enabled;
      if (this.biomeDataMap != null) {
         Map<Short, BiomeData> m = new HashMap<>();

         for (Entry<Short, BiomeData> e : this.biomeDataMap.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.biomeDataMap = m;
      }

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
               && Objects.equals(this.biomeDataMap, other.biomeDataMap)
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
         this.biomeDataMap,
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
