package com.hypixel.hytale.builtin.triggervolumes.command;

import com.hypixel.hytale.builtin.triggervolumes.EntityTargetType;
import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.DeleteVolumeEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.ItemCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.ModifyTagsEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.PlaySoundEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.PlayVfxEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.SendMessageEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.SetVelocityEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.ShowEventTitleEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions.BlockTypeCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions.CooldownCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions.GameModeCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions.PermissionCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions.RandomChanceCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions.TagCondition;
import com.hypixel.hytale.builtin.triggervolumes.manager.ConditionTiming;
import com.hypixel.hytale.builtin.triggervolumes.manager.CooldownMode;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.builtin.triggervolumes.shape.BoxShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.CylinderShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.SphereShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class TriggerVolumeTestCommand extends AbstractWorldCommand {
   private static final String TEST_PREFIX = "tvtest_";
   private static final double SPACING = 8.0;
   private final FlagArg cleanupFlag = this.withFlagArg("cleanup", "server.commands.triggervolume.test.cleanup.desc");

   public TriggerVolumeTestCommand() {
      super("test", "server.commands.triggervolume.test.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         String worldName = world.getName().toLowerCase(Locale.ROOT);
         if (!this.cleanupFlag.get(context)) {
            Ref<EntityStore> playerRef = context.senderAsPlayerRef();
            if (playerRef != null && playerRef.isValid()) {
               TransformComponent transform = store.getComponent(playerRef, TransformComponent.getComponentType());
               if (transform != null) {
                  Vector3d position = transform.getPosition();
                  float yaw = transform.getRotation().yaw();
                  double forwardX = -Math.sin(yaw);
                  double forwardZ = -Math.cos(yaw);
                  EnumSet<EntityTargetType> targets = EnumSet.of(EntityTargetType.PLAYER);
                  BoxShape box = new BoxShape(new Vector3d(-2.0, 0.0, -2.0), new Vector3d(2.0, 3.0, 2.0));
                  int created = 0;
                  int slot = 0;
                  created += this.registerVolume(
                     manager,
                     worldName,
                     "tvtest_enter_message",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.enter")),
                     targets
                  );
                  created += this.registerVolume(
                     manager,
                     worldName,
                     "tvtest_exit_message",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(SendMessageEffect.create(TriggerEventType.EXIT, "server.commands.triggervolume.test.msg.exit")),
                     targets
                  );
                  created += this.registerVolume(
                     manager,
                     worldName,
                     "tvtest_launch_pad",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     new SphereShape(new Vector3d(0.0, 1.5, 0.0), 3.0),
                     List.of(SetVelocityEffect.create(TriggerEventType.ENTER, new Vector3d(0.0, 20.0, 0.0), false)),
                     targets
                  );
                  created += this.registerVolume(
                     manager,
                     worldName,
                     "tvtest_play_sound",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     new CylinderShape(new Vector3d(0.0, 0.0, 0.0), 3.0, 4.0),
                     List.of(PlaySoundEffect.create(TriggerEventType.ENTER, "SFX_Player_Pickup_Item")),
                     targets
                  );
                  SendMessageEffect tickMsg = SendMessageEffect.create(TriggerEventType.TICK, "server.commands.triggervolume.test.msg.tick");
                  tickMsg.setInterval(2.0F);
                  created += this.registerVolume(
                     manager,
                     worldName,
                     "tvtest_tick_message_2s",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     new SphereShape(new Vector3d(0.0, 1.5, 0.0), 3.0),
                     List.of(tickMsg),
                     targets
                  );
                  VolumeEntry conditional = this.registerVolumeEntry(
                     manager,
                     worldName,
                     "tvtest_condition_random_chance",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.conditional")),
                     targets
                  );
                  if (conditional != null) {
                     conditional.getConditions().add(RandomChanceCondition.create(TriggerEventType.ENTER, 0.5F));
                     created++;
                  }

                  VolumeEntry cooldownPerPlayer = this.registerVolumeEntry(
                     manager,
                     worldName,
                     "tvtest_condition_cooldown_per_player",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.cooldownConditionPerPlayerAccepted")),
                     targets
                  );
                  if (cooldownPerPlayer != null) {
                     cooldownPerPlayer.getConditions().add(CooldownCondition.create(TriggerEventType.ENTER, 5.0F, CooldownCondition.Scope.PER_PLAYER));
                     cooldownPerPlayer.getRejectionEffects()
                        .add(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.cooldownConditionPerPlayerRejected"));
                     created++;
                  }

                  VolumeEntry cooldownWholeVolume = this.registerVolumeEntry(
                     manager,
                     worldName,
                     "tvtest_condition_cooldown_whole_volume",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.cooldownConditionWholeVolumeAccepted")),
                     targets
                  );
                  if (cooldownWholeVolume != null) {
                     cooldownWholeVolume.getConditions().add(CooldownCondition.create(TriggerEventType.ENTER, 5.0F, CooldownCondition.Scope.WHOLE_VOLUME));
                     cooldownWholeVolume.getRejectionEffects()
                        .add(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.cooldownConditionWholeVolumeRejected"));
                     created++;
                  }

                  VolumeEntry gameModeReject = this.registerVolumeEntry(
                     manager,
                     worldName,
                     "tvtest_condition_reject_gamemode",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.gameModeAccepted")),
                     targets
                  );
                  if (gameModeReject != null) {
                     gameModeReject.getConditions().add(GameModeCondition.create(TriggerEventType.ENTER, GameMode.Creative));
                     gameModeReject.getRejectionEffects()
                        .add(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.gameModeRejected"));
                     created++;
                  }

                  VolumeEntry permissionReject = this.registerVolumeEntry(
                     manager,
                     worldName,
                     "tvtest_condition_reject_permission",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.permissionAccepted")),
                     targets
                  );
                  if (permissionReject != null) {
                     permissionReject.getConditions().add(PermissionCondition.create(TriggerEventType.ENTER, "hytale.triggervolume.test.permission"));
                     permissionReject.getRejectionEffects()
                        .add(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.permissionRejected"));
                     created++;
                  }

                  VolumeEntry itemReject = this.registerVolumeEntry(
                     manager,
                     worldName,
                     "tvtest_condition_reject_item",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.itemAccepted")),
                     targets
                  );
                  if (itemReject != null) {
                     itemReject.getConditions().add(ItemCondition.create(TriggerEventType.ENTER, "Potion_Empty", ItemCondition.Location.IN_HAND, 1));
                     itemReject.getRejectionEffects()
                        .add(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.itemRejected"));
                     created++;
                  }

                  VolumeEntry delayedCondition = this.registerVolumeEntry(
                     manager,
                     worldName,
                     "tvtest_condition_after_delay",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.afterDelayAccepted")),
                     targets
                  );
                  if (delayedCondition != null) {
                     delayedCondition.setActivationDelay(2.0F);
                     delayedCondition.setConditionTiming(ConditionTiming.AFTER_VOLUME_DELAY);
                     delayedCondition.getConditions().add(RandomChanceCondition.create(TriggerEventType.ENTER, 1.0F));
                     delayedCondition.getRejectionEffects()
                        .add(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.afterDelayRejected"));
                     created++;
                  }

                  VolumeEntry delayedVol = this.registerVolumeEntry(
                     manager,
                     worldName,
                     "tvtest_delayed_2s_message",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.delayed")),
                     targets
                  );
                  if (delayedVol != null) {
                     delayedVol.setActivationDelay(2.0F);
                     created++;
                  }

                  SendMessageEffect seq1 = SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.seq1");
                  SendMessageEffect seq2 = SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.seq2");
                  seq2.setDelay(1.0F);
                  SendMessageEffect seq3 = SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.seq3");
                  seq3.setDelay(2.0F);
                  created += this.registerVolume(
                     manager,
                     worldName,
                     "tvtest_sequenced_messages",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(seq1, seq2, seq3),
                     targets
                  );
                  created += this.registerVolume(
                     manager,
                     worldName,
                     "tvtest_event_title",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(
                        ShowEventTitleEffect.create(
                           TriggerEventType.ENTER,
                           "server.commands.triggervolume.test.msg.titlePrimary",
                           "server.commands.triggervolume.test.msg.titleSecondary",
                           true
                        )
                     ),
                     targets
                  );
                  created += this.registerVolume(
                     manager,
                     worldName,
                     "tvtest_one_shot_destroy",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(
                        SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.oneShot"),
                        DeleteVolumeEffect.create(TriggerEventType.ENTER)
                     ),
                     targets
                  );
                  created += this.registerVolume(
                     manager,
                     worldName,
                     "tvtest_vfx_at_origin",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(PlayVfxEffect.create(TriggerEventType.ENTER, "Totem_Heal_Simple_Test", false)),
                     targets
                  );
                  created += this.registerVolume(
                     manager,
                     worldName,
                     "tvtest_vfx_at_entity",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(PlayVfxEffect.create(TriggerEventType.ENTER, "Totem_Heal_Simple_Test", true)),
                     targets
                  );
                  VolumeEntry perPlayerCd = this.registerVolumeEntry(
                     manager,
                     worldName,
                     "tvtest_cooldown_per_player_5s",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.cooldownPerPlayer")),
                     targets
                  );
                  if (perPlayerCd != null) {
                     perPlayerCd.setCooldown(5.0F);
                     perPlayerCd.setCooldownMode(CooldownMode.PER_ENTITY);
                     created++;
                  }

                  VolumeEntry totalCd = this.registerVolumeEntry(
                     manager,
                     worldName,
                     "tvtest_cooldown_total_10s",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.cooldownTotal")),
                     targets
                  );
                  if (totalCd != null) {
                     totalCd.setCooldown(10.0F);
                     totalCd.setCooldownMode(CooldownMode.TOTAL);
                     created++;
                  }

                  VolumeEntry blockPlaced = this.registerVolumeEntry(
                     manager,
                     worldName,
                     "tvtest_block_placed_soil_dirt",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(SendMessageEffect.create(TriggerEventType.BLOCK_PLACED, "server.commands.triggervolume.test.msg.blockPlaced")),
                     targets
                  );
                  if (blockPlaced != null) {
                     blockPlaced.getConditions().add(BlockTypeCondition.create(TriggerEventType.BLOCK_PLACED, "Soil_Dirt"));
                     created++;
                  }

                  VolumeEntry tagMutation = this.registerVolumeEntry(
                     manager,
                     worldName,
                     "tvtest_tag_mutation",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(
                        SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.tagMutationEnter"),
                        ModifyTagsEffect.set(TriggerEventType.ENTER, "tvtest_state", "active"),
                        ModifyTagsEffect.remove(TriggerEventType.EXIT, "tvtest_state", null),
                        SendMessageEffect.create(TriggerEventType.TAG_ADDED, "server.commands.triggervolume.test.msg.tagAdded"),
                        SendMessageEffect.create(TriggerEventType.TAG_REMOVED, "server.commands.triggervolume.test.msg.tagRemoved")
                     ),
                     targets
                  );
                  if (tagMutation != null) {
                     tagMutation.getConditions().add(TagCondition.forEvent(TriggerEventType.TAG_ADDED, "tvtest_state", "active"));
                     tagMutation.getConditions().add(TagCondition.forEvent(TriggerEventType.TAG_REMOVED, "tvtest_state", "active"));
                     created++;
                  }

                  String remoteTag = "tvtest_remote_target";
                  String remoteStateTag = "tvtest_remote_state";
                  VolumeEntry remoteTarget = this.registerVolumeEntry(
                     manager,
                     worldName,
                     "tvtest_remote_tag_target",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(
                        SendMessageEffect.create(TriggerEventType.TAG_ADDED, "server.commands.triggervolume.test.msg.remoteTagAdded"),
                        SendMessageEffect.create(TriggerEventType.TAG_REMOVED, "server.commands.triggervolume.test.msg.remoteTagRemoved")
                     ),
                     targets
                  );
                  if (remoteTarget != null) {
                     remoteTarget.setTags(Map.of(remoteTag, "target"));
                     remoteTarget.getConditions().add(TagCondition.forEvent(TriggerEventType.TAG_ADDED, remoteStateTag, "armed"));
                     remoteTarget.getConditions().add(TagCondition.forEvent(TriggerEventType.TAG_REMOVED, remoteStateTag, "armed"));
                     created++;
                  }

                  created += this.registerVolume(
                     manager,
                     worldName,
                     "tvtest_remote_tag_controller",
                     slotPosition(position, forwardX, forwardZ, ++slot),
                     box,
                     List.of(
                        SendMessageEffect.create(TriggerEventType.ENTER, "server.commands.triggervolume.test.msg.remoteControllerEnter"),
                        ModifyTagsEffect.set(TriggerEventType.ENTER, remoteStateTag, "armed").withMatchTag(remoteTag, null),
                        SendMessageEffect.create(TriggerEventType.EXIT, "server.commands.triggervolume.test.msg.remoteControllerExit"),
                        ModifyTagsEffect.remove(TriggerEventType.EXIT, remoteStateTag, null).withMatchTag(remoteTag, null)
                     ),
                     targets
                  );
                  context.sendMessage(Message.translation("server.commands.triggervolume.test.success").param("count", created));
               }
            }
         } else {
            ArrayList<String> toRemove = new ArrayList<>();

            for (Entry<String, VolumeEntry> entry : manager.getVolumesMap().entrySet()) {
               if (entry.getKey().startsWith("tvtest_")) {
                  toRemove.add(entry.getKey());
               }
            }

            for (String id : toRemove) {
               manager.unregister(id);
               manager.notifyViewersRemove(id);
            }

            context.sendMessage(Message.translation("server.commands.triggervolume.test.cleaned").param("count", toRemove.size()));
         }
      }
   }

   @Nonnull
   private static Vector3d slotPosition(@Nonnull Vector3d origin, double forwardX, double forwardZ, int slot) {
      return new Vector3d(origin.x() + forwardX * 8.0 * slot, origin.y(), origin.z() + forwardZ * 8.0 * slot);
   }

   private int registerVolume(
      @Nonnull TriggerVolumeManager manager,
      @Nonnull String worldName,
      @Nonnull String id,
      @Nonnull Vector3d position,
      @Nonnull TriggerVolumeShape shape,
      @Nonnull List<TriggerEffect> effects,
      @Nonnull Set<EntityTargetType> targets
   ) {
      this.registerVolumeEntry(manager, worldName, id, position, shape, effects, targets);
      return 1;
   }

   @Nullable
   private VolumeEntry registerVolumeEntry(
      @Nonnull TriggerVolumeManager manager,
      @Nonnull String worldName,
      @Nonnull String id,
      @Nonnull Vector3d position,
      @Nonnull TriggerVolumeShape shape,
      @Nonnull List<TriggerEffect> effects,
      @Nonnull Set<EntityTargetType> targets
   ) {
      if (manager.hasVolume(id)) {
         manager.unregister(id);
      }

      VolumeEntry entry = new VolumeEntry(id, worldName, position, shape, new ArrayList<>(effects), targets, true);
      manager.register(id, entry);
      return entry;
   }
}
