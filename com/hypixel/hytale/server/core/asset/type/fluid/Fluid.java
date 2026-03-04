package com.hypixel.hytale.server.core.asset.type.fluid;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorSectionStart;
import com.hypixel.hytale.codec.schema.metadata.ui.UIPropertyTitle;
import com.hypixel.hytale.codec.schema.metadata.ui.UIRebuildCaches;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.BlockTextures;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.FluidDrawType;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.protocol.ShaderType;
import com.hypixel.hytale.server.core.asset.type.blockparticle.config.BlockParticleSet;
import com.hypixel.hytale.server.core.asset.type.blocksound.config.BlockSoundSet;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockTypeTextures;
import com.hypixel.hytale.server.core.asset.type.fluidfx.config.FluidFX;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.InteractionTypeUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.chunk.section.palette.ISectionPalette;
import com.hypixel.hytale.server.core.util.io.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Collections;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Fluid implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, Fluid>>, NetworkSerializable<com.hypixel.hytale.protocol.Fluid> {
   public static final AssetBuilderCodec<String, Fluid> CODEC = AssetBuilderCodec.builder(
         Fluid.class, Fluid::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (asset, data) -> asset.data = data, asset -> asset.data
      )
      .appendInherited(
         new KeyedCodec<>("MaxFluidLevel", Codec.INTEGER),
         (asset, v) -> asset.maxFluidLevel = v,
         asset -> asset.maxFluidLevel,
         (asset, parent) -> asset.maxFluidLevel = parent.maxFluidLevel
      )
      .addValidator(Validators.range(0, 15))
      .add()
      .<BlockTypeTextures[]>appendInherited(
         new KeyedCodec<>("Textures", new ArrayCodec<>(BlockTypeTextures.CODEC, BlockTypeTextures[]::new)),
         (fluid, o) -> fluid.textures = o,
         fluid -> fluid.textures,
         (fluid, parent) -> fluid.textures = parent.textures
      )
      .metadata(new UIPropertyTitle("Fluid Textures"))
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS, UIRebuildCaches.ClientCache.BLOCK_TEXTURES))
      .add()
      .<ShaderType[]>appendInherited(
         new KeyedCodec<>("Effect", new ArrayCodec<>(new EnumCodec<>(ShaderType.class), ShaderType[]::new)),
         (fluid, o) -> fluid.effect = o,
         fluid -> fluid.effect,
         (fluid, parent) -> fluid.effect = parent.effect
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .add()
      .<FluidDrawType>appendInherited(
         new KeyedCodec<>("DrawType", new EnumCodec<>(FluidDrawType.class)),
         (fluid, o) -> fluid.drawType = o,
         fluid -> fluid.drawType,
         (fluid, parent) -> fluid.drawType = parent.drawType
      )
      .addValidator(Validators.nonNull())
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS, UIRebuildCaches.ClientCache.BLOCK_TEXTURES, UIRebuildCaches.ClientCache.MODEL_TEXTURES))
      .metadata(new UIEditorSectionStart("Rendering"))
      .add()
      .<Opacity>appendInherited(
         new KeyedCodec<>("Opacity", new EnumCodec<>(Opacity.class)),
         (fluid, o) -> fluid.opacity = o,
         fluid -> fluid.opacity,
         (fluid, parent) -> fluid.opacity = parent.opacity
      )
      .addValidator(Validators.nonNull())
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("RequiresAlphaBlending", Codec.BOOLEAN),
         (fluid, o) -> fluid.requiresAlphaBlending = o,
         fluid -> fluid.requiresAlphaBlending,
         (fluid, parent) -> fluid.requiresAlphaBlending = parent.requiresAlphaBlending
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("FluidFXId", Codec.STRING),
         (fluid, o) -> fluid.fluidFXId = o,
         fluid -> fluid.fluidFXId,
         (fluid, parent) -> fluid.fluidFXId = parent.fluidFXId
      )
      .addValidator(FluidFX.VALIDATOR_CACHE.getValidator())
      .add()
      .<ModelParticle[]>appendInherited(
         new KeyedCodec<>("Particles", ModelParticle.ARRAY_CODEC),
         (fluid, s) -> fluid.particles = s,
         fluid -> fluid.particles,
         (fluid, parent) -> fluid.particles = parent.particles
      )
      .documentation("The particles defined here will be spawned on top of fluids of this type placed in the world.")
      .metadata(new UIPropertyTitle("Fluid Particles"))
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .addValidator(Validators.nonNullArrayElements())
      .add()
      .appendInherited(
         new KeyedCodec<>("Ticker", FluidTicker.CODEC), (fluid, o) -> fluid.ticker = o, fluid -> fluid.ticker, (fluid, parent) -> fluid.ticker = parent.ticker
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Light", ProtocolCodecs.COLOR_LIGHT),
         (fluid, o) -> fluid.light = o,
         fluid -> fluid.light,
         (fluid, parent) -> fluid.light = parent.light
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("DamageToEntities", Codec.INTEGER),
         (fluid, s) -> fluid.damageToEntities = s,
         fluid -> fluid.damageToEntities,
         (fluid, parent) -> fluid.damageToEntities = parent.damageToEntities
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("BlockParticleSetId", Codec.STRING),
         (fluid, s) -> fluid.blockParticleSetId = s,
         fluid -> fluid.blockParticleSetId,
         (fluid, parent) -> fluid.blockParticleSetId = parent.blockParticleSetId
      )
      .documentation(
         "The block particle set defined here defines which particles should be spawned when an entity interacts with this block (like when stepping on it for example"
      )
      .addValidator(BlockParticleSet.VALIDATOR_CACHE.getValidator())
      .add()
      .appendInherited(
         new KeyedCodec<>("ParticleColor", ProtocolCodecs.COLOR),
         (fluid, s) -> fluid.particleColor = s,
         fluid -> fluid.particleColor,
         (fluid, parent) -> fluid.particleColor = parent.particleColor
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("BlockSoundSetId", Codec.STRING),
         (fluid, o) -> fluid.blockSoundSetId = o,
         fluid -> fluid.blockSoundSetId,
         (fluid, parent) -> fluid.blockSoundSetId = parent.blockSoundSetId
      )
      .documentation("Sets the **BlockSoundSet** that will be used for this block for various events e.g. placement, breaking")
      .addValidator(BlockSoundSet.VALIDATOR_CACHE.getValidator())
      .add()
      .<Map<InteractionType, String>>appendInherited(
         new KeyedCodec<>("Interactions", new EnumMapCodec<>(InteractionType.class, RootInteraction.CHILD_ASSET_CODEC)),
         (item, v) -> item.interactions = v,
         item -> item.interactions,
         (item, parent) -> item.interactions = parent.interactions
      )
      .addValidator(RootInteraction.VALIDATOR_CACHE.getMapValueValidator())
      .metadata(new UIEditorSectionStart("Interactions"))
      .add()
      .afterDecode(Fluid::processConfig)
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(Fluid::getAssetStore));
   public static final String UNKNOWN_TEXTURE = "BlockTextures/Unknown.png";
   public static final BlockTextures[] UNKNOWN_BLOCK_TEXTURES = new BlockTextures[]{
      new BlockTextures(
         "BlockTextures/Unknown.png",
         "BlockTextures/Unknown.png",
         "BlockTextures/Unknown.png",
         "BlockTextures/Unknown.png",
         "BlockTextures/Unknown.png",
         "BlockTextures/Unknown.png",
         1.0F
      )
   };
   public static final ShaderType[] DEFAULT_SHADER_EFFECTS = new ShaderType[]{ShaderType.None};
   public static final ISectionPalette.KeySerializer KEY_SERIALIZER = (buf, id) -> {
      String key = getAssetMap().getAssetOrDefault(id, Fluid.UNKNOWN).getId();
      ByteBufUtil.writeUTF(buf, key);
   };
   public static final ToIntFunction<ByteBuf> KEY_DESERIALIZER = byteBuf -> {
      String fluid = ByteBufUtil.readUTF(byteBuf);
      return getFluidIdOrUnknown(fluid, "Failed to find fluid '%s' in chunk section!", fluid);
   };
   public static final int EMPTY_ID = 0;
   public static final String EMPTY_KEY = "Empty";
   public static final Fluid EMPTY = new Fluid("Empty") {
      {
         this.processConfig();
      }
   };
   public static final int UNKNOWN_ID = 1;
   public static final Fluid UNKNOWN = new Fluid("Unknown") {
      {
         this.unknown = true;
         this.processConfig();
      }
   };
   private static AssetStore<String, Fluid, IndexedLookupTableAssetMap<String, Fluid>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected boolean unknown;
   private int maxFluidLevel = 8;
   private BlockTypeTextures[] textures;
   private ShaderType[] effect;
   protected ModelParticle[] particles;
   private FluidDrawType drawType = FluidDrawType.Liquid;
   @Nonnull
   private Opacity opacity = Opacity.Solid;
   private boolean requiresAlphaBlending = true;
   private String fluidFXId = "Empty";
   protected transient int fluidFXIndex = 0;
   private FluidTicker ticker = DefaultFluidTicker.INSTANCE;
   protected int damageToEntities;
   protected ColorLight light;
   protected Color particleColor;
   protected String blockSoundSetId = "EMPTY";
   protected transient int blockSoundSetIndex = 0;
   public String blockParticleSetId;
   protected Map<InteractionType, String> interactions = Collections.emptyMap();
   protected transient boolean isTrigger;

   public static AssetStore<String, Fluid, IndexedLookupTableAssetMap<String, Fluid>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(Fluid.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, Fluid> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, Fluid>)getAssetStore().getAssetMap();
   }

   public Fluid() {
   }

   public Fluid(String id) {
      this.id = id;
   }

   public Fluid(@Nonnull Fluid other) {
      this.data = other.data;
      this.id = other.id;
      this.unknown = other.unknown;
      this.maxFluidLevel = other.maxFluidLevel;
      this.textures = other.textures;
      this.effect = other.effect;
      this.opacity = other.opacity;
      this.requiresAlphaBlending = other.requiresAlphaBlending;
      this.fluidFXId = other.fluidFXId;
      this.particles = other.particles;
      this.drawType = other.drawType;
      this.damageToEntities = other.damageToEntities;
      this.light = other.light;
      this.particleColor = other.particleColor;
      this.blockSoundSetId = other.blockSoundSetId;
      this.interactions = other.interactions;
      this.isTrigger = other.isTrigger;
      this.processConfig();
   }

   public AssetExtraInfo.Data getData() {
      return this.data;
   }

   public String getId() {
      return this.id;
   }

   public boolean isUnknown() {
      return this.unknown;
   }

   public int getMaxFluidLevel() {
      return this.maxFluidLevel;
   }

   public boolean hasEffect(ShaderType shader) {
      if (this.effect == null) {
         return false;
      } else {
         for (ShaderType e : this.effect) {
            if (e == shader) {
               return true;
            }
         }

         return false;
      }
   }

   public FluidTicker getTicker() {
      return this.ticker;
   }

   public int getDamageToEntities() {
      return this.damageToEntities;
   }

   public String getFluidFXId() {
      return this.fluidFXId;
   }

   public int getFluidFXIndex() {
      return this.fluidFXIndex;
   }

   public ColorLight getLight() {
      return this.light;
   }

   public Color getParticleColor() {
      return this.particleColor;
   }

   public boolean isTrigger() {
      return this.isTrigger;
   }

   public Map<InteractionType, String> getInteractions() {
      return this.interactions;
   }

   protected void processConfig() {
      this.fluidFXIndex = this.fluidFXId.equals("Empty") ? 0 : FluidFX.getAssetMap().getIndex(this.fluidFXId);
      this.blockSoundSetIndex = this.blockSoundSetId.equals("EMPTY") ? 0 : BlockSoundSet.getAssetMap().getIndex(this.blockSoundSetId);

      for (InteractionType type : this.interactions.keySet()) {
         if (InteractionTypeUtils.isCollisionType(type)) {
            this.isTrigger = true;
            break;
         }
      }
   }

   @Nonnull
   public static Fluid getUnknownFor(String key) {
      return UNKNOWN.clone(key);
   }

   @Nonnull
   public Fluid clone(String newKey) {
      if (this.id != null && this.id.equals(newKey)) {
         return this;
      } else {
         Fluid fluid = new Fluid(this);
         fluid.id = newKey;
         return fluid;
      }
   }

   public static int getFluidIdOrUnknown(String key, String message, Object... params) {
      return getFluidIdOrUnknown(getAssetMap(), key, message, params);
   }

   public static int getFluidIdOrUnknown(@Nonnull IndexedLookupTableAssetMap<String, Fluid> assetMap, String key, String message, Object... params) {
      int fluidId = assetMap.getIndex(key);
      if (fluidId == Integer.MIN_VALUE) {
         HytaleLogger.getLogger().at(Level.WARNING).logVarargs(message, params);
         AssetRegistry.getAssetStore(Fluid.class).loadAssets("Hytale:Hytale", Collections.singletonList(getUnknownFor(key)));
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         fluidId = index;
      }

      return fluidId;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.Fluid toPacket() {
      com.hypixel.hytale.protocol.Fluid packet = new com.hypixel.hytale.protocol.Fluid();
      packet.id = this.id;
      packet.maxFluidLevel = this.maxFluidLevel;
      packet.fluidFXIndex = this.fluidFXIndex;
      packet.opacity = this.opacity;
      packet.light = this.light;
      if (this.effect != null && this.effect.length > 0) {
         packet.shaderEffect = this.effect;
      } else {
         packet.shaderEffect = DEFAULT_SHADER_EFFECTS;
      }

      if (this.textures != null && this.textures.length > 0) {
         int totalWeight = 0;

         for (BlockTypeTextures texture : this.textures) {
            totalWeight = (int)(totalWeight + texture.getWeight());
         }

         BlockTextures[] texturePackets = new BlockTextures[this.textures.length];

         for (int i = 0; i < this.textures.length; i++) {
            texturePackets[i] = this.textures[i].toPacket(totalWeight);
         }

         packet.cubeTextures = texturePackets;
      } else {
         packet.cubeTextures = UNKNOWN_BLOCK_TEXTURES;
      }

      if (this.particles != null && this.particles.length > 0) {
         packet.particles = new com.hypixel.hytale.protocol.ModelParticle[this.particles.length];

         for (int i = 0; i < this.particles.length; i++) {
            packet.particles[i] = this.particles[i].toPacket();
         }
      }

      packet.drawType = this.drawType;
      packet.requiresAlphaBlending = this.requiresAlphaBlending;
      packet.blockSoundSetIndex = this.blockSoundSetIndex;
      packet.blockParticleSetId = this.blockParticleSetId;
      packet.particleColor = this.particleColor;
      packet.fluidFXIndex = this.fluidFXIndex;
      if (this.data != null) {
         IntSet tags = this.data.getExpandedTagIndexes();
         if (tags != null) {
            packet.tagIndexes = tags.toIntArray();
         }
      }

      return packet;
   }

   @Nullable
   @Deprecated(forRemoval = true)
   public static String convertLegacyName(@Nonnull String fluidName, byte level) {
      return switch (fluidName) {
         case "Fluid_Water" -> level == 0 ? "Water_Source" : "Water";
         case "Fluid_Water_Test" -> "Water_Finite";
         case "Fluid_Lava" -> level == 0 ? "Lava_Source" : "Lava";
         case "Fluid_Tar" -> level == 0 ? "Tar_Source" : "Tar";
         case "Fluid_Slime" -> level == 0 ? "Slime_Source" : "Slime";
         case "Fluid_Poison" -> level == 0 ? "Poison_Source" : "Poison";
         default -> null;
      };
   }

   @Nullable
   @Deprecated(forRemoval = true)
   public static Fluid.ConversionResult convertBlockToFluid(@Nonnull String blockTypeStr) {
      int fluidPos = blockTypeStr.indexOf("|Fluid=");
      if (fluidPos != -1) {
         int fluidNameEnd = blockTypeStr.indexOf(124, fluidPos + 2);
         String fluidName;
         if (fluidNameEnd != -1) {
            fluidName = blockTypeStr.substring(fluidPos + "|Fluid=".length(), fluidNameEnd);
            blockTypeStr = blockTypeStr.substring(0, fluidPos) + blockTypeStr.substring(fluidNameEnd);
         } else {
            fluidName = blockTypeStr.substring(fluidPos + "|Fluid=".length());
            blockTypeStr = blockTypeStr.substring(0, fluidPos);
         }

         int fluidLevelStart = blockTypeStr.indexOf("|FluidLevel=");
         byte fluidLevel;
         if (fluidLevelStart != -1) {
            int fluidLevelEnd = blockTypeStr.indexOf(124, fluidLevelStart + 2);
            String fluidLevelStr;
            if (fluidLevelEnd != -1) {
               fluidLevelStr = blockTypeStr.substring(fluidLevelStart + "|FluidLevel=".length(), fluidLevelEnd);
               blockTypeStr = blockTypeStr.substring(0, fluidLevelStart) + blockTypeStr.substring(fluidLevelEnd);
            } else {
               fluidLevelStr = blockTypeStr.substring(fluidLevelStart + "|FluidLevel=".length());
               blockTypeStr = blockTypeStr.substring(0, fluidLevelStart);
            }

            fluidLevel = Byte.parseByte(fluidLevelStr);
         } else {
            fluidLevel = 0;
         }

         fluidName = convertLegacyName(fluidName, fluidLevel);
         int fluidId = getFluidIdOrUnknown(fluidName, "Failed to find fluid '%s'", fluidName);
         fluidLevel = fluidLevel == 0 ? (byte)getAssetMap().getAsset(fluidId).getMaxFluidLevel() : fluidLevel;
         return new Fluid.ConversionResult(blockTypeStr, fluidId, fluidLevel);
      } else if (blockTypeStr.startsWith("Fluid_")) {
         int fluidLevelStart = blockTypeStr.indexOf("|FluidLevel=");
         byte fluidLevel;
         if (fluidLevelStart != -1) {
            int fluidLevelEnd = blockTypeStr.indexOf(124, fluidLevelStart + 2);
            String fluidLevelStr;
            if (fluidLevelEnd != -1) {
               fluidLevelStr = blockTypeStr.substring(fluidLevelStart + "|FluidLevel=".length(), fluidLevelEnd);
               blockTypeStr = blockTypeStr.substring(0, fluidLevelStart) + blockTypeStr.substring(fluidLevelEnd);
            } else {
               fluidLevelStr = blockTypeStr.substring(fluidLevelStart + "|FluidLevel=".length());
               blockTypeStr = blockTypeStr.substring(0, fluidLevelStart);
            }

            fluidLevel = Byte.parseByte(fluidLevelStr);
         } else {
            fluidLevel = 0;
         }

         String newFluidName = convertLegacyName(blockTypeStr, fluidLevel);
         int fluidId = getFluidIdOrUnknown(newFluidName, "Failed to find fluid '%s'", newFluidName);
         fluidLevel = fluidLevel == 0 ? (byte)getAssetMap().getAsset(fluidId).getMaxFluidLevel() : fluidLevel;
         return new Fluid.ConversionResult(null, fluidId, fluidLevel);
      } else {
         return null;
      }
   }

   @Deprecated(forRemoval = true)
   public static class ConversionResult {
      public String blockTypeStr;
      public int fluidId;
      public byte fluidLevel;

      public ConversionResult(String blockTypeStr, int fluidId, byte fluidLevel) {
         this.blockTypeStr = blockTypeStr;
         this.fluidId = fluidId;
         this.fluidLevel = fluidLevel;
      }
   }
}
