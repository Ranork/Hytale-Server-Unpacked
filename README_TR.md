# Hytale Sunucu Eklenti API Dokümantasyonu

Bu dokümantasyon, derlenmiş kaynak kodundan otomatik olarak oluşturulmuştur.

## AssetEditor
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.asseteditor.AssetEditorPlugin`

_Hiçbir genel (public) metot bulunamadı veya dosya ayrıştırma hatası._

---

## BlockSpawner
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.blockspawner.BlockSpawnerPlugin`

### Genel Metotlar
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
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.blocktick.BlockTickPlugin`

### Genel Metotlar
```java
public static BlockTickPlugin get();
public TickProcedure getTickProcedure(int blockId);
public int discoverTickingBlocks(@Nonnull Holder<ChunkStore> holder, @Nonnull WorldChunk chunk);
```

---

## BlockPhysics
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.blockphysics.BlockPhysicsPlugin`

### Genel Metotlar
```java
public static void validatePrefabs(@Nonnull LoadAssetEvent event);
```

---

## BuilderTools
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin`

_Hiçbir genel (public) metot bulunamadı veya dosya ayrıştırma hatası._

---

## Crafting
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.crafting.CraftingPlugin`

### Açıklama
Reçeteler, üretim tezgahları (istasyonları) ve oyuncu tarafından açılan reçeteleri içeren üretim sistemini yönetir.
Bir oyuncunun gerekli malzemelere sahip olup olmadığını ve belirli bir reçetenin verilen tezgah için geçerli olup olmadığını kontrol eder.

### Genel Metotlar
```java
// Tekil (singleton) örneği döndürür
public static CraftingPlugin get();

// Belirli bir tezgah kimliği ve kategori için mevcut tüm reçete kimliklerini getirir.
public static Set<String> getAvailableRecipesForCategory(String benchId, String benchCategoryId);

// Bir eşya yığınının, bir tezgahın mevcut durumunda malzeme olarak kullanılıp kullanılamayacağını kontrol eder.
public static boolean isValidCraftingMaterialForBench(BenchState benchState, ItemStack itemStack);

// Bir eşyanın bir tezgahı yükseltmek için geçerli olup olmadığını kontrol eder.
public static boolean isValidUpgradeMaterialForBench(BenchState benchState, ItemStack itemStack);

// Verilen bir Tezgah bloğu için mevcut tüm reçetelerin bir listesini getirir.
public static List<CraftingRecipe> getBenchRecipes(@Nonnull Bench bench);

// Bir tezgah türü (ör. Üretim, Diyagram, Yapısal) ve adı için reçeteleri getirir.
public static List<CraftingRecipe> getBenchRecipes(BenchType benchType, String name);

// Bir oyuncu için bir reçetenin kilidini açar ("öğrenir"). Eğer yeni öğrenildiyse true döndürür.
// Belirli bir oyuncu Varlık Referansı gerektirir.
public static boolean learnRecipe(@Nonnull Ref<EntityStore> ref, @Nonnull String recipeId, @Nonnull ComponentAccessor<EntityStore> componentAccessor);

// Bir oyuncu için bir reçeteyi kilitler ("unutturur").
public static boolean forgetRecipe(@Nonnull Ref<EntityStore> ref, @Nonnull String itemId, @Nonnull ComponentAccessor<EntityStore> componentAccessor);

// İstemciye, bildikleri reçetelerin listesini senkronize eden bir paket gönderir.
public static void sendKnownRecipes(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor);
```

---

## CommandMacro
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.commandmacro.MacroCommandPlugin`

### Genel Metotlar
```java
public static MacroCommandPlugin get();
public void loadCommandMacroAsset(@Nonnull LoadedAssetsEvent<String, MacroCommandBuilder, DefaultAssetMap<String, MacroCommandBuilder>> event);
```

---

## Instances
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.instances.InstancesPlugin`

_Hiçbir genel (public) metot bulunamadı veya dosya ayrıştırma hatası._

---

## LANDiscovery
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.landiscovery.LANDiscoveryPlugin`

### Genel Metotlar
```java
public static LANDiscoveryPlugin get();
public void setLANDiscoveryEnabled(boolean enabled);
public boolean isLANDiscoveryEnabled();
public LANDiscoveryThread getLanDiscoveryThread();
```

---

## NPC
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.server.npc.NPCPlugin`

### Açıklama
NPC Sistemi, Hytale Sunucusu'ndaki en karmaşık sistemlerden biridir. Tüm Oyuncu Olmayan Karakterlerin (NPC'ler) yaşam döngüsünü, yapay zekasını, davranışlarını ve verilerini yönetir.
NPC'leri varlıklardan oluşturmak için bir "Builder" (İnşa Edici) deseni kullanır ve Sensörler (gözler/kulaklar), Eylemler (saldırılar/hareketler) ve Hareketler gibi çeşitli yapay zeka bileşenlerini kaydeder.
Ayrıca, `Blackboard` (hafıza), `CombatData` ve `Timers` gibi bileşenleri yönetmek için `EntityStore` ile yoğun bir şekilde etkileşime girer.

### Genel Metotlar
```java
// NPCPlugin'in tekil (singleton) örneğini döndürür
public static NPCPlugin get();

// Bir konumda belirli bir türde (rol) bir NPC oluşturur.
// Varlık referansı ve NPC bileşenini içeren bir Çift (Pair) döndürür.
public Pair<Ref<EntityStore>, INonPlayerCharacter> spawnNPC(@Nonnull Store<EntityStore> store, @Nonnull String npcType, @Nullable String groupType, @Nonnull Vector3d position, @Nonnull Vector3f rotation);

// Belirli bir rol indeksini paylaşan tüm aktif NPC'leri yeniden yükler. Canlı yapay zeka davranışı güncellemeleri için kullanışlıdır.
public static void reloadNPCsWithRole(int roleIndex);

// NPC planlarından/şablonlarından sorumlu yöneticiyi getirir.
public BuilderManager getBuilderManager();

// Farklı hizipler/gruplar arasındaki tutum haritasını (Dostça, Düşmanca, Nötr) getirir.
public AttitudeMap getAttitudeMap();

// NPC'lerin belirli eşyalara (örneğin bir silah tutmaya karşı bir çiçek tutmaya) nasıl tepki vereceğini belirleyen haritayı getirir.
public ItemAttitudeMap getItemAttitudeMap();

// Belirli bir rol adının (örneğin "kweebec_guard") var olup olmadığını belirler.
public boolean hasRoleName(String roleName);

// Tüm temel yapay zeka fabrikalarını (Eylemler, Sensörler, Hareketler) kaydeder. Genellikle dahili kullanım içindir ancak bilinmesi iyidir.
public void setupNPCLoading();

// Bir inşa edici indeksinin insan tarafından okunabilir adını getirir.
public String getName(int builderIndex);

// Yapay zeka rollerinin performans testi için kıyaslama metotları.
public boolean startRoleBenchmark(double seconds, @Nonnull Consumer<Int2ObjectMap<TimeDistributionRecorder>> onFinished);
```

---

## NPCObjectives
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.adventure.npcobjectives.NPCObjectivesPlugin`

### Genel Metotlar
```java
public static NPCObjectivesPlugin get();
public static boolean hasTask(@Nonnull UUID playerUUID, @Nonnull UUID npcId, @Nonnull String taskId);
public static String updateTaskCompletion(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull UUID npcId, @Nonnull String taskId);
public static void startObjective(@Nonnull Ref<EntityStore> playerReference, @Nonnull String taskId, @Nonnull Store<EntityStore> store);
```

---

## ObjectiveReputation
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.adventure.objectivereputation.ObjectiveReputationPlugin`

### Genel Metotlar
```java
public static ObjectiveReputationPlugin get();
```

---

## Objectives
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin`

### Açıklama
Görev ve hedef sistemini yönetir. `Objectives` eklentisini gerektirir.
"Hedef Satırları" (görev zincirleri) ve bireysel "Hedefleri" yönetir.
Oyuncular için ilerlemeyi takip eder, görev tamamlamayı (örneğin "Odun Topla", "İskeletleri Öldür") ve ödülleri yönetir.

### Genel Metotlar
```java
// Tekil (singleton) örneği döndürür
public static ObjectivePlugin get();

// Bir veya daha fazla oyuncu için belirli bir hedefi başlatır.
// Eğer markerUUID sağlanırsa, bir konum işaretçisi gösterebilir.
public Objective startObjective(@Nonnull String objectiveId, @Nonnull Set<UUID> playerUUIDs, @Nonnull UUID worldUUID, @Nullable UUID markerUUID, @Nonnull Store<EntityStore> store);

// Bütün bir hedef zincirini (Hedef Satırı) başlatır.
public Objective startObjectiveLine(@Nonnull Store<EntityStore> store, @Nonnull String objectiveLineId, @Nonnull Set<UUID> playerUUIDs, @Nonnull UUID worldUUID, @Nullable UUID markerUUID);

// Bir oyuncunun bir hedefi başlatıp başlatamayacağını (örneğin halihazırda yapmıyorsa) kontrol eder.
public boolean canPlayerDoObjective(@Nonnull Player player, @Nonnull String objectiveAssetId);

// Bir oyuncunun bir hedef satırını başlatıp başlatamayacağını kontrol eder.
public boolean canPlayerDoObjectiveLine(@Nonnull Player player, @Nonnull String objectiveLineId);

// İlgili oyuncular için bir hedefi tamamlandı olarak işaretler ve ödülleri/sonraki adımları yönetir.
public void objectiveCompleted(@Nonnull Objective objective, @Nonnull Store<EntityStore> store);

// Aktif bir hedefi iptal eder.
public void cancelObjective(@Nonnull UUID objectiveUUID, @Nonnull Store<EntityStore> store);

// Bir oyuncuyu halihazırda çalışan bir hedef örneğine (ortak görevler) ekler.
public void addPlayerToExistingObjective(@Nonnull Store<EntityStore> store, @Nonnull UUID playerUUID, @Nonnull UUID objectiveUUID);

// Bir oyuncuyu bir hedeften kaldırır.
public void removePlayerFromExistingObjective(@Nonnull Store<EntityStore> store, @Nonnull UUID playerUUID, @Nonnull UUID objectiveUUID);

// Belirli bir oyuncu için bir hedefin takibini durdurur (istemci tarafı güncellemesi).
public void untrackObjectiveForPlayer(@Nonnull Objective objective, @Nonnull UUID playerUUID);

// Mevcut hedef verisinin bir hata ayıklama dökümünü döndürür.
public String getObjectiveDataDump();
```

---

## ObjectiveShop
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.adventure.objectiveshop.ObjectiveShopPlugin`

### Genel Metotlar
```java
public static ObjectiveShopPlugin get();
```

---

## Path
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.path.PathPlugin`

_Hiçbir genel (public) metot bulunamadı veya dosya ayrıştırma hatası._

---

## Reputation
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.adventure.reputation.ReputationPlugin`

### Genel Metotlar
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
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.adventure.npcreputation.NPCReputationPlugin`

_Hiçbir genel (public) metot bulunamadı veya dosya ayrıştırma hatası._

---

## Shop
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.adventure.shop.ShopPlugin`

### Genel Metotlar
```java
public static ShopPlugin get();
```

---

## ShopReputation
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.adventure.shopreputation.ShopReputationPlugin`

_Hiçbir genel (public) metot bulunamadı veya dosya ayrıştırma hatası._

---

## NPCShop
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.adventure.npcshop.NPCShopPlugin`

_Hiçbir genel (public) metot bulunamadı veya dosya ayrıştırma hatası._

---

## NPCEditor
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.npceditor.NPCEditorPlugin`

_Hiçbir genel (public) metot bulunamadı veya dosya ayrıştırma hatası._

---

## Stash
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.adventure.stash.StashPlugin`

### Genel Metotlar
```java
public static ListTransaction<ItemStackTransaction> stash(@Nonnull ItemContainerState containerState, boolean clearDropList);
public Query<ChunkStore> getQuery();
public void onEntityAdded(@Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer);
public void onEntityRemove(@Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer);
public Set<Dependency<ChunkStore>> getDependencies();
```

---

## TagSet
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.tagset.TagSetPlugin`

### Genel Metotlar
```java
public static TagSetPlugin get();
public boolean tagInSet(int tagSet, int tagIndex);
public IntSet getSet(int tagSet);
```

---

## Teleport
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.teleport.TeleportPlugin`

### Açıklama
"Warp"ları (ışınlanma noktaları) ve ışınlanma mantığını yönetir.
Dünyada isimlendirilmiş Warp noktaları oluşturmaya, kaydetmeye ve yüklemeye izin verir.
`/warp` ve `/tppos` gibi komutlar tarafından kullanılır.

### Genel Metotlar
```java
// Tekil (singleton) örneği döndürür
public static TeleportPlugin get();

// Warp'ların diskten yüklenip yüklenmediğini kontrol eder.
public boolean isWarpsLoaded();

// Evren dizinindeki `warps.json` veya `warps.bson` dosyasından warp'ları yükler.
public void loadWarps();

// Mevcut warp'ları `warps.json` dosyasına kaydeder.
public void saveWarps();

// Dünyada bir Warp varlığı (işaretçi) oluşturur.
public Holder<EntityStore> createWarp(@Nonnull Warp warp, @Nonnull Store<EntityStore> store);

// Yüklü warp'ların haritasını döndürür. (Not: Mantıktan çıkarılmıştır, derlenmiş koddaki metot adı genellikle `getWarps()` şeklindedir)
public Map<String, Warp> getWarps();

// Menzil içindeki oyuncular için haritadaki işaretçileri günceller.
public void update(@Nonnull World world, @Nonnull GameplayConfig gameplayConfig, @Nonnull WorldMapTracker tracker, int chunkViewRadius, int playerChunkX, int playerChunkZ);
```

---

## Fluid
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.fluid.FluidPlugin`

### Genel Metotlar
```java
public static FluidPlugin get();
public FluidSection getFluidSection(int cx, int cy, int cz);
public BlockSection getBlockSection(int cx, int cy, int cz);
public void setBlock(int x, int y, int z, int blockId);
```

---

## Weather
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.weather.WeatherPlugin`

### Genel Metotlar
```java
public static WeatherPlugin get();
```

---

## WorldGen
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.worldgen.WorldGenPlugin`

### Genel Metotlar
```java
public static WorldGenPlugin get();
```

---

## Farming
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.adventure.farming.FarmingPlugin`

### Genel Metotlar
```java
public static FarmingPlugin get();
```

---

## Camera
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.adventure.camera.CameraPlugin`

_Hiçbir genel (public) metot bulunamadı veya dosya ayrıştırma hatası._

---

## WorldLocationCondition
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.adventure.worldlocationcondition.WorldLocationConditionPlugin`

_Hiçbir genel (public) metot bulunamadı veya dosya ayrıştırma hatası._

---

## NPCCombatActionEvaluator
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.npccombatactionevaluator.NPCCombatActionEvaluatorPlugin`

### Genel Metotlar
```java
public static NPCCombatActionEvaluatorPlugin get();
```

---

## Model
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.model.ModelPlugin`

_Hiçbir genel (public) metot bulunamadı veya dosya ayrıştırma hatası._

---

## Mantling
- **Sürüm**: 1.0.0
- **Açıklama**: Tırmanmayı (mantling) etkinleştir
- **Ana Sınıf**: `com.hypixel.hytale.builtin.mantling.MantlingPlugin`

_Hiçbir genel (public) metot bulunamadı veya dosya ayrıştırma hatası._

---

## SafetyRoll
- **Sürüm**: 1.0.0
- **Açıklama**: Güvenli Yuvarlanmayı (Safety Roll) Etkinleştir
- **Ana Sınıf**: `com.hypixel.hytale.builtin.safetyroll.SafetyRollPlugin`

_Hiçbir genel (public) metot bulunamadı veya dosya ayrıştırma hatası._

---

## SprintForce
- **Sürüm**: 1.0.0
- **Açıklama**: Sprint hızlanmasını/yavaşlamasını etkinleştir
- **Ana Sınıf**: `com.hypixel.hytale.builtin.sprintforce.SprintForcePlugin`

_Hiçbir genel (public) metot bulunamadı veya dosya ayrıştırma hatası._

---

## CrouchSlide
- **Sürüm**: 1.0.0
- **Açıklama**: Eğilerek Kaymayı (Crouch Sliding) Etkinleştir
- **Ana Sınıf**: `com.hypixel.hytale.builtin.crouchslide.CrouchSlidePlugin`

_Hiçbir genel (public) metot bulunamadı veya dosya ayrıştırma hatası._

---

## Parkour
- **Sürüm**: 1.0.0
- **Açıklama**: Kontrol noktası sistemine sahip bir zamanlayıcı eklemek için modül
- **Ana Sınıf**: `com.hypixel.hytale.builtin.parkour.ParkourPlugin`

### Genel Metotlar
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
- **Sürüm**: 1.0.0
- **Açıklama**: Binekleri eklemek için modül
- **Ana Sınıf**: `com.hypixel.hytale.builtin.mounts.MountPlugin`

### Genel Metotlar
```java
public static MountPlugin getInstance();
public static void checkDismountNpc(@Nonnull ComponentAccessor<EntityStore> store, @Nonnull Player playerComponent);
public static void dismountNpc(@Nonnull ComponentAccessor<EntityStore> store, int mountEntityId);
public static void resetOriginalPlayerMovementSettings(@Nonnull PlayerRef playerRef, @Nonnull ComponentAccessor<EntityStore> store);
```

---

## HytaleGenerator
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.hytalegenerator.plugin.HytaleGenerator`

### Genel Metotlar
```java
public CompletableFuture<GeneratedChunk> submitChunkRequest(@Nonnull ChunkRequest request);
public NStagedChunkGenerator createStagedChunkGenerator(@Nonnull ChunkRequest.GeneratorProfile generatorProfile, @Nonnull WorldStructureAsset worldStructureAsset, @Nonnull SettingsAsset settingsAsset);
```

---

## Teleporter
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.adventure.teleporter.TeleporterPlugin`

_Hiçbir genel (public) metot bulunamadı veya dosya ayrıştırma hatası._

---

## Memories
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin`

### Genel Metotlar
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
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.deployables.DeployablesPlugin`

### Genel Metotlar
```java
public static DeployablesPlugin get();
```

---

## Portals
- **Sürüm**: 1.0.0
- **Açıklama**: Portalları eklemek için modül
- **Ana Sınıf**: `com.hypixel.hytale.builtin.portals.PortalsPlugin`

### Genel Metotlar
```java
public static PortalsPlugin getInstance();
public int countActiveFragments();
```

---

## Beds
- **Sürüm**: 1.0.0
- **Açıklama**: Yatakları ve yataklarda uymayı yönetmek için modül
- **Ana Sınıf**: `com.hypixel.hytale.builtin.beds.BedsPlugin`

### Genel Metotlar
```java
public static BedsPlugin getInstance();
```

---

## Ambience
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.ambience.AmbiencePlugin`

### Genel Metotlar
```java
public static AmbiencePlugin get();
public Model getAmbientEmitterModel();
```

---

## CreativeHub
- **Sürüm**: 1.0.0
- **Ana Sınıf**: `com.hypixel.hytale.builtin.creativehub.CreativeHubPlugin`

### Genel Metotlar
```java
public static CreativeHubPlugin get();
public World getOrSpawnHubInstance(@Nonnull World parentWorld, @Nonnull CreativeHubWorldConfig hubConfig, @Nonnull Transform returnPoint);
public World getActiveHubInstance(@Nonnull World parentWorld);
public void clearHubInstance(@Nonnull UUID parentWorldUuid);
public CompletableFuture<World> spawnPermanentWorldFromTemplate(@Nonnull String instanceAssetName, @Nonnull String permanentWorldName);
```
