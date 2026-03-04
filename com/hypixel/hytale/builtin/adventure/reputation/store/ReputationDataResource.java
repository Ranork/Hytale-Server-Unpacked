package com.hypixel.hytale.builtin.adventure.reputation.store;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.Object2IntMapCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import javax.annotation.Nonnull;

public class ReputationDataResource implements Resource<EntityStore> {
   @Nonnull
   public static final BuilderCodec<ReputationDataResource> CODEC = BuilderCodec.builder(ReputationDataResource.class, ReputationDataResource::new)
      .append(
         new KeyedCodec<>("Stats", new Object2IntMapCodec<>(Codec.STRING, Object2IntOpenHashMap::new, false)),
         (reputationDataResource, stringObject2IntMap) -> reputationDataResource.reputationStats = stringObject2IntMap,
         reputationDataResource -> reputationDataResource.reputationStats
      )
      .add()
      .build();
   @Nonnull
   private Object2IntMap<String> reputationStats = new Object2IntOpenHashMap(0);

   @Nonnull
   public Object2IntMap<String> getReputationStats() {
      return this.reputationStats;
   }

   @Nonnull
   @Override
   public Resource<EntityStore> clone() {
      ReputationDataResource resource = new ReputationDataResource();
      resource.reputationStats = new Object2IntOpenHashMap(this.reputationStats);
      return resource;
   }
}
