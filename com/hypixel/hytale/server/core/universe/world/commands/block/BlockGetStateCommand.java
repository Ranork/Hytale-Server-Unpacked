package com.hypixel.hytale.server.core.universe.world.commands.block;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public class BlockGetStateCommand extends SimpleBlockCommand {
   public BlockGetStateCommand() {
      super("getstate", "server.commands.block.getstate.desc");
   }

   @Override
   protected void executeWithBlock(@Nonnull CommandContext context, @Nonnull WorldChunk chunk, int x, int y, int z) {
      CommandSender sender = context.sender();
      Ref<ChunkStore> ref = chunk.getBlockComponentEntity(x, y, z);
      if (ref != null) {
         StringBuilder stateString = new StringBuilder();
         Archetype<ChunkStore> archetype = ref.getStore().getArchetype(ref);

         for (int i = archetype.getMinIndex(); i < archetype.length(); i++) {
            ComponentType<ChunkStore, ? extends Component<ChunkStore>> c = (ComponentType<ChunkStore, ? extends Component<ChunkStore>>)archetype.get(i);
            if (c != null) {
               stateString.append(c.getTypeClass().getSimpleName()).append(" = ").append(ref.getStore().getComponent(ref, c)).append('\n');
            }
         }

         sender.sendMessage(
            Message.translation("server.commands.block.getstate.info").param("x", x).param("y", y).param("z", z).param("state", stateString.toString())
         );
      }
   }
}
