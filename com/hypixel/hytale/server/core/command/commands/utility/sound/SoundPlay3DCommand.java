package com.hypixel.hytale.server.core.command.commands.utility.sound;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeVector3i;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoundPlay3DCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private final RequiredArg<SoundEvent> soundEventArg = this.withRequiredArg("sound", "server.commands.sound.play3d.sound.desc", ArgTypes.SOUND_EVENT_ASSET);
   @Nonnull
   private final DefaultArg<SoundCategory> categoryArg = this.withDefaultArg(
      "category", "server.commands.sound.category.desc", ArgTypes.SOUND_CATEGORY, SoundCategory.SFX, "server.commands.sound.category.default"
   );
   @Nonnull
   private final RequiredArg<RelativeVector3i> positionArg = this.withRequiredArg(
      "position", "server.commands.sound.play3d.position.desc", ArgTypes.RELATIVE_VECTOR3I
   );
   @Nonnull
   private final FlagArg allFlag = this.withFlagArg("all", "server.commands.sound.all.desc");

   public SoundPlay3DCommand() {
      super("3d", "server.commands.sound.3d.desc");
      this.addAliases("play3d");
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
      SoundEvent soundEvent = this.soundEventArg.get(context);
      SoundCategory soundCategory = this.categoryArg.get(context);
      RelativeVector3i relativePosition = this.positionArg.get(context);
      int soundEventIndex = SoundEvent.getAssetMap().getIndex(soundEvent.getId());
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d basePosition = transformComponent.getPosition();
      Vector3i blockPosition = relativePosition.resolve(MathUtil.floor(basePosition.x), MathUtil.floor(basePosition.y), MathUtil.floor(basePosition.z));
      if (this.allFlag.provided(context)) {
         SoundUtil.playSoundEvent3d(soundEventIndex, soundCategory, blockPosition.x, blockPosition.y, blockPosition.z, world.getEntityStore().getStore());
      } else {
         SoundUtil.playSoundEvent3dToPlayer(ref, soundEventIndex, soundCategory, blockPosition.x, blockPosition.y, blockPosition.z, store);
      }
   }
}
