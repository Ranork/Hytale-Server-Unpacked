package com.hypixel.hytale.server.flock;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.config.FlockAsset;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PersistentFlockData implements Component<EntityStore> {
   public static final BuilderCodec<PersistentFlockData> CODEC = BuilderCodec.builder(PersistentFlockData.class, PersistentFlockData::new)
      .append(new KeyedCodec<>("MaxGrowSize", Codec.INTEGER), (flock, i) -> flock.maxGrowSize = i, flock -> flock.maxGrowSize)
      .add()
      .append(new KeyedCodec<>("AllowedRoles", Codec.STRING_ARRAY), (flock, o) -> flock.flockAllowedRoles = o, flock -> flock.flockAllowedRoles)
      .add()
      .append(new KeyedCodec<>("Size", Codec.INTEGER), (flock, i) -> flock.size = i, flock -> flock.size)
      .add()
      .build();
   private int maxGrowSize = Integer.MAX_VALUE;
   private String[] flockAllowedRoles = ArrayUtil.EMPTY_STRING_ARRAY;
   private int size;

   public static ComponentType<EntityStore, PersistentFlockData> getComponentType() {
      return FlockPlugin.get().getPersistentFlockDataComponentType();
   }

   public PersistentFlockData(@Nullable FlockAsset flockDefinition, @Nonnull String[] allowedRoles) {
      this.flockAllowedRoles = allowedRoles;
      Arrays.sort((Object[])this.flockAllowedRoles);
      if (flockDefinition != null) {
         this.maxGrowSize = flockDefinition.getMaxGrowSize();
         String[] blockedRoles = flockDefinition.getBlockedRoles();
         if (blockedRoles.length > 0 && this.flockAllowedRoles.length > 0) {
            ObjectArrayList<String> combinedList = new ObjectArrayList(this.flockAllowedRoles.length);
            Collections.addAll(combinedList, this.flockAllowedRoles);

            for (String blockedRole : blockedRoles) {
               combinedList.remove(blockedRole);
            }

            this.flockAllowedRoles = (String[])combinedList.toArray(String[]::new);
         }
      }
   }

   private PersistentFlockData() {
   }

   public int getMaxGrowSize() {
      return this.maxGrowSize;
   }

   public boolean isFlockAllowedRole(String role) {
      return Arrays.binarySearch(this.flockAllowedRoles, role) >= 0;
   }

   public void increaseSize() {
      this.size++;
   }

   public void decreaseSize() {
      this.size--;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      PersistentFlockData data = new PersistentFlockData();
      data.maxGrowSize = this.maxGrowSize;
      data.flockAllowedRoles = this.flockAllowedRoles;
      data.size = this.size;
      return data;
   }
}
