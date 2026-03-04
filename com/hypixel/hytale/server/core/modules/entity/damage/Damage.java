package com.hypixel.hytale.server.core.modules.entity.damage;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.math.vector.Vector4d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.asset.type.particle.config.WorldParticle;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.meta.DynamicMetaStore;
import com.hypixel.hytale.server.core.meta.IMetaStore;
import com.hypixel.hytale.server.core.meta.IMetaStoreImpl;
import com.hypixel.hytale.server.core.meta.MetaKey;
import com.hypixel.hytale.server.core.meta.MetaRegistry;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Damage extends CancellableEcsEvent implements IMetaStore<Damage> {
   @Nonnull
   private static final Message MESSAGE_GENERAL_DAMAGE_CAUSE_UNKNOWN = Message.translation("server.general.damageCauses.unknown");
   @Nonnull
   public static final MetaRegistry<Damage> META_REGISTRY = new MetaRegistry<>();
   @Nonnull
   public static final MetaKey<Vector4d> HIT_LOCATION = META_REGISTRY.registerMetaObject();
   @Nonnull
   public static final MetaKey<Float> HIT_ANGLE = META_REGISTRY.registerMetaObject();
   @Nonnull
   public static final MetaKey<Damage.Particles> IMPACT_PARTICLES = META_REGISTRY.registerMetaObject();
   @Nonnull
   public static final MetaKey<Damage.SoundEffect> IMPACT_SOUND_EFFECT = META_REGISTRY.registerMetaObject();
   @Nonnull
   public static final MetaKey<Damage.SoundEffect> PLAYER_IMPACT_SOUND_EFFECT = META_REGISTRY.registerMetaObject();
   @Nonnull
   public static final MetaKey<Damage.CameraEffect> CAMERA_EFFECT = META_REGISTRY.registerMetaObject();
   @Nonnull
   public static final MetaKey<String> DEATH_ICON = META_REGISTRY.registerMetaObject();
   @Nonnull
   public static final MetaKey<Boolean> BLOCKED = META_REGISTRY.registerMetaObject(data -> Boolean.FALSE);
   @Nonnull
   public static final MetaKey<Float> STAMINA_DRAIN_MULTIPLIER = META_REGISTRY.registerMetaObject();
   @Nonnull
   public static final MetaKey<Boolean> CAN_BE_PREDICTED = META_REGISTRY.registerMetaObject(data -> Boolean.FALSE);
   @Nonnull
   public static final MetaKey<KnockbackComponent> KNOCKBACK_COMPONENT = META_REGISTRY.registerMetaObject();
   @Nonnull
   public static final Damage.Source NULL_SOURCE = new Damage.Source() {};
   @Nonnull
   private final IMetaStoreImpl<Damage> metaStore = new DynamicMetaStore<>(this, META_REGISTRY);
   private final float initialAmount;
   private int damageCauseIndex;
   @Nonnull
   private Damage.Source source;
   private float amount;

   public Damage(@Nonnull Damage.Source source, @Nonnull DamageCause damageCause, float amount) {
      this.source = source;
      this.damageCauseIndex = DamageCause.getAssetMap().getIndex(damageCause.getId());
      this.initialAmount = this.amount = amount;
   }

   public Damage(@Nonnull Damage.Source source, int damageCauseIndex, float amount) {
      this.source = source;
      this.damageCauseIndex = damageCauseIndex;
      this.initialAmount = this.amount = amount;
   }

   public int getDamageCauseIndex() {
      return this.damageCauseIndex;
   }

   public void setDamageCauseIndex(int damageCauseIndex) {
      this.damageCauseIndex = damageCauseIndex;
   }

   @Deprecated
   @Nullable
   public DamageCause getCause() {
      return DamageCause.getAssetMap().getAsset(this.damageCauseIndex);
   }

   @Nonnull
   public Damage.Source getSource() {
      return this.source;
   }

   public void setSource(@Nonnull Damage.Source source) {
      this.source = source;
   }

   public float getAmount() {
      return this.amount;
   }

   public void setAmount(float amount) {
      this.amount = amount;
   }

   public float getInitialAmount() {
      return this.initialAmount;
   }

   @Nonnull
   public Message getDeathMessage(@Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return this.source.getDeathMessage(this, targetRef, componentAccessor);
   }

   @Nonnull
   @Override
   public IMetaStoreImpl<Damage> getMetaStore() {
      return this.metaStore;
   }

   public record CameraEffect(int cameraEffectIndex) {
      public int getEffectIndex() {
         return this.cameraEffectIndex;
      }
   }

   public static class CommandSource implements Damage.Source {
      @Nonnull
      private static final String COMMAND_NAME_UNKNOWN = "Unknown";
      @Nonnull
      private final CommandSender commandSender;
      @Nullable
      private final String commandName;

      public CommandSource(@Nonnull CommandSender commandSender, @Nonnull AbstractCommand cmd) {
         this(commandSender, cmd.getName());
      }

      public CommandSource(@Nonnull CommandSender commandSender, @Nullable String commandName) {
         this.commandSender = commandSender;
         this.commandName = commandName;
      }

      @Nonnull
      @Override
      public Message getDeathMessage(@Nonnull Damage info, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
         return Message.translation("server.general.killedByCommand")
            .param("displayName", this.commandSender.getDisplayName())
            .param("commandName", this.commandName != null ? this.commandName : "Unknown");
      }
   }

   public static class EntitySource implements Damage.Source {
      @Nonnull
      protected final Ref<EntityStore> sourceRef;

      public EntitySource(@Nonnull Ref<EntityStore> sourceRef) {
         this.sourceRef = sourceRef;
      }

      @Nonnull
      public Ref<EntityStore> getRef() {
         return this.sourceRef;
      }

      @Nonnull
      @Override
      public Message getDeathMessage(@Nonnull Damage info, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
         Message damageCauseMessage = Damage.MESSAGE_GENERAL_DAMAGE_CAUSE_UNKNOWN;
         DisplayNameComponent displayNameComponent = componentAccessor.getComponent(this.sourceRef, DisplayNameComponent.getComponentType());
         if (displayNameComponent != null) {
            Message displayName = displayNameComponent.getDisplayName();
            if (displayName != null) {
               damageCauseMessage = displayName;
            }
         }

         return Message.translation("server.general.killedBy").param("damageSource", damageCauseMessage);
      }
   }

   public static class EnvironmentSource implements Damage.Source {
      @Nonnull
      private final String type;

      public EnvironmentSource(@Nonnull String type) {
         this.type = type;
      }

      @Nonnull
      public String getType() {
         return this.type;
      }

      @Nonnull
      @Override
      public Message getDeathMessage(@Nonnull Damage info, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
         return Message.translation("server.general.killedBy").param("damageSource", this.type);
      }
   }

   public static class Particles {
      @Nullable
      protected ModelParticle[] modelParticles;
      @Nullable
      protected WorldParticle[] worldParticles;
      protected double viewDistance;

      public Particles(@Nullable ModelParticle[] modelParticles, @Nullable WorldParticle[] worldParticles, double viewDistance) {
         this.modelParticles = modelParticles;
         this.worldParticles = worldParticles;
         this.viewDistance = viewDistance;
      }

      @Nullable
      public ModelParticle[] getModelParticles() {
         return this.modelParticles;
      }

      public void setModelParticles(@Nullable ModelParticle[] modelParticles) {
         this.modelParticles = modelParticles;
      }

      @Nullable
      public WorldParticle[] getWorldParticles() {
         return this.worldParticles;
      }

      public void setWorldParticles(@Nullable WorldParticle[] worldParticles) {
         this.worldParticles = worldParticles;
      }

      public double getViewDistance() {
         return this.viewDistance;
      }

      public void setViewDistance(double viewDistance) {
         this.viewDistance = viewDistance;
      }
   }

   public static class ProjectileSource extends Damage.EntitySource {
      @Nonnull
      protected final Ref<EntityStore> projectile;

      public ProjectileSource(@Nonnull Ref<EntityStore> shooter, @Nonnull Ref<EntityStore> projectile) {
         super(shooter);
         this.projectile = projectile;
      }

      @Nonnull
      public Ref<EntityStore> getProjectile() {
         return this.projectile;
      }
   }

   public static class SoundEffect {
      private int soundEventIndex;

      public SoundEffect(int soundEventIndex) {
         this.soundEventIndex = soundEventIndex;
      }

      public void setSoundEventIndex(int soundEventIndex) {
         this.soundEventIndex = soundEventIndex;
      }

      public int getSoundEventIndex() {
         return this.soundEventIndex;
      }
   }

   public interface Source {
      @Nonnull
      default Message getDeathMessage(@Nonnull Damage info, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
         DamageCause damageCauseAsset = DamageCause.getAssetMap().getAsset(info.damageCauseIndex);
         String causeId = damageCauseAsset != null ? damageCauseAsset.getId().toLowerCase(Locale.ROOT) : "unknown";
         Message damageCauseMessage = Message.translation("server.general.damageCauses." + causeId);
         return Message.translation("server.general.killedBy").param("damageSource", damageCauseMessage);
      }
   }
}
