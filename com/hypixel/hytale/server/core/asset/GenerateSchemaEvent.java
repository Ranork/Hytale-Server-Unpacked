package com.hypixel.hytale.server.core.asset;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.event.IEvent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;

public class GenerateSchemaEvent implements IEvent<Void> {
   protected final Map<String, Schema> schemas;
   protected final SchemaContext context;
   protected final BsonDocument vsCodeConfig;

   public GenerateSchemaEvent(Map<String, Schema> schemas, SchemaContext context, BsonDocument vsCodeConfig) {
      this.schemas = schemas;
      this.context = context;
      this.vsCodeConfig = vsCodeConfig;
   }

   public SchemaContext getContext() {
      return this.context;
   }

   public BsonDocument getVsCodeConfig() {
      return this.vsCodeConfig;
   }

   public void addSchemaLink(String name, @Nonnull List<String> paths, @Nullable String extension) {
      BsonDocument config = new BsonDocument();
      config.put("fileMatch", new BsonArray(paths.stream().map(v -> new BsonString("/Server/" + v)).collect(Collectors.toList())));
      config.put("url", new BsonString("./Schema/" + name + ".json"));
      this.vsCodeConfig.getArray("json.schemas").add(config);
      if (extension != null && !extension.equals(".json")) {
         this.vsCodeConfig.getDocument("files.associations").put("*" + extension, new BsonString("json"));
      }
   }

   public void addSchema(String fileName, Schema schema) {
      this.schemas.put(fileName, schema);
   }
}
