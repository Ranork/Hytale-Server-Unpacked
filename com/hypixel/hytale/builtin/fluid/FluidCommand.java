package com.hypixel.hytale.builtin.fluid;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.AssetArgumentType;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeIntPosition;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import javax.annotation.Nonnull;

public class FluidCommand extends AbstractCommandCollection {
   @Nonnull
   private static final SingleArgumentType<Fluid> FLUID_ARG = new AssetArgumentType("Fluid", Fluid.class, "server.commands.fluid.fluidArgType.desc");

   public FluidCommand() {
      super("fluid", "server.commands.fluid.desc");
      this.addSubCommand(new FluidCommand.SetCommand());
      this.addSubCommand(new FluidCommand.GetCommand());
      this.addSubCommand(new FluidCommand.SetRadiusCommand());
   }

   public static class GetCommand extends AbstractPlayerCommand {
      @Nonnull
      private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_LOOKING_AT_BLOCK = Message.translation("server.commands.errors.playerNotLookingAtBlock");
      @Nonnull
      private static final Message MESSAGE_COMMANDS_NO_SECTION_COMPONENT = Message.translation("server.commands.noSectionComponent");
      @Nonnull
      private final OptionalArg<RelativeIntPosition> targetOffset = this.withOptionalArg(
         "offset", "server.commands.fluid.get.offset.desc", ArgTypes.RELATIVE_BLOCK_POSITION
      );

      public GetCommand() {
         super("get", "server.commands.fluid.get.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         RelativeIntPosition offset = this.targetOffset.get(context);
         Vector3i blockTarget = TargetUtil.getTargetBlock(ref, 8.0, store);
         if (blockTarget == null) {
            playerRef.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_LOOKING_AT_BLOCK);
         } else {
            ChunkStore chunkStore = world.getChunkStore();
            Vector3i pos = offset == null ? blockTarget : offset.getBlockPosition(blockTarget.toVector3d(), chunkStore);
            chunkStore.getChunkSectionReferenceAsync(ChunkUtil.chunkCoordinate(pos.x), ChunkUtil.chunkCoordinate(pos.y), ChunkUtil.chunkCoordinate(pos.z))
               .thenAcceptAsync(
                  section -> {
                     Store<ChunkStore> sectionStore = section.getStore();
                     FluidSection fluidSection = sectionStore.getComponent((Ref<ChunkStore>)section, FluidSection.getComponentType());
                     if (fluidSection == null) {
                        playerRef.sendMessage(MESSAGE_COMMANDS_NO_SECTION_COMPONENT);
                     } else {
                        int index = ChunkUtil.indexBlock(pos.x, pos.y, pos.z);
                        Fluid fluid = fluidSection.getFluid(index);
                        byte level = fluidSection.getFluidLevel(index);
                        playerRef.sendMessage(
                           Message.translation("server.commands.get.success")
                              .param("x", pos.x)
                              .param("y", pos.y)
                              .param("z", pos.z)
                              .param("id", fluid.getId())
                              .param("level", (int)level)
                        );
                     }
                  },
                  world
               );
         }
      }
   }

   public static class SetCommand extends AbstractPlayerCommand {
      @Nonnull
      private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_LOOKING_AT_BLOCK = Message.translation("server.commands.errors.playerNotLookingAtBlock");
      @Nonnull
      private static final Message MESSAGE_COMMANDS_SET_UNKNOWN_FLUID = Message.translation("server.commands.set.unknownFluid");
      @Nonnull
      private static final Message MESSAGE_COMMANDS_NO_SECTION_COMPONENT = Message.translation("server.commands.noSectionComponent");
      @Nonnull
      private final RequiredArg<Fluid> fluid = this.withRequiredArg("fluid", "server.commands.fluid.set.fluid.desc", FluidCommand.FLUID_ARG);
      @Nonnull
      private final RequiredArg<Integer> level = this.withRequiredArg("level", "server.commands.fluid.set.level.desc", ArgTypes.INTEGER);
      @Nonnull
      private final OptionalArg<RelativeIntPosition> targetOffset = this.withOptionalArg(
         "offset", "server.commands.fluid.set.offset.desc", ArgTypes.RELATIVE_BLOCK_POSITION
      );

      public SetCommand() {
         super("set", "server.commands.fluid.set.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         RelativeIntPosition offset = this.targetOffset.get(context);
         Vector3i blockTarget = TargetUtil.getTargetBlock(ref, 8.0, store);
         if (blockTarget == null) {
            playerRef.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_LOOKING_AT_BLOCK);
         } else {
            ChunkStore chunkStore = world.getChunkStore();
            Vector3i pos = offset == null ? blockTarget : offset.getBlockPosition(blockTarget.toVector3d(), chunkStore);
            Fluid fluid = this.fluid.get(context);
            if (fluid == null) {
               playerRef.sendMessage(MESSAGE_COMMANDS_SET_UNKNOWN_FLUID);
            } else {
               Integer level = this.level.get(context);
               if (level > fluid.getMaxFluidLevel()) {
                  level = fluid.getMaxFluidLevel();
                  playerRef.sendMessage(Message.translation("server.commands.set.maxFluidLevelClamped").param("level", fluid.getMaxFluidLevel()));
               }

               Integer finalLevel = level;
               chunkStore.getChunkSectionReferenceAsync(ChunkUtil.chunkCoordinate(pos.x), ChunkUtil.chunkCoordinate(pos.y), ChunkUtil.chunkCoordinate(pos.z))
                  .thenAcceptAsync(
                     section -> {
                        Store<ChunkStore> sectionStore = section.getStore();
                        FluidSection fluidSection = sectionStore.getComponent((Ref<ChunkStore>)section, FluidSection.getComponentType());
                        if (fluidSection == null) {
                           playerRef.sendMessage(MESSAGE_COMMANDS_NO_SECTION_COMPONENT);
                        } else {
                           int index = ChunkUtil.indexBlock(pos.x, pos.y, pos.z);
                           fluidSection.setFluid(index, fluid, finalLevel.byteValue());
                           playerRef.sendMessage(
                              Message.translation("server.commands.set.success")
                                 .param("x", pos.x)
                                 .param("y", pos.y)
                                 .param("z", pos.z)
                                 .param("id", fluid.getId())
                                 .param("level", finalLevel)
                           );
                           ChunkSection chunkSection = sectionStore.getComponent((Ref<ChunkStore>)section, ChunkSection.getComponentType());
                           WorldChunk worldChunk = sectionStore.getComponent(chunkSection.getChunkColumnReference(), WorldChunk.getComponentType());
                           worldChunk.markNeedsSaving();
                           worldChunk.setTicking(pos.x, pos.y, pos.z, true);
                        }
                     },
                     world
                  );
            }
         }
      }
   }

   public static class SetRadiusCommand extends AbstractPlayerCommand {
      @Nonnull
      private static final Message MESSAGE_COMMANDS_SET_UNKNOWN_FLUID = Message.translation("server.commands.set.unknownFluid");
      @Nonnull
      private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_LOOKING_AT_BLOCK = Message.translation("server.commands.errors.playerNotLookingAtBlock");
      @Nonnull
      private final RequiredArg<Integer> radius = this.withRequiredArg("radius", "server.commands.fluid.setradius.radius.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Fluid> fluid = this.withRequiredArg("fluid", "server.commands.fluid.setradius.fluid.desc", FluidCommand.FLUID_ARG);
      @Nonnull
      private final RequiredArg<Integer> level = this.withRequiredArg("level", "server.commands.fluid.setradius.level.desc", ArgTypes.INTEGER);
      @Nonnull
      private final OptionalArg<RelativeIntPosition> targetOffset = this.withOptionalArg(
         "offset", "server.commands.fluid.setradius.offset.desc", ArgTypes.RELATIVE_BLOCK_POSITION
      );

      public SetRadiusCommand() {
         super("setradius", "server.commands.fluid.setradius.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         RelativeIntPosition offset = this.targetOffset.get(context);
         Vector3i blockTarget = TargetUtil.getTargetBlock(ref, 8.0, store);
         if (blockTarget == null) {
            playerRef.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_LOOKING_AT_BLOCK);
         } else {
            ChunkStore chunkStore = world.getChunkStore();
            Vector3i pos = offset == null ? blockTarget : offset.getBlockPosition(blockTarget.toVector3d(), chunkStore);
            Fluid fluid = this.fluid.get(context);
            if (fluid == null) {
               playerRef.sendMessage(MESSAGE_COMMANDS_SET_UNKNOWN_FLUID);
            } else {
               Integer level = this.level.get(context);
               if (level > fluid.getMaxFluidLevel()) {
                  level = fluid.getMaxFluidLevel();
                  playerRef.sendMessage(Message.translation("server.commands.set.maxFluidLevelClamped").param("level", fluid.getMaxFluidLevel()));
               }

               Integer radius = this.radius.get(context);
               int minX = pos.x - radius;
               int maxX = pos.x + radius;
               int minY = pos.y - radius;
               int maxY = pos.y + radius;
               int minZ = pos.z - radius;
               int maxZ = pos.z + radius;
               int minCX = ChunkUtil.chunkCoordinate(minX);
               int maxCX = ChunkUtil.chunkCoordinate(maxX);
               int minCY = ChunkUtil.chunkCoordinate(minY);
               int maxCY = ChunkUtil.chunkCoordinate(maxY);
               int minCZ = ChunkUtil.chunkCoordinate(minZ);
               int maxCZ = ChunkUtil.chunkCoordinate(maxZ);
               byte levelByteValue = level.byteValue();

               for (int cx = minCX; cx <= maxCX; cx++) {
                  for (int cz = minCZ; cz <= maxCZ; cz++) {
                     int minBlockX = ChunkUtil.minBlock(cx);
                     int minBlockZ = ChunkUtil.minBlock(cz);
                     int relMinX = MathUtil.clamp(minX - minBlockX, 0, 32);
                     int relMaxX = MathUtil.clamp(maxX - minBlockX, 0, 32);
                     int relMinZ = MathUtil.clamp(minZ - minBlockZ, 0, 32);
                     int relMaxZ = MathUtil.clamp(maxZ - minBlockZ, 0, 32);

                     for (int cy = minCY; cy <= maxCY; cy++) {
                        chunkStore.getChunkSectionReferenceAsync(cx, cy, cz)
                           .thenAcceptAsync(
                              section -> {
                                 Store<ChunkStore> sectionStore = section.getStore();
                                 FluidSection fluidSectionComponent = sectionStore.getComponent((Ref<ChunkStore>)section, FluidSection.getComponentType());
                                 if (fluidSectionComponent != null) {
                                    ChunkSection chunkSectionComponent = sectionStore.getComponent((Ref<ChunkStore>)section, ChunkSection.getComponentType());
                                    if (chunkSectionComponent != null) {
                                       WorldChunk worldChunkComponent = sectionStore.getComponent(
                                          chunkSectionComponent.getChunkColumnReference(), WorldChunk.getComponentType()
                                       );
                                       if (worldChunkComponent != null) {
                                          int relMinY = MathUtil.clamp(minY - ChunkUtil.minBlock(fluidSectionComponent.getY()), 0, 32);
                                          int relMaxY = MathUtil.clamp(maxY - ChunkUtil.minBlock(fluidSectionComponent.getY()), 0, 32);

                                          for (int y = relMinY; y < relMaxY; y++) {
                                             for (int z = relMinZ; z < relMaxZ; z++) {
                                                for (int x = relMinX; x < relMaxX; x++) {
                                                   int blockIndex = ChunkUtil.indexBlock(x, y, z);
                                                   fluidSectionComponent.setFluid(blockIndex, fluid, levelByteValue);
                                                   worldChunkComponent.setTicking(pos.x, pos.y, pos.z, true);
                                                }
                                             }
                                          }

                                          worldChunkComponent.markNeedsSaving();
                                       }
                                    }
                                 }
                              },
                              world
                           );
                     }
                  }
               }
            }
         }
      }
   }
}
