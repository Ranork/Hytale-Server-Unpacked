package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.PickupLocation;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public record PlayerSettings(
   boolean showEntityMarkers,
   @Nonnull PickupLocation armorItemsPreferredPickupLocation,
   @Nonnull PickupLocation weaponAndToolItemsPreferredPickupLocation,
   @Nonnull PickupLocation usableItemsItemsPreferredPickupLocation,
   @Nonnull PickupLocation solidBlockItemsPreferredPickupLocation,
   @Nonnull PickupLocation miscItemsPreferredPickupLocation,
   PlayerCreativeSettings creativeSettings,
   boolean hideHelmet,
   boolean hideCuirass,
   boolean hideGauntlets,
   boolean hidePants
) implements Component<EntityStore> {
   @Nonnull
   private static final PlayerSettings INSTANCE = new PlayerSettings(
      false,
      PickupLocation.Hotbar,
      PickupLocation.Hotbar,
      PickupLocation.Hotbar,
      PickupLocation.Hotbar,
      PickupLocation.Hotbar,
      new PlayerCreativeSettings(),
      false,
      false,
      false,
      false
   );

   @Nonnull
   public static ComponentType<EntityStore, PlayerSettings> getComponentType() {
      return EntityModule.get().getPlayerSettingsComponentType();
   }

   @Nonnull
   public static PlayerSettings defaults() {
      return INSTANCE;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new PlayerSettings(
         this.showEntityMarkers,
         this.armorItemsPreferredPickupLocation,
         this.weaponAndToolItemsPreferredPickupLocation,
         this.usableItemsItemsPreferredPickupLocation,
         this.solidBlockItemsPreferredPickupLocation,
         this.miscItemsPreferredPickupLocation,
         this.creativeSettings.clone(),
         this.hideHelmet,
         this.hideCuirass,
         this.hideGauntlets,
         this.hidePants
      );
   }
}
