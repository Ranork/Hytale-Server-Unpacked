package com.hypixel.hytale.server.core.schema;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;

public class SchemaGenerator {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final List<SchemaGenerator.ConfigRegistration> configRegistrations = new CopyOnWriteArrayList<>();
   private static final List<SchemaGenerator.AssetSchemaRegistration> assetSchemaRegistrations = new CopyOnWriteArrayList<>();

   public static void registerConfig(
      @Nonnull String name, @Nonnull BuilderCodec<?> codec, @Nullable String virtualPath, @Nullable List<String> fileMatchPatterns
   ) {
      configRegistrations.add(new SchemaGenerator.ConfigRegistration(name, codec, virtualPath, fileMatchPatterns));
   }

   public static void registerAssetSchema(
      @Nonnull String fileName, @Nonnull Function<SchemaContext, Schema> factory, @Nullable List<String> fileMatchPatterns, @Nullable String extension
   ) {
      assetSchemaRegistrations.add(new SchemaGenerator.AssetSchemaRegistration(fileName, factory, fileMatchPatterns, extension));
   }

   @Nonnull
   public static Map<String, Schema> generateAssetSchemas() {
      SchemaGenerator.GenerationResult result = collectSchemas(false);
      LinkedHashMap<String, Schema> schemas = new LinkedHashMap<>();
      schemas.putAll(result.assetSchemas());
      schemas.putAll(result.customAssetSchemas());
      schemas.putAll(result.sharedSchemas());
      return schemas;
   }

   public static void generate(@Nullable Path assetOutputDir, @Nullable Path configOutputDir) {
      try {
         SchemaGenerator.GenerationResult result = collectSchemas(configOutputDir != null);
         boolean sameDir = assetOutputDir != null
            && configOutputDir != null
            && assetOutputDir.toAbsolutePath().normalize().equals(configOutputDir.toAbsolutePath().normalize());
         if (sameDir) {
            Path schemaDir = assetOutputDir.resolve("Schema");
            cleanAndCreateSchemaDir(schemaDir);
            LinkedHashMap<String, Schema> allSchemas = new LinkedHashMap<>();
            allSchemas.putAll(result.assetSchemas());
            allSchemas.putAll(result.customAssetSchemas());
            allSchemas.putAll(result.configSchemas());
            allSchemas.putAll(result.sharedSchemas());
            writeSchemas(allSchemas, schemaDir);
            ArrayList<SchemaGenerator.VsCodeEntry> allVsCodeEntries = new ArrayList<>();
            allVsCodeEntries.addAll(result.assetVsCodeEntries());
            allVsCodeEntries.addAll(result.customAssetVsCodeEntries());
            allVsCodeEntries.addAll(result.configVsCodeEntries());
            writeVsCodeSettings(assetOutputDir, allVsCodeEntries);
         } else {
            if (assetOutputDir != null) {
               Path schemaDir = assetOutputDir.resolve("Schema");
               cleanAndCreateSchemaDir(schemaDir);
               LinkedHashMap<String, Schema> allAssetSchemas = new LinkedHashMap<>();
               allAssetSchemas.putAll(result.assetSchemas());
               allAssetSchemas.putAll(result.customAssetSchemas());
               allAssetSchemas.putAll(result.sharedSchemas());
               writeSchemas(allAssetSchemas, schemaDir);
               ArrayList<SchemaGenerator.VsCodeEntry> allAssetVsCode = new ArrayList<>();
               allAssetVsCode.addAll(result.assetVsCodeEntries());
               allAssetVsCode.addAll(result.customAssetVsCodeEntries());
               writeVsCodeSettings(assetOutputDir, allAssetVsCode);
            }

            if (configOutputDir != null) {
               Path schemaDir = configOutputDir.resolve("Schema");
               cleanAndCreateSchemaDir(schemaDir);
               LinkedHashMap<String, Schema> allConfigSchemas = new LinkedHashMap<>();
               allConfigSchemas.putAll(result.configSchemas());
               allConfigSchemas.putAll(result.sharedSchemas());
               writeSchemas(allConfigSchemas, schemaDir);
               writeVsCodeSettings(configOutputDir, result.configVsCodeEntries());
            }
         }
      } catch (Throwable var7) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var7)).log("Schema generation failed");
         throw new RuntimeException("Schema generation failed", var7);
      }
   }

   private static SchemaGenerator.GenerationResult collectSchemas(boolean includeConfigs) {
      SchemaContext context = new SchemaContext();
      AssetStore[] assetStores = AssetRegistry.getStoreMap().values().toArray(AssetStore[]::new);
      Arrays.sort(assetStores, Comparator.comparing(store -> store.getAssetClass().getSimpleName()));

      for (AssetStore store : assetStores) {
         String name = store.getAssetClass().getSimpleName();
         context.addFileReference(name + ".json", store.getCodec());
      }

      LinkedHashMap<String, Schema> assetSchemas = new LinkedHashMap<>();
      ArrayList<SchemaGenerator.VsCodeEntry> assetVsCodeEntries = new ArrayList<>();

      for (AssetStore store : assetStores) {
         Class assetClass = store.getAssetClass();
         String path = store.getPath();
         String name = assetClass.getSimpleName();
         AssetCodec codec = store.getCodec();
         Schema schema = codec.toSchema(context);
         if (codec instanceof AssetCodecMapCodec) {
            schema.setTitle(name);
         }

         schema.setId(name + ".json");
         Schema.HytaleMetadata hytale = schema.getHytale();
         hytale.setPath(path);
         hytale.setExtension(store.getExtension());
         Class idProvider = store.getIdProvider();
         if (idProvider != null) {
            hytale.setIdProvider(idProvider.getSimpleName());
         }

         List preload = store.getPreAddedAssets();
         if (preload != null && !preload.isEmpty()) {
            String[] internal = new String[preload.size()];

            for (int i = 0; i < preload.size(); i++) {
               Object p = preload.get(i);
               Object k = store.getKeyFunction().apply(p);
               internal[i] = k.toString();
            }

            hytale.setInternalKeys(internal);
         }

         assetSchemas.put(name + ".json", schema);
         assetVsCodeEntries.add(
            new SchemaGenerator.VsCodeEntry(
               name + ".json",
               List.of("/Server/" + path + "/*" + store.getExtension(), "/Server/" + path + "/**/*" + store.getExtension()),
               store.getExtension()
            )
         );
      }

      LinkedHashMap<String, Schema> customAssetSchemas = new LinkedHashMap<>();
      ArrayList<SchemaGenerator.VsCodeEntry> customAssetVsCodeEntries = new ArrayList<>();

      for (SchemaGenerator.AssetSchemaRegistration reg : assetSchemaRegistrations) {
         Schema schemax = reg.factory().apply(context);
         customAssetSchemas.put(reg.fileName(), schemax);
         if (reg.fileMatchPatterns() != null && !reg.fileMatchPatterns().isEmpty()) {
            String ext = reg.extension() != null ? reg.extension() : ".json";
            customAssetVsCodeEntries.add(
               new SchemaGenerator.VsCodeEntry(reg.fileName(), reg.fileMatchPatterns().stream().map(px -> "/Server/" + px).toList(), ext)
            );
         }
      }

      LinkedHashMap<String, Schema> configSchemas = new LinkedHashMap<>();
      ArrayList<SchemaGenerator.VsCodeEntry> configVsCodeEntries = new ArrayList<>();
      if (includeConfigs) {
         for (SchemaGenerator.ConfigRegistration regx : configRegistrations) {
            try {
               ObjectSchema schemax = regx.codec().toSchema(context);
               schemax.setTitle(regx.name());
               String fileName = toFileName(regx.name());
               schemax.setId(fileName);
               Schema.HytaleMetadata hytalex = schemax.getHytale();
               if (regx.virtualPath() != null) {
                  hytalex.setVirtualPath(regx.virtualPath());
               }

               configSchemas.put(fileName, schemax);
               if (regx.fileMatchPatterns() != null && !regx.fileMatchPatterns().isEmpty()) {
                  configVsCodeEntries.add(new SchemaGenerator.VsCodeEntry(fileName, regx.fileMatchPatterns(), null));
               }
            } catch (Throwable var21) {
               ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var21)).log("Failed to generate config schema for '%s', skipping", regx.name());
            }
         }
      }

      LinkedHashMap<String, Schema> sharedSchemas = new LinkedHashMap<>();
      Schema definitions = new Schema();
      definitions.setDefinitions(context.getDefinitions());
      definitions.setId("common.json");
      sharedSchemas.put("common.json", definitions);
      Schema otherDefinitions = new Schema();
      otherDefinitions.setDefinitions(context.getOtherDefinitions());
      otherDefinitions.setId("other.json");
      sharedSchemas.put("other.json", otherDefinitions);
      return new SchemaGenerator.GenerationResult(
         assetSchemas, customAssetSchemas, configSchemas, sharedSchemas, assetVsCodeEntries, customAssetVsCodeEntries, configVsCodeEntries
      );
   }

   private static void cleanAndCreateSchemaDir(@Nonnull Path schemaDir) {
      try {
         Files.createDirectories(schemaDir);

         try (Stream<Path> stream = Files.walk(schemaDir, 1)) {
            stream.filter(v -> v.toString().endsWith(".json")).forEach(SneakyThrow.sneakyConsumer(Files::delete));
         }
      } catch (Exception var6) {
         throw new RuntimeException("Failed to prepare schema directory: " + schemaDir, var6);
      }
   }

   private static void writeVsCodeSettings(@Nonnull Path outputDir, @Nonnull List<SchemaGenerator.VsCodeEntry> entries) {
      try {
         BsonDocument vsCodeConfig = new BsonDocument();
         BsonArray vsCodeSchemas = new BsonArray();
         BsonDocument vsCodeFiles = new BsonDocument();
         vsCodeConfig.put("json.schemas", vsCodeSchemas);
         vsCodeConfig.put("files.associations", vsCodeFiles);
         vsCodeConfig.put("editor.tabSize", new BsonInt32(2));

         for (SchemaGenerator.VsCodeEntry entry : entries) {
            addVsCodeSchemaLink(vsCodeConfig, entry.schemaFileName(), entry.fileMatchPatterns(), entry.extension());
         }

         Files.createDirectories(outputDir.resolve(".vscode"));
         BsonUtil.writeDocument(outputDir.resolve(".vscode/settings.json"), vsCodeConfig, false).join();
      } catch (Exception var7) {
         throw new RuntimeException("Failed to write .vscode/settings.json", var7);
      }
   }

   @Nonnull
   public static String toFileName(@Nonnull String name) {
      return name.replace(':', '.') + ".json";
   }

   public static void writeSchemas(@Nonnull Map<String, Schema> schemas, @Nonnull Path schemaDir) {
      for (Entry<String, Schema> schema : schemas.entrySet()) {
         BsonUtil.writeDocument(schemaDir.resolve(schema.getKey()), Schema.CODEC.encode(schema.getValue(), EmptyExtraInfo.EMPTY).asDocument(), false).join();
      }
   }

   public static void addVsCodeSchemaLink(
      @Nonnull BsonDocument vsCodeConfig, @Nonnull String schemaFileName, @Nonnull List<String> fileMatchPatterns, @Nullable String extension
   ) {
      BsonDocument config = new BsonDocument();
      config.put("fileMatch", new BsonArray(fileMatchPatterns.stream().map(BsonString::new).toList()));
      config.put("url", new BsonString("./Schema/" + schemaFileName));
      vsCodeConfig.getArray("json.schemas").add(config);
      if (extension != null && !extension.equals(".json")) {
         vsCodeConfig.getDocument("files.associations").put("*" + extension, new BsonString("json"));
      }
   }

   private record AssetSchemaRegistration(
      @Nonnull String fileName, @Nonnull Function<SchemaContext, Schema> factory, @Nullable List<String> fileMatchPatterns, @Nullable String extension
   ) {
   }

   private record ConfigRegistration(
      @Nonnull String name, @Nonnull BuilderCodec<?> codec, @Nullable String virtualPath, @Nullable List<String> fileMatchPatterns
   ) {
   }

   private record GenerationResult(
      LinkedHashMap<String, Schema> assetSchemas,
      LinkedHashMap<String, Schema> customAssetSchemas,
      LinkedHashMap<String, Schema> configSchemas,
      LinkedHashMap<String, Schema> sharedSchemas,
      List<SchemaGenerator.VsCodeEntry> assetVsCodeEntries,
      List<SchemaGenerator.VsCodeEntry> customAssetVsCodeEntries,
      List<SchemaGenerator.VsCodeEntry> configVsCodeEntries
   ) {
   }

   private record VsCodeEntry(String schemaFileName, List<String> fileMatchPatterns, @Nullable String extension) {
   }
}
