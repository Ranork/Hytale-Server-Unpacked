package com.hypixel.hytale.server.npc.valuestore;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.SlotMapper;
import java.util.Arrays;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class ValueStore implements Component<EntityStore> {
   @Nonnull
   private final String[] stringValues;
   @Nonnull
   private final int[] intValues;
   @Nonnull
   private final double[] doubleValues;

   public static ComponentType<EntityStore, ValueStore> getComponentType() {
      return NPCPlugin.get().getValueStoreComponentType();
   }

   private ValueStore(int stringCount, int intCount, int doubleCount) {
      this.stringValues = new String[stringCount];
      this.intValues = new int[intCount];
      this.doubleValues = new double[doubleCount];
      Arrays.fill(this.intValues, Integer.MIN_VALUE);
      Arrays.fill(this.doubleValues, -Double.MAX_VALUE);
   }

   public String readString(int slot) {
      return this.stringValues[slot];
   }

   public void storeString(int slot, String value) {
      this.stringValues[slot] = value;
   }

   public int readInt(int slot) {
      return this.intValues[slot];
   }

   public void storeInt(int slot, int value) {
      this.intValues[slot] = value;
   }

   public double readDouble(int slot) {
      return this.doubleValues[slot];
   }

   public void storeDouble(int slot, double value) {
      this.doubleValues[slot] = value;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new ValueStore(this.stringValues.length, this.intValues.length, this.doubleValues.length);
   }

   public static class Builder {
      private final SlotMapper stringSlots = new SlotMapper();
      private final SlotMapper intSlots = new SlotMapper();
      private final SlotMapper doubleSlots = new SlotMapper();

      public int getStringSlot(String name) {
         return this.stringSlots.getSlot(name);
      }

      public int getIntSlot(String name) {
         return this.intSlots.getSlot(name);
      }

      public int getDoubleSlot(String name) {
         return this.doubleSlots.getSlot(name);
      }

      @Nonnull
      public ValueStore build() {
         return new ValueStore(this.stringSlots.slotCount(), this.intSlots.slotCount(), this.doubleSlots.slotCount());
      }
   }

   public static enum Type implements Supplier<String> {
      String("String value"),
      Int("Integer value"),
      Double("Double value");

      public static final ValueStore.Type[] VALUES = values();
      private final String description;

      private Type(String description) {
         this.description = description;
      }

      public String get() {
         return this.description;
      }
   }
}
