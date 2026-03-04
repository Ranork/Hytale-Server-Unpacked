package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.PickupLocation;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SyncPlayerPreferences implements Packet, ToServerPacket {
   public static final int PACKET_ID = 116;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 12;
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
      boolean hidePants
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
   }

   @Nonnull
   public static SyncPlayerPreferences deserialize(@Nonnull ByteBuf buf, int offset) {
      SyncPlayerPreferences obj = new SyncPlayerPreferences();
      obj.showEntityMarkers = buf.getByte(offset + 0) != 0;
      obj.armorItemsPreferredPickupLocation = PickupLocation.fromValue(buf.getByte(offset + 1));
      obj.weaponAndToolItemsPreferredPickupLocation = PickupLocation.fromValue(buf.getByte(offset + 2));
      obj.usableItemsItemsPreferredPickupLocation = PickupLocation.fromValue(buf.getByte(offset + 3));
      obj.solidBlockItemsPreferredPickupLocation = PickupLocation.fromValue(buf.getByte(offset + 4));
      obj.miscItemsPreferredPickupLocation = PickupLocation.fromValue(buf.getByte(offset + 5));
      obj.allowNPCDetection = buf.getByte(offset + 6) != 0;
      obj.respondToHit = buf.getByte(offset + 7) != 0;
      obj.hideHelmet = buf.getByte(offset + 8) != 0;
      obj.hideCuirass = buf.getByte(offset + 9) != 0;
      obj.hideGauntlets = buf.getByte(offset + 10) != 0;
      obj.hidePants = buf.getByte(offset + 11) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 12;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
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
   }

   @Override
   public int computeSize() {
      return 12;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 12 ? ValidationResult.error("Buffer too small: expected at least 12 bytes") : ValidationResult.OK;
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
               && this.hidePants == other.hidePants;
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
         this.hidePants
      );
   }
}
