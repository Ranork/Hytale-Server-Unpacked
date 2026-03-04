package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTool;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.Knockback;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExplosionConfig {
   @Nonnull
   public static final BuilderCodec<ExplosionConfig> CODEC = BuilderCodec.builder(ExplosionConfig.class, ExplosionConfig::new)
      .appendInherited(
         new KeyedCodec<>("DamageEntities", Codec.BOOLEAN),
         (explosionConfig, b) -> explosionConfig.damageEntities = b,
         explosionConfig -> explosionConfig.damageEntities,
         (explosionConfig, parent) -> explosionConfig.damageEntities = parent.damageEntities
      )
      .documentation("Determines whether the explosion should damage entities.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("DamageBlocks", Codec.BOOLEAN),
         (explosionConfig, b) -> explosionConfig.damageBlocks = b,
         explosionConfig -> explosionConfig.damageBlocks,
         (explosionConfig, parent) -> explosionConfig.damageBlocks = parent.damageBlocks
      )
      .documentation("Determines whether the explosion should damage blocks.")
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("BlockDamageRadius", Codec.INTEGER),
         (explosionConfig, i) -> explosionConfig.blockDamageRadius = i,
         explosionConfig -> explosionConfig.blockDamageRadius,
         (explosionConfig, parent) -> explosionConfig.blockDamageRadius = parent.blockDamageRadius
      )
      .documentation("The radius in which blocks should be damaged by the explosion.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("BlockDamageFalloff", Codec.FLOAT),
         (explosionConfig, f) -> explosionConfig.blockDamageFalloff = f,
         explosionConfig -> explosionConfig.entityDamageFalloff,
         (explosionConfig, parent) -> explosionConfig.entityDamageFalloff = parent.entityDamageFalloff
      )
      .documentation("The falloff applied to the block damage.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("BlockDropChance", Codec.FLOAT),
         (explosionConfig, f) -> explosionConfig.blockDropChance = f,
         explosionConfig -> explosionConfig.blockDropChance,
         (explosionConfig, parent) -> explosionConfig.blockDropChance = parent.blockDropChance
      )
      .documentation("The chance in which a block drops its loot after breaking.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("EntityDamageRadius", Codec.FLOAT),
         (explosionConfig, f) -> explosionConfig.entityDamageRadius = f,
         explosionConfig -> explosionConfig.entityDamageRadius,
         (explosionConfig, parent) -> explosionConfig.entityDamageRadius = parent.entityDamageRadius
      )
      .documentation("The radius in which entities should be damaged by the explosion.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("EntityDamage", Codec.FLOAT),
         (explosionConfig, f) -> explosionConfig.entityDamage = f,
         explosionConfig -> explosionConfig.entityDamage,
         (explosionConfig, parent) -> explosionConfig.entityDamage = parent.entityDamage
      )
      .documentation("The amount of damage to be applied to entities within range.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("EntityDamageFalloff", Codec.FLOAT),
         (explosionConfig, f) -> explosionConfig.entityDamageFalloff = f,
         explosionConfig -> explosionConfig.entityDamageFalloff,
         (explosionConfig, parent) -> explosionConfig.entityDamageFalloff = parent.entityDamageFalloff
      )
      .documentation("The falloff applied to the entity damage.")
      .add()
      .<Knockback>appendInherited(
         new KeyedCodec<>("Knockback", Knockback.CODEC),
         (explosionConfig, s) -> explosionConfig.knockback = s,
         explosionConfig -> explosionConfig.knockback,
         (explosionConfig, parent) -> explosionConfig.knockback = parent.knockback
      )
      .documentation("Determines the knockback effect applied to damaged entities.")
      .add()
      .<ItemTool>appendInherited(
         new KeyedCodec<>("ItemTool", ItemTool.CODEC),
         (damageEffects, s) -> damageEffects.itemTool = s,
         damageEffects -> damageEffects.itemTool,
         (damageEffects, parent) -> damageEffects.itemTool = parent.itemTool
      )
      .documentation("The item tool to reference when applying damage to blocks.")
      .add()
      .build();
   protected boolean damageEntities = true;
   protected boolean damageBlocks = true;
   protected int blockDamageRadius = 3;
   protected float blockDamageFalloff = 1.0F;
   protected float entityDamageRadius = 5.0F;
   protected float entityDamage = 50.0F;
   protected float entityDamageFalloff = 1.0F;
   protected float blockDropChance = 1.0F;
   @Nullable
   protected Knockback knockback;
   @Nullable
   protected ItemTool itemTool;
}
