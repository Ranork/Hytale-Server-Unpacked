package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.util.PositionProbeAir;
import com.hypixel.hytale.server.npc.util.PositionProbeWater;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class NPCTestCommand extends AbstractCommandCollection {
   public NPCTestCommand() {
      super("test", "server.commands.npc.test.desc");
      this.addSubCommand(new NPCTestCommand.ProbeTestCommand());
   }

   public static class ProbeTestCommand extends AbstractPlayerCommand {
      public ProbeTestCommand() {
         super("probe", "server.commands.npc.test.probe.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         CollisionResult collisionResult = new CollisionResult();
         PositionProbeAir probeAir = new PositionProbeAir();
         PositionProbeWater probeWater = new PositionProbeWater();
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         BoundingBox boundingBoxComponent = store.getComponent(ref, BoundingBox.getComponentType());

         assert boundingBoxComponent != null;

         Box playerCollider = boundingBoxComponent.getBoundingBox();
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         ModelComponent modelComponent = store.getComponent(ref, ModelComponent.getComponentType());

         assert modelComponent != null;

         Vector3d position = transformComponent.getPosition();
         Model model = modelComponent.getModel();
         float eyeHeight = model != null ? model.getEyeHeight(ref, store) : 0.0F;
         boolean testAir = probeAir.probePosition(ref, playerCollider, position, collisionResult, store);
         boolean testWater = probeWater.probePosition(ref, playerCollider, position, collisionResult, eyeHeight, store);
         boolean validatePosition = CollisionModule.get()
               .validatePosition(
                  world,
                  playerCollider,
                  position,
                  4,
                  null,
                  (_this, collisionCode, collision, collisionConfig) -> collisionConfig.blockId != -1,
                  collisionResult
               )
            != -1;
         NPCPlugin npcPlugin = NPCPlugin.get();
         String text = "Pos Y ["
            + position.y
            + ", "
            + playerCollider
            + "] Height="
            + world.getChunk(ChunkUtil.indexChunkFromBlock(position.x, position.z)).getHeight(MathUtil.floor(position.x), MathUtil.floor(position.z));
         context.sendMessage(Message.raw(text));
         npcPlugin.getLogger().at(Level.INFO).log(text);
         text = "Air " + testAir + " " + probeAir;
         context.sendMessage(Message.raw(text));
         npcPlugin.getLogger().at(Level.INFO).log(text);
         text = "Water " + testWater + " " + probeWater;
         context.sendMessage(Message.raw(text));
         npcPlugin.getLogger().at(Level.INFO).log(text);
         text = "ValidatePosition " + validatePosition;
         context.sendMessage(Message.raw(text));
         npcPlugin.getLogger().at(Level.INFO).log(text);
      }
   }
}
