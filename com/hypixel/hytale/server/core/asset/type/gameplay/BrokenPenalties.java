package com.hypixel.hytale.server.core.asset.type.gameplay;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class BrokenPenalties {
   public static final BrokenPenalties DEFAULT = new BrokenPenalties();
   @Nonnull
   public static final BuilderCodec<BrokenPenalties> CODEC = BuilderCodec.builder(BrokenPenalties.class, BrokenPenalties::new)
      .addField(new KeyedCodec<>("Tool", Codec.DOUBLE), (o, i) -> o.tool = i, o -> o.tool)
      .addField(new KeyedCodec<>("Armor", Codec.DOUBLE), (o, i) -> o.armor = i, o -> o.armor)
      .addField(new KeyedCodec<>("Weapon", Codec.DOUBLE), (o, i) -> o.weapon = i, o -> o.weapon)
      .build();
   private Double tool;
   private Double armor;
   private Double weapon;

   public double getTool(double defaultValue) {
      return this.tool == null ? defaultValue : this.tool;
   }

   public double getArmor(double defaultValue) {
      return this.armor == null ? defaultValue : this.armor;
   }

   public double getWeapon(double defaultValue) {
      return this.weapon == null ? defaultValue : this.weapon;
   }
}
