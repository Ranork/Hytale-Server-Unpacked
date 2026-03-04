package com.hypixel.hytale.server.core.universe.world.chunk.section.blockpositions;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.function.consumer.IntObjectConsumer;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.LegacyModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.BitSet;
import java.util.List;
import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockPositionProvider implements Component<ChunkStore> {
   private final BitSet searchedBlockSets;
   @Nullable
   private final Int2ObjectMap<List<IBlockPositionData>> blockData;
   private final short lightChangeCounter;

   public static ComponentType<ChunkStore, BlockPositionProvider> getComponentType() {
      return LegacyModule.get().getBlockPositionProviderComponentType();
   }

   public BlockPositionProvider(@Nonnull BitSet blockSets, @Nullable Int2ObjectOpenHashMap<List<IBlockPositionData>> data, short lightChangeCounter) {
      this.searchedBlockSets = (BitSet)blockSets.clone();
      this.lightChangeCounter = lightChangeCounter;
      if (data != null) {
         this.blockData = Int2ObjectMaps.unmodifiable(data);
      } else {
         this.blockData = null;
      }
   }

   public boolean isStale(int currentBlockSet, @Nonnull BlockSection section) {
      return this.lightChangeCounter != section.getLocalChangeCounter() || !this.searchedBlockSets.get(currentBlockSet);
   }

   public <T> void findBlocks(
      @Nonnull List<IBlockPositionData> resultList,
      int blockSet,
      double range,
      double yRange,
      @Nonnull Ref<EntityStore> ref,
      @Nullable BiPredicate<IBlockPositionData, T> filter,
      T obj,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.blockData != null) {
         List<IBlockPositionData> data = (List<IBlockPositionData>)this.blockData.getOrDefault(blockSet, null);
         if (data != null) {
            TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

            assert transformComponent != null;

            Vector3d pos = transformComponent.getPosition();
            double range2 = range * range;

            for (int i = 0; i < data.size(); i++) {
               IBlockPositionData entry = data.get(i);
               double entryY = entry.getYCentre();
               if (Math.abs(pos.y - entryY) <= yRange
                  && pos.distanceSquaredTo(entry.getXCentre(), entryY, entry.getZCentre()) <= range2
                  && (filter == null || !filter.test(entry, obj))) {
                  resultList.add(entry);
               }
            }
         }
      }
   }

   public BitSet getSearchedBlockSets() {
      return (BitSet)this.searchedBlockSets.clone();
   }

   public void forEachBlockSet(@Nonnull IntObjectConsumer<List<IBlockPositionData>> listConsumer) {
      if (this.blockData != null) {
         this.blockData.forEach(listConsumer::accept);
      }
   }

   @Nonnull
   @Override
   public Component<ChunkStore> clone() {
      return this;
   }
}
