package com.hypixel.hytale.server.core.modules.interaction.interaction.config.client;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockConditionInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<BlockConditionInteraction> CODEC = BuilderCodec.builder(
         BlockConditionInteraction.class, BlockConditionInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Tests the target block and executes `Next` if it matches all the conditions, otherwise `Failed` is run.")
      .<BlockConditionInteraction.BlockMatcher[]>appendInherited(
         new KeyedCodec<>("Matchers", new ArrayCodec<>(BlockConditionInteraction.BlockMatcher.CODEC, BlockConditionInteraction.BlockMatcher[]::new)),
         (o, i) -> o.matchers = i,
         o -> o.matchers,
         (o, p) -> o.matchers = p.matchers
      )
      .documentation("The matchers to test the block against.")
      .add()
      .build();
   private BlockConditionInteraction.BlockMatcher[] matchers;

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
      BlockFace face = context.getClientState().blockFace;
      this.doInteraction(context, world, targetBlock, face);
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
      context.getState().blockFace = BlockFace.Up;
      this.doInteraction(context, world, targetBlock, context.getState().blockFace);
   }

   private void doInteraction(@Nonnull InteractionContext context, @Nonnull World world, @Nonnull Vector3i targetBlock, @Nonnull BlockFace face) {
      WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
      if (chunk != null) {
         BlockType blockType = chunk.getBlockType(targetBlock);
         RotationTuple blockRotation = chunk.getRotation(targetBlock.x, targetBlock.y, targetBlock.z);
         Item itemType = blockType.getItem();
         if (itemType == null) {
            context.getState().state = InteractionState.Failed;
         } else {
            boolean ok = false;
            BlockConditionInteraction.BlockMatcher[] var10 = this.matchers;
            int var11 = var10.length;
            int var12 = 0;

            while (var12 < var11) {
               label69: {
                  label77: {
                     BlockConditionInteraction.BlockMatcher matcher = var10[var12];
                     if (matcher.face != BlockFace.None) {
                        BlockFace transformedFace = matcher.face;
                        if (!matcher.staticFace) {
                           Rotation yaw = blockRotation.yaw();
                           Rotation pitch = blockRotation.pitch();
                           com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFace newFace = com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFace.rotate(
                              com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFace.fromProtocolFace(transformedFace), yaw, pitch
                           );
                           transformedFace = com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFace.toProtocolFace(newFace);
                        }

                        if (!transformedFace.equals(face)) {
                           break label77;
                        }
                     }

                     if (matcher.block == null) {
                        break label69;
                     }

                     label61:
                     if (matcher.block.id == null || matcher.block.id.equals(itemType.getId())) {
                        if (matcher.block.state != null) {
                           String state = blockType.getStateForBlock(blockType);
                           if (state == null) {
                              state = "default";
                           }

                           if (!matcher.block.state.equals(state)) {
                              break label61;
                           }
                        }

                        if (matcher.block.tag == null) {
                           break label69;
                        }

                        AssetExtraInfo.Data data = blockType.getData();
                        if (data != null) {
                           Int2ObjectMap<IntSet> tags = data.getTags();
                           if (tags != null && tags.containsKey(matcher.block.tagIndex)) {
                              break label69;
                           }
                        }
                     }
                  }

                  var12++;
                  continue;
               }

               ok = true;
               break;
            }

            if (ok) {
               context.getState().state = InteractionState.Finished;
            } else {
               context.getState().state = InteractionState.Failed;
            }
         }
      }
   }

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.BlockConditionInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.BlockConditionInteraction p = (com.hypixel.hytale.protocol.BlockConditionInteraction)packet;
      if (this.matchers != null) {
         p.matchers = new com.hypixel.hytale.protocol.BlockMatcher[this.matchers.length];

         for (int i = 0; i < this.matchers.length; i++) {
            p.matchers[i] = this.matchers[i].toPacket();
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "BlockConditionInteraction{matchers=" + Arrays.toString((Object[])this.matchers) + "} " + super.toString();
   }

   public static class BlockIdMatcher implements NetworkSerializable<com.hypixel.hytale.protocol.BlockIdMatcher> {
      @Nonnull
      public static BuilderCodec<BlockConditionInteraction.BlockIdMatcher> CODEC = BuilderCodec.builder(
            BlockConditionInteraction.BlockIdMatcher.class, BlockConditionInteraction.BlockIdMatcher::new
         )
         .appendInherited(
            new KeyedCodec<>("Id", Codec.STRING),
            (blockIdMatcher, s) -> blockIdMatcher.id = s,
            blockIdMatcher -> blockIdMatcher.id,
            (blockIdMatcher, parent) -> blockIdMatcher.id = parent.id
         )
         .addValidatorLate(() -> BlockType.VALIDATOR_CACHE.getValidator().late())
         .documentation("Match against a specific block id.")
         .add()
         .<String>appendInherited(
            new KeyedCodec<>("State", Codec.STRING),
            (blockIdMatcher, s) -> blockIdMatcher.state = s,
            blockIdMatcher -> blockIdMatcher.state,
            (blockIdMatcher, parent) -> blockIdMatcher.state = parent.state
         )
         .documentation("Match against specific block state.")
         .add()
         .<String>appendInherited(
            new KeyedCodec<>("Tag", Codec.STRING),
            (blockIdMatcher, s) -> blockIdMatcher.tag = s,
            blockIdMatcher -> blockIdMatcher.tag,
            (blockIdMatcher, parent) -> blockIdMatcher.tag = parent.tag
         )
         .documentation("Match against specific block tag.")
         .add()
         .afterDecode(blockIdMatcher -> {
            if (blockIdMatcher.tag != null) {
               blockIdMatcher.tagIndex = AssetRegistry.getOrCreateTagIndex(blockIdMatcher.tag);
            }
         })
         .build();
      protected String id;
      protected String state;
      protected String tag;
      protected int tagIndex = Integer.MIN_VALUE;

      @Nonnull
      public com.hypixel.hytale.protocol.BlockIdMatcher toPacket() {
         com.hypixel.hytale.protocol.BlockIdMatcher packet = new com.hypixel.hytale.protocol.BlockIdMatcher();
         if (this.id != null) {
            packet.id = this.id;
         }

         if (this.state != null) {
            packet.state = this.state;
         }

         packet.tagIndex = this.tagIndex;
         return packet;
      }

      @Nonnull
      @Override
      public String toString() {
         return "BlockIdMatcher{id='" + this.id + "', state='" + this.state + "', tag='" + this.tag + "'}";
      }
   }

   public static class BlockMatcher implements NetworkSerializable<com.hypixel.hytale.protocol.BlockMatcher> {
      @Nonnull
      public static BuilderCodec<BlockConditionInteraction.BlockMatcher> CODEC = BuilderCodec.builder(
            BlockConditionInteraction.BlockMatcher.class, BlockConditionInteraction.BlockMatcher::new
         )
         .appendInherited(
            new KeyedCodec<>("Block", BlockConditionInteraction.BlockIdMatcher.CODEC),
            (blockMatcher, blockIdMatcher) -> blockMatcher.block = blockIdMatcher,
            blockMatcher -> blockMatcher.block,
            (blockMatcher, parent) -> blockMatcher.block = parent.block
         )
         .documentation("Match against block values")
         .add()
         .<com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFace>appendInherited(
            new KeyedCodec<>("Face", com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFace.CODEC),
            (blockMatcher, face) -> blockMatcher.face = com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFace.toProtocolFace(face),
            blockMatcher -> com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFace.fromProtocolFace(blockMatcher.face),
            (blockMatcher, parent) -> blockMatcher.face = parent.face
         )
         .documentation("Match against a specific block face.")
         .add()
         .<Boolean>appendInherited(
            new KeyedCodec<>("StaticFace", Codec.BOOLEAN),
            (blockMatcher, aBoolean) -> blockMatcher.staticFace = aBoolean,
            blockMatcher -> blockMatcher.staticFace,
            (blockMatcher, parent) -> blockMatcher.staticFace = parent.staticFace
         )
         .documentation("Whether the face matching is unaffected by the block rotation or not.")
         .add()
         .build();
      protected BlockConditionInteraction.BlockIdMatcher block;
      protected BlockFace face = BlockFace.None;
      protected boolean staticFace;

      @Nonnull
      public com.hypixel.hytale.protocol.BlockMatcher toPacket() {
         com.hypixel.hytale.protocol.BlockMatcher packet = new com.hypixel.hytale.protocol.BlockMatcher();
         if (this.block != null) {
            packet.block = this.block.toPacket();
         }

         if (this.face != null) {
            packet.face = this.face;
         }

         packet.staticFace = this.staticFace;
         return packet;
      }

      @Nonnull
      @Override
      public String toString() {
         return "BlockMatcher{block=" + this.block + ", face=" + this.face + ", staticFace=" + this.staticFace + "}";
      }
   }
}
