# Documentación de la API Principal de Hytale (ES)

Este proyecto se genera automáticamente a partir del código fuente descompilado y se comparte solo como referencia, para permitir la lectura de métodos públicos. No está destinado para fines de compilación.
<br>

For more details you can visit our discord channel:  https://www.akatron.net/hytale-discord


## Integración con IA

Si tienes la intención de utilizar este proyecto como referencia para escribir código con inteligencia artificial, puedes descargarlo y especificar la ubicación del archivo com/hypixel/hytale en tus indicaciones para que el agente pueda recibir información sobre el proyecto.

## Idiomas disponibles
- [English](README.md)
- [Türkçe (Turkish)](README_TR.md)
- [Español (Spanish)](README_ES.md)

## Core API Documentation
- [English](HYTALE_CORE_API.md)
- [Türkçe](HYTALE_CORE_API_TR.md)
- [Español](HYTALE_CORE_API_ES.md)


## AssetEditor
- **Version**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.asseteditor.AssetEditorPlugin`

_No existen métodos públicos o error al analizar el archivo._

---

## BlockSpawner
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.blockspawner.BlockSpawnerPlugin`

### Métodos Públicos
```java
public static BlockSpawnerPlugin get();
public Query<ChunkStore> getQuery();
public void onEntityAdded(@Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer);
public void onEntityRemove(@Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer);
public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store);
public void onEntityRemoved(@Nonnull Holder<ChunkStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store);
public Query<ChunkStore> getQuery();
```

---

## BlockTick
- **Version**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.blocktick.BlockTickPlugin`

### Métodos Públicos
```java
public static BlockTickPlugin get();
public TickProcedure getTickProcedure(int blockId);
public int discoverTickingBlocks(@Nonnull Holder<ChunkStore> holder, @Nonnull WorldChunk chunk);
```

---

## BlockPhysics
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.blockphysics.BlockPhysicsPlugin`

### Métodos Públicos
```java
public static void validatePrefabs(@Nonnull LoadAssetEvent event);
```

---

## BuilderTools
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin`

_No existen métodos públicos o error al analizar el archivo._

---

## Crafting
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.crafting.CraftingPlugin`

### Descripción
Gestiona el sistema de elaboración, incluyendo recetas, bancos de trabajo (estaciones) y recetas desbloqueadas por el jugador.
Se encarga de verificar si un jugador tiene los materiales requeridos y si una receta específica es válida para un banco dado.

### Métodos Públicos
```java
// Devuelve la instancia singleton
public static CraftingPlugin get();

// Devuelve todos los IDs de recetas disponibles para un banco específico y categoría.
public static Set<String> getAvailableRecipesForCategory(String benchId, String benchCategoryId);

// Comprueba si un stack de ítems puede usarse como material en el estado actual de un banco.
public static boolean isValidCraftingMaterialForBench(BenchState benchState, ItemStack itemStack);

// Comprueba si un ítem es válido para mejorar un banco.
public static boolean isValidUpgradeMaterialForBench(BenchState benchState, ItemStack itemStack);

// Devuelve una lista de todas las recetas disponibles para un bloque de banco dado.
public static List<CraftingRecipe> getBenchRecipes(@Nonnull Bench bench);

// Devuelve recetas para un tipo de banco (por ejemplo, Crafting, Diagram, Structural) y nombre.
public static List<CraftingRecipe> getBenchRecipes(BenchType benchType, String name);

// Desbloquea una receta para un jugador ("la aprende"). Devuelve true si fue aprendida recientemente.
// Requiere la referencia específica del jugador Entity.
public static boolean learnRecipe(@Nonnull Ref<EntityStore> ref, @Nonnull String recipeId, @Nonnull ComponentAccessor<EntityStore> componentAccessor);

// Bloquea una receta para un jugador ("la olvida").
public static boolean forgetRecipe(@Nonnull Ref<EntityStore> ref, @Nonnull String itemId, @Nonnull ComponentAccessor<EntityStore> componentAccessor);

// Envía un paquete al cliente sincronizando su lista de recetas conocidas.
public static void sendKnownRecipes(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor);
```

---

## CommandMacro
- **Version**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.commandmacro.MacroCommandPlugin`

### Métodos Públicos
```java
public static MacroCommandPlugin get();
public void loadCommandMacroAsset(@Nonnull LoadedAssetsEvent<String, MacroCommandBuilder, DefaultAssetMap<String, MacroCommandBuilder>> event);
```

---

## Instances
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.instances.InstancesPlugin`

_No existen métodos públicos o error al analizar el archivo._

---

## LANDiscovery
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.landiscovery.LANDiscoveryPlugin`

### Métodos Públicos
```java
public static LANDiscoveryPlugin get();
public void setLANDiscoveryEnabled(boolean enabled);
public boolean isLANDiscoveryEnabled();
public LANDiscoveryThread getLanDiscoveryThread();
```

---

## NPC
- **Version**: 1.0.0
- **Main Class**: `com.hypixel.hytale.server.npc.NPCPlugin`

### Descripción
El sistema de NPC es uno de los sistemas más complejos en Hytale Server. Gestiona el ciclo de vida, IA, comportamiento y datos de todos los personajes no jugadores.
Utiliza un patrón "Builder" para construir NPCs a partir de assets y registra varios componentes de IA como Sensores (ojos/oídos), Acciones (ataques/movimientos) y Motions.
También interactúa de forma frecuente con el `EntityStore` para gestionar componentes como `Blackboard` (memoria), `CombatData` y `Timers`.

### Métodos Públicos
```java
// Devuelve la instancia singleton del NPCPlugin
public static NPCPlugin get();

// Genera un NPC de un tipo específico (rol) en una ubicación.
// Devuelve un Par que contiene la referencia de la Entidad y el componente NPC.
public Pair<Ref<EntityStore>, INonPlayerCharacter> spawnNPC(@Nonnull Store<EntityStore> store, @Nonnull String npcType, @Nullable String groupType, @Nonnull Vector3d position, @Nonnull Vector3f rotation);

// Recarga todos los NPCs activos que comparten un índice de rol específico. Útil para actualizar en vivo el comportamiento de la IA.
public static void reloadNPCsWithRole(int roleIndex);

// Obtiene el gestor responsable de los planos/plantillas de NPC.
public BuilderManager getBuilderManager();

// Obtiene el mapa de actitudes (Friendly, Hostile, Neutral) entre diferentes facciones/grupos.
public AttitudeMap getAttitudeMap();

// Obtiene el mapa que determina cómo reaccionan los NPCs a objetos específicos (por ejemplo, sostener un arma vs una flor).
public ItemAttitudeMap getItemAttitudeMap();

// Determina si existe un nombre de rol específico (por ejemplo, "kweebec_guard").
public boolean hasRoleName(String roleName);

// Registra todas las fábricas principales de IA (Acciones, Sensores, Motions). Uso principalmente interno pero bueno saberlo.
public void setupNPCLoading();

// Obtiene el nombre legible de un índice de builder.
public String getName(int builderIndex);

// Métodos de benchmarking para pruebas de rendimiento de roles de IA.
public boolean startRoleBenchmark(double seconds, @Nonnull Consumer<Int2ObjectMap<TimeDistributionRecorder>> onFinished);
```

---

## NPCObjectives
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.adventure.npcobjectives.NPCObjectivesPlugin`

### Métodos Públicos
```java
public static NPCObjectivesPlugin get();
public static boolean hasTask(@Nonnull UUID playerUUID, @Nonnull UUID npcId, @Nonnull String taskId);
public static String updateTaskCompletion(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull UUID npcId, @Nonnull String taskId);
public static void startObjective(@Nonnull Ref<EntityStore> playerReference, @Nonnull String taskId, @Nonnull Store<EntityStore> store);
```

---

## ObjectiveReputation
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.adventure.objectivereputation.ObjectiveReputationPlugin`

### Métodos Públicos
```java
public static ObjectiveReputationPlugin get();
```

---

## Objectives
- **Version**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin`

### Descripción
Maneja el sistema de misiones y objetivos. Requiere el plugin `Objectives`.
Gestiona "Objective Lines" (cadenas de misiones) y "Objectives" individuales.
Realiza un seguimiento del progreso de los jugadores, maneja la finalización de tareas (por ejemplo, "Recolectar madera", "Matar esqueletos") y las recompensas.

### Métodos Públicos
```java
// Devuelve la instancia singleton
public static ObjectivePlugin get();

// Inicia un objetivo específico para uno o más jugadores.
// Si se proporciona markerUUID, podría mostrar un marcador de ubicación.
public Objective startObjective(@Nonnull String objectiveId, @Nonnull Set<UUID> playerUUIDs, @Nonnull UUID worldUUID, @Nullable UUID markerUUID, @Nonnull Store<EntityStore> store);

// Inicia toda una cadena de objetivos (Línea de Objetivos).
public Objective startObjectiveLine(@Nonnull Store<EntityStore> store, @Nonnull String objectiveLineId, @Nonnull Set<UUID> playerUUIDs, @Nonnull UUID worldUUID, @Nullable UUID markerUUID);

// Verifica si un jugador tiene permitido iniciar un objetivo (por ejemplo, si no lo está haciendo ya).
public boolean canPlayerDoObjective(@Nonnull Player player, @Nonnull String objectiveAssetId);

// Verifica si un jugador puede iniciar una línea de objetivos.
public boolean canPlayerDoObjectiveLine(@Nonnull Player player, @Nonnull String objectiveLineId);

// Marca un objetivo como completado para los jugadores asociados y maneja recompensas/pasos siguientes.
public void objectiveCompleted(@Nonnull Objective objective, @Nonnull Store<EntityStore> store);

// Cancela un objetivo activo.
public void cancelObjective(@Nonnull UUID objectiveUUID, @Nonnull Store<EntityStore> store);

// Añade un jugador a una instancia de objetivo ya en ejecución (misiones cooperativas).
public void addPlayerToExistingObjective(@Nonnull Store<EntityStore> store, @Nonnull UUID playerUUID, @Nonnull UUID objectiveUUID);

// Elimina a un jugador de un objetivo.
public void removePlayerFromExistingObjective(@Nonnull Store<EntityStore> store, @Nonnull UUID playerUUID, @Nonnull UUID objectiveUUID);

// Deja de rastrear un objetivo para un jugador específico (actualización del lado del cliente).
public void untrackObjectiveForPlayer(@Nonnull Objective objective, @Nonnull UUID playerUUID);

// Devuelve un volcado de depuración de los datos actuales del objetivo.
public String getObjectiveDataDump();
```

---

## ObjectiveShop
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.adventure.objectiveshop.ObjectiveShopPlugin`

### Métodos Públicos
```java
public static ObjectiveShopPlugin get();
```

---

## Path
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.path.PathPlugin`

_No se encontraron métodos públicos o error al analizar el archivo._

---

## Reputation
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.adventure.reputation.ReputationPlugin`

### Métodos Públicos
```java
public static ReputationPlugin get();
public int changeReputation(@Nonnull Player player, @Nonnull Ref<EntityStore> npcRef, int value, @Nonnull ComponentAccessor<EntityStore> componentAccessor);
public int changeReputation(@Nonnull Player player, @Nonnull String reputationGroupId, int value, @Nonnull ComponentAccessor<EntityStore> componentAccessor);
public int changeReputation(@Nonnull World world, @Nonnull String reputationGroupId, int value);
public int getReputationValue(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull Ref<EntityStore> npcEntityRef);
public int getReputationValue(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull String reputationGroupId);
public int getReputationValue(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef);
public int getReputationValue(@Nonnull Store<EntityStore> store, String reputationGroupId);
public ReputationRank getReputationRank(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> npcRef);
public ReputationRank getReputationRank(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull String reputationGroupId);
public ReputationRank getReputationRankFromValue(int value);
public ReputationRank getReputationRank(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef);
public Attitude getAttitude(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> npc);
public Attitude getAttitude(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef);
```

---

## NPCReputation
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.adventure.npcreputation.NPCReputationPlugin`

_No se encontraron métodos públicos o error al analizar el archivo._

---

## Shop
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.adventure.shop.ShopPlugin`

### Métodos Públicos
```java
public static ShopPlugin get();
```

---

## ShopReputation
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.adventure.shopreputation.ShopReputationPlugin`

_No se encontraron métodos públicos o error al analizar el archivo._
---

## NPCShop
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.adventure.npcshop.NPCShopPlugin`

_No se encontraron métodos públicos o error al analizar el archivo._

---

## NPCEditor
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.npceditor.NPCEditorPlugin`

_No se encontraron métodos públicos o error al analizar el archivo._

---

## Stash
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.adventure.stash.StashPlugin`

### Métodos Públicos
```java
public static ListTransaction<ItemStackTransaction> stash(@Nonnull ItemContainerState containerState, boolean clearDropList);
public Query<ChunkStore> getQuery();
public void onEntityAdded(@Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer);
public void onEntityRemove(@Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer);
public Set<Dependency<ChunkStore>> getDependencies();
```

---

## TagSet
- **Version**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.tagset.TagSetPlugin`

### Public Methods
```java
public static TagSetPlugin get();
public boolean tagInSet(int tagSet, int tagIndex);
public IntSet getSet(int tagSet);
```

---

## Teleport
- **Version**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.teleport.TeleportPlugin`

### Descripción
Gestiona "Warps" y la lógica de teletransporte.
Permite crear, guardar y cargar puntos Warp nombrados en el mundo.
Utilizado por comandos como `/warp` y `/tppos`.

### Métodos Públicos
```java
// Devuelve la instancia singleton
public static TeleportPlugin get();

// Comprueba si los warps se han cargado desde el disco.
public boolean isWarpsLoaded();

// Carga los warps desde `warps.json` o `warps.bson` en el directorio del universo.
public void loadWarps();

// Guarda los warps actuales en `warps.json`.
public void saveWarps();

// Crea una entidad Warp (marcador) en el mundo.
public Holder<EntityStore> createWarp(@Nonnull Warp warp, @Nonnull Store<EntityStore> store);

// Devuelve el mapa de warps cargados. (Nota: Inferido de la lógica, el nombre del método genérico en el código descompilado suele ser `getWarps()`)
public Map<String, Warp> getWarps();

// Actualiza los marcadores en el mapa para los jugadores dentro del rango.
public void update(@Nonnull World world, @Nonnull GameplayConfig gameplayConfig, @Nonnull WorldMapTracker tracker, int chunkViewRadius, int playerChunkX, int playerChunkZ);
```

---

## Fluid
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.fluid.FluidPlugin`

### Métodos Públicos
```java
public static FluidPlugin get();
public FluidSection getFluidSection(int cx, int cy, int cz);
public BlockSection getBlockSection(int cx, int cy, int cz);
public void setBlock(int x, int y, int z, int blockId);
```

---

## Weather
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.weather.WeatherPlugin`

### Métodos Públicos
```java
public static WeatherPlugin get();
```

---

## WorldGen
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.worldgen.WorldGenPlugin`

### Métodos Públicos
```java
public static WorldGenPlugin get();
```

---

## Farming
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.adventure.farming.FarmingPlugin`

### Métodos Públicos
```java
public static FarmingPlugin get();
```

---

## Camera
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.adventure.camera.CameraPlugin`

_No se encontraron métodos públicos o error al analizar el archivo._
---

## WorldLocationCondition
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.adventure.worldlocationcondition.WorldLocationConditionPlugin`

_No se encontraron métodos públicos o error al analizar el archivo._

---

## NPCCombatActionEvaluator
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.npccombatactionevaluator.NPCCombatActionEvaluatorPlugin`

### Métodos Públicos
```java
public static NPCCombatActionEvaluatorPlugin get();
```

---

## Model
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.model.ModelPlugin`

_No se encontraron métodos públicos o error al analizar el archivo._
---

## Mantling
- **Versión**: 1.0.0
- **Descripción**: Habilitar mantling
- **Main Class**: `com.hypixel.hytale.builtin.mantling.MantlingPlugin`

_No se encontraron métodos públicos o error al analizar el archivo._

---

## SafetyRoll
- **Versión**: 1.0.0
- **Descripción**: Habilitar Safety Roll
- **Main Class**: `com.hypixel.hytale.builtin.safetyroll.SafetyRollPlugin`

_No se encontraron métodos públicos o error al analizar el archivo._
---

## SprintForce
- **Versión**: 1.0.0
- **Descripción**: Habilitar aceleración/desaceleración al sprintar
- **Main Class**: `com.hypixel.hytale.builtin.sprintforce.SprintForcePlugin`

_No se encontraron métodos públicos o error al analizar el archivo._

---

## CrouchSlide
- **Versión**: 1.0.0
- **Descripción**: Habilitar deslizamiento agachado
- **Main Class**: `com.hypixel.hytale.builtin.crouchslide.CrouchSlidePlugin`

_No se encontraron métodos públicos o error al analizar el archivo._
---

## Parkour
- **Versión**: 1.0.0
- **Descripción**: Módulo para añadir un temporizador con un sistema de checkpoints
- **Main Class**: `com.hypixel.hytale.builtin.parkour.ParkourPlugin`

### Métodos Públicos
```java
public static ParkourPlugin get();
public Model getParkourCheckpointModel();
public Object2IntMap<UUID> getCurrentCheckpointByPlayerMap();
public Object2LongMap<UUID> getStartTimeByPlayerMap();
public Int2ObjectMap<UUID> getCheckpointUUIDMap();
public int getLastIndex();
public void updateLastIndex(int index);
public void updateLastIndex();
public void resetPlayer(UUID playerUuid);
```

---

## Mounts
- **Versión**: 1.0.0
- **Descripción**: Módulo para añadir monturas
- **Main Class**: `com.hypixel.hytale.builtin.mounts.MountPlugin`

### Métodos Públicos
```java
public static MountPlugin getInstance();
public static void checkDismountNpc(@Nonnull ComponentAccessor<EntityStore> store, @Nonnull Player playerComponent);
public static void dismountNpc(@Nonnull ComponentAccessor<EntityStore> store, int mountEntityId);
public static void resetOriginalPlayerMovementSettings(@Nonnull PlayerRef playerRef, @Nonnull ComponentAccessor<EntityStore> store);
```

---

## HytaleGenerator
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.hytalegenerator.plugin.HytaleGenerator`

### Métodos Públicos
```java
public CompletableFuture<GeneratedChunk> submitChunkRequest(@Nonnull ChunkRequest request);
public NStagedChunkGenerator createStagedChunkGenerator(@Nonnull ChunkRequest.GeneratorProfile generatorProfile, @Nonnull WorldStructureAsset worldStructureAsset, @Nonnull SettingsAsset settingsAsset);
```

---

## Teleporter
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.adventure.teleporter.TeleporterPlugin`

_No se encontraron métodos públicos o error al analizar el archivo._

---

## Memories
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin`

### Métodos Públicos
```java
public static MemoriesPlugin get();
public MemoriesPluginConfig getConfig();
public int getMemoriesLevel(@Nonnull GameplayConfig gameplayConfig);
public int getMemoriesForNextLevel(@Nonnull GameplayConfig gameplayConfig);
public boolean hasRecordedMemory(Memory memory);
public boolean recordPlayerMemories(@Nonnull PlayerMemories playerMemories);
public Set<Memory> getRecordedMemories();
public void clearRecordedMemories();
public void recordAllMemories();
public Object2DoubleMap<String> getCollectionRadius();
public Query<EntityStore> getQuery();
public Set<Dependency<EntityStore>> getDependencies();
public void onEntityAdded(@Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer);
public void onEntityRemove(@Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer);
```

---

## Deployables
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.deployables.DeployablesPlugin`

### Métodos Públicos
```java
public static DeployablesPlugin get();
```

---

## Portals
- **Versión**: 1.0.0
- **Descripción**: Módulo para añadir portales
- **Main Class**: `com.hypixel.hytale.builtin.portals.PortalsPlugin`

### Métodos Públicos
```java
public static PortalsPlugin getInstance();
public int countActiveFragments();
```

---

## Beds
- **Versión**: 1.0.0
- **Descripción**: Módulo para manejar camas y dormir en ellas
- **Main Class**: `com.hypixel.hytale.builtin.beds.BedsPlugin`

### Métodos Públicos
```java
public static BedsPlugin getInstance();
```

---

## Ambience
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.ambience.AmbiencePlugin`

### Métodos Públicos
```java
public static AmbiencePlugin get();
public Model getAmbientEmitterModel();
```

---

## CreativeHub
- **Versión**: 1.0.0
- **Main Class**: `com.hypixel.hytale.builtin.creativehub.CreativeHubPlugin`

### Métodos Públicos
```java
public static CreativeHubPlugin get();
public World getOrSpawnHubInstance(@Nonnull World parentWorld, @Nonnull CreativeHubWorldConfig hubConfig, @Nonnull Transform returnPoint);
public World getActiveHubInstance(@Nonnull World parentWorld);
public void clearHubInstance(@Nonnull UUID parentWorldUuid);
public CompletableFuture<World> spawnPermanentWorldFromTemplate(@Nonnull String instanceAssetName, @Nonnull String permanentWorldName);
```

---

