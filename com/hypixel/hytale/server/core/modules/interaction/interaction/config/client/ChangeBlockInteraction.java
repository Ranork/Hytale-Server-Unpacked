package com.hypixel.hytale.server.core.modules.interaction.interaction.config.client;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.BlockRotation;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.validator.SoundEventValidators;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChangeBlockInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<ChangeBlockInteraction> CODEC = BuilderCodec.builder(
         ChangeBlockInteraction.class, ChangeBlockInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Changes the target block to another block based on the block types provided.")
      .<Map>appendInherited(
         new KeyedCodec<>("Changes", new MapCodec<>(Codec.STRING, HashMap::new)),
         (interaction, changeMap) -> interaction.blockTypeKeys = changeMap,
         interaction -> interaction.blockTypeKeys,
         (o, p) -> o.blockTypeKeys = p.blockTypeKeys
      )
      .documentation(
         "A map of the target block to the new block.\n\nWhen the interaction runs it will look for the block that was interacted with in this map and if found it will replace it with specified block"
      )
      .addValidator(BlockType.VALIDATOR_CACHE.getMapKeyValidator().late())
      .addValidator(BlockType.VALIDATOR_CACHE.getMapValueValidator().late())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("WorldSoundEventId", Codec.STRING),
         (interaction, s) -> interaction.soundEventId = s,
         interaction -> interaction.soundEventId,
         (interaction, parent) -> interaction.soundEventId = parent.soundEventId
      )
      .documentation("Sound event to play at the block location on block change.")
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("RequireNotBroken", Codec.BOOLEAN),
         (interaction, s) -> interaction.requireNotBroken = s,
         interaction -> interaction.requireNotBroken,
         (interaction, parent) -> interaction.requireNotBroken = parent.requireNotBroken
      )
      .documentation("If true, the interaction will fail if the held item is broken (durability = 0).")
      .add()
      .afterDecode(ChangeBlockInteraction::processConfig)
      .build();
   private static final int SET_BLOCK_SETTINGS = 256;
   protected Map<String, String> blockTypeKeys;
   protected Int2IntMap changeMapIds;
   @Nullable
   protected String soundEventId = null;
   protected transient int soundEventIndex = 0;
   protected boolean requireNotBroken = false;

   protected void processConfig() {
      if (this.soundEventId != null) {
         this.soundEventIndex = SoundEvent.getAssetMap().getIndex(this.soundEventId);
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
      if (this.requireNotBroken && itemInHand != null && itemInHand.isBroken()) {
         context.getState().state = InteractionState.Failed;
      } else {
         int x = targetBlock.getX();
         int y = targetBlock.getY();
         int z = targetBlock.getZ();
         WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(x, z));
         int current = chunk.getBlock(x, y, z);
         int to = this.getChangeMapIds().get(current);
         if (to != Integer.MIN_VALUE) {
            BlockType toBlockType = BlockType.getAssetMap().getAsset(to);
            int rotationBefore = chunk.getRotationIndex(x, y, z);
            chunk.setBlock(x, y, z, to, toBlockType, rotationBefore, 0, 256);
            context.getState().blockPosition = new BlockPosition(x, y, z);
            context.getState().placedBlockId = to;
            RotationTuple resultRotation = RotationTuple.get(rotationBefore);
            context.getState().blockRotation = new BlockRotation(
               resultRotation.yaw().toPacket(), resultRotation.pitch().toPacket(), resultRotation.roll().toPacket()
            );
            if (this.soundEventIndex != 0) {
               Ref<EntityStore> ref = context.getEntity();
               Vector3d pos = new Vector3d(x + 0.5, y + 0.5, z + 0.5);
               SoundUtil.playSoundEvent3d(ref, this.soundEventIndex, pos, true, commandBuffer);
            }
         } else {
            context.getState().state = InteractionState.Failed;
         }
      }
   }

   @Nonnull
   private Int2IntMap getChangeMapIds() {
      if (this.changeMapIds == null) {
         Int2IntOpenHashMap ids = new Int2IntOpenHashMap(this.blockTypeKeys.size());
         ids.defaultReturnValue(Integer.MIN_VALUE);
         this.blockTypeKeys.forEach((fromKey, toKey) -> {
            int fromId = BlockType.getAssetMap().getIndex(fromKey);
            int toId = BlockType.getAssetMap().getIndex(toKey);
            if (fromId == Integer.MIN_VALUE) {
               HytaleLogger.getLogger().at(Level.SEVERE).log("Invalid BlockType: Interaction: %s, BlockType: %s", this.id, fromKey);
            } else if (toId == Integer.MIN_VALUE) {
               HytaleLogger.getLogger().at(Level.SEVERE).log("Invalid BlockType: Interaction: %s, BlockType: %s", this.id, toKey);
            } else {
               ids.put(fromId, toId);
            }
         });
         this.changeMapIds = ids;
      }

      return this.changeMapIds;
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
      if (this.requireNotBroken && itemInHand != null && itemInHand.isBroken()) {
         context.getState().state = InteractionState.Failed;
      } else {
         int current = world.getBlock(targetBlock);
         int to = this.getChangeMapIds().get(current);
         if (to == Integer.MIN_VALUE) {
            context.getState().state = InteractionState.Failed;
         }
      }
   }

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.ChangeBlockInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.ChangeBlockInteraction p = (com.hypixel.hytale.protocol.ChangeBlockInteraction)packet;
      p.blockChanges = this.getChangeMapIds();
      p.worldSoundEventIndex = this.soundEventIndex;
      p.requireNotBroken = this.requireNotBroken;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ChangeBlockInteraction{blockTypeKeys="
         + this.blockTypeKeys
         + ", changeMapIds="
         + this.changeMapIds
         + ", soundEventId='"
         + this.soundEventId
         + "', soundEventIndex="
         + this.soundEventIndex
         + ", requireNotBroken="
         + this.requireNotBroken
         + "} "
         + super.toString();
   }
}
