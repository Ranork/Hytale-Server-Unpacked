package com.hypixel.hytale.builtin.commandmacro;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
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
      .<String>append(
         new KeyedCodec<>("Description", Codec.STRING, true), (builder, description) -> builder.description = description, builder -> builder.description
      )
      .addValidator(Validators.serverLocKeyValidator())
      .add()
      .append(
         new KeyedCodec<>("Parameters", new ArrayCodec<>(MacroCommandParameter.CODEC, MacroCommandParameter[]::new), false),
         (builder, parameters) -> builder.parameters = parameters,
         builder -> builder.parameters
      )
      .add()
      .append(new KeyedCodec<>("Commands", Codec.STRING_ARRAY, true), (builder, commands) -> builder.commands = commands, builder -> builder.commands)
      .add()
      .append(
         new KeyedCodec<>("PermissionGroup", Codec.STRING, false),
         (builder, permissionGroup) -> builder.permissionGroup = permissionGroup,
         builder -> builder.permissionGroup
      )
      .add()
      .build();
   private String id;
   private String name;
   private String[] aliases;
   private String description;
   private MacroCommandParameter[] parameters;
   private String[] commands;
   private String permissionGroup;
   private AssetExtraInfo.Data data;

   @Nullable
   public String[] getPathTokens() {
      if (this.name == null) {
         return null;
      } else {
         String trimmed = this.name.strip();
         if (!trimmed.isEmpty() && trimmed.length() == this.name.length()) {
            String[] tokens = trimmed.split(" ");

            for (String token : tokens) {
               if (token.isEmpty()) {
                  return null;
               }
            }

            return tokens;
         } else {
            return null;
         }
      }
   }

   @Nonnull
   MacroCommandBase createBase(@Nonnull String leafToken, @Nonnull String fullPath) {
      return new MacroCommandBase(leafToken, fullPath, this.aliases, this.description, this.parameters, this.commands, this.permissionGroup);
   }

   public String getName() {
      return this.name;
   }

   public String getId() {
      return this.id;
   }
}
