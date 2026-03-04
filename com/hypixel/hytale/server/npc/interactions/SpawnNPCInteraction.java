package com.hypixel.hytale.server.npc.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.map.IWeightedElement;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.validators.NPCRoleValidator;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpawnNPCInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<SpawnNPCInteraction> CODEC = BuilderCodec.builder(
         SpawnNPCInteraction.class, SpawnNPCInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Spawns an NPC on the block that is being interacted with.")
      .<String>append(
         new KeyedCodec<>("EntityId", Codec.STRING),
         (spawnNPCInteraction, s) -> spawnNPCInteraction.entityId = s,
         spawnNPCInteraction -> spawnNPCInteraction.entityId
      )
      .documentation("The ID of the entity asset to spawn.")
      .addValidator(NPCRoleValidator.INSTANCE)
      .add()
      .<SpawnNPCInteraction.WeightedNPCSpawn[]>append(
         new KeyedCodec<>("WeightedEntityIds", new ArrayCodec<>(SpawnNPCInteraction.WeightedNPCSpawn.CODEC, SpawnNPCInteraction.WeightedNPCSpawn[]::new)),
         (spawnNPCInteraction, o) -> spawnNPCInteraction.weightedSpawns = o,
         spawnNPCInteraction -> spawnNPCInteraction.weightedSpawns
      )
      .documentation("A weighted list of entity IDs from which an entity will be selected for spawning. Supersedes any provided EntityId.")
      .add()
      .<Vector3d>append(
         new KeyedCodec<>("SpawnOffset", Vector3d.CODEC),
         (spawnNPCInteraction, s) -> spawnNPCInteraction.spawnOffset.assign(s),
         spawnNPCInteraction -> spawnNPCInteraction.spawnOffset
      )
      .documentation("The offset to apply to the spawn position of the NPC, relative to the block's rotation and center.")
      .add()
      .<Float>append(
         new KeyedCodec<>("SpawnYawOffset", Codec.FLOAT),
         (spawnNPCInteraction, f) -> spawnNPCInteraction.spawnYawOffset = f,
         spawnNPCInteraction -> spawnNPCInteraction.spawnYawOffset
      )
      .documentation("The yaw rotation offset in radians to apply to the NPC rotation, relative to the block's yaw.")
      .add()
      .<Float>append(
         new KeyedCodec<>("SpawnChance", Codec.FLOAT),
         (spawnNPCInteraction, f) -> spawnNPCInteraction.spawnChance = f,
         spawnNPCInteraction -> spawnNPCInteraction.spawnChance
      )
      .documentation("The chance of the NPC spawning when the interaction is triggered.")
      .add()
      .afterDecode(interaction -> {
         if (interaction.weightedSpawns != null && interaction.weightedSpawns.length > 0) {
            WeightedMap.Builder<String> mapBuilder = WeightedMap.builder(ArrayUtil.EMPTY_STRING_ARRAY);

            for (SpawnNPCInteraction.WeightedNPCSpawn entry : interaction.weightedSpawns) {
               mapBuilder.put(entry.id, entry.weight);
            }

            interaction.weightedSpawnMap = mapBuilder.build();
         }
      })
      .build();
   protected String entityId;
   protected SpawnNPCInteraction.WeightedNPCSpawn[] weightedSpawns;
   protected IWeightedMap<String> weightedSpawnMap;
   @Nonnull
   protected Vector3d spawnOffset = new Vector3d();
   protected float spawnYawOffset;
   protected float spawnChance = 1.0F;

   private void spawnNPC(@Nonnull Store<EntityStore> store, @Nonnull Vector3i targetBlock) {
      World world = store.getExternalData().getWorld();
      SpawnNPCInteraction.SpawnData spawnData = this.computeSpawnData(world, targetBlock);
      String entityToSpawn = this.entityId;
      if (this.weightedSpawnMap != null) {
         entityToSpawn = this.weightedSpawnMap.get(ThreadLocalRandom.current());
      }

      if (entityToSpawn != null) {
         NPCPlugin.get().spawnNPC(store, entityToSpawn, null, spawnData.position(), spawnData.rotation());
      }
   }

   @Nonnull
   private SpawnNPCInteraction.SpawnData computeSpawnData(@Nonnull World world, @Nonnull Vector3i targetBlock) {
      long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z);
      ChunkStore chunkStore = world.getChunkStore();
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
      if (chunkRef != null && chunkRef.isValid()) {
         WorldChunk worldChunkComponent = chunkStore.getStore().getComponent(chunkRef, WorldChunk.getComponentType());

         assert worldChunkComponent != null;

         BlockType blockType = worldChunkComponent.getBlockType(targetBlock.x, targetBlock.y, targetBlock.z);
         if (blockType == null) {
            return new SpawnNPCInteraction.SpawnData(this.spawnOffset.clone().add(targetBlock).add(0.5, 0.5, 0.5), Vector3f.ZERO);
         } else {
            BlockChunk blockChunkComponent = chunkStore.getStore().getComponent(chunkRef, BlockChunk.getComponentType());
            if (blockChunkComponent == null) {
               return new SpawnNPCInteraction.SpawnData(this.spawnOffset.clone().add(targetBlock).add(0.5, 0.5, 0.5), Vector3f.ZERO);
            } else {
               BlockSection section = blockChunkComponent.getSectionAtBlockY(targetBlock.y);
               int rotationIndex = section.getRotationIndex(targetBlock.x, targetBlock.y, targetBlock.z);
               RotationTuple rotationTuple = RotationTuple.get(rotationIndex);
               Vector3d position = rotationTuple.rotate(this.spawnOffset);
               Vector3d blockCenter = new Vector3d();
               blockType.getBlockCenter(rotationIndex, blockCenter);
               position.add(blockCenter).add(targetBlock);
               Vector3f rotation = new Vector3f(0.0F, (float)(rotationTuple.yaw().getRadians() + Math.toRadians(this.spawnYawOffset)), 0.0F);
               return new SpawnNPCInteraction.SpawnData(position, rotation);
            }
         }
      } else {
         return new SpawnNPCInteraction.SpawnData(this.spawnOffset.clone().add(targetBlock).add(0.5, 0.5, 0.5), Vector3f.ZERO);
      }
   }

   @Override
   protected void interactWithBlock(
      @Nonnull World world,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nullable ItemStack itemInHand,
      @Nonnull Vector3i targetBlock,
      @Nonnull CooldownHandler cooldownHandler
   ) {
      if (!(ThreadLocalRandom.current().nextFloat() > this.spawnChance)) {
         commandBuffer.run(store -> this.spawnNPC(world.getEntityStore().getStore(), targetBlock));
      }
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
      if (!(ThreadLocalRandom.current().nextFloat() > this.spawnChance)) {
         CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

         assert commandBuffer != null;

         commandBuffer.run(store -> this.spawnNPC(world.getEntityStore().getStore(), targetBlock));
      }
   }

   private record SpawnData(@Nonnull Vector3d position, @Nonnull Vector3f rotation) {
   }

   protected static class WeightedNPCSpawn implements IWeightedElement {
      private static final BuilderCodec<SpawnNPCInteraction.WeightedNPCSpawn> CODEC = BuilderCodec.builder(
            SpawnNPCInteraction.WeightedNPCSpawn.class, SpawnNPCInteraction.WeightedNPCSpawn::new
         )
         .append(new KeyedCodec<>("Id", Codec.STRING), (spawn, s) -> spawn.id = s, spawn -> spawn.id)
         .documentation("The Role ID of the NPC to spawn.")
         .addValidator(Validators.nonNull())
         .addValidator(NPCRoleValidator.INSTANCE)
         .add()
         .<Double>append(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (spawn, d) -> spawn.weight = d, spawn -> spawn.weight)
         .documentation("The relative weight of this NPC (chance of being spawned is this value relative to the sum of all weights).")
         .addValidator(Validators.nonNull())
         .addValidator(Validators.greaterThan(0.0))
         .add()
         .build();
      private String id;
      private double weight;

      private WeightedNPCSpawn() {
      }

      @Override
      public double getWeight() {
         return this.weight;
      }
   }
}
