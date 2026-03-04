package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public abstract class NPCMultiSelectCommandBase extends NPCWorldCommandBase {
   protected static final float DEFAULT_CONE_ANGLE = 30.0F;
   protected static final float DEFAULT_RANGE = 8.0F;
   protected static final float RANGE_MIN = 0.0F;
   protected static final float RANGE_MAX = 2048.0F;
   protected static final float CONE_ANGLE_MIN = 0.0F;
   protected static final float CONE_ANGLE_MAX = 180.0F;
   @Nonnull
   protected final OptionalArg<Float> coneAngleArg = this.withOptionalArg("angle", "server.commands.npc.command.angle.desc", ArgTypes.FLOAT);
   @Nonnull
   protected final OptionalArg<Float> rangeArg = this.withOptionalArg("range", "server.commands.npc.command.range.desc", ArgTypes.FLOAT);
   @Nonnull
   private final OptionalArg<String> rolesArg = this.withOptionalArg("roles", "server.commands.npc.command.roles.desc", ArgTypes.STRING);
   @Nonnull
   private final FlagArg nearestArg = this.withFlagArg("nearest", "server.commands.npc.command.nearest.desc");
   @Nonnull
   private final FlagArg presetCone30 = this.withFlagArg("cone", "server.commands.npc.command.preset.cone.desc");
   @Nonnull
   private final FlagArg presetCone30all = this.withFlagArg("coneAll", "server.commands.npc.command.preset.cone_all.desc");
   @Nonnull
   private final FlagArg presetSphere = this.withFlagArg("sphere", "server.commands.npc.command.preset.sphere.desc");
   @Nonnull
   private final FlagArg presetRay = this.withFlagArg("ray", "server.commands.npc.command.preset.ray.desc");

   public NPCMultiSelectCommandBase(@Nonnull String name, @Nonnull String description) {
      super(name, description);
   }

   public NPCMultiSelectCommandBase(@Nonnull String name, @Nonnull String description, boolean requiresConfirmation) {
      super(name, description, requiresConfirmation);
   }

   public NPCMultiSelectCommandBase(@Nonnull String description) {
      super(description);
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      if (this.entityArg.provided(context)) {
         Ref<EntityStore> ref = this.entityArg.get(store, context);
         if (ref != null) {
            NPCEntity npc = ensureIsNPC(context, store, ref);
            if (npc != null) {
               this.execute(context, npc, world, store, ref);
            }
         }
      } else {
         Ref<EntityStore> playerRef = null;
         if (context.isPlayer()) {
            playerRef = context.senderAsPlayerRef();
         }

         if (playerRef != null && playerRef.isValid()) {
            Set<String> roleSet = new HashSet<>();
            if (this.rolesArg.provided(context)) {
               String roleString = this.rolesArg.get(context);
               if (roleString == null || roleString.isEmpty()) {
                  context.sendMessage(Message.translation("server.commands.errors.npc.no_role_list_provided"));
                  return;
               }

               String[] roles = roleString.split(",");

               for (String role : roles) {
                  if (!role.isBlank()) {
                     if (!NPCPlugin.get().hasRoleName(role)) {
                        context.sendMessage(Message.translation("server.commands.errors.npc.unknown_role").param("role", role));
                        return;
                     }

                     roleSet.add(role);
                  }
               }
            }

            float range = this.rangeArg.provided(context) ? this.rangeArg.get(context) : 8.0F;
            if (!(range < 0.0F) && !(range > 2048.0F)) {
               float coneAngleDeg;
               boolean nearest;
               if (this.presetCone30.provided(context)) {
                  coneAngleDeg = 30.0F;
                  nearest = true;
               } else if (this.presetCone30all.provided(context)) {
                  coneAngleDeg = 30.0F;
                  nearest = false;
               } else if (this.presetSphere.provided(context)) {
                  coneAngleDeg = 180.0F;
                  nearest = false;
               } else if (this.presetRay.provided(context)) {
                  coneAngleDeg = 0.0F;
                  nearest = true;
               } else {
                  coneAngleDeg = this.coneAngleArg.provided(context) ? this.coneAngleArg.get(context).intValue() : 30.0F;
                  if (coneAngleDeg < 0.0F || coneAngleDeg > 180.0F) {
                     context.sendMessage(
                        Message.translation("server.commands.errors.validation.range.between_inclusive")
                           .param("param", "angle")
                           .param("min", 0.0F)
                           .param("max", 180.0F)
                           .param("value", coneAngleDeg)
                     );
                     return;
                  }

                  nearest = this.nearestArg.provided(context);
               }

               List<Ref<EntityStore>> refs = null;
               ComponentType<EntityStore, NPCEntity> npcEntityComponentType = NPCEntity.getComponentType();

               assert npcEntityComponentType != null;

               Vector3d eyePosition;
               if (coneAngleDeg == 0.0F) {
                  Ref<EntityStore> ref = TargetUtil.getTargetEntity(playerRef, range, store);
                  if (ref != null && store.getComponent(ref, npcEntityComponentType) != null) {
                     refs = new ArrayList<>();
                     refs.add(ref);
                  }

                  eyePosition = Vector3d.ZERO;
               } else {
                  TransformComponent playerTransform = store.getComponent(playerRef, TransformComponent.getComponentType());

                  assert playerTransform != null;

                  Transform viewTransform = TargetUtil.getLook(playerRef, store);
                  eyePosition = viewTransform.getPosition();
                  Vector3d eyeDirection = viewTransform.getDirection();

                  assert eyePosition.length() == 1.0;

                  refs = TargetUtil.getAllEntitiesInSphere(eyePosition, range, store);
                  float cosineConeAngle = (float)Math.cos((float)Math.toRadians(coneAngleDeg));

                  assert coneAngleDeg != 180.0F || cosineConeAngle == -1.0F;

                  refs.removeIf(entityRef -> {
                     if (store.getComponent((Ref<EntityStore>)entityRef, npcEntityComponentType) == null) {
                        return true;
                     } else if (cosineConeAngle <= -1.0F) {
                        return false;
                     } else {
                        TransformComponent entityTransform = store.getComponent((Ref<EntityStore>)entityRef, TransformComponent.getComponentType());

                        assert entityTransform != null;

                        Vector3d direction = Vector3d.directionTo(eyePosition, entityTransform.getPosition());
                        double lengthDirection = direction.length();
                        return lengthDirection < 1.0E-4 ? true : eyeDirection.dot(direction) < cosineConeAngle * lengthDirection;
                     }
                  });
               }

               if (refs != null && !refs.isEmpty() && !roleSet.isEmpty()) {
                  refs.removeIf(refx -> {
                     NPCEntity npc = store.getComponent(refx, npcEntityComponentType);
                     return !roleSet.contains(npc.getRoleName());
                  });
               }

               if (refs != null && !refs.isEmpty()) {
                  if (nearest && refs.size() > 1) {
                     Ref<EntityStore> nearestRef = refs.getFirst();
                     double nearestDistanceSq = Double.MAX_VALUE;

                     for (Ref<EntityStore> ref : refs) {
                        TransformComponent npcTransform = store.getComponent(ref, TransformComponent.getComponentType());

                        assert npcTransform != null;

                        double distanceSq = Vector3d.directionTo(eyePosition, npcTransform.getPosition()).squaredLength();
                        if (distanceSq < nearestDistanceSq) {
                           nearestDistanceSq = distanceSq;
                           nearestRef = ref;
                        }
                     }

                     refs = List.of(nearestRef);
                  }

                  this.processEntityList(context, world, store, refs);
               } else {
                  context.sendMessage(MESSAGE_COMMANDS_ERRORS_NO_ENTITY_IN_VIEW);
               }
            } else {
               context.sendMessage(
                  Message.translation("server.commands.errors.validation.range.between_inclusive")
                     .param("param", "range")
                     .param("min", 0.0F)
                     .param("max", 2048.0F)
                     .param("value", range)
               );
            }
         } else {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_OR_ARG);
         }
      }
   }

   protected void processEntityList(
      @Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store, @Nonnull List<Ref<EntityStore>> refs
   ) {
      refs.forEach(ref -> {
         NPCEntity npc = store.getComponent((Ref<EntityStore>)ref, NPCEntity.getComponentType());

         assert npc != null;

         this.execute(context, npc, world, store, (Ref<EntityStore>)ref);
      });
   }
}
