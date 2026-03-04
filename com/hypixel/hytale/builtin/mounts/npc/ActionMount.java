package com.hypixel.hytale.builtin.mounts.npc;

import com.hypixel.hytale.builtin.mounts.NPCMountComponent;
import com.hypixel.hytale.builtin.mounts.npc.builders.BuilderActionMount;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementConfig;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.systems.RoleChangeSystem;
import javax.annotation.Nonnull;

public class ActionMount extends ActionBase {
   public static final String EMPTY_ROLE_ID = "Empty_Role";
   protected final float anchorX;
   protected final float anchorY;
   protected final float anchorZ;
   protected final String movementConfigId;
   protected final int emptyRoleIndex;

   public ActionMount(@Nonnull BuilderActionMount builderActionMount, @Nonnull BuilderSupport builderSupport) {
      super(builderActionMount);
      this.anchorX = builderActionMount.getAnchorX(builderSupport);
      this.anchorY = builderActionMount.getAnchorY(builderSupport);
      this.anchorZ = builderActionMount.getAnchorZ(builderSupport);
      this.movementConfigId = builderActionMount.getMovementConfig(builderSupport);
      this.emptyRoleIndex = NPCPlugin.get().getIndex("Empty_Role");
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      Ref<EntityStore> target = role.getStateSupport().getInteractionIterationTarget();
      boolean targetExists = target != null && !store.getArchetype(target).contains(DeathComponent.getComponentType());
      return super.canExecute(ref, role, sensorInfo, dt, store) && targetExists;
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      ComponentType<EntityStore, NPCMountComponent> mountComponentType = NPCMountComponent.getComponentType();
      NPCMountComponent mountComponent = store.getComponent(ref, mountComponentType);
      if (mountComponent != null) {
         return false;
      } else {
         mountComponent = store.ensureAndGetComponent(ref, mountComponentType);
         mountComponent.setOriginalRoleIndex(NPCPlugin.get().getIndex(role.getRoleName()));
         Ref<EntityStore> playerReference = role.getStateSupport().getInteractionIterationTarget();
         if (playerReference == null) {
            return false;
         } else {
            PlayerRef playerRefComponent = store.getComponent(playerReference, PlayerRef.getComponentType());

            assert playerRefComponent != null;

            mountComponent.setOwnerPlayerRef(playerRefComponent);
            mountComponent.setAnchor(this.anchorX, this.anchorY, this.anchorZ);
            Player playerComponent = store.getComponent(playerReference, Player.getComponentType());

            assert playerComponent != null;

            PhysicsValues playerPhysicsValues = store.getComponent(playerReference, PhysicsValues.getComponentType());
            RoleChangeSystem.requestRoleChange(ref, role, this.emptyRoleIndex, false, null, null, store);
            MovementConfig movementConfig = MovementConfig.getAssetMap().getAsset(this.movementConfigId);
            if (movementConfig != null) {
               MovementManager movementManagerComponent = store.getComponent(playerReference, MovementManager.getComponentType());

               assert movementManagerComponent != null;

               movementManagerComponent.setDefaultSettings(movementConfig.toPacket(), playerPhysicsValues, playerComponent.getGameMode());
               movementManagerComponent.applyDefaultSettings();
               movementManagerComponent.update(playerRefComponent.getPacketHandler());
            }

            return true;
         }
      }
   }
}
