package com.hypixel.hytale.server.flock.commands;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.flock.FlockMembershipSystems;
import com.hypixel.hytale.server.flock.FlockPlugin;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

public class NPCFlockCommand extends AbstractCommandCollection {
   private static final double ENTITY_IN_VIEW_DISTANCE = 8.0;
   private static final float ENTITY_IN_VIEW_ANGLE = 30.0F;
   private static final int ENTITY_IN_VIEW_HEIGHT = 2;

   public NPCFlockCommand() {
      super("flock", "server.commands.npc.flock.desc");
      this.addSubCommand(new NPCFlockCommand.LeaveCommand());
      this.addSubCommand(new NPCFlockCommand.GrabCommand());
      this.addSubCommand(new NPCFlockCommand.JoinCommand());
      this.addSubCommand(new NPCFlockCommand.PlayerLeaveCommand());
   }

   public static int forNpcEntitiesInViewCone(
      @Nonnull Ref<EntityStore> playerReference, @Nonnull Store<EntityStore> store, @Nonnull BiPredicate<Ref<EntityStore>, NPCEntity> predicate
   ) {
      ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
      TransformComponent transformComponent = store.getComponent(playerReference, transformComponentType);

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      HeadRotation headRotationComponent = store.getComponent(playerReference, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      Vector3f headRotation = headRotationComponent.getRotation();
      float lookYaw = headRotation.getYaw();
      double x = position.getX();
      double y = position.getY();
      double z = position.getZ();
      SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(NPCPlugin.get().getNpcSpatialResource());
      ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
      spatialResource.getSpatialStructure().collect(position, 8.0, results);
      ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();

      assert npcComponentType != null;

      int count = 0;
      ObjectListIterator var19 = results.iterator();

      while (var19.hasNext()) {
         Ref<EntityStore> targetRef = (Ref<EntityStore>)var19.next();
         NPCEntity targetNpcComponent = store.getComponent(targetRef, npcComponentType);

         assert targetNpcComponent != null;

         TransformComponent entityTransformComponent = store.getComponent(targetRef, transformComponentType);

         assert entityTransformComponent != null;

         Vector3d entityPosition = entityTransformComponent.getPosition();
         if (Math.abs(entityPosition.getY() - y) < 2.0
            && NPCPhysicsMath.inViewSector(x, z, lookYaw, (float) (Math.PI / 6), entityPosition.getX(), entityPosition.getZ())
            && predicate.test(targetRef, targetNpcComponent)) {
            count++;
         }
      }

      return count;
   }

   public static boolean anyEntityInViewCone(
      @Nonnull Ref<EntityStore> playerReference, @Nonnull Store<EntityStore> store, @Nonnull Predicate<Ref<EntityStore>> predicate
   ) {
      ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
      TransformComponent transformComponent = store.getComponent(playerReference, transformComponentType);

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      HeadRotation headRotationComponent = store.getComponent(playerReference, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      Vector3f headRotation = headRotationComponent.getRotation();
      float lookYaw = headRotation.getYaw();
      double x = position.getX();
      double y = position.getY();
      double z = position.getZ();
      SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(NPCPlugin.get().getNpcSpatialResource());
      ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
      spatialResource.getSpatialStructure().ordered(position, 8.0, results);
      ObjectListIterator var17 = results.iterator();

      while (var17.hasNext()) {
         Ref<EntityStore> entityRef = (Ref<EntityStore>)var17.next();
         TransformComponent entityTransformComponent = store.getComponent(entityRef, transformComponentType);

         assert entityTransformComponent != null;

         Vector3d entityPosition = entityTransformComponent.getPosition();
         if (Math.abs(entityPosition.getY() - y) < 2.0
            && NPCPhysicsMath.inViewSector(x, z, lookYaw, (float) (Math.PI / 6), entityPosition.getX(), entityPosition.getZ())
            && predicate.test(entityRef)) {
            return true;
         }
      }

      return false;
   }

   public static class GrabCommand extends AbstractPlayerCommand {
      public GrabCommand() {
         super("grab", "server.commands.npc.flock.grab.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         int count = NPCFlockCommand.forNpcEntitiesInViewCone(ref, store, (targetRef, targetNpcComponent) -> {
            FlockMembership membership = store.getComponent(targetRef, FlockMembership.getComponentType());
            if (membership == null) {
               Ref<EntityStore> flockReference = FlockPlugin.getFlockReference(ref, store);
               if (flockReference == null) {
                  flockReference = FlockPlugin.createFlock(store, targetNpcComponent.getRole());
                  FlockMembershipSystems.join(ref, flockReference, store);
               }

               FlockMembershipSystems.join(targetRef, flockReference, store);
               return true;
            } else {
               return false;
            }
         });
         context.sendMessage(Message.translation("server.commands.npc.flock.addedToFlock").param("count", count));
      }
   }

   public static class JoinCommand extends AbstractPlayerCommand {
      public JoinCommand() {
         super("join", "server.commands.npc.flock.join.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         boolean success = NPCFlockCommand.anyEntityInViewCone(ref, store, npcReference -> {
            Ref<EntityStore> flockReference = FlockPlugin.getFlockReference(npcReference, store);
            if (flockReference != null) {
               FlockMembershipSystems.join(ref, flockReference, store);
               return true;
            } else {
               return false;
            }
         });
         if (!success) {
            context.sendMessage(Message.translation("server.commands.npc.flock.resultJoinFlock").param("status", "Failed"));
         } else {
            world.execute(() -> {
               String status = FlockPlugin.isFlockMember(ref, store) ? "Succeeded" : "Failed";
               context.sendMessage(Message.translation("server.commands.npc.flock.resultJoinFlock").param("status", status));
            });
         }
      }
   }

   public static class LeaveCommand extends AbstractPlayerCommand {
      public LeaveCommand() {
         super("leave", "server.commands.npc.flock.leave.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         int count = NPCFlockCommand.forNpcEntitiesInViewCone(ref, store, (targetRef, targetNpcComponent) -> {
            store.tryRemoveComponent(targetRef, FlockMembership.getComponentType());
            return true;
         });
         context.sendMessage(Message.translation("server.commands.npc.flock.removedFromFlock").param("count", count));
      }
   }

   public static class PlayerLeaveCommand extends AbstractPlayerCommand {
      @Nonnull
      private static final Message MESSAGE_COMMANDS_NPC_FLOCK_LEFT_FLOCK = Message.translation("server.commands.npc.flock.leftFlock");
      @Nonnull
      private static final Message MESSAGE_COMMANDS_NPC_FLOCK_FAILED_LEAVE_FLOCK = Message.translation("server.commands.npc.flock.failedLeaveFlock");

      public PlayerLeaveCommand() {
         super("playerleave", "server.commands.npc.flock.playerleave.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         if (store.removeComponentIfExists(ref, FlockMembership.getComponentType())) {
            context.sendMessage(MESSAGE_COMMANDS_NPC_FLOCK_LEFT_FLOCK);
         } else {
            context.sendMessage(MESSAGE_COMMANDS_NPC_FLOCK_FAILED_LEAVE_FLOCK);
         }
      }
   }
}
