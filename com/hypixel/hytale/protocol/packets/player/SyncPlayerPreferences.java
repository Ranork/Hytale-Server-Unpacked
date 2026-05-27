package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.PickupLocation;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SyncPlayerPreferences implements Packet, ToServerPacket {
   public static final int PACKET_ID = 116;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 19;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 19;
   public static final int MAX_SIZE = 16384024;
   public boolean showEntityMarkers;
   @Nonnull
   public PickupLocation armorItemsPreferredPickupLocation = PickupLocation.Hotbar;
   @Nonnull
   public PickupLocation weaponAndToolItemsPreferredPickupLocation = PickupLocation.Hotbar;
   @Nonnull
   public PickupLocation usableItemsItemsPreferredPickupLocation = PickupLocation.Hotbar;
   @Nonnull
   public PickupLocation solidBlockItemsPreferredPickupLocation = PickupLocation.Hotbar;
   @Nonnull
   public PickupLocation miscItemsPreferredPickupLocation = PickupLocation.Hotbar;
   public boolean allowNPCDetection;
   public boolean respondToHit;
   public boolean hideHelmet;
   public boolean hideCuirass;
   public boolean hideGauntlets;
   public boolean hidePants;
   @Nullable
   public String placeMode;
   public int creativeInteractionDistance;
   public boolean showBuilderToolNotifications;
   public boolean noPhysics;

   @Override
   public int getId() {
      return 116;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SyncPlayerPreferences() {
   }

   public SyncPlayerPreferences(
      boolean showEntityMarkers,
      @Nonnull PickupLocation armorItemsPreferredPickupLocation,
      @Nonnull PickupLocation weaponAndToolItemsPreferredPickupLocation,
      @Nonnull PickupLocation usableItemsItemsPreferredPickupLocation,
      @Nonnull PickupLocation solidBlockItemsPreferredPickupLocation,
      @Nonnull PickupLocation miscItemsPreferredPickupLocation,
      boolean allowNPCDetection,
      boolean respondToHit,
      boolean hideHelmet,
      boolean hideCuirass,
      boolean hideGauntlets,
      boolean hidePants,
      @Nullable String placeMode,
      int creativeInteractionDistance,
      boolean showBuilderToolNotifications,
      boolean noPhysics
   ) {
      this.showEntityMarkers = showEntityMarkers;
      this.armorItemsPreferredPickupLocation = armorItemsPreferredPickupLocation;
      this.weaponAndToolItemsPreferredPickupLocation = weaponAndToolItemsPreferredPickupLocation;
      this.usableItemsItemsPreferredPickupLocation = usableItemsItemsPreferredPickupLocation;
      this.solidBlockItemsPreferredPickupLocation = solidBlockItemsPreferredPickupLocation;
      this.miscItemsPreferredPickupLocation = miscItemsPreferredPickupLocation;
      this.allowNPCDetection = allowNPCDetection;
      this.respondToHit = respondToHit;
      this.hideHelmet = hideHelmet;
      this.hideCuirass = hideCuirass;
      this.hideGauntlets = hideGauntlets;
      this.hidePants = hidePants;
      this.placeMode = placeMode;
      this.creativeInteractionDistance = creativeInteractionDistance;
      this.showBuilderToolNotifications = showBuilderToolNotifications;
      this.noPhysics = noPhysics;
   }

   public SyncPlayerPreferences(@Nonnull SyncPlayerPreferences other) {
      this.showEntityMarkers = other.showEntityMarkers;
      this.armorItemsPreferredPickupLocation = other.armorItemsPreferredPickupLocation;
      this.weaponAndToolItemsPreferredPickupLocation = other.weaponAndToolItemsPreferredPickupLocation;
      this.usableItemsItemsPreferredPickupLocation = other.usableItemsItemsPreferredPickupLocation;
      this.solidBlockItemsPreferredPickupLocation = other.solidBlockItemsPreferredPickupLocation;
      this.miscItemsPreferredPickupLocation = other.miscItemsPreferredPickupLocation;
      this.allowNPCDetection = other.allowNPCDetection;
      this.respondToHit = other.respondToHit;
      this.hideHelmet = other.hideHelmet;
      this.hideCuirass = other.hideCuirass;
      this.hideGauntlets = other.hideGauntlets;
      this.hidePants = other.hidePants;
      this.placeMode = other.placeMode;
      this.creativeInteractionDistance = other.creativeInteractionDistance;
      this.showBuilderToolNotifications = other.showBuilderToolNotifications;
      this.noPhysics = other.noPhysics;
   }

   @Nonnull
   public static SyncPlayerPreferences deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 19) {
         throw ProtocolException.bufferTooSmall("SyncPlayerPreferences", 19, buf.readableBytes() - offset);
      } else {
         SyncPlayerPreferences obj = new SyncPlayerPreferences();
         byte nullBits = buf.getByte(offset);
         obj.showEntityMarkers = buf.getByte(offset + 1) != 0;
         obj.armorItemsPreferredPickupLocation = PickupLocation.fromValue(buf.getByte(offset + 2));
         obj.weaponAndToolItemsPreferredPickupLocation = PickupLocation.fromValue(buf.getByte(offset + 3));
         obj.usableItemsItemsPreferredPickupLocation = PickupLocation.fromValue(buf.getByte(offset + 4));
         obj.solidBlockItemsPreferredPickupLocation = PickupLocation.fromValue(buf.getByte(offset + 5));
         obj.miscItemsPreferredPickupLocation = PickupLocation.fromValue(buf.getByte(offset + 6));
         obj.allowNPCDetection = buf.getByte(offset + 7) != 0;
         obj.respondToHit = buf.getByte(offset + 8) != 0;
         obj.hideHelmet = buf.getByte(offset + 9) != 0;
         obj.hideCuirass = buf.getByte(offset + 10) != 0;
         obj.hideGauntlets = buf.getByte(offset + 11) != 0;
         obj.hidePants = buf.getByte(offset + 12) != 0;
         obj.creativeInteractionDistance = buf.getIntLE(offset + 13);
         obj.showBuilderToolNotifications = buf.getByte(offset + 17) != 0;
         obj.noPhysics = buf.getByte(offset + 18) != 0;
         int pos = offset + 19;
         if ((nullBits & 1) != 0) {
            int placeModeLen = VarInt.peek(buf, pos);
            if (placeModeLen < 0) {
               throw ProtocolException.invalidVarInt("PlaceMode");
            }

            int placeModeVarLen = VarInt.size(placeModeLen);
            if (placeModeLen > 4096000) {
               throw ProtocolException.stringTooLong("PlaceMode", placeModeLen, 4096000);
            }

            if (pos + placeModeVarLen + placeModeLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("PlaceMode", pos + placeModeVarLen + placeModeLen, buf.readableBytes());
            }

            obj.placeMode = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += placeModeVarLen + placeModeLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 19;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 19L;
   }

   public static boolean getShowEntityMarkers(MemorySegment mem) {
      return getShowEntityMarkers(mem, 0);
   }

   public static boolean getShowEntityMarkers(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static PickupLocation getArmorItemsPreferredPickupLocation(MemorySegment mem) {
      return getArmorItemsPreferredPickupLocation(mem, 0);
   }

   public static PickupLocation getArmorItemsPreferredPickupLocation(MemorySegment mem, int offset) {
      return PickupLocation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2)));
   }

   public static PickupLocation getWeaponAndToolItemsPreferredPickupLocation(MemorySegment mem) {
      return getWeaponAndToolItemsPreferredPickupLocation(mem, 0);
   }

   public static PickupLocation getWeaponAndToolItemsPreferredPickupLocation(MemorySegment mem, int offset) {
      return PickupLocation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3)));
   }

   public static PickupLocation getUsableItemsItemsPreferredPickupLocation(MemorySegment mem) {
      return getUsableItemsItemsPreferredPickupLocation(mem, 0);
   }

   public static PickupLocation getUsableItemsItemsPreferredPickupLocation(MemorySegment mem, int offset) {
      return PickupLocation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4)));
   }

   public static PickupLocation getSolidBlockItemsPreferredPickupLocation(MemorySegment mem) {
      return getSolidBlockItemsPreferredPickupLocation(mem, 0);
   }

   public static PickupLocation getSolidBlockItemsPreferredPickupLocation(MemorySegment mem, int offset) {
      return PickupLocation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 5)));
   }

   public static PickupLocation getMiscItemsPreferredPickupLocation(MemorySegment mem) {
      return getMiscItemsPreferredPickupLocation(mem, 0);
   }

   public static PickupLocation getMiscItemsPreferredPickupLocation(MemorySegment mem, int offset) {
      return PickupLocation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 6)));
   }

   public static boolean getAllowNPCDetection(MemorySegment mem) {
      return getAllowNPCDetection(mem, 0);
   }

   public static boolean getAllowNPCDetection(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 7));
   }

   public static boolean getRespondToHit(MemorySegment mem) {
      return getRespondToHit(mem, 0);
   }

   public static boolean getRespondToHit(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 8));
   }

   public static boolean getHideHelmet(MemorySegment mem) {
      return getHideHelmet(mem, 0);
   }

   public static boolean getHideHelmet(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 9));
   }

   public static boolean getHideCuirass(MemorySegment mem) {
      return getHideCuirass(mem, 0);
   }

   public static boolean getHideCuirass(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 10));
   }

   public static boolean getHideGauntlets(MemorySegment mem) {
      return getHideGauntlets(mem, 0);
   }

   public static boolean getHideGauntlets(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 11));
   }

   public static boolean getHidePants(MemorySegment mem) {
      return getHidePants(mem, 0);
   }

   public static boolean getHidePants(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 12));
   }

   @Nullable
   public static String getPlaceMode(MemorySegment mem) {
      return getPlaceMode(mem, 0);
   }

   @Nullable
   public static String getPlaceMode(MemorySegment mem, int offset) {
      return hasPlaceMode(mem, offset) ? PacketIO.readVarString("PlaceMode", mem, offset + 19, 4096000, PacketIO.UTF8) : null;
   }

   public static int getCreativeInteractionDistance(MemorySegment mem) {
      return getCreativeInteractionDistance(mem, 0);
   }

   public static int getCreativeInteractionDistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 13));
   }

   public static boolean getShowBuilderToolNotifications(MemorySegment mem) {
      return getShowBuilderToolNotifications(mem, 0);
   }

   public static boolean getShowBuilderToolNotifications(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 17));
   }

   public static boolean getNoPhysics(MemorySegment mem) {
      return getNoPhysics(mem, 0);
   }

   public static boolean getNoPhysics(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 18));
   }

   public static boolean hasPlaceMode(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static SyncPlayerPreferences toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SyncPlayerPreferences toObject(MemorySegment mem, int offset) {
      if (offset + 19 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SyncPlayerPreferences", offset + 19, (int)mem.byteSize());
      } else {
         return new SyncPlayerPreferences(
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            PickupLocation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2))),
            PickupLocation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3))),
            PickupLocation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4))),
            PickupLocation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 5))),
            PickupLocation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 6))),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 7)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 10)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 11)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 12)),
            hasPlaceMode(mem, offset) ? PacketIO.readVarString("PlaceMode", mem, offset + 19, 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 13)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 17)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 18))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.placeMode != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.showEntityMarkers ? 1 : 0);
      buf.writeByte(this.armorItemsPreferredPickupLocation.getValue());
      buf.writeByte(this.weaponAndToolItemsPreferredPickupLocation.getValue());
      buf.writeByte(this.usableItemsItemsPreferredPickupLocation.getValue());
      buf.writeByte(this.solidBlockItemsPreferredPickupLocation.getValue());
      buf.writeByte(this.miscItemsPreferredPickupLocation.getValue());
      buf.writeByte(this.allowNPCDetection ? 1 : 0);
      buf.writeByte(this.respondToHit ? 1 : 0);
      buf.writeByte(this.hideHelmet ? 1 : 0);
      buf.writeByte(this.hideCuirass ? 1 : 0);
      buf.writeByte(this.hideGauntlets ? 1 : 0);
      buf.writeByte(this.hidePants ? 1 : 0);
      buf.writeIntLE(this.creativeInteractionDistance);
      buf.writeByte(this.showBuilderToolNotifications ? 1 : 0);
      buf.writeByte(this.noPhysics ? 1 : 0);
      if (this.placeMode != null) {
         PacketIO.writeVarString(buf, this.placeMode, 4096000);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.placeMode != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.showEntityMarkers);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), (byte)this.armorItemsPreferredPickupLocation.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 3), (byte)this.weaponAndToolItemsPreferredPickupLocation.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 4), (byte)this.usableItemsItemsPreferredPickupLocation.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 5), (byte)this.solidBlockItemsPreferredPickupLocation.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 6), (byte)this.miscItemsPreferredPickupLocation.getValue());
      mem.set(PacketIO.PROTO_BOOL, offset + 7, this.allowNPCDetection);
      mem.set(PacketIO.PROTO_BOOL, offset + 8, this.respondToHit);
      mem.set(PacketIO.PROTO_BOOL, offset + 9, this.hideHelmet);
      mem.set(PacketIO.PROTO_BOOL, offset + 10, this.hideCuirass);
      mem.set(PacketIO.PROTO_BOOL, offset + 11, this.hideGauntlets);
      mem.set(PacketIO.PROTO_BOOL, offset + 12, this.hidePants);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 13), this.creativeInteractionDistance);
      mem.set(PacketIO.PROTO_BOOL, offset + 17, this.showBuilderToolNotifications);
      mem.set(PacketIO.PROTO_BOOL, offset + 18, this.noPhysics);
      int varOffset = offset + 19;
      if (this.placeMode != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.placeMode, 4096000);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 19;
      if (this.placeMode != null) {
         size += PacketIO.stringSize(this.placeMode);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 19) {
         return ValidationResult.error("Buffer too small: expected at least 19 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 2) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid PickupLocation value for ArmorItemsPreferredPickupLocation");
         } else {
            v = buffer.getByte(offset + 3) & 255;
            if (v >= 3) {
               return ValidationResult.error("Invalid PickupLocation value for WeaponAndToolItemsPreferredPickupLocation");
            } else {
               v = buffer.getByte(offset + 4) & 255;
               if (v >= 3) {
                  return ValidationResult.error("Invalid PickupLocation value for UsableItemsItemsPreferredPickupLocation");
               } else {
                  v = buffer.getByte(offset + 5) & 255;
                  if (v >= 3) {
                     return ValidationResult.error("Invalid PickupLocation value for SolidBlockItemsPreferredPickupLocation");
                  } else {
                     v = buffer.getByte(offset + 6) & 255;
                     if (v >= 3) {
                        return ValidationResult.error("Invalid PickupLocation value for MiscItemsPreferredPickupLocation");
                     } else {
                        v = offset + 19;
                        if ((nullBits & 1) != 0) {
                           int placeModeLen = VarInt.peek(buffer, v);
                           if (placeModeLen < 0) {
                              return ValidationResult.error("Invalid string length for PlaceMode");
                           }

                           if (placeModeLen > 4096000) {
                              return ValidationResult.error("PlaceMode exceeds max length 4096000");
                           }

                           v += VarInt.size(placeModeLen);
                           v += placeModeLen;
                           if (v > buffer.writerIndex()) {
                              return ValidationResult.error("Buffer overflow reading PlaceMode");
                           }
                        }

                        return ValidationResult.OK;
                     }
                  }
               }
            }
         }
      }
   }

   public SyncPlayerPreferences clone() {
      SyncPlayerPreferences copy = new SyncPlayerPreferences();
      copy.showEntityMarkers = this.showEntityMarkers;
      copy.armorItemsPreferredPickupLocation = this.armorItemsPreferredPickupLocation;
      copy.weaponAndToolItemsPreferredPickupLocation = this.weaponAndToolItemsPreferredPickupLocation;
      copy.usableItemsItemsPreferredPickupLocation = this.usableItemsItemsPreferredPickupLocation;
      copy.solidBlockItemsPreferredPickupLocation = this.solidBlockItemsPreferredPickupLocation;
      copy.miscItemsPreferredPickupLocation = this.miscItemsPreferredPickupLocation;
      copy.allowNPCDetection = this.allowNPCDetection;
      copy.respondToHit = this.respondToHit;
      copy.hideHelmet = this.hideHelmet;
      copy.hideCuirass = this.hideCuirass;
      copy.hideGauntlets = this.hideGauntlets;
      copy.hidePants = this.hidePants;
      copy.placeMode = this.placeMode;
      copy.creativeInteractionDistance = this.creativeInteractionDistance;
      copy.showBuilderToolNotifications = this.showBuilderToolNotifications;
      copy.noPhysics = this.noPhysics;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SyncPlayerPreferences other)
            ? false
            : this.showEntityMarkers == other.showEntityMarkers
               && Objects.equals(this.armorItemsPreferredPickupLocation, other.armorItemsPreferredPickupLocation)
               && Objects.equals(this.weaponAndToolItemsPreferredPickupLocation, other.weaponAndToolItemsPreferredPickupLocation)
               && Objects.equals(this.usableItemsItemsPreferredPickupLocation, other.usableItemsItemsPreferredPickupLocation)
               && Objects.equals(this.solidBlockItemsPreferredPickupLocation, other.solidBlockItemsPreferredPickupLocation)
               && Objects.equals(this.miscItemsPreferredPickupLocation, other.miscItemsPreferredPickupLocation)
               && this.allowNPCDetection == other.allowNPCDetection
               && this.respondToHit == other.respondToHit
               && this.hideHelmet == other.hideHelmet
               && this.hideCuirass == other.hideCuirass
               && this.hideGauntlets == other.hideGauntlets
               && this.hidePants == other.hidePants
               && Objects.equals(this.placeMode, other.placeMode)
               && this.creativeInteractionDistance == other.creativeInteractionDistance
               && this.showBuilderToolNotifications == other.showBuilderToolNotifications
               && this.noPhysics == other.noPhysics;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.showEntityMarkers,
         this.armorItemsPreferredPickupLocation,
         this.weaponAndToolItemsPreferredPickupLocation,
         this.usableItemsItemsPreferredPickupLocation,
         this.solidBlockItemsPreferredPickupLocation,
         this.miscItemsPreferredPickupLocation,
         this.allowNPCDetection,
         this.respondToHit,
         this.hideHelmet,
         this.hideCuirass,
         this.hideGauntlets,
         this.hidePants,
         this.placeMode,
         this.creativeInteractionDistance,
         this.showBuilderToolNotifications,
         this.noPhysics
      );
   }
}
