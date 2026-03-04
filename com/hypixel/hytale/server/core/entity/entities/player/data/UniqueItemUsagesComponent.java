package com.hypixel.hytale.server.core.entity.entities.player.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UniqueItemUsagesComponent implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<UniqueItemUsagesComponent> CODEC = BuilderCodec.builder(UniqueItemUsagesComponent.class, UniqueItemUsagesComponent::new)
      .append(new KeyedCodec<>("UniqueItemUsed", new ArrayCodec<>(Codec.STRING, String[]::new)), (playerMemories, usages) -> {
         if (usages != null) {
            Collections.addAll(playerMemories.usedUniqueItems, usages);
         }
      }, playerMemories -> playerMemories.usedUniqueItems.toArray(String[]::new))
      .add()
      .build();
   private final Set<String> usedUniqueItems = new HashSet<>();

   public static ComponentType<EntityStore, UniqueItemUsagesComponent> getComponentType() {
      return EntityModule.get().getUniqueItemUsagesComponentType();
   }

   @Nullable
   @Override
   public Component<EntityStore> clone() {
      UniqueItemUsagesComponent component = new UniqueItemUsagesComponent();
      component.usedUniqueItems.addAll(this.usedUniqueItems);
      return component;
   }

   public boolean hasUsedUniqueItem(String itemId) {
      return this.usedUniqueItems.contains(itemId);
   }

   public void recordUniqueItemUsage(String itemId) {
      this.usedUniqueItems.add(itemId);
   }
}
