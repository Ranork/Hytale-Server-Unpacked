package com.hypixel.hytale.server.core.asset.type.particle.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.asset.type.particle.pages.ParticleSpawnPage;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ParticleSpawnCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private final RequiredArg<ParticleSystem> particleSystemArg = this.withRequiredArg(
      "particle", "server.commands.particle.spawn.particle.desc", ArgTypes.PARTICLE_SYSTEM
   );

   public ParticleSpawnCommand() {
      super("spawn", "server.commands.particle.spawn.desc");
      this.addUsageVariant(new ParticleSpawnCommand.ParticleSpawnPageCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      ParticleSystem particleSystem = this.particleSystemArg.get(context);
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = store.getResource(EntityModule.get().getPlayerSpatialResourceType());
      ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
      playerSpatialResource.getSpatialStructure().collect(position, 75.0, results);
      ParticleUtil.spawnParticleEffect(particleSystem.getId(), position, transformComponent.getRotation(), results, store);
   }

   private static class ParticleSpawnPageCommand extends AbstractTargetPlayerCommand {
      public ParticleSpawnPageCommand() {
         super("server.commands.particle.spawn.page.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context,
         @Nullable Ref<EntityStore> sourceRef,
         @Nonnull Ref<EntityStore> ref,
         @Nonnull PlayerRef playerRef,
         @Nonnull World world,
         @Nonnull Store<EntityStore> store
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         playerComponent.getPageManager().openCustomPage(ref, store, new ParticleSpawnPage(playerRef));
      }
   }
}
