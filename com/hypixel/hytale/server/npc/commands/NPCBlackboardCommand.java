package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.AndQuery;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeChunkPosition;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeIntPosition;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.blockpositions.BlockPositionProvider;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.BlockRegionView;
import com.hypixel.hytale.server.npc.blackboard.view.blocktype.BlockTypeView;
import com.hypixel.hytale.server.npc.blackboard.view.event.block.BlockEventView;
import com.hypixel.hytale.server.npc.blackboard.view.event.entity.EntityEventView;
import com.hypixel.hytale.server.npc.blackboard.view.interaction.InteractionView;
import com.hypixel.hytale.server.npc.blackboard.view.interaction.ReservationStatus;
import com.hypixel.hytale.server.npc.blackboard.view.resource.ResourceView;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.BitSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NPCBlackboardCommand extends AbstractCommandCollection {
   public NPCBlackboardCommand() {
      super("blackboard", "server.commands.npc.blackboard.desc");
      this.addSubCommand(new NPCBlackboardCommand.ChunksCommand());
      this.addSubCommand(new NPCBlackboardCommand.ChunkCommand());
      this.addSubCommand(new NPCBlackboardCommand.DropCommand());
      this.addSubCommand(new NPCBlackboardCommand.ViewsCommand());
      this.addSubCommand(new NPCBlackboardCommand.ViewCommand());
      this.addSubCommand(new NPCBlackboardCommand.BlockEventsCommand());
      this.addSubCommand(new NPCBlackboardCommand.EntityEventsCommand());
      this.addSubCommand(new NPCBlackboardCommand.ResourceViewsCommand());
      this.addSubCommand(new NPCBlackboardCommand.ResourceViewCommand());
      this.addSubCommand(new NPCBlackboardCommand.ReserveCommand());
      this.addSubCommand(new NPCBlackboardCommand.ReservationCommand());
   }

   public static class BlockEventsCommand extends AbstractWorldCommand {
      public BlockEventsCommand() {
         super("blockevents", "server.commands.npc.blackboard.blockevents.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         BlockEventView blockEventView = store.getResource(Blackboard.getResourceType()).getView(BlockEventView.class, 0L);
         StringBuilder sb = new StringBuilder("Block Event View:\n");
         sb.append(" Total BlockSets: ").append(blockEventView.getSetCount());
         sb.append("\n BlockSets:\n");
         Message msg = Message.translation("server.commands.npc.blackboard.blockevents.title").param("count", blockEventView.getSetCount());
         blockEventView.forEach((b, t) -> {
            sb.append("  ").append(BlockSet.getAssetMap().getAsset(b).getId()).append(" (").append(t.get()).append("):\n");
            msg.insert("  " + BlockSet.getAssetMap().getAsset(b).getId() + " (" + t.get() + "):\n");
         }, e -> {
            UUIDComponent uuidComponent = store.getComponent(e, UUIDComponent.getComponentType());

            assert uuidComponent != null;

            UUID uuid = uuidComponent.getUuid();
            NPCEntity npcComponent = store.getComponent(e, NPCEntity.getComponentType());

            assert npcComponent != null;

            String roleName = npcComponent.getRoleName();
            sb.append("   ").append(uuid).append(": ").append(roleName).append("\n");
            msg.insert("   " + uuid + ": " + roleName + "\n");
         });
         context.sendMessage(msg);
         NPCPlugin.get().getLogger().at(Level.INFO).log(sb.toString());
      }
   }

   public static class ChunkCommand extends AbstractWorldCommand {
      @Nonnull
      private final RequiredArg<RelativeIntPosition> positionArg = this.withRequiredArg(
         "position", "server.commands.npc.blackboard.chunk.position.desc", ArgTypes.RELATIVE_BLOCK_POSITION
      );

      public ChunkCommand() {
         super("chunk", "server.commands.npc.blackboard.chunk.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         Vector3i blockPosition = this.positionArg.get(context).getBlockPosition(context, store);
         Vector3i position = new Vector3i(
            ChunkUtil.chunkCoordinate(blockPosition.x), ChunkUtil.chunkCoordinate(blockPosition.y), ChunkUtil.chunkCoordinate(blockPosition.z)
         );
         long chunkIndex = ChunkUtil.indexChunk(position.x, position.z);
         StringBuilder sb = new StringBuilder("Blackboard chunk entry " + chunkIndex);
         sb.append(" (").append(position.x).append(", ").append(position.y).append(", ").append(position.z).append("):\n");
         sb.append(" Partial blackboard grid coordinates: ");
         sb.append(BlockRegionView.chunkToRegionalBlackboardCoordinate(position.x)).append(", ");
         sb.append(BlockRegionView.chunkToRegionalBlackboardCoordinate(position.z)).append('\n');
         Message msg = Message.translation("server.commands.npc.blackboard.chunk.entry")
            .param("index", chunkIndex)
            .param("chunkX", position.x)
            .param("chunkY", position.y)
            .param("chunkZ", position.z)
            .param("regionChunkX", BlockRegionView.chunkToRegionalBlackboardCoordinate(position.x))
            .param("regionChunkZ", BlockRegionView.chunkToRegionalBlackboardCoordinate(position.z))
            .insert("\n");
         ChunkStore chunkStore = world.getChunkStore();
         Store<ChunkStore> chunkStoreStore = chunkStore.getStore();
         Ref<ChunkStore> chunkSection = chunkStore.getChunkSectionReference(position.x, position.y, position.z);
         if (chunkSection == null) {
            sb.append(" Chunk not loaded");
            msg.insert(Message.translation("server.commands.npc.blackboard.chunk.notLoaded"));
         } else {
            BlockPositionProvider entry = chunkStoreStore.getComponent(chunkSection, BlockPositionProvider.getComponentType());
            if (entry == null) {
               sb.append(" No entry exists");
               msg.insert(Message.translation("server.commands.npc.blackboard.chunk.noEntry"));
            } else {
               sb.append(" Searched BlockSets: [ ");
               msg.insert(Message.translation("server.commands.npc.blockSetsSearched"));
               BitSet searchedBlockSets = entry.getSearchedBlockSets();
               boolean subsequent = false;

               for (int i = searchedBlockSets.nextSetBit(0); i >= 0; i = searchedBlockSets.nextSetBit(i + 1)) {
                  if (subsequent) {
                     sb.append(", ");
                     msg.insert(", ");
                  }

                  sb.append(BlockSet.getAssetMap().getAsset(i).getId());
                  msg.insert(BlockSet.getAssetMap().getAsset(i).getId());
                  subsequent = true;
                  if (i == Integer.MAX_VALUE) {
                     break;
                  }
               }

               sb.append(" ]\n Entries:\n");
               msg.insert(Message.translation("server.commands.npc.blackboard.chunk.entries"));
               entry.forEachBlockSet(
                  (blockSet, list) -> {
                     sb.append("  BlockSet: ").append(BlockSet.getAssetMap().getAsset(blockSet).getId()).append("\n   Blocks:\n");
                     msg.insert(
                        Message.translation("server.commands.npc.blackboard.chunk.blockSet").param("id", BlockSet.getAssetMap().getAsset(blockSet).getId())
                     );
                     msg.insert("\n");
                     list.forEach(
                        dataEntry -> {
                           sb.append("    [ ").append(BlockType.getAssetMap().getAsset(dataEntry.getBlockType()).getId());
                           sb.append(" (").append(dataEntry.getX()).append(", ").append(dataEntry.getY()).append(", ").append(dataEntry.getZ()).append(") ]\n");
                           msg.insert(
                              "    [ "
                                 + BlockType.getAssetMap().getAsset(dataEntry.getBlockType()).getId()
                                 + " ("
                                 + dataEntry.getX()
                                 + ", "
                                 + dataEntry.getY()
                                 + ", "
                                 + dataEntry.getZ()
                                 + ") ]"
                           );
                        }
                     );
                  }
               );
            }
         }

         context.sendMessage(msg);
         NPCPlugin.get().getLogger().at(Level.INFO).log(sb.toString());
      }
   }

   public static class ChunksCommand extends AbstractWorldCommand {
      public ChunksCommand() {
         super("chunks", "server.commands.npc.blackboard.chunks.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
         StringBuilder sb = new StringBuilder("Blackboard chunk info:\n");
         int[] count = new int[]{0};
         chunkStore.forEachChunk(BlockPositionProvider.getComponentType(), (archetypeChunk, commandBuffer) -> count[0] += archetypeChunk.size());
         sb.append(" Total sections: ").append(count[0]).append('\n');
         sb.append(" Chunk sections:\n");
         Message msg = Message.translation("server.commands.npc.blackboard.chunks.chunkInfo").param("nb", count[0]);
         AndQuery<ChunkStore> query = Query.and(ChunkSection.getComponentType(), BlockPositionProvider.getComponentType());
         chunkStore.forEachChunk(
            query,
            (archetypeChunk, commandBuffer) -> {
               for (int index = 0; index < archetypeChunk.size(); index++) {
                  BlockPositionProvider blockPositionProviderComponent = archetypeChunk.getComponent(index, BlockPositionProvider.getComponentType());

                  assert blockPositionProviderComponent != null;

                  ChunkSection chunkSectionComponent = archetypeChunk.getComponent(index, ChunkSection.getComponentType());

                  assert chunkSectionComponent != null;

                  int x = chunkSectionComponent.getX();
                  int z = chunkSectionComponent.getZ();
                  sb.append(' ').append(x).append(", ").append(chunkSectionComponent.getY()).append(", ").append(z);
                  int[] entryCount = new int[]{0};
                  blockPositionProviderComponent.forEachBlockSet((set, data) -> entryCount[0] += data.size());
                  sb.append(" (")
                     .append(entryCount[0])
                     .append(" entries, ")
                     .append(blockPositionProviderComponent.getSearchedBlockSets().cardinality())
                     .append(" BlockSets)\n");
                  msg.insert(
                     Message.translation("server.commands.npc.blackboard.chunks.detailed_entry")
                        .param("x", x)
                        .param("y", chunkSectionComponent.getY())
                        .param("z", z)
                        .param("count", entryCount[0])
                        .param("blockSets", blockPositionProviderComponent.getSearchedBlockSets().cardinality())
                  );
               }
            }
         );
         context.sendMessage(msg);
         NPCPlugin.get().getLogger().at(Level.INFO).log(sb.toString());
      }
   }

   public static class DropCommand extends AbstractWorldCommand {
      public DropCommand() {
         super("drop", "server.commands.npc.blackboard.drop.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         store.getResource(Blackboard.getResourceType()).clear();
         context.sendMessage(Message.translation("server.commands.npc.blackboard.cleared"));
      }
   }

   public static class EntityEventsCommand extends AbstractWorldCommand {
      public EntityEventsCommand() {
         super("entityevents", "server.commands.npc.blackboard.entityevents.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         EntityEventView view = store.getResource(Blackboard.getResourceType()).getView(EntityEventView.class, 0L);
         StringBuilder sb = new StringBuilder("Entity Event View:\n");
         sb.append(" Total NPCGroups: ").append(view.getSetCount());
         sb.append("\n NPCGroups:\n");
         Message msg = Message.translation("server.commands.npc.blackboard.entityevents.title").param("count", view.getSetCount());
         view.forEach((b, t) -> {
            sb.append("  ").append(NPCGroup.getAssetMap().getAsset(b).getId()).append(" (").append(t.get()).append("):\n");
            msg.insert("  " + NPCGroup.getAssetMap().getAsset(b).getId() + " (" + t.get() + "):\n");
         }, e -> {
            UUIDComponent uuidComponent = store.getComponent(e, UUIDComponent.getComponentType());

            assert uuidComponent != null;

            UUID uuid = uuidComponent.getUuid();
            NPCEntity npcComponent = store.getComponent(e, NPCEntity.getComponentType());

            assert npcComponent != null;

            String roleName = npcComponent.getRoleName();
            sb.append("   ").append(uuid).append(": ").append(roleName).append("\n");
            msg.insert("   " + uuid + ": " + roleName + "\n");
         });
         context.sendMessage(msg);
         NPCPlugin.get().getLogger().at(Level.INFO).log(sb.toString());
      }
   }

   public static class ReservationCommand extends AbstractPlayerCommand {
      @Nonnull
      private final EntityWrappedArg entityArg = this.withOptionalArg("entity", "server.commands.entity.entity.desc", ArgTypes.ENTITY_ID);

      public ReservationCommand() {
         super("reservation", "server.commands.npc.blackboard.reservation.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Ref<EntityStore> npcRef = this.getNPCRef(context, store);
         if (npcRef != null) {
            NPCEntity npcEntity = store.getComponent(npcRef, NPCEntity.getComponentType());

            assert npcEntity != null;

            Blackboard blackBoardResource = store.getResource(Blackboard.getResourceType());
            InteractionView reservationView = blackBoardResource.getView(InteractionView.class, 0L);
            ReservationStatus reservationStatus = reservationView.getReservationStatus(npcRef, ref, store);
            context.sendMessage(Message.translation("server.commands.npc.blackboard.reservationStatus").param("status", reservationStatus.toString()));
         }
      }

      @Nullable
      private Ref<EntityStore> getNPCRef(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store) {
         Ref<EntityStore> ref;
         if (this.entityArg.provided(context)) {
            ref = this.entityArg.get(store, context);
         } else {
            Ref<EntityStore> playerRef = context.senderAsPlayerRef();
            if (playerRef == null || !playerRef.isValid()) {
               context.sendMessage(Message.translation("server.commands.errors.playerOrArg").param("option", "entity"));
               return null;
            }

            ref = TargetUtil.getTargetEntity(playerRef, store);
            if (ref == null) {
               context.sendMessage(Message.translation("server.commands.errors.no_entity_in_view").param("option", "entity"));
               return null;
            }
         }

         if (ref == null) {
            return null;
         } else {
            NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());
            if (npcComponent == null) {
               UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

               assert uuidComponent != null;

               UUID uuid = uuidComponent.getUuid();
               context.sendMessage(Message.translation("server.commands.errors.not_npc").param("uuid", uuid.toString()));
               return null;
            } else {
               return ref;
            }
         }
      }
   }

   public static class ReserveCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<Boolean> reserveArg = this.withRequiredArg("reserve", "server.commands.npc.blackboard.reserve.reserve.desc", ArgTypes.BOOLEAN);
      @Nonnull
      private final EntityWrappedArg entityArg = this.withOptionalArg("entity", "server.commands.entity.entity.desc", ArgTypes.ENTITY_ID);

      public ReserveCommand() {
         super("reserve", "server.commands.npc.blackboard.reserve.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

         assert uuidComponent != null;

         UUID playerUUID = uuidComponent.getUuid();
         Ref<EntityStore> npcRef = this.getNPCRef(context, store);
         if (npcRef != null) {
            NPCEntity npcEntity = store.getComponent(npcRef, NPCEntity.getComponentType());

            assert npcEntity != null;

            if (this.reserveArg.get(context)) {
               npcEntity.addReservation(playerUUID);
               context.sendMessage(Message.translation("server.commands.npc.blackboard.roleReserved").param("role", npcEntity.getRoleName()));
            } else {
               npcEntity.removeReservation(playerUUID);
               context.sendMessage(Message.translation("server.commands.npc.blackboard.roleReleased").param("role", npcEntity.getRoleName()));
            }
         }
      }

      @Nullable
      private Ref<EntityStore> getNPCRef(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store) {
         Ref<EntityStore> ref;
         if (this.entityArg.provided(context)) {
            ref = this.entityArg.get(store, context);
         } else {
            Ref<EntityStore> playerRef = context.senderAsPlayerRef();
            if (playerRef == null || !playerRef.isValid()) {
               context.sendMessage(Message.translation("server.commands.errors.playerOrArg").param("option", "entity"));
               return null;
            }

            ref = TargetUtil.getTargetEntity(playerRef, store);
            if (ref == null) {
               context.sendMessage(Message.translation("server.commands.errors.no_entity_in_view").param("option", "entity"));
               return null;
            }
         }

         if (ref == null) {
            return null;
         } else {
            NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());
            if (npcComponent == null) {
               UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

               assert uuidComponent != null;

               UUID uuid = uuidComponent.getUuid();
               context.sendMessage(Message.translation("server.commands.errors.not_npc").param("uuid", uuid.toString()));
               return null;
            } else {
               return ref;
            }
         }
      }
   }

   public static class ResourceViewCommand extends AbstractWorldCommand {
      @Nonnull
      private final RequiredArg<RelativeChunkPosition> chunkArg = this.withRequiredArg(
         "chunk", "server.commands.npc.blackboard.resourceview.chunk.desc", ArgTypes.RELATIVE_CHUNK_POSITION
      );

      public ResourceViewCommand() {
         super("resourceview", "server.commands.npc.blackboard.resourceview.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         Vector2i chunkPosition = this.chunkArg.get(context).getChunkPosition(context, store);
         long viewIndex = ResourceView.indexView(chunkPosition.x, chunkPosition.y);
         Blackboard blackboard = store.getResource(Blackboard.getResourceType());
         ResourceView view = blackboard.getView(ResourceView.class, viewIndex);
         int viewX = ResourceView.xOfViewIndex(viewIndex);
         int viewZ = ResourceView.zOfViewIndex(viewIndex);
         StringBuilder sb = new StringBuilder("View (");
         sb.append(viewX).append(", ").append(viewZ).append(")\n");
         sb.append(" Spans world coordinates: (").append(ResourceView.toWorldCoordinate(viewX)).append(", ").append(ResourceView.toWorldCoordinate(viewZ));
         sb.append(") to (").append(ResourceView.toWorldCoordinate(viewX + 1)).append(", ").append(ResourceView.toWorldCoordinate(viewZ + 1)).append(")\n");
         Message msg = Message.translation("server.commands.npc.blackboard.view.title").param("x", viewX).param("z", viewZ);
         msg.insert(
            Message.translation("server.commands.npc.blackboard.view.coordinates")
               .param("x1", ResourceView.toWorldCoordinate(viewX))
               .param("z1", ResourceView.toWorldCoordinate(viewZ))
               .param("x2", ResourceView.toWorldCoordinate(viewX + 1))
               .param("z2", ResourceView.toWorldCoordinate(viewZ + 1))
         );
         msg.insert("\n");
         if (view == null) {
            sb.append(" No resource view exists");
            msg.insert(Message.translation("server.commands.npc.blackboard.resourceview.noResourceView"));
         } else {
            sb.append(" Reservations: [ ").append('\n');
            msg.insert(Message.translation("server.commands.npc.blackboard.resourceview.reservations"));
            view.getReservationsByEntity()
               .forEach(
                  (ref, reservation) -> {
                     if (!ref.isValid()) {
                        sb.append("!!!INVALID ENTITY!!!");
                        msg.insert(Message.translation("server.commands.npc.blackboard.view.invalidEntity"));
                     } else {
                        UUIDComponent uuidComponent = store.getComponent((Ref<EntityStore>)ref, UUIDComponent.getComponentType());

                        assert uuidComponent != null;

                        UUID uuid = uuidComponent.getUuid();
                        sb.append("  ").append(uuid).append(": ");
                        msg.insert("  " + uuid + ": ");
                        NPCEntity npc = store.getComponent((Ref<EntityStore>)ref, NPCEntity.getComponentType());
                        if (npc == null) {
                           sb.append("!!!NON-NPC ENTITY!!!");
                           msg.insert(Message.translation("server.commands.npc.blackboard.view.nonNpcEntity"));
                        } else {
                           sb.append(npc.getRoleName());
                           msg.insert(npc.getRoleName());
                        }

                        int blockIndex = reservation.getBlockIndex();
                        int blockX = ResourceView.xFromIndex(blockIndex) + (viewX << 7);
                        int blockY = ResourceView.yFromIndex(blockIndex) + (reservation.getSectionIndex() << 5);
                        int blockZ = ResourceView.zFromIndex(blockIndex) + (viewZ << 7);
                        BlockType blockType = BlockType.getAssetMap().getAsset(world.getBlock(blockX, blockY, blockZ));
                        sb.append(" reserved block ")
                           .append(blockType.getId())
                           .append(" at ")
                           .append(blockX)
                           .append(", ")
                           .append(blockY)
                           .append(", ")
                           .append(blockZ)
                           .append('\n');
                        msg.insert(
                           Message.translation("server.commands.npc.blackboard.resourceview.reservedBlock")
                              .param("name", blockType.getId())
                              .param("x", blockX)
                              .param("y", blockY)
                              .param("z", blockZ)
                        );
                     }
                  }
               );
            sb.append(" ]");
            msg.insert(" ]");
         }

         context.sendMessage(msg);
         NPCPlugin.get().getLogger().at(Level.INFO).log(sb.toString());
      }
   }

   public static class ResourceViewsCommand extends AbstractWorldCommand {
      public ResourceViewsCommand() {
         super("resourceviews", "server.commands.npc.blackboard.resourceviews.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         StringBuilder sb = new StringBuilder("Resource views:\n");
         Message msg = Message.translation("server.commands.npc.blackboard.resourceviews.title");
         Blackboard blackboard = store.getResource(Blackboard.getResourceType());
         int[] count = new int[]{0};
         blackboard.forEachView(ResourceView.class, entry -> count[0]++);
         sb.append(" Total resource views: ").append(count[0]).append('\n').append(" Views:\n");
         msg.insert(Message.translation("server.commands.npc.blackboard.resourceviews.totalViews").param("count", count[0]));
         blackboard.forEachView(
            ResourceView.class,
            entry -> {
               sb.append("  View (").append(ResourceView.xOfViewIndex(entry.getIndex())).append(", ").append(ResourceView.zOfViewIndex(entry.getIndex()));
               sb.append(") Reservations: ").append(entry.getReservationsByEntity().size()).append('\n');
               msg.insert(
                  Message.translation("server.commands.npc.blackboard.resourceviews.view")
                     .param("x", ResourceView.xOfViewIndex(entry.getIndex()))
                     .param("z", ResourceView.zOfViewIndex(entry.getIndex()))
                     .param("count", entry.getReservationsByEntity().size())
               );
            }
         );
         context.sendMessage(msg);
         NPCPlugin.get().getLogger().at(Level.INFO).log(sb.toString());
      }
   }

   public static class ViewCommand extends AbstractWorldCommand {
      @Nonnull
      private final RequiredArg<RelativeChunkPosition> chunkArg = this.withRequiredArg(
         "chunk", "server.commands.npc.blackboard.view.chunk.desc", ArgTypes.RELATIVE_CHUNK_POSITION
      );

      public ViewCommand() {
         super("view", "server.commands.npc.blackboard.view.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         Vector2i chunkPosition = this.chunkArg.get(context).getChunkPosition(context, store);
         long viewIndex = BlockTypeView.indexView(chunkPosition.x, chunkPosition.y);
         Blackboard blackboard = store.getResource(Blackboard.getResourceType());
         BlockTypeView view = blackboard.getView(BlockTypeView.class, viewIndex);
         int viewX = BlockTypeView.xOfViewIndex(viewIndex);
         int viewZ = BlockTypeView.zOfViewIndex(viewIndex);
         StringBuilder sb = new StringBuilder("View (");
         sb.append(viewX).append(", ").append(viewZ).append(")\n");
         sb.append(" Spans world coordinates: (").append(BlockTypeView.toWorldCoordinate(viewX)).append(", ").append(BlockTypeView.toWorldCoordinate(viewZ));
         sb.append(") to (").append(BlockTypeView.toWorldCoordinate(viewX + 1)).append(", ").append(BlockTypeView.toWorldCoordinate(viewZ + 1)).append(")\n");
         Message msg = Message.translation("server.commands.npc.blackboard.view.title").param("x", viewX).param("z", viewZ);
         msg.insert(
            Message.translation("server.commands.npc.blackboard.view.coordinates")
               .param("x1", BlockTypeView.toWorldCoordinate(viewX))
               .param("z1", BlockTypeView.toWorldCoordinate(viewZ))
               .param("x2", BlockTypeView.toWorldCoordinate(viewX + 1))
               .param("z2", BlockTypeView.toWorldCoordinate(viewZ + 1))
         );
         msg.insert("\n");
         if (view == null) {
            sb.append(" No partial view exists");
            msg.insert(Message.translation("server.commands.npc.blackboard.view.noPartialViews"));
         } else {
            sb.append(" Searched BlockSets: [ ");
            msg.insert(Message.translation("server.commands.npc.blockSetsSearched"));
            BitSet searchedBlockSets = view.getAllBlockSets();
            Int2IntMap counts = view.getBlockSetCounts();
            boolean subsequent = false;

            for (int i = searchedBlockSets.nextSetBit(0); i >= 0; i = searchedBlockSets.nextSetBit(i + 1)) {
               if (subsequent) {
                  sb.append(", ");
                  msg.insert(", ");
               }

               sb.append(BlockSet.getAssetMap().getAsset(i).getId()).append(" (").append(counts.getOrDefault(i, 0)).append(')');
               msg.insert(BlockSet.getAssetMap().getAsset(i).getId() + " (" + counts.getOrDefault(i, 0) + ")");
               subsequent = true;
               if (i == Integer.MAX_VALUE) {
                  break;
               }
            }

            Set<Ref<EntityStore>> entities = view.getEntities();
            sb.append(" ]\n Entities (").append(entities.size()).append("):\n");
            msg.insert(Message.translation("server.commands.npc.blackboard.view.entities").param("count", entities.size()));
            entities.forEach(ref -> {
               sb.append("  [").append(ref.getIndex()).append("] ");
               msg.insert("  [" + ref.getIndex() + "] ");
               if (!ref.isValid()) {
                  sb.append("!!!INVALID ENTITY!!!");
                  msg.insert(Message.translation("server.commands.npc.blackboard.view.invalidEntity"));
               } else {
                  NPCEntity npc = store.getComponent((Ref<EntityStore>)ref, NPCEntity.getComponentType());
                  if (npc == null) {
                     sb.append("!!!NON-NPC ENTITY!!!\n");
                     msg.insert(Message.translation("server.commands.npc.blackboard.view.nonNpcEntity"));
                  } else {
                     sb.append(npc.getRoleName()).append("\n    BlockSets: [ ");
                     msg.insert(Message.translation("server.commands.npc.blackboard.view.blockSets"));
                     IntList blockSets = npc.getBlackboardBlockTypeSets();

                     for (int i = 0; i < blockSets.size(); i++) {
                        if (i > 0) {
                           sb.append(", ");
                           msg.insert(", ");
                        }

                        String blockSetId = BlockSet.getAssetMap().getAsset(blockSets.getInt(i)).getId();
                        sb.append(blockSetId);
                        msg.insert(blockSetId);
                     }

                     sb.append(" ]\n");
                     msg.insert(" ]\n");
                  }
               }
            });
         }

         context.sendMessage(msg);
         NPCPlugin.get().getLogger().at(Level.INFO).log(sb.toString());
      }
   }

   public static class ViewsCommand extends AbstractWorldCommand {
      public ViewsCommand() {
         super("views", "server.commands.npc.blackboard.views.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         StringBuilder sb = new StringBuilder("Blackboard views:\n");
         Message msg = Message.translation("server.commands.npc.blackboard.views.title");
         Blackboard blackboard = store.getResource(Blackboard.getResourceType());
         int[] count = new int[]{0};
         blackboard.forEachView(BlockTypeView.class, entry -> count[0]++);
         sb.append(" Total partial views: ").append(count[0]).append('\n').append(" Views:\n");
         msg.insert(Message.translation("server.commands.npc.blackboard.views.partialViews").param("count", count[0]));
         msg.insert("\n");
         blackboard.forEachView(
            BlockTypeView.class,
            entry -> {
               sb.append("  View (").append(BlockTypeView.xOfViewIndex(entry.getIndex())).append(", ").append(BlockTypeView.zOfViewIndex(entry.getIndex()));
               sb.append(") Entities: ").append(entry.getEntities().size()).append(", BlockSets: ").append(entry.getAllBlockSets().cardinality()).append('\n');
               msg.insert(
                  Message.translation("server.commands.npc.blackboard.views.view")
                     .param("x", BlockTypeView.xOfViewIndex(entry.getIndex()))
                     .param("z", BlockTypeView.zOfViewIndex(entry.getIndex()))
                     .param("size", entry.getEntities().size())
                     .param("cardinal", entry.getAllBlockSets().cardinality())
               );
               msg.insert("\n");
            }
         );
         context.sendMessage(msg);
         NPCPlugin.get().getLogger().at(Level.INFO).log(sb.toString());
      }
   }
}
