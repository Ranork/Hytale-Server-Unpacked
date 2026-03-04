package com.hypixel.hytale.server.core.command.commands.world.chunk;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.ChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.accessor.LocalCachedChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javax.annotation.Nonnull;

public class ChunkTintCommand extends AbstractPlayerCommand {
   private static final int BLUR_RADIUS = 5;
   private static final double BLUR_SIGMA = 1.5;
   @Nonnull
   private final RequiredArg<Integer> colorArg = this.withRequiredArg("color", "server.commands.chunk.tint.color.desc", ArgTypes.COLOR);
   @Nonnull
   private final DefaultArg<Integer> radiusArg = this.withDefaultArg(
         "radius", "server.commands.chunk.tint.radius.desc", ArgTypes.INTEGER, 5, "server.commands.chunk.tint.radius.default"
      )
      .addValidator(Validators.greaterThan(0));
   @Nonnull
   private final DefaultArg<Double> sigmaArg = this.withDefaultArg(
         "sigma", "server.commands.chunk.tint.sigma.desc", ArgTypes.DOUBLE, 1.5, "server.commands.chunk.tint.sigma.default"
      )
      .addValidator(Validators.greaterThan(0.0));
   @Nonnull
   private final FlagArg blurArg = this.withFlagArg("blur", "server.commands.chunk.tint.blur.desc");

   public ChunkTintCommand() {
      super("tint", "server.commands.chunk.tint.desc");
      this.addUsageVariant(new ChunkTintCommand.TintChunkPageCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      int color = this.colorArg.get(context);
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      int chunkX = MathUtil.floor(position.getX()) >> 5;
      int chunkZ = MathUtil.floor(position.getZ()) >> 5;
      ChunkStore chunkStore = world.getChunkStore();
      Store<ChunkStore> chunkStoreStore = chunkStore.getStore();
      LongOpenHashSet updateChunks = new LongOpenHashSet();
      int radius = 0;
      double sigma = 0.0;
      long chunkIndex = ChunkUtil.indexChunk(chunkX, chunkZ);
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
      if (chunkRef != null && chunkRef.isValid()) {
         BlockChunk blockChunk = chunkStoreStore.getComponent(chunkRef, BlockChunk.getComponentType());
         if (blockChunk != null) {
            for (int x = 0; x < 32; x++) {
               for (int z = 0; z < 32; z++) {
                  blockChunk.setTint(x, z, color);
               }
            }

            updateChunks.add(chunkIndex);
         }

         if (this.blurArg.provided(context)) {
            radius = this.radiusArg.get(context);
            sigma = this.sigmaArg.get(context);
            double[] matrix = gaussianMatrix(sigma, radius);
            int blockX = chunkX << 5;
            int blockZ = chunkZ << 5;
            Long2IntOpenHashMap newTintMap = new Long2IntOpenHashMap();
            LocalCachedChunkAccessor accessor = LocalCachedChunkAccessor.atWorldCoords(world, blockX, blockZ, 32 + radius * 2);

            for (int x = -radius; x <= 32 + radius; x++) {
               for (int z = -radius; z <= 32 + radius; z++) {
                  int offsetX = blockX + x;
                  int offsetZ = blockZ + z;
                  int blurred = blur(accessor, radius, matrix, offsetX, offsetZ);
                  newTintMap.put(MathUtil.packLong(offsetX, offsetZ), blurred);
               }
            }

            ObjectIterator var37 = newTintMap.long2IntEntrySet().iterator();

            while (var37.hasNext()) {
               Entry entry = (Entry)var37.next();
               long key = entry.getLongKey();
               int x = MathUtil.unpackLeft(key);
               int z = MathUtil.unpackRight(key);
               long chunkIndexx = ChunkUtil.indexChunkFromBlock(x, z);
               Ref<ChunkStore> chunkRefx = chunkStore.getChunkReference(chunkIndexx);
               if (chunkRefx != null && chunkRefx.isValid()) {
                  BlockChunk blockChunkx = chunkStoreStore.getComponent(chunkRefx, BlockChunk.getComponentType());
                  if (blockChunkx != null) {
                     blockChunkx.setTint(x, z, entry.getIntValue());
                     updateChunks.add(chunkIndexx);
                  }
               }
            }
         }

         updateChunks.forEach(chunkIndexx -> world.getNotificationHandler().updateChunk(chunkIndexx));
         if (this.blurArg.provided(context)) {
            context.sendMessage(
               Message.translation("server.commands.chunk.tint.success.blur")
                  .param("chunkX", chunkX)
                  .param("chunkZ", chunkZ)
                  .param("chunksAffected", updateChunks.size())
                  .param("radius", radius)
                  .param("sigma", sigma)
            );
         } else {
            context.sendMessage(Message.translation("server.commands.chunk.tint.success").param("chunkX", chunkX).param("chunkZ", chunkZ));
         }
      } else {
         context.sendMessage(Message.translation("server.general.chunkNotLoaded").param("chunkX", chunkX).param("chunkZ", chunkZ));
      }
   }

   private static int blur(@Nonnull ChunkAccessor<WorldChunk> chunkAccessor, int radius, double[] matrix, int x, int z) {
      double r = 0.0;
      double g = 0.0;
      double b = 0.0;

      for (int ix = -radius; ix <= radius; ix++) {
         for (int iz = -radius; iz <= radius; iz++) {
            double factor = matrix[gaussianIndex(radius, ix, iz)];
            int ax = x + ix;
            int az = z + iz;
            WorldChunk worldChunk = chunkAccessor.getChunk(ChunkUtil.indexChunkFromBlock(ax, az));
            if (worldChunk != null) {
               BlockChunk blockChunk = worldChunk.getBlockChunk();
               if (blockChunk != null) {
                  int c = blockChunk.getTint(ax, az);
                  r += (c >> 16 & 0xFF) * factor;
                  g += (c >> 8 & 0xFF) * factor;
                  b += (c & 0xFF) * factor;
               }
            }
         }
      }

      return 0xFF000000 | MathUtil.floor(r) << 16 | MathUtil.floor(g) << 8 | MathUtil.floor(b);
   }

   private static double gaussian2d(double sigma, double x, double y) {
      return 1.0 / ((Math.PI * 2) * sigma * sigma) * Math.pow(Math.E, -(x * x + y * y) / (2.0 * sigma * sigma));
   }

   private static double[] gaussianMatrix(double sigma, int radius) {
      int length = 2 * radius + 1;
      double[] matrix = new double[length * length];

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            double value = gaussian2d(sigma, x, y);
            matrix[gaussianIndex(radius, x, y)] = value;
         }
      }

      double sum = 0.0;

      for (double val : matrix) {
         sum += val;
      }

      for (int i = 0; i < matrix.length; i++) {
         matrix[i] /= sum;
      }

      return matrix;
   }

   private static int gaussianIndex(int radius, int x, int y) {
      x += radius;
      y += radius;
      return x * (2 * radius + 1) + y;
   }

   public static class TintChunkPage extends InteractiveCustomUIPage<ChunkTintCommand.TintChunkPage.TintChunkPageEventData> {
      TintChunkPage(@Nonnull PlayerRef playerRef) {
         super(playerRef, CustomPageLifetime.CanDismiss, ChunkTintCommand.TintChunkPage.TintChunkPageEventData.CODEC);
      }

      @Override
      public void build(
         @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
      ) {
         commandBuilder.append("Pages/TintChunkPage.ui");
         eventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#ColorPicker",
            new EventData().append("@Color", "#ColorPicker.Value").append("Submit", ChunkTintCommand.TintChunkPage.TintChunkPageAction.ColorChanged.name()),
            false
         );
         eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#ApplyButton",
            new EventData()
               .append("@Color", "#ColorPicker.Value")
               .append("@Radius", "#Radius.Value")
               .append("@BlurEnabled", "#BlurEnabledContainer #CheckBox.Value")
               .append("@Sigma", "#Sigma.Value")
               .append("@HexColor", "#HexColor.Value")
               .append("Submit", ChunkTintCommand.TintChunkPage.TintChunkPageAction.Submit.name()),
            false
         );
      }

      public void handleDataEvent(
         @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ChunkTintCommand.TintChunkPage.TintChunkPageEventData data
      ) {
         switch (data.getAction()) {
            case Submit: {
               String color = data.getColor().substring(0, 7);
               int radiusStr = data.getRadius();
               int sigmaStr = data.getSigma();
               if (!data.getKeyHexColor().isEmpty()) {
                  color = data.getKeyHexColor();
                  if (!color.startsWith("#")) {
                     color = "#" + color;
                  }
               }

               if (data.isBlurEnabled()) {
                  CommandManager.get().handleCommand(this.playerRef, "chunk tint " + color + " --blur --radius=" + radiusStr + " --sigma=" + sigmaStr);
               } else {
                  CommandManager.get().handleCommand(this.playerRef, "chunk tint " + color);
               }
               break;
            }
            case ColorChanged: {
               String color = data.getColor().substring(0, 7);
               UICommandBuilder commands = new UICommandBuilder();
               commands.set("#HexColor.Value", color);
               this.sendUpdate(commands);
            }
         }
      }

      public static enum TintChunkPageAction {
         Submit,
         ColorChanged;
      }

      public static class TintChunkPageEventData {
         public static final String KEY_COLOR = "@Color";
         public static final String KEY_RADIUS = "@Radius";
         public static final String KEY_SIGMA = "@Sigma";
         public static final String KEY_BLUR_ENABLED = "@BlurEnabled";
         public static final String KEY_HEX_COLOR = "@HexColor";
         public static final String KEY_ACTION = "Submit";
         @Nonnull
         public static BuilderCodec<ChunkTintCommand.TintChunkPage.TintChunkPageEventData> CODEC = BuilderCodec.builder(
               ChunkTintCommand.TintChunkPage.TintChunkPageEventData.class, ChunkTintCommand.TintChunkPage.TintChunkPageEventData::new
            )
            .addField(new KeyedCodec<>("@Color", Codec.STRING), (entry, s) -> entry.color = s, entry -> entry.color)
            .addField(new KeyedCodec<>("@Radius", Codec.INTEGER), (entry, s) -> entry.radius = s, entry -> entry.radius)
            .addField(new KeyedCodec<>("@Sigma", Codec.INTEGER), (entry, s) -> entry.sigma = s, entry -> entry.sigma)
            .addField(new KeyedCodec<>("@BlurEnabled", Codec.BOOLEAN), (entry, b) -> entry.isBlurEnabled = b, entry -> entry.isBlurEnabled)
            .addField(new KeyedCodec<>("@HexColor", Codec.STRING), (entry, s) -> entry.hexColor = s, entry -> entry.hexColor)
            .addField(
               new KeyedCodec<>("Submit", new EnumCodec<>(ChunkTintCommand.TintChunkPage.TintChunkPageAction.class)),
               (entry, s) -> entry.action = s,
               entry -> entry.action
            )
            .build();
         private String color;
         private int radius;
         private int sigma;
         private String hexColor;
         private boolean isBlurEnabled;
         private ChunkTintCommand.TintChunkPage.TintChunkPageAction action;

         public String getColor() {
            return this.color;
         }

         public int getRadius() {
            return this.radius;
         }

         public int getSigma() {
            return this.sigma;
         }

         public boolean isBlurEnabled() {
            return this.isBlurEnabled;
         }

         public String getKeyHexColor() {
            return this.hexColor;
         }

         public ChunkTintCommand.TintChunkPage.TintChunkPageAction getAction() {
            return this.action;
         }
      }
   }

   static class TintChunkPageCommand extends AbstractPlayerCommand {
      TintChunkPageCommand() {
         super("server.commands.chunk.tint.get");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         playerComponent.getPageManager().openCustomPage(ref, store, new ChunkTintCommand.TintChunkPage(playerRef));
      }
   }
}
