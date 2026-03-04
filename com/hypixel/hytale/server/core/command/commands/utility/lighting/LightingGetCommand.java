package com.hypixel.hytale.server.core.command.commands.utility.lighting;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeIntPosition;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkLightData;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class LightingGetCommand extends AbstractWorldCommand {
   @Nonnull
   private final RequiredArg<RelativeIntPosition> positionArg = this.withRequiredArg(
      "x y z", "server.commands.light.get.position.desc", ArgTypes.RELATIVE_BLOCK_POSITION
   );
   @Nonnull
   private final FlagArg hexFlag = this.withFlagArg("hex", "server.commands.light.get.hex.desc");

   public LightingGetCommand() {
      super("get", "server.commands.light.get.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      ChunkStore chunkStore = world.getChunkStore();
      Vector3i position = this.positionArg.get(context).getBlockPosition(context, store);
      int x = position.x;
      int y = position.y;
      int z = position.z;
      long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
      Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);
      if (chunkReference != null && chunkReference.isValid()) {
         BlockChunk blockChunkComponent = chunkStore.getStore().getComponent(chunkReference, BlockChunk.getComponentType());

         assert blockChunkComponent != null;

         BlockSection section = blockChunkComponent.getSectionAtBlockY(y);
         short lightValue = section.getGlobalLight().getLightRaw(x, y, z);
         byte redLight = ChunkLightData.getLightValue(lightValue, 0);
         byte greenLight = ChunkLightData.getLightValue(lightValue, 1);
         byte blueLight = ChunkLightData.getLightValue(lightValue, 2);
         byte skyLight = ChunkLightData.getLightValue(lightValue, 3);
         boolean displayHex = this.hexFlag.get(context);
         Message messageToSend = Message.translation("server.commands.light.get").param("x", x).param("y", y).param("z", z).param("worldName", world.getName());
         if (displayHex) {
            String hexString = Integer.toHexString(lightValue);
            messageToSend.insert("#" + "0".repeat(8 - hexString.length()) + hexString);
         } else {
            messageToSend.insert(
               Message.translation("server.commands.light.value")
                  .param("red", (int)redLight)
                  .param("green", (int)greenLight)
                  .param("blue", (int)blueLight)
                  .param("sky", (int)skyLight)
            );
         }

         context.sendMessage(messageToSend);
      } else {
         Message errorMessage = Message.translation("server.commands.errors.chunkNotLoaded")
            .param("chunkX", ChunkUtil.chunkCoordinate(x))
            .param("chunkZ", ChunkUtil.chunkCoordinate(z))
            .param("world", world.getName());
         context.sendMessage(errorMessage);
      }
   }
}
