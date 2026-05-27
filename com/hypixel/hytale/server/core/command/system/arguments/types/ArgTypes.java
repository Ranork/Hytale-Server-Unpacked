package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.common.util.StringCompareUtil;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Rotation3fc;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.asset.type.ambiencefx.config.AmbienceFX;
import com.hypixel.hytale.server.core.asset.type.audiostate.config.AudioState;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.musiccontainer.config.MusicContainer;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.auth.ProfileServiceClient;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.system.ArgWrapper;
import com.hypixel.hytale.server.core.command.system.arguments.system.Argument;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.modules.entity.repulsion.RepulsionConfig;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockFilter;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.Pair;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3i;

public final class ArgTypes {
   public static final SingleArgumentType<Boolean> BOOLEAN = new SingleArgumentType<Boolean>(
      "server.commands.parsing.argtype.boolean.name", "server.commands.parsing.argtype.boolean.usage", "true", "false"
   ) {
      private static final String TRUE_STRING = "true";
      private static final String FALSE_STRING = "false";

      @Nonnull
      public Boolean parse(String input, ParseResult parseResult) {
         return Boolean.parseBoolean(input);
      }

      @Override
      public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
         textAlreadyEntered = textAlreadyEntered.toLowerCase();
         if ("false".startsWith(textAlreadyEntered)) {
            result.suggest("false");
            result.suggest("true");
         } else {
            result.suggest("true");
            result.suggest("false");
         }
      }

      @Override
      public int getSuggestionValueCount() {
         return 2;
      }
   };
   public static final SingleArgumentType<Integer> INTEGER = new SingleArgumentType<Integer>(
      "server.commands.parsing.argtype.integer.name", "server.commands.parsing.argtype.integer.usage", "-27432", "-1", "0", "1", "56346"
   ) {
      @Nullable
      public Integer parse(@Nonnull String input, ParseResult parseResult) {
         try {
            return Integer.parseInt(input);
         } catch (NumberFormatException var4) {
            parseResult.fail(Message.translation("server.commands.parsing.argtype.integer.fail").param("input", input));
            return null;
         }
      }
   };
   public static final SingleArgumentType<String> STRING = new SingleArgumentType<String>(
      "server.commands.parsing.argtype.string.name",
      "server.commands.parsing.argtype.string.usage",
      "\"Hytale is really cool!\"",
      "\"Numbers work 2!\"",
      "\"If you can type it...\""
   ) {
      public String parse(String input, ParseResult parseResult) {
         return input;
      }
   };
   public static final SingleArgumentType<String> GREEDY_STRING = new SingleArgumentType<String>(
      "server.commands.parsing.argtype.greedystring.name",
      "server.commands.parsing.argtype.greedystring.usage",
      "Hello world!",
      "Let's go everyone",
      "This is a multi-word sentence."
   ) {
      public String parse(String input, ParseResult parseResult) {
         return input;
      }

      @Override
      public boolean isGreedyString() {
         return true;
      }
   };
   public static final SingleArgumentType<Float> FLOAT = new SingleArgumentType<Float>(
      "server.commands.parsing.argtype.float.name", "server.commands.parsing.argtype.float.usage", "3.14159", "-2.5", "7"
   ) {
      @Nullable
      public Float parse(@Nonnull String input, ParseResult parseResult) {
         try {
            return Float.parseFloat(input);
         } catch (NumberFormatException var4) {
            parseResult.fail(Message.translation("server.commands.parsing.argtype.float.fail").param("input", input));
            return null;
         }
      }
   };
   public static final SingleArgumentType<Double> DOUBLE = new SingleArgumentType<Double>(
      "server.commands.parsing.argtype.double.name", "server.commands.parsing.argtype.double.usage", "-3.14", "0.0", "3.141596"
   ) {
      @Nullable
      public Double parse(@Nonnull String input, ParseResult parseResult) {
         try {
            return Double.parseDouble(input);
         } catch (NumberFormatException var4) {
            parseResult.fail(Message.translation("server.commands.parsing.argtype.double.fail").param("input", input));
            return null;
         }
      }
   };
   public static final SingleArgumentType<UUID> UUID = new SingleArgumentType<UUID>(
      "server.commands.parsing.argtype.uuid.name", "server.commands.parsing.argtype.uuid.usage", java.util.UUID.randomUUID().toString()
   ) {
      @Nullable
      public UUID parse(@Nonnull String input, ParseResult parseResult) {
         try {
            return java.util.UUID.fromString(input);
         } catch (IllegalArgumentException var4) {
            parseResult.fail(Message.translation("server.commands.parsing.argtype.uuid.fail").param("input", input));
            return null;
         }
      }
   };
   public static final SingleArgumentType<UUID> PLAYER_UUID = new SingleArgumentType<UUID>(
      "server.commands.parsing.argtype.playerUuid.name",
      "server.commands.parsing.argtype.playerUuid.usage",
      java.util.UUID.randomUUID().toString(),
      "john_doe",
      "user123"
   ) {
      @Nullable
      public UUID parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
         try {
            return java.util.UUID.fromString(input);
         } catch (IllegalArgumentException var7) {
            for (World world : Universe.get().getWorlds().values()) {
               Collection<PlayerRef> playerRefs = world.getPlayerRefs();
               PlayerRef playerRef = NameMatching.DEFAULT.find(playerRefs, input, PlayerRef::getUsername);
               if (playerRef != null) {
                  return playerRef.getUuid();
               }
            }

            parseResult.fail(Message.translation("server.commands.parsing.argtype.playerUuid.fail").param("input", input));
            return null;
         }
      }
   };
   public static final SingleArgumentType<CompletableFuture<ProfileServiceClient.PublicGameProfile>> GAME_PROFILE_LOOKUP_ASYNC = new SingleArgumentType<CompletableFuture<ProfileServiceClient.PublicGameProfile>>(
      "server.commands.parsing.argtype.playerUuidLookup.name",
      "server.commands.parsing.argtype.playerUuidLookup.usage",
      java.util.UUID.randomUUID().toString(),
      "john_doe",
      "user123"
   ) {
      @Nullable
      public CompletableFuture<ProfileServiceClient.PublicGameProfile> parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
         for (World world : Universe.get().getWorlds().values()) {
            Collection<PlayerRef> playerRefs = world.getPlayerRefs();
            PlayerRef playerRef = NameMatching.DEFAULT.find(playerRefs, input, PlayerRef::getUsername);
            if (playerRef != null) {
               return CompletableFuture.completedFuture(new ProfileServiceClient.PublicGameProfile(playerRef.getUuid(), playerRef.getUsername()));
            }
         }

         UUID inputAsUuid = null;

         try {
            inputAsUuid = java.util.UUID.fromString(input);
         } catch (IllegalArgumentException var8) {
         }

         ServerAuthManager authManager = ServerAuthManager.getInstance();
         String sessionToken = authManager.getSessionToken();
         if (sessionToken == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("No session token available for profile lookup"));
         } else {
            ProfileServiceClient profileClient = authManager.getProfileServiceClient();
            CompletableFuture<ProfileServiceClient.PublicGameProfile> future;
            if (inputAsUuid != null) {
               future = profileClient.getProfileByUuidAsync(inputAsUuid, sessionToken);
            } else {
               future = profileClient.getProfileByUsernameAsync(input, sessionToken);
            }

            return future.thenApply(profile -> {
               if (profile != null && profile.getUuid() != null) {
                  return (ProfileServiceClient.PublicGameProfile)profile;
               } else {
                  throw new IllegalArgumentException("Player not found: " + input);
               }
            });
         }
      }
   };
   public static final SingleArgumentType<ProfileServiceClient.PublicGameProfile> GAME_PROFILE_LOOKUP = new SingleArgumentType<ProfileServiceClient.PublicGameProfile>(
      "server.commands.parsing.argtype.playerUuidLookup.name",
      "server.commands.parsing.argtype.playerUuidLookup.usage",
      java.util.UUID.randomUUID().toString(),
      "john_doe",
      "user123"
   ) {
      @Nullable
      public ProfileServiceClient.PublicGameProfile parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
         try {
            return ArgTypes.GAME_PROFILE_LOOKUP_ASYNC.parse(input, parseResult).join();
         } catch (Exception var5) {
            Throwable cause = (Throwable)(var5.getCause() != null ? var5.getCause() : var5);
            if (cause instanceof IllegalStateException) {
               parseResult.fail(Message.translation("server.commands.parsing.argtype.playerUuidLookup.noAuth").param("input", input));
            } else if (cause instanceof IllegalArgumentException) {
               parseResult.fail(Message.translation("server.commands.parsing.argtype.playerUuidLookup.notFound").param("input", input));
            } else {
               parseResult.fail(
                  Message.translation("server.commands.parsing.argtype.playerUuidLookup.lookupError")
                     .param("input", input)
                     .param("error", cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName())
               );
            }

            return null;
         }
      }

      @Override
      public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
         ArgTypes.suggestOnlinePlayers(sender, textAlreadyEntered, result);
      }
   };
   public static final SingleArgumentType<Coord> RELATIVE_DOUBLE_COORD = new SingleArgumentType<Coord>(
      "server.commands.parsing.argtype.doubleCoordinate.name", "server.commands.parsing.argtype.doubleCoordinate.usage", "5.0", "~-2.3", "0.0"
   ) {
      @Nullable
      public Coord parse(@Nonnull String input, ParseResult parseResult) {
         try {
            return Coord.parse(input);
         } catch (NumberFormatException var4) {
            parseResult.fail(Message.translation("server.commands.parsing.argtype.doubleCoordinate.fail").param("input", input));
            return null;
         }
      }

      @Override
      public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
         if (textAlreadyEntered.isEmpty() || textAlreadyEntered.startsWith("~")) {
            result.suggest("~");
         }
      }
   };
   public static final SingleArgumentType<IntCoord> RELATIVE_INT_COORD = (new SingleArgumentType<Object>(
         "server.commands.parsing.argtype.integerCoordinate.name", "server.commands.parsing.argtype.integerCoordinate.usage", "5", "~-2", "0"
      ) {
         @Nullable
         public IntCoord parse(@Nonnull String input, ParseResult parseResult) {
            try {
               return IntCoord.parse(input);
            } catch (NumberFormatException var4) {
               parseResult.fail(Message.translation("server.commands.parsing.argtype.integerCoordinate.fail").param("input", input));
               return null;
            }
         }

         @Override
         public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
            if (textAlreadyEntered.isEmpty() || textAlreadyEntered.startsWith("~")) {
               result.suggest("~");
            }
         }
      })
      .withSharedSuggestions(RELATIVE_DOUBLE_COORD);
   public static final SingleArgumentType<RelativeInteger> RELATIVE_INTEGER = new SingleArgumentType<RelativeInteger>(
      "Relative Integer", "A tilde to mark an integer as relative to a base", "5", "~-2", "0"
   ) {
      @Nullable
      public RelativeInteger parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
         return RelativeInteger.parse(input, parseResult);
      }
   };
   public static final SingleArgumentType<RelativeFloat> RELATIVE_FLOAT = (new SingleArgumentType<Object>(
         "server.commands.parsing.argtype.relativeFloat.name", "server.commands.parsing.argtype.relativeFloat.usage", "90.0", "~-45.5", "~"
      ) {
         @Nullable
         public RelativeFloat parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
            return RelativeFloat.parse(input, parseResult);
         }

         @Override
         public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
            if (textAlreadyEntered.isEmpty() || textAlreadyEntered.startsWith("~")) {
               result.suggest("~");
            }
         }
      })
      .withSharedSuggestions(RELATIVE_DOUBLE_COORD);
   public static final SingleArgumentType<PlayerRef> PLAYER_REF = (new SingleArgumentType<PlayerRef>(
         "server.commands.parsing.argtype.player.name", "server.commands.parsing.argtype.player.usage", "john_doe", "user123"
      ) {
         @Nullable
         public PlayerRef parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
            PlayerRef playerRef = null;

            for (World world : Universe.get().getWorlds().values()) {
               Collection<PlayerRef> playerRefs = world.getPlayerRefs();
               playerRef = NameMatching.DEFAULT.find(playerRefs, input, PlayerRef::getUsername);
               if (playerRef != null) {
                  break;
               }
            }

            if (playerRef == null) {
               parseResult.fail(Message.translation("server.commands.errors.noSuchPlayer").param("username", input));
               return null;
            } else {
               return playerRef;
            }
         }

         @Override
         public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
            ArgTypes.suggestOnlinePlayers(sender, textAlreadyEntered, result);
         }

         @Nonnull
         public PlayerRef processedGet(CommandSender sender, @Nonnull CommandContext context, Argument<?, PlayerRef> argument) {
            PlayerRef playerRef = context.get(argument);
            if (playerRef != null) {
               return playerRef;
            } else if (sender instanceof PlayerRef senderPlayerRef) {
               return senderPlayerRef;
            } else {
               throw new GeneralCommandException(Message.translation("server.commands.errors.playerOrArg").param("option", "player"));
            }
         }
      })
      .withSharedSuggestions(GAME_PROFILE_LOOKUP);
   public static final SingleArgumentType<World> WORLD = new SingleArgumentType<World>(
      "server.commands.parsing.argtype.world.name", "server.commands.parsing.argtype.world.usage", "default"
   ) {
      @Nullable
      public World parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
         World world = Universe.get().getWorld(input);
         if (world == null) {
            parseResult.fail(Message.translation("server.commands.errors.noSuchWorld").param("name", input));
            return null;
         } else {
            return world;
         }
      }

      @Override
      public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
         String lowerInput = textAlreadyEntered.toLowerCase();

         for (World world : Universe.get().getWorlds().values()) {
            String worldName = world.getName();
            if (worldName != null && worldName.toLowerCase().startsWith(lowerInput)) {
               result.suggest(worldName);
            }
         }
      }

      @Nullable
      public World processedGet(CommandSender sender, @Nonnull CommandContext context, @Nonnull Argument<?, World> argument) {
         World world = argument.get(context);
         if (world != null) {
            return world;
         } else {
            Universe universe = Universe.get();
            if (context.isPlayer()) {
               Ref<EntityStore> playerRef = context.senderAsPlayerRef();
               if (playerRef != null && playerRef.isValid()) {
                  return playerRef.getStore().getExternalData().getWorld();
               }
            }

            if (universe.getWorlds().size() == 1) {
               Iterator<World> iterator = universe.getWorlds().values().iterator();
               if (iterator.hasNext()) {
                  return iterator.next();
               }
            }

            throw new GeneralCommandException(Message.translation("server.commands.errors.playerOrArg").param("option", "world"));
         }
      }
   };
   public static final SingleArgumentType<ModelAsset> MODEL_ASSET = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.model.name", ModelAsset.class, "server.commands.parsing.argtype.asset.model.usage"
   );
   public static final SingleArgumentType<Weather> WEATHER_ASSET = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.weather.name", Weather.class, "server.commands.parsing.argtype.asset.weather.usage"
   );
   public static final SingleArgumentType<Interaction> INTERACTION_ASSET = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.interaction.name", Interaction.class, "server.commands.parsing.argtype.asset.interaction.usage"
   );
   public static final SingleArgumentType<RootInteraction> ROOT_INTERACTION_ASSET = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.rootinteraction.name", RootInteraction.class, "server.commands.parsing.argtype.asset.interaction.usage"
   );
   public static final SingleArgumentType<EntityEffect> EFFECT_ASSET = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.effect.name", EntityEffect.class, "server.commands.parsing.argtype.asset.effect.usage"
   );
   public static final SingleArgumentType<Environment> ENVIRONMENT_ASSET = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.environment.name", Environment.class, "server.commands.parsing.argtype.asset.environment.usage"
   );
   public static final SingleArgumentType<Item> ITEM_ASSET = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.item.name", Item.class, "server.commands.parsing.argtype.asset.item.usage"
   );
   public static final SingleArgumentType<BlockType> BLOCK_TYPE_ASSET = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.blocktype.name", BlockType.class, "server.commands.parsing.argtype.asset.blocktype.usage"
   );
   public static final SingleArgumentType<ParticleSystem> PARTICLE_SYSTEM = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.particlesystem.name", ParticleSystem.class, "server.commands.parsing.argtype.asset.particlesystem.usage"
   );
   public static final SingleArgumentType<HitboxCollisionConfig> HITBOX_COLLISION_CONFIG = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.hitboxcollisionconfig.name",
      HitboxCollisionConfig.class,
      "server.commands.parsing.argtype.asset.hitboxcollisionconfig.usage"
   );
   public static final SingleArgumentType<RepulsionConfig> REPULSION_CONFIG = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.repulsionconfig.name", RepulsionConfig.class, "server.commands.parsing.argtype.asset.repulsionconfig.usage"
   );
   public static final SingleArgumentType<SoundEvent> SOUND_EVENT_ASSET = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.soundevent.name", SoundEvent.class, "server.commands.parsing.argtype.asset.soundevent.usage"
   );
   public static final SingleArgumentType<AmbienceFX> AMBIENCE_FX_ASSET = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.ambiencefx.name", AmbienceFX.class, "server.commands.parsing.argtype.asset.ambiencefx.usage"
   );
   public static final SingleArgumentType<MusicContainer> MUSIC_CONTAINER_ASSET = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.musiccontainer.name", MusicContainer.class, "server.commands.parsing.argtype.asset.musiccontainer.usage"
   );
   public static final SingleArgumentType<AudioState> AUDIO_STATE_ASSET = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.audiostate.name", AudioState.class, "server.commands.parsing.argtype.asset.audiostate.usage"
   );
   public static final SingleArgumentType<SoundCategory> SOUND_CATEGORY = forEnum("server.commands.parsing.argtype.soundcategory.name", SoundCategory.class);
   public static final ArgWrapper<EntityWrappedArg, UUID> ENTITY_ID = new ArgWrapper<>(
      UUID.withOverriddenUsage("server.commands.parsing.argtype.entityid.usage"), EntityWrappedArg::new
   );
   public static final SingleArgumentType<ArgTypes.IntegerComparisonOperator> INTEGER_COMPARISON_OPERATOR = new SingleArgumentType<ArgTypes.IntegerComparisonOperator>(
      "Integer Comparison Operator", "A mathematical sign for integer comparison", ">", "<", ">=", "<=", "%", "=", "!="
   ) {
      @Nullable
      public ArgTypes.IntegerComparisonOperator parse(String input, @Nonnull ParseResult parseResult) {
         ArgTypes.IntegerComparisonOperator integerComparisonOperator = ArgTypes.IntegerComparisonOperator.getFromStringRepresentation(input);
         if (integerComparisonOperator == null) {
            parseResult.fail(Message.raw("Could not find an integer comparison operator for value: '" + input + "'."));
            return null;
         } else {
            return integerComparisonOperator;
         }
      }
   };
   public static final SingleArgumentType<ArgTypes.IntegerOperation> INTEGER_OPERATION = new SingleArgumentType<ArgTypes.IntegerOperation>(
      "Integer Operator", "A mathematical sign for performing an operation", "+", "-", "*", "/", "%", "="
   ) {
      @Nullable
      public ArgTypes.IntegerOperation parse(String input, @Nonnull ParseResult parseResult) {
         ArgTypes.IntegerOperation integerOperation = ArgTypes.IntegerOperation.getFromStringRepresentation(input);
         if (integerOperation == null) {
            parseResult.fail(Message.raw("Could not find an integer operator for value: '" + input + "'."));
            return null;
         } else {
            return integerOperation;
         }
      }
   };
   public static final ArgumentType<Pair<Integer, Integer>> INT_RANGE = new MultiArgumentType<Pair<Integer, Integer>>(
      "Integer Range", "Two integers representing a minimum and maximum of a range", "-2 8", "5 5", "1 5"
   ) {
      private final WrappedArgumentType<Integer> minValue = this.withParameter(
         "min", "Minimum value, must be less than or equal to max value", ArgTypes.INTEGER
      );
      private final WrappedArgumentType<Integer> maxValue = this.withParameter(
         "max", "Maximum value, must be greater than or equal to min value", ArgTypes.INTEGER
      );

      @Nullable
      public Pair<Integer, Integer> parse(@Nonnull MultiArgumentContext context, @Nonnull ParseResult parseResult) {
         if (context.get(this.minValue) > context.get(this.maxValue)) {
            parseResult.fail(
               Message.raw(
                  "You cannot set the minimum value as larger than the maximum value. Min: "
                     + context.get(this.minValue)
                     + " Max: "
                     + context.get(this.maxValue)
               )
            );
            return null;
         } else {
            return Pair.of(context.get(this.minValue), context.get(this.maxValue));
         }
      }
   };
   public static final ArgumentType<RelativeIntegerRange> RELATIVE_INT_RANGE = new MultiArgumentType<RelativeIntegerRange>(
      "Integer Range", "Two integers representing a minimum and maximum of a range", "~-2 ~8", "~5 ~5", "~1 ~5"
   ) {
      private final WrappedArgumentType<RelativeInteger> minValue = this.withParameter(
         "min", "Minimum value, must be less than or equal to max value", ArgTypes.RELATIVE_INTEGER
      );
      private final WrappedArgumentType<RelativeInteger> maxValue = this.withParameter(
         "max", "Maximum value, must be greater than or equal to min value", ArgTypes.RELATIVE_INTEGER
      );

      @Nullable
      public RelativeIntegerRange parse(@Nonnull MultiArgumentContext context, @Nonnull ParseResult parseResult) {
         RelativeInteger min = this.minValue.get(context);
         RelativeInteger max = this.maxValue.get(context);
         if (min != null && max != null) {
            if (min.isRelative() != max.isRelative()) {
               parseResult.fail(Message.raw("Your range must have both min and max as relative, or both as not relative. You can not mix relatives in ranges."));
               return null;
            } else if (min.getRawValue() > max.getRawValue()) {
               parseResult.fail(
                  Message.raw(
                     "You cannot set the minimum value as larger than the maximum value. Min: "
                        + context.get(this.minValue)
                        + " Max: "
                        + context.get(this.maxValue)
                  )
               );
               return null;
            } else {
               return new RelativeIntegerRange(context.get(this.minValue), context.get(this.maxValue));
            }
         } else {
            parseResult.fail(Message.raw("Could not parse min or max value of the range."));
            return null;
         }
      }
   };
   public static final ArgumentType<Vector2i> VECTOR2I = new MultiArgumentType<Vector2i>(
      "Integer Vector 2D", "Two integers, generally corresponding to x/z axis", "124 232", "5 -3", "1 1"
   ) {
      private final WrappedArgumentType<Integer> xValue = this.withParameter("x", "X value", ArgTypes.INTEGER);
      private final WrappedArgumentType<Integer> zValue = this.withParameter("z", "Z value", ArgTypes.INTEGER);

      @Nonnull
      public Vector2i parse(@Nonnull MultiArgumentContext context, ParseResult parseResult) {
         return new Vector2i(context.get(this.xValue), context.get(this.zValue));
      }
   };
   public static final ArgumentType<Vector3i> VECTOR3I = new MultiArgumentType<Vector3i>(
      "Integer Vector", "Three integers, generally corresponding to x/y/z axis", "124 232 234", "5 0 -3", "1 1 1"
   ) {
      private final WrappedArgumentType<Integer> xValue = this.withParameter("x", "X value", ArgTypes.INTEGER);
      private final WrappedArgumentType<Integer> yValue = this.withParameter("y", "Y value", ArgTypes.INTEGER);
      private final WrappedArgumentType<Integer> zValue = this.withParameter("z", "Z value", ArgTypes.INTEGER);

      @Nonnull
      public Vector3i parse(@Nonnull MultiArgumentContext context, ParseResult parseResult) {
         return new Vector3i(context.get(this.xValue), context.get(this.yValue), context.get(this.zValue));
      }
   };
   public static final ArgumentType<RelativeVector3i> RELATIVE_VECTOR3I = new MultiArgumentType<RelativeVector3i>(
      "Relative Integer Vector", "Three optionally relative integers, generally corresponding to x/y/z axis", "124 ~232 234", "~5 0 ~-3", "1 ~1 1"
   ) {
      private final WrappedArgumentType<RelativeInteger> xValue = this.withParameter("x", "X value", ArgTypes.RELATIVE_INTEGER);
      private final WrappedArgumentType<RelativeInteger> yValue = this.withParameter("y", "Y value", ArgTypes.RELATIVE_INTEGER);
      private final WrappedArgumentType<RelativeInteger> zValue = this.withParameter("z", "Z value", ArgTypes.RELATIVE_INTEGER);

      @Nonnull
      public RelativeVector3i parse(@Nonnull MultiArgumentContext context, ParseResult parseResult) {
         return new RelativeVector3i(context.get(this.xValue), context.get(this.yValue), context.get(this.zValue));
      }
   };
   public static final ArgumentType<RelativeIntPosition> RELATIVE_BLOCK_POSITION = new MultiArgumentType<RelativeIntPosition>(
      "server.commands.parsing.argtype.relativeBlockPosition.name",
      "server.commands.parsing.argtype.relativeBlockPosition.usage",
      "124 232 234",
      "~5 ~ ~-3",
      "~ ~ ~"
   ) {
      private final WrappedArgumentType<IntCoord> xValue = this.withParameter("x", "server.commands.parsing.argtype.xCoord.usage", ArgTypes.RELATIVE_INT_COORD);
      private final WrappedArgumentType<IntCoord> yValue = this.withParameter("y", "server.commands.parsing.argtype.yCoord.usage", ArgTypes.RELATIVE_INT_COORD);
      private final WrappedArgumentType<IntCoord> zValue = this.withParameter("z", "server.commands.parsing.argtype.zCoord.usage", ArgTypes.RELATIVE_INT_COORD);

      @Nonnull
      public RelativeIntPosition parse(@Nonnull MultiArgumentContext context, ParseResult parseResult) {
         return new RelativeIntPosition(context.get(this.xValue), context.get(this.yValue), context.get(this.zValue));
      }

      @Override
      public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
         ArgTypes.suggestPosition(
            sender, textAlreadyEntered, result, "~ ~ ~", pos -> (int)Math.floor(pos.x) + " " + (int)Math.floor(pos.y) + " " + (int)Math.floor(pos.z)
         );
      }
   };
   public static final ArgumentType<RelativeDoublePosition> RELATIVE_POSITION = new MultiArgumentType<RelativeDoublePosition>(
      "server.commands.parsing.argtype.relativePosition.name",
      "server.commands.parsing.argtype.relativePosition.usage",
      "124.63 232.27 234.22",
      "~5.5 ~ ~",
      "~ ~ ~"
   ) {
      private final WrappedArgumentType<Coord> xValue = this.withParameter("x", "server.commands.parsing.argtype.xCoord.usage", ArgTypes.RELATIVE_DOUBLE_COORD);
      private final WrappedArgumentType<Coord> yValue = this.withParameter("y", "server.commands.parsing.argtype.yCoord.usage", ArgTypes.RELATIVE_DOUBLE_COORD);
      private final WrappedArgumentType<Coord> zValue = this.withParameter("z", "server.commands.parsing.argtype.zCoord.usage", ArgTypes.RELATIVE_DOUBLE_COORD);

      @Nonnull
      public RelativeDoublePosition parse(@Nonnull MultiArgumentContext context, ParseResult parseResult) {
         return new RelativeDoublePosition(context.get(this.xValue), context.get(this.yValue), context.get(this.zValue));
      }

      @Override
      public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
         ArgTypes.suggestPosition(sender, textAlreadyEntered, result, "~ ~ ~", pos -> String.format("%.2f %.2f %.2f", pos.x, pos.y, pos.z));
      }
   };
   public static final ArgumentType<RelativeChunkPosition> RELATIVE_CHUNK_POSITION = new MultiArgumentType<RelativeChunkPosition>(
      "server.commands.parsing.argtype.relativeChunkPosition.name", "server.commands.parsing.argtype.relativeChunkPosition.usage", "5 10", "~c2 ~c-3", "~ ~"
   ) {
      private final WrappedArgumentType<IntCoord> xValue = this.withParameter("x", "server.commands.parsing.argtype.xCoord.usage", ArgTypes.RELATIVE_INT_COORD);
      private final WrappedArgumentType<IntCoord> zValue = this.withParameter("z", "server.commands.parsing.argtype.zCoord.usage", ArgTypes.RELATIVE_INT_COORD);

      @Nonnull
      public RelativeChunkPosition parse(@Nonnull MultiArgumentContext context, ParseResult parseResult) {
         return new RelativeChunkPosition(context.get(this.xValue), context.get(this.zValue));
      }

      @Override
      public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
         ArgTypes.suggestPosition(sender, textAlreadyEntered, result, "~ ~", pos -> ((int)Math.floor(pos.x) >> 5) + " " + ((int)Math.floor(pos.z) >> 5));
      }
   };
   public static final ArgumentType<Rotation3fc> ROTATION = new MultiArgumentType<Rotation3fc>(
      "server.commands.parsing.argtype.rotation.name", "server.commands.parsing.argtype.rotation.usage", "124.63 232.27 234.22"
   ) {
      private final WrappedArgumentType<Float> pitch = this.withParameter(
         "server.commands.parsing.argtype.pitch.name", "server.commands.parsing.argtype.pitch.usage", ArgTypes.FLOAT
      );
      private final WrappedArgumentType<Float> yaw = this.withParameter(
         "server.commands.parsing.argtype.yaw.name", "server.commands.parsing.argtype.yaw.usage", ArgTypes.FLOAT
      );
      private final WrappedArgumentType<Float> roll = this.withParameter(
         "server.commands.parsing.argtype.roll.name", "server.commands.parsing.argtype.roll.usage", ArgTypes.FLOAT
      );

      @Nonnull
      public Rotation3fc parse(@Nonnull MultiArgumentContext context, ParseResult parseResult) {
         return new Rotation3f(context.get(this.pitch), context.get(this.yaw), context.get(this.roll));
      }
   };
   public static final SingleArgumentType<String> BLOCK_TYPE_KEY = new SingleArgumentType<String>("Block Type Key", "A block type", "Wood_Drywood_Planks_Half") {
      @Nullable
      public String parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
         try {
            return input;
         } catch (Exception var4) {
            parseResult.fail(Message.raw(var4.getMessage()));
            return null;
         }
      }

      @Override
      public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
         ArgTypes.suggestBlockTypeKeys(textAlreadyEntered, "", result);
      }

      @Nonnull
      @Override
      public int getSuggestionValueCount() {
         return BlockType.getAssetMap().getAssetMap().size();
      }
   };
   public static final ArgumentType<Integer> BLOCK_ID = new ProcessedArgumentType<String, Integer>(
      "Block Id", Message.raw("A block type, converted to an int id"), BLOCK_TYPE_KEY, "Wood_Drywood_Planks_Half"
   ) {
      @Nonnull
      public Integer processInput(String blockTypeKey) {
         return BlockType.getAssetMap().getIndex(blockTypeKey);
      }
   };
   public static final SingleArgumentType<Integer> COLOR = new SingleArgumentType<Integer>(
      "server.commands.parsing.argtype.color.name", "server.commands.parsing.argtype.color.usage", "#FF0000", "#00FF00FF", "16711680", "0xFF0000"
   ) {
      @Nullable
      public Integer parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
         input = input.trim();
         if (input.isEmpty()) {
            parseResult.fail(Message.raw("Color cannot be empty"));
            return null;
         } else if (input.charAt(0) == '#') {
            try {
               String hexString = input.substring(1);
               long value = Long.parseLong(hexString, 16);
               switch (hexString.length()) {
                  case 3:
                     int red = (int)(value >> 8 & 15L);
                     int green = (int)(value >> 4 & 15L);
                     int blue = (int)(value & 15L);
                     return 0xFF000000 | red << 20 | red << 16 | green << 12 | green << 8 | blue << 4 | blue;
                  case 4:
                     int red4 = (int)(value >> 12 & 15L);
                     int green4 = (int)(value >> 8 & 15L);
                     int blue4 = (int)(value >> 4 & 15L);
                     int alpha4 = (int)(value & 15L);
                     return red4 << 28 | red4 << 24 | green4 << 20 | green4 << 16 | blue4 << 12 | blue4 << 8 | alpha4 << 4 | alpha4;
                  case 5:
                  case 7:
                  default:
                     parseResult.fail(Message.raw("Invalid hex color format. Expected #RGB, #RGBA, #RRGGBB, or #RRGGBBAA, got: '" + input + "'"));
                     return null;
                  case 6:
                     return 0xFF000000 | (int)value;
                  case 8:
                     return (int)value;
               }
            } catch (NumberFormatException var13) {
               parseResult.fail(Message.raw("Invalid hex color: '" + input + "'. " + var13.getMessage()));
               return null;
            }
         } else if (input.length() <= 2 || !input.startsWith("0x") && !input.startsWith("0X")) {
            try {
               return Integer.parseInt(input);
            } catch (NumberFormatException var15) {
               parseResult.fail(
                  Message.raw("Invalid color format. Expected hex color (#RRGGBB), hex integer (0xFF0000), or decimal integer (16711680), got: '" + input + "'")
               );
               return null;
            }
         } else {
            try {
               return Integer.parseUnsignedInt(input.substring(2), 16);
            } catch (NumberFormatException var14) {
               parseResult.fail(Message.raw("Invalid hex integer color: '" + input + "'. " + var14.getMessage()));
               return null;
            }
         }
      }
   };
   public static final ArgumentType<Pair<Integer, String>> LAYER_ENTRY_TYPE = new MultiArgumentType<Pair<Integer, String>>(
      "Layer Entry Type", "A thickness for a corresponding block pattern", "1 Rock_Stone", "3 50%Rock_Basalt;50%Rock_Stone"
   ) {
      private final WrappedArgumentType<Integer> thickness = this.withParameter("thickness", "How thick the layer should be", ArgTypes.INTEGER);
      private final WrappedArgumentType<String> blockPattern = this.withParameter(
         "blockPattern", "The block pattern to use for the layer. If using with percentages, separate values with a ';'", ArgTypes.STRING
      );

      @Nonnull
      public Pair<Integer, String> parse(@Nonnull MultiArgumentContext context, ParseResult parseResult) {
         return Pair.of(context.get(this.thickness), context.get(this.blockPattern));
      }
   };
   public static final ArgumentType<Pair<Integer, String>> WEIGHTED_BLOCK_TYPE = new MultiArgumentType<Pair<Integer, String>>(
      "Weighted Block Type", "A weight corresponding to a blocktype", "5 Empty", "20 Rock_Stone", "2 Rock_Shale"
   ) {
      private final WrappedArgumentType<Integer> weight = this.withParameter(
         "weight", "The relative weight of this entry. Think of it as a lottery ticket", ArgTypes.INTEGER
      );
      private final WrappedArgumentType<String> blockType = this.withParameter(
         "blockType", "The BlockTypeKey associated with the weight", ArgTypes.BLOCK_TYPE_KEY
      );

      @Nonnull
      public Pair<Integer, String> parse(@Nonnull MultiArgumentContext context, ParseResult parseResult) {
         return Pair.of(context.get(this.weight), context.get(this.blockType));
      }
   };
   private static final ArgumentType<String> WEIGHTED_BLOCK_ENTRY = new SingleArgumentType<String>(
      "Weighted Block Entry", "A block with optional weight prefix (e.g., 20%Rock_Stone or Rock_Stone)", "Rock_Stone", "20%Rock_Stone", "50%Rock_Stone|Yaw=90"
   ) {
      @Nullable
      public String parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
         if (input.isEmpty()) {
            parseResult.fail(Message.raw("Block entry cannot be empty"));
            return null;
         } else {
            String blockName = input;
            int percentIndex = input.indexOf(37);
            if (percentIndex != -1) {
               blockName = input.substring(percentIndex + 1);
            }

            int pipeIndex = blockName.indexOf(124);
            if (pipeIndex != -1) {
               blockName = blockName.substring(0, pipeIndex);
            }

            int blockId = BlockType.getAssetMap().getIndex(blockName);
            if (blockId == Integer.MIN_VALUE) {
               parseResult.fail(Message.translation("server.builderTools.invalidBlockType").param("name", "").param("key", blockName));
               return null;
            } else {
               return input;
            }
         }
      }

      @Override
      public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
         int percentIndex = textAlreadyEntered.indexOf(37);
         if (percentIndex >= 0) {
            String prefix = textAlreadyEntered.substring(0, percentIndex + 1);
            String blockPartial = textAlreadyEntered.substring(percentIndex + 1);
            int pipeIndex = blockPartial.indexOf(124);
            if (pipeIndex < 0) {
               ArgTypes.suggestBlockTypeKeys(blockPartial, prefix, result);
               if (!blockPartial.isEmpty() && ArgTypes.isExactBlockTypeKey(blockPartial)) {
                  result.suggestContinuation(textAlreadyEntered + ",");
               }
            }
         } else if (!textAlreadyEntered.isEmpty()
            && Character.isDigit(textAlreadyEntered.charAt(0))
            && textAlreadyEntered.chars().allMatch(c -> Character.isDigit((char)c) || c == 46)) {
            result.suggestContinuation(textAlreadyEntered + "%");
         } else {
            ArgTypes.suggestBlockTypeKeys(textAlreadyEntered, "", result);
         }
      }
   };
   public static final ArgumentType<BlockPattern> BLOCK_PATTERN = new ProcessedArgumentType<List<String>, BlockPattern>(
      "Block Pattern",
      Message.raw("A list of blocks with optional weights (e.g., [20%Rock_Stone, 80%Rock_Shale])"),
      new ListArgumentType<>(WEIGHTED_BLOCK_ENTRY),
      "[Rock_Stone]",
      "[20%Rock_Stone, 80%Rock_Shale]",
      "[50%Rock_Stone|Yaw=90, 50%Fluid_Water]"
   ) {
      @Nonnull
      public BlockPattern processInput(@Nonnull List<String> entries) {
         String patternString = String.join(",", entries);
         return BlockPattern.parse(patternString);
      }
   };
   private static final String[] MASK_PREFIXES = new String[]{"!", ">", "<", "~", "^", "#", "+n", "+e", "+s", "+w", "%xy", "%xz", "%zy"};
   private static final String[] MASK_INVERT_PREFIXES = new String[]{"!>", "!<", "!~", "!^", "!#", "!+n", "!+e", "!+s", "!+w", "!%xy", "!%xz", "!%zy"};
   private static final ArgumentType<BlockMask> INDIVIDUAL_BLOCK_MASK = new SingleArgumentType<BlockMask>(
      "Block Mask", "Create a block mask using symbols and block names", ">Grass_Full", "!Fluid_Water", "!^Fluid_Lava", "!#"
   ) {
      @Nullable
      public BlockMask parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
         try {
            BlockMask mask = BlockMask.parse(input);
            if (mask.hasInvalidBlocks()) {
               BlockFilter.ParsedFilterParts parts = BlockFilter.parseComponents(input);
               parseResult.fail(Message.translation("server.builderTools.invalidBlockType").param("key", parts.blocks()));
               return null;
            } else {
               return mask;
            }
         } catch (Exception var5) {
            parseResult.fail(Message.raw("There was an error in the parsing of your block mask: " + input + ", please try again."));
            return null;
         }
      }

      @Override
      public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
         if (textAlreadyEntered.isEmpty()) {
            for (String p : ArgTypes.MASK_PREFIXES) {
               result.suggest(p);
            }
         } else if ("!".equals(textAlreadyEntered)) {
            for (String p : ArgTypes.MASK_INVERT_PREFIXES) {
               result.suggest(p);
            }

            ArgTypes.suggestBlockTypeKeys(textAlreadyEntered, "!", result);
         } else {
            String stripped = textAlreadyEntered;
            String prefix = "";

            for (String p : ArgTypes.MASK_INVERT_PREFIXES) {
               if (stripped.startsWith(p)) {
                  prefix = p;
                  stripped = stripped.substring(p.length());
                  break;
               }
            }

            if (prefix.isEmpty()) {
               for (String px : ArgTypes.MASK_PREFIXES) {
                  if (!px.equals("#") && stripped.startsWith(px)) {
                     prefix = px;
                     stripped = stripped.substring(px.length());
                     break;
                  }
               }
            }

            ArgTypes.suggestBlockTypeKeys(stripped, prefix, result);
         }
      }
   };
   public static final ArgumentType<BlockMask> BLOCK_MASK = new ProcessedArgumentType<List<BlockMask>, BlockMask>(
      "Block Mask",
      Message.raw("A list of block masks that combine together"),
      new ListArgumentType<>(INDIVIDUAL_BLOCK_MASK),
      "[!Fluid_Water, !^Fluid_Lava]",
      "[>Grass_Full, !#]"
   ) {
      public BlockMask processInput(@Nonnull List<BlockMask> masks) {
         return BlockMask.combine(masks.toArray(BlockMask[]::new));
      }
   };
   public static final SingleArgumentType<Integer> TICK_RATE = new SingleArgumentType<Integer>(
      "server.commands.parsing.argtype.tickrate.name", "server.commands.parsing.argtype.tickrate.usage", "30tps", "33ms", "60", "20tps", "50ms"
   ) {
      @Nullable
      public Integer parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
         String trimmed = input.trim().toLowerCase();
         if (trimmed.isEmpty()) {
            parseResult.fail(Message.translation("server.commands.parsing.argtype.tickrate.empty"));
            return null;
         } else {
            try {
               if (trimmed.endsWith("tps")) {
                  String value = trimmed.substring(0, trimmed.length() - 3).trim();
                  return Integer.parseInt(value);
               } else if (trimmed.endsWith("ms")) {
                  String value = trimmed.substring(0, trimmed.length() - 2).trim();
                  double ms = Double.parseDouble(value);
                  if (ms <= 0.0) {
                     parseResult.fail(Message.translation("server.commands.parsing.argtype.tickrate.invalidMs").param("input", input));
                     return null;
                  } else {
                     return (int)Math.round(1000.0 / ms);
                  }
               } else {
                  return Integer.parseInt(trimmed);
               }
            } catch (NumberFormatException var7) {
               parseResult.fail(Message.translation("server.commands.parsing.argtype.tickrate.fail").param("input", input));
               return null;
            }
         }
      }

      @Override
      public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
         result.suggest("30tps");
         result.suggest("60tps");
         result.suggest("20tps");
         result.suggest("33ms");
         result.suggest("16ms");
         result.suggest("50ms");
      }
   };
   public static final SingleArgumentType<GameMode> GAME_MODE = new GameModeArgumentType();
   private static final int MAX_BLOCK_SUGGESTIONS = 20;

   @Nonnull
   public static <E extends Enum<E>> SingleArgumentType<E> forEnum(String name, @Nonnull Class<E> enumType) {
      return new EnumArgumentType<>(name, enumType);
   }

   private static boolean isExactBlockTypeKey(@Nonnull String name) {
      for (String key : BlockType.getAssetMap().getAssetMap().keySet()) {
         if (key.toString().equalsIgnoreCase(name)) {
            return true;
         }
      }

      return false;
   }

   private static void suggestBlockTypeKeys(@Nonnull String textAlreadyEntered, @Nonnull String prefix, @Nonnull SuggestionResult result) {
      Set<String> keys = BlockType.getAssetMap().getAssetMap().keySet();
      if (textAlreadyEntered.isEmpty()) {
         int count = 0;

         for (String key : keys) {
            result.suggest(prefix + key.toString());
            if (++count >= 20) {
               break;
            }
         }
      } else {
         int minScore = textAlreadyEntered.length();
         List<String> sorted = StringUtil.sortByFuzzyDistance(textAlreadyEntered, keys);
         int count = 0;

         for (String keyx : sorted) {
            if (StringCompareUtil.getFuzzyDistance(keyx.toString(), textAlreadyEntered, Locale.ENGLISH) < minScore) {
               break;
            }

            result.suggest(prefix + keyx.toString());
            if (++count >= 20) {
               break;
            }
         }
      }
   }

   private static void suggestOnlinePlayers(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, @Nonnull SuggestionResult result) {
      String lowerInput = textAlreadyEntered.toLowerCase();

      for (World world : Universe.get().getWorlds().values()) {
         for (PlayerRef player : world.getPlayerRefs()) {
            String username = player.getUsername();
            if (username != null && username.toLowerCase().startsWith(lowerInput)) {
               result.suggest(username);
            }
         }
      }
   }

   private static void suggestPosition(
      @Nonnull CommandSender sender,
      @Nonnull String textAlreadyEntered,
      @Nonnull SuggestionResult result,
      @Nonnull String tildeTemplate,
      @Nonnull Function<Vector3d, String> formatter
   ) {
      if (textAlreadyEntered.isEmpty() || textAlreadyEntered.startsWith("~")) {
         result.suggest(tildeTemplate);
      }

      if (sender instanceof PlayerRef playerRef) {
         Vector3d pos = playerRef.getTransform().getPosition();
         result.suggest(formatter.apply(pos));
      }
   }

   public static enum IntegerComparisonOperator {
      GREATER_THAN((left, right) -> left > right, ">"),
      GREATER_THAN_EQUAL_TO((left, right) -> left >= right, ">="),
      LESS_THAN((left, right) -> left < right, "<"),
      LESS_THAN_EQUAL_TO((left, right) -> left <= right, "<="),
      MOD_EQUAL_ZERO((left, right) -> left % right == 0, "%"),
      MOD_NOT_EQUAL_ZERO((left, right) -> left % right != 0, "!%"),
      EQUAL_TO(Integer::equals, "="),
      NOT_EQUAL_TO((left, right) -> !left.equals(right), "!=");

      private final BiFunction<Integer, Integer, Boolean> comparisonFunction;
      private final String stringRepresentation;

      private IntegerComparisonOperator(BiFunction<Integer, Integer, Boolean> comparisonFunction, String stringRepresentation) {
         this.comparisonFunction = comparisonFunction;
         this.stringRepresentation = stringRepresentation;
      }

      public boolean compare(int left, int right) {
         return this.comparisonFunction.apply(left, right);
      }

      public String getStringRepresentation() {
         return this.stringRepresentation;
      }

      @Nullable
      public static ArgTypes.IntegerComparisonOperator getFromStringRepresentation(String stringRepresentation) {
         for (ArgTypes.IntegerComparisonOperator value : values()) {
            if (value.stringRepresentation.equals(stringRepresentation)) {
               return value;
            }
         }

         return null;
      }
   }

   public static enum IntegerOperation {
      ADD(Integer::sum, "+"),
      SUBTRACT((previous, modifier) -> previous - modifier, "-"),
      MULTIPLY((previous, modifier) -> previous * modifier, "*"),
      DIVIDE((previous, modifier) -> previous / modifier, "/"),
      MODULUS((previous, modifier) -> previous % modifier, "%"),
      SET((previous, modifier) -> modifier, "=");

      @Nonnull
      private final BiFunction<Integer, Integer, Integer> operationFunction;
      @Nonnull
      private final String stringRepresentation;

      private IntegerOperation(@Nonnull final BiFunction<Integer, Integer, Integer> operationFunction, @Nonnull final String stringRepresentation) {
         this.operationFunction = operationFunction;
         this.stringRepresentation = stringRepresentation;
      }

      public int operate(int previous, int modifier) {
         return this.operationFunction.apply(previous, modifier);
      }

      public String getStringRepresentation() {
         return this.stringRepresentation;
      }

      @Nullable
      public static ArgTypes.IntegerOperation getFromStringRepresentation(@Nonnull String stringRepresentation) {
         for (ArgTypes.IntegerOperation value : values()) {
            if (value.stringRepresentation.equals(stringRepresentation)) {
               return value;
            }
         }

         return null;
      }
   }
}
