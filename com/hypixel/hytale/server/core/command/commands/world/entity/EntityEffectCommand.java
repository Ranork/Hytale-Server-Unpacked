package com.hypixel.hytale.server.core.command.commands.world.entity;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetEntityCommand;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import javax.annotation.Nonnull;

public class EntityEffectCommand extends AbstractTargetEntityCommand {
   @Nonnull
   private final RequiredArg<EntityEffect> effectArg = this.withRequiredArg("effect", "server.commands.entity.effect.effect.desc", ArgTypes.EFFECT_ASSET);
   @Nonnull
   private final DefaultArg<Float> durationArg = this.withDefaultArg(
         "duration", "server.commands.entity.effect.duration.desc", ArgTypes.FLOAT, 100.0F, "server.commands.entity.effect.duration.default"
      )
      .addValidator(Validators.greaterThan(0.0F));

   public EntityEffectCommand() {
      super("effect", "server.commands.entity.effect.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull List<Ref<EntityStore>> entities, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      EntityEffect entityEffect = this.effectArg.get(context);
      float duration = this.durationArg.get(context);

      for (Ref<EntityStore> entityRef : entities) {
         EffectControllerComponent effectControllerComponent = store.getComponent(entityRef, EffectControllerComponent.getComponentType());
         if (effectControllerComponent != null) {
            effectControllerComponent.addEffect(entityRef, entityEffect, duration, OverlapBehavior.OVERWRITE, store);
         }
      }
   }
}
