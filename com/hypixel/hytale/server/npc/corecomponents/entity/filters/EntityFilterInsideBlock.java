package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.blockset.BlockSetModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterInsideBlock;
import com.hypixel.hytale.server.npc.role.Role;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityFilterInsideBlock extends EntityFilterBase {
   public static final int COST = 400;
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected static final ComponentType<EntityStore, BoundingBox> BOUNDING_BOX_COMPONENT_TYPE = BoundingBox.getComponentType();
   protected static final ComponentType<ChunkStore, BlockChunk> BLOCK_CHUNK_COMPONENT_TYPE = BlockChunk.getComponentType();
   protected final int blockSet;
   @Nullable
   protected ChunkStore chunkStore;
   protected long chunkIndex = ChunkUtil.NOT_FOUND;
   @Nullable
   protected BlockChunk blockChunk;
   protected int chunkSectionIndex = Integer.MIN_VALUE;
   @Nullable
   protected BlockSection chunkSection;
   protected boolean matches;

   public EntityFilterInsideBlock(@Nonnull BuilderEntityFilterInsideBlock builder, @Nonnull BuilderSupport support) {
      this.blockSet = builder.getBlockSet(support);
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      this.chunkStore = store.getExternalData().getWorld().getChunkStore();
      this.matches = false;
      Vector3d position = store.getComponent(targetRef, TRANSFORM_COMPONENT_TYPE).getPosition();
      Box boundingBox = store.getComponent(targetRef, BOUNDING_BOX_COMPONENT_TYPE).getBoundingBox();
      boundingBox.forEachBlock(position.x, position.y, position.z, this, EntityFilterInsideBlock::accept);
      this.chunkStore = null;
      this.chunkIndex = ChunkUtil.NOT_FOUND;
      this.blockChunk = null;
      this.chunkSectionIndex = Integer.MIN_VALUE;
      this.chunkSection = null;
      return this.matches;
   }

   @Override
   public int cost() {
      return 400;
   }

   private static boolean accept(int x, int y, int z, @Nonnull EntityFilterInsideBlock filter) {
      long index = ChunkUtil.indexChunkFromBlock(x, z);
      if (index != filter.chunkIndex) {
         filter.chunkIndex = index;
         filter.blockChunk = filter.chunkStore.getChunkComponent(index, BLOCK_CHUNK_COMPONENT_TYPE);
      }

      if (filter.blockChunk == null) {
         return false;
      } else {
         int section = ChunkUtil.indexSection(y);
         if (section != filter.chunkSectionIndex) {
            filter.chunkSectionIndex = section;
            filter.chunkSection = section >= 0 && section < 10 ? filter.blockChunk.getSectionAtIndex(section) : null;
         }

         if (filter.chunkSection == null) {
            filter.matches = BlockSetModule.getInstance().blockInSet(filter.blockSet, 0);
            return !filter.matches;
         } else {
            int blockId = filter.chunkSection.get(x, y, z);
            filter.matches = BlockSetModule.getInstance().blockInSet(filter.blockSet, blockId);
            return !filter.matches;
         }
      }
   }
}
