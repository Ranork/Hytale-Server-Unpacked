package com.hypixel.hytale.builtin.fallingblocks;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.BlockParticleEvent;
import com.hypixel.hytale.protocol.BlockSoundEvent;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocksound.config.BlockSoundSet;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockBreakingDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockGathering;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.SoftBlockDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.fallingblocks.FallingBlockImpact;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import java.util.List;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class BreakFallingBlockImpact extends FallingBlockImpact {
   public static final String ID = "Break";
   public static final BuilderCodec<BreakFallingBlockImpact> CODEC = BuilderCodec.builder(BreakFallingBlockImpact.class, BreakFallingBlockImpact::new).build();

   @Override
   public void apply(
      @Nonnull WorldChunk worldChunk,
      @Nonnull World world,
      @Nonnull BlockType blockType,
      @Nonnull Vector3d position,
      @Nonnull RotationTuple rotation,
      @Nonnull Store<EntityStore> store
   ) {
      breakFallingBlock(world, blockType, position, rotation, store);
   }

   public static void breakFallingBlock(
      @Nonnull World world, @Nonnull BlockType blockType, @Nonnull Vector3d position, @Nonnull RotationTuple rotation, @Nonnull Store<EntityStore> store
   ) {
      int blockId = BlockType.getAssetMap().getIndex(blockType.getId());
      BlockBoundingBoxes blockBoundingBoxes = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
      if (blockBoundingBoxes != null) {
         BlockBoundingBoxes.RotatedVariantBoxes rotatedBoxes = blockBoundingBoxes.get(rotation.index());
         if (rotatedBoxes != null) {
            FillerBlockUtil.forEachFillerBlock(
               rotatedBoxes,
               (x, y, z) -> world.getNotificationHandler().sendBlockParticle(position.x + x, position.y + y, position.z + z, blockId, BlockParticleEvent.Break)
            );
            BlockSoundSet soundSet = BlockSoundSet.getAssetMap().getAsset(blockType.getBlockSoundSetIndex());
            if (soundSet != null) {
               int soundEventIndex = soundSet.getSoundEventIndices().getOrDefault(BlockSoundEvent.Break, 0);
               if (soundEventIndex != 0) {
                  SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, position, store);
               }
            }

            BlockGathering gathering = blockType.getGathering();
            if (gathering != null) {
               Vector3d dropPosition = position.add(0.5, 0.0, 0.5);
               if (gathering.isSoft()) {
                  SoftBlockDropType softDrops = blockType.getGathering().getSoft();
                  List<ItemStack> itemStacks = BlockHarvestUtils.getDrops(blockType, 1, softDrops.getItemId(), softDrops.getDropListId());
                  Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(store, itemStacks, dropPosition, Rotation3f.IDENTITY);
                  world.execute(() -> store.addEntities(itemEntityHolders, AddReason.SPAWN));
               } else {
                  BlockBreakingDropType drops = blockType.getGathering().getBreaking();
                  List<ItemStack> itemStacks = BlockHarvestUtils.getDrops(blockType, 1, drops.getItemId(), drops.getDropListId());
                  Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(store, itemStacks, dropPosition, Rotation3f.IDENTITY);
                  world.execute(() -> store.addEntities(itemEntityHolders, AddReason.SPAWN));
               }
            }
         }
      }
   }
}
