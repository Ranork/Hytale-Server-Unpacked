package com.hypixel.hytale.server.core.modules.entitystats.asset;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;

public abstract class DefaultEntityStatTypes {
   private static int HEALTH;
   private static int OXYGEN;
   private static int STAMINA;
   private static int MANA;
   private static int SIGNATURE_ENERGY;
   private static int AMMO;

   public static int getHealth() {
      return HEALTH;
   }

   public static int getOxygen() {
      return OXYGEN;
   }

   public static int getStamina() {
      return STAMINA;
   }

   public static int getMana() {
      return MANA;
   }

   public static int getSignatureEnergy() {
      return SIGNATURE_ENERGY;
   }

   public static int getAmmo() {
      return AMMO;
   }

   private DefaultEntityStatTypes() {
   }

   public static void update() {
      IndexedLookupTableAssetMap<String, EntityStatType> assetMap = EntityStatType.getAssetMap();
      HEALTH = assetMap.getIndex("Health");
      OXYGEN = assetMap.getIndex("Oxygen");
      STAMINA = assetMap.getIndex("Stamina");
      MANA = assetMap.getIndex("Mana");
      SIGNATURE_ENERGY = assetMap.getIndex("SignatureEnergy");
      AMMO = assetMap.getIndex("Ammo");
   }
}
