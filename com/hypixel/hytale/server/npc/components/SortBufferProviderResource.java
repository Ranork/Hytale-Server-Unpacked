package com.hypixel.hytale.server.npc.components;

import com.hypixel.hytale.common.collection.BucketList;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import javax.annotation.Nonnull;

public class SortBufferProviderResource implements Resource<EntityStore> {
   private final BucketList.SortBufferProvider sortBufferProvider = new BucketList.SortBufferProvider();

   public static ResourceType<EntityStore, SortBufferProviderResource> getResourceType() {
      return NPCPlugin.get().getSortBufferProviderResourceResourceType();
   }

   @Nonnull
   public BucketList.SortBufferProvider getSortBufferProvider() {
      return this.sortBufferProvider;
   }

   @Nonnull
   @Override
   public Resource<EntityStore> clone() {
      return new SortBufferProviderResource();
   }
}
