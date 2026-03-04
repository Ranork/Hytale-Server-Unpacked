package com.hypixel.hytale.builtin.commandmacro;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MacroCommandBuilder implements JsonAssetWithMap<String, DefaultAssetMap<String, MacroCommandBuilder>> {
   @Nonnull
   public static final AssetBuilderCodec<String, MacroCommandBuilder> CODEC = AssetBuilderCodec.builder(
         MacroCommandBuilder.class,
         MacroCommandBuilder::new,
         Codec.STRING,
         (builder, id) -> builder.id = id,
         builder -> builder.id,
         (builder, data) -> builder.data = data,
         builder -> builder.data
      )
      .append(new KeyedCodec<>("Name", Codec.STRING, true), (builder, name) -> builder.name = name, builder -> builder.name)
      .add()
      .append(new KeyedCodec<>("Aliases", Codec.STRING_ARRAY, false), (builder, aliases) -> builder.aliases = aliases, builder -> builder.aliases)
      .add()
      .append(new KeyedCodec<>("Description", Codec.STRING, true), (builder, description) -> builder.description = description, builder -> builder.description)
      .add()
      .append(
         new KeyedCodec<>("Parameters", new ArrayCodec<>(MacroCommandParameter.CODEC, MacroCommandParameter[]::new), false),
         (builder, parameters) -> builder.parameters = parameters,
         builder -> builder.parameters
      )
      .add()
      .append(new KeyedCodec<>("Commands", Codec.STRING_ARRAY, true), (builder, commands) -> builder.commands = commands, builder -> builder.commands)
      .add()
      .build();
   private String id;
   private String name;
   private String[] aliases;
   private String description;
   private MacroCommandParameter[] parameters;
   private String[] commands;
   private AssetExtraInfo.Data data;

   @Nullable
   public static CommandRegistration createAndRegisterCommand(@Nonnull MacroCommandBuilder builder) {
      if (builder.name == null) {
         return null;
      } else {
         MacroCommandBase macroCommandBase = new MacroCommandBase(builder.name, builder.aliases, builder.description, builder.parameters, builder.commands);
         return MacroCommandPlugin.get().getCommandRegistry().registerCommand(macroCommandBase);
      }
   }

   public String getName() {
      return this.name;
   }

   public String getId() {
      return this.id;
   }
}
