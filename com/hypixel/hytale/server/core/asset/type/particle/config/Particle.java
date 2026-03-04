package com.hypixel.hytale.server.core.asset.type.particle.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.map.Int2ObjectMapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorSectionStart;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.MapKeyValidator;
import com.hypixel.hytale.protocol.ParticleScaleRatioConstraint;
import com.hypixel.hytale.protocol.ParticleUVOption;
import com.hypixel.hytale.protocol.Size;
import com.hypixel.hytale.protocol.SoftParticle;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javax.annotation.Nonnull;

public class Particle implements NetworkSerializable<com.hypixel.hytale.protocol.Particle> {
   public static final BuilderCodec<Particle> CODEC = BuilderCodec.builder(Particle.class, Particle::new)
      .append(new KeyedCodec<>("Texture", Codec.STRING), (particle, s) -> particle.texture = s, particle -> particle.texture)
      .addValidator(Validators.nonNull())
      .addValidator(CommonAssetValidator.TEXTURE_PARTICLES)
      .metadata(new UIEditorSectionStart("Material"))
      .add()
      .addField(new KeyedCodec<>("FrameSize", ProtocolCodecs.SIZE), (particle, o) -> particle.frameSize = o, particle -> particle.frameSize)
      .<SoftParticle>append(
         new KeyedCodec<>("SoftParticles", new EnumCodec<>(SoftParticle.class)), (particle, o) -> particle.softParticle = o, particle -> particle.softParticle
      )
      .addValidator(Validators.nonNull())
      .add()
      .<Float>append(
         new KeyedCodec<>("SoftParticlesFadeFactor", Codec.FLOAT),
         (particle, f) -> particle.softParticlesFadeFactor = f,
         particle -> particle.softParticlesFadeFactor
      )
      .addValidator(Validators.range(0.1F, 2.0F))
      .add()
      .appendInherited(
         new KeyedCodec<>("UseSpriteBlending", Codec.BOOLEAN),
         (particle, s) -> particle.useSpriteBlending = s,
         particle -> particle.useSpriteBlending,
         (particle, parent) -> particle.useSpriteBlending = parent.useSpriteBlending
      )
      .add()
      .<Int2ObjectMap<ParticleAnimationFrame>>append(
         new KeyedCodec<>("Animation", new Int2ObjectMapCodec<>(ParticleAnimationFrame.CODEC, Int2ObjectOpenHashMap::new)),
         (particle, o) -> particle.animation = o,
         particle -> particle.animation
      )
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyMap())
      .addValidator(new MapKeyValidator<>(Validators.range(0, 100)))
      .metadata(new UIEditorSectionStart("Animation"))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .append(
         new KeyedCodec<>("CollisionAnimationFrame", ParticleAnimationFrame.CODEC),
         (particle, o) -> particle.collisionAnimationFrame = o,
         particle -> particle.collisionAnimationFrame
      )
      .add()
      .<ParticleUVOption>append(
         new KeyedCodec<>("UVOption", new EnumCodec<>(ParticleUVOption.class)), (particle, o) -> particle.uvOption = o, particle -> particle.uvOption
      )
      .addValidator(Validators.nonNull())
      .metadata(new UIEditorSectionStart("Initial Frame"))
      .add()
      .<ParticleScaleRatioConstraint>append(
         new KeyedCodec<>("ScaleRatioConstraint", new EnumCodec<>(ParticleScaleRatioConstraint.class)),
         (particle, o) -> particle.scaleRatioConstraint = o,
         particle -> particle.scaleRatioConstraint
      )
      .addValidator(Validators.nonNull())
      .add()
      .<ParticleAnimationFrame>append(
         new KeyedCodec<>("InitialAnimationFrame", ParticleAnimationFrame.CODEC),
         (particle, o) -> particle.initialAnimationFrame = o,
         particle -> particle.initialAnimationFrame
      )
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .build();
   protected String texture;
   protected Size frameSize;
   @Nonnull
   protected ParticleUVOption uvOption = ParticleUVOption.None;
   @Nonnull
   protected ParticleScaleRatioConstraint scaleRatioConstraint = ParticleScaleRatioConstraint.OneToOne;
   @Nonnull
   protected SoftParticle softParticle = SoftParticle.Enable;
   protected float softParticlesFadeFactor = 1.0F;
   protected boolean useSpriteBlending;
   protected ParticleAnimationFrame initialAnimationFrame;
   protected ParticleAnimationFrame collisionAnimationFrame;
   protected Int2ObjectMap<ParticleAnimationFrame> animation;

   public Particle(
      String texture,
      Size frameSize,
      ParticleUVOption uvOption,
      ParticleScaleRatioConstraint scaleRatioConstraint,
      SoftParticle softParticle,
      float softParticlesFadeFactor,
      boolean useSpriteBlending,
      ParticleAnimationFrame initialAnimationFrame,
      ParticleAnimationFrame collisionAnimationFrame,
      Int2ObjectMap<ParticleAnimationFrame> animation
   ) {
      this.texture = texture;
      this.frameSize = frameSize;
      this.uvOption = uvOption;
      this.scaleRatioConstraint = scaleRatioConstraint;
      this.softParticle = softParticle;
      this.softParticlesFadeFactor = softParticlesFadeFactor;
      this.useSpriteBlending = useSpriteBlending;
      this.initialAnimationFrame = initialAnimationFrame;
      this.collisionAnimationFrame = collisionAnimationFrame;
      this.animation = animation;
   }

   protected Particle() {
   }

   public String getTexture() {
      return this.texture;
   }

   public Size getFrameSize() {
      return this.frameSize;
   }

   public ParticleUVOption getUvOption() {
      return this.uvOption;
   }

   public ParticleScaleRatioConstraint getScaleRatioConstraint() {
      return this.scaleRatioConstraint;
   }

   public SoftParticle getSoftParticle() {
      return this.softParticle;
   }

   public float getSoftParticlesFadeFactor() {
      return this.softParticlesFadeFactor;
   }

   public boolean isUseSpriteBlending() {
      return this.useSpriteBlending;
   }

   public ParticleAnimationFrame getInitialAnimationFrame() {
      return this.initialAnimationFrame;
   }

   public ParticleAnimationFrame getCollisionAnimationFrame() {
      return this.collisionAnimationFrame;
   }

   public Int2ObjectMap<ParticleAnimationFrame> getAnimation() {
      return this.animation;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.Particle toPacket() {
      com.hypixel.hytale.protocol.Particle packet = new com.hypixel.hytale.protocol.Particle();
      packet.texturePath = this.texture;
      packet.frameSize = this.frameSize;
      packet.uvOption = this.uvOption;
      packet.scaleRatioConstraint = this.scaleRatioConstraint;
      packet.softParticles = this.softParticle;
      packet.softParticlesFadeFactor = this.softParticlesFadeFactor;
      packet.useSpriteBlending = this.useSpriteBlending;
      if (this.initialAnimationFrame != null) {
         packet.initialAnimationFrame = this.initialAnimationFrame.toPacket();
      }

      if (this.collisionAnimationFrame != null) {
         packet.collisionAnimationFrame = this.collisionAnimationFrame.toPacket();
      }

      if (this.animation != null) {
         packet.animationFrames = new Int2ObjectOpenHashMap();
         ObjectIterator var2 = this.animation.int2ObjectEntrySet().iterator();

         while (var2.hasNext()) {
            Entry<ParticleAnimationFrame> entry = (Entry<ParticleAnimationFrame>)var2.next();
            packet.animationFrames.put(entry.getIntKey(), ((ParticleAnimationFrame)entry.getValue()).toPacket());
         }
      }

      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Particle{texture='"
         + this.texture
         + "', frameSize="
         + this.frameSize
         + ", uvOption="
         + this.uvOption
         + ", scaleRatioConstraint="
         + this.scaleRatioConstraint
         + ", softParticle="
         + this.softParticle
         + ", softParticlesFadeFactor="
         + this.softParticlesFadeFactor
         + ", useSpriteBlending="
         + this.useSpriteBlending
         + ", initialAnimationFrame="
         + this.initialAnimationFrame
         + ", collisionAnimationFrame="
         + this.collisionAnimationFrame
         + ", animation="
         + this.animation
         + "}";
   }
}
