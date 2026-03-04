package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OpenCustomUIInteraction extends SimpleInstantInteraction {
   public static final CodecMapCodec<OpenCustomUIInteraction.CustomPageSupplier> PAGE_CODEC = new CodecMapCodec<>();
   public static final BuilderCodec<OpenCustomUIInteraction> CODEC = BuilderCodec.builder(
         OpenCustomUIInteraction.class, OpenCustomUIInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Opens a custom ui page.")
      .<OpenCustomUIInteraction.CustomPageSupplier>appendInherited(
         new KeyedCodec<>("Page", PAGE_CODEC),
         (o, v) -> o.customPageSupplier = v,
         o -> o.customPageSupplier,
         (o, p) -> o.customPageSupplier = p.customPageSupplier
      )
      .addValidator(Validators.nonNull())
      .add()
      .build();
   private OpenCustomUIInteraction.CustomPageSupplier customPageSupplier;

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> ref = context.getEntity();
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         PageManager pageManager = playerComponent.getPageManager();
         if (pageManager.getCustomPage() == null) {
            PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());

            assert playerRef != null;

            CustomUIPage page = this.customPageSupplier.tryCreate(ref, commandBuffer, playerRef, context);
            if (page != null) {
               Store<EntityStore> store = commandBuffer.getStore();
               pageManager.openCustomPage(ref, store, page);
            }
         }
      }
   }

   public static <S extends OpenCustomUIInteraction.CustomPageSupplier> void registerCustomPageSupplier(
      @Nonnull PluginBase plugin, Class<?> tClass, String id, @Nonnull S supplier
   ) {
      plugin.getCodecRegistry(PAGE_CODEC)
         .register(
            id,
            (Class<? extends OpenCustomUIInteraction.CustomPageSupplier>)supplier.getClass(),
            (Codec<? extends OpenCustomUIInteraction.CustomPageSupplier>)BuilderCodec.builder(tClass, () -> supplier).build()
         );
   }

   public static void registerSimple(@Nonnull PluginBase plugin, Class<?> tClass, String id, @Nonnull Function<PlayerRef, CustomUIPage> supplier) {
      registerCustomPageSupplier(plugin, tClass, id, (ref, componentAccessor, playerRef, context) -> supplier.apply(playerRef));
   }

   @Deprecated
   public static <T extends BlockState> void registerBlockCustomPage(
      @Nonnull PluginBase plugin,
      Class<?> tClass,
      String id,
      @Nonnull Class<T> stateClass,
      @Nonnull OpenCustomUIInteraction.BlockCustomPageSupplier<T> blockSupplier
   ) {
      registerBlockCustomPage(plugin, tClass, id, stateClass, blockSupplier, false);
   }

   @Deprecated
   public static <T extends BlockState> void registerBlockCustomPage(
      @Nonnull PluginBase plugin,
      Class<?> tClass,
      String id,
      @Nonnull Class<T> stateClass,
      @Nonnull OpenCustomUIInteraction.BlockCustomPageSupplier<T> blockSupplier,
      boolean createState
   ) {
      OpenCustomUIInteraction.CustomPageSupplier supplier = (ref, componentAccessor, playerRef, context) -> {
         BlockPosition targetBlock = context.getTargetBlock();
         if (targetBlock == null) {
            return null;
         } else {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            BlockState state = world.getState(targetBlock.x, targetBlock.y, targetBlock.z, true);
            if (state == null) {
               if (createState) {
                  WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
                  state = BlockStateModule.get()
                     .createBlockState(
                        stateClass,
                        chunk,
                        new Vector3i(targetBlock.x, targetBlock.y, targetBlock.z),
                        chunk.getBlockType(targetBlock.x, targetBlock.y, targetBlock.z)
                     );
                  chunk.setState(targetBlock.x, targetBlock.y, targetBlock.z, state);
               }

               if (state == null) {
                  return null;
               }
            }

            return stateClass.isInstance(state) ? blockSupplier.tryCreate(playerRef, stateClass.cast(state)) : null;
         }
      };
      registerCustomPageSupplier(plugin, tClass, id, supplier);
   }

   public static void registerBlockEntityCustomPage(
      @Nonnull PluginBase plugin, Class<?> tClass, String id, @Nonnull OpenCustomUIInteraction.BlockEntityCustomPageSupplier blockSupplier
   ) {
      OpenCustomUIInteraction.CustomPageSupplier supplier = (ref, componentAccessor, playerRef, context) -> {
         BlockPosition targetBlock = context.getTargetBlock();
         if (targetBlock == null) {
            return null;
         } else {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
            if (chunk == null) {
               return null;
            } else {
               BlockPosition targetBaseBlock = world.getBaseBlock(targetBlock);
               Ref<ChunkStore> blockEntityRef = chunk.getBlockComponentEntity(targetBaseBlock.x, targetBaseBlock.y, targetBaseBlock.z);
               return blockEntityRef == null ? null : blockSupplier.tryCreate(playerRef, blockEntityRef);
            }
         }
      };
      registerCustomPageSupplier(plugin, tClass, id, supplier);
   }

   public static void registerBlockEntityCustomPage(
      @Nonnull PluginBase plugin,
      Class<?> tClass,
      String id,
      @Nonnull OpenCustomUIInteraction.BlockEntityCustomPageSupplier blockSupplier,
      Supplier<Holder<ChunkStore>> creator
   ) {
      OpenCustomUIInteraction.CustomPageSupplier supplier = (ref, componentAccessor, playerRef, context) -> {
         BlockPosition targetBlock = context.getTargetBlock();
         if (targetBlock == null) {
            return null;
         } else {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z);
            WorldChunk worldChunkComponent = world.getChunkIfInMemory(chunkIndex);
            if (worldChunkComponent == null) {
               return null;
            } else {
               BlockPosition targetBaseBlock = world.getBaseBlock(targetBlock);
               BlockComponentChunk blockComponentChunk = worldChunkComponent.getBlockComponentChunk();
               int index = ChunkUtil.indexBlockInColumn(targetBaseBlock.x, targetBaseBlock.y, targetBaseBlock.z);
               Ref<ChunkStore> blockEntityRef = blockComponentChunk.getEntityReference(index);
               if (blockEntityRef == null || !blockEntityRef.isValid()) {
                  Holder<ChunkStore> holder = creator.get();
                  holder.putComponent(BlockModule.BlockStateInfo.getComponentType(), new BlockModule.BlockStateInfo(index, worldChunkComponent.getReference()));
                  blockEntityRef = world.getChunkStore().getStore().addEntity(holder, AddReason.SPAWN);
               }

               return blockSupplier.tryCreate(playerRef, blockEntityRef);
            }
         }
      };
      registerCustomPageSupplier(plugin, tClass, id, supplier);
   }

   @FunctionalInterface
   public interface BlockCustomPageSupplier<T extends BlockState> {
      CustomUIPage tryCreate(@Nonnull PlayerRef var1, @Nonnull T var2);
   }

   @FunctionalInterface
   public interface BlockEntityCustomPageSupplier {
      CustomUIPage tryCreate(@Nonnull PlayerRef var1, @Nonnull Ref<ChunkStore> var2);
   }

   @FunctionalInterface
   public interface CustomPageSupplier {
      @Nullable
      CustomUIPage tryCreate(
         @Nonnull Ref<EntityStore> var1, @Nonnull ComponentAccessor<EntityStore> var2, @Nonnull PlayerRef var3, @Nonnull InteractionContext var4
      );
   }
}
