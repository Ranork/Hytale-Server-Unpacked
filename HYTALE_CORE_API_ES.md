# Hytale Server Core API Documentación

Este documento contiene descripciones detalladas de las clases y métodos importantes en el paquete `com.hypixel.hytale.server.core` y sus subpaquetes. Esta información está preparada para servir como referencia para la gestión del servidor y los procesos de desarrollo de mods.


## Tabla de Contenidos
1. [Core (Paquete Principal)](#core-paquete-principal)
2. [Auth (Autenticación)](#auth-autenticación)
3. [Command](#command)
4. [Event](#event)
5. [Plugin](#plugin)
6. [Permissions](#permissions)
7. [Util (Herramientas)](#util-herramientas)


---

## Core (Paquete Principal)
**Paquete:** `com.hypixel.hytale.server.core`

### `HytaleServer`
La clase central de gestión del servidor (Singleton). Coordina el ciclo de vida del servidor (inicio, bucle, apagado), los gestores (plugin, command, event) y los módulos.

**Métodos públicos importantes:**
*   `static HytaleServer get()`: Devuelve la única instancia del servidor en ejecución.
*   `EventBus getEventBus()`: Devuelve el objeto `EventBus` donde se gestionan los eventos a nivel de servidor.
*   `PluginManager getPluginManager()`: Devuelve el gestor de plugins.
*   `CommandManager getCommandManager()`: Devuelve el gestor de comandos.
*   `HytaleServerConfig getConfig()`: Devuelve el objeto que representa el archivo de configuración del servidor (`hytale-server.json`).
*   `void shutdownServer(ShutdownReason reason)`: Apaga el servidor por la razón especificada (`ShutdownReason`).
*   `String getServerName()`: Devuelve el nombre del servidor establecido en la configuración.
*   `boolean isBooted()`: Indica si el servidor ha arrancado completamente.
*   `boolean isShuttingDown()`: Indica si el servidor está en proceso de apagado.
*   `Instant getBoot()`: Devuelve el momento en que el servidor fue iniciado.

### `HytaleServerConfig`
Mantiene y gestiona la configuración del servidor. Los cambios pueden guardarse en el archivo en disco.

**Métodos públicos importantes:**
*   `static HytaleServerConfig load()`: Carga la configuración desde la ruta predeterminada.
*   `static CompletableFuture<Void> save(HytaleServerConfig config)`: Guarda la configuración en disco.
*   `void setMotd(String motd)`: Establece el mensaje del día (MOTD) visible en la lista de servidores.
*   `int getMaxPlayers()`: Devuelve el número máximo de jugadores.
*   `void setMaxPlayers(int maxPlayers)`: Establece el número máximo de jugadores.
*   `Module getModule(String moduleName)`: Recupera la configuración del módulo nombrado (por ejemplo, "WorldModule").

---

## Auth (Autenticación)
**Paquete:** `com.hypixel.hytale.server.core.auth`

### `ServerAuthManager`
Gestiona los procesos de autenticación en el servidor. Verifica la validez de jugadores.

**Métodos públicos importantes:**
*   `static ServerAuthManager getInstance()`: Devuelve la instancia del gestor.
*   `void initialize()`: Prepara las claves y estructuras de autenticación.
*   `AuthMode getAuthMode()`: Devuelve el modo de autenticación del servidor (ONLINE, OFFLINE, etc.).

### `SessionServiceClient`
Se comunica con los servicios de sesión de Hytale (API Backend). Se utiliza para verificar sesiones de jugadores y recuperar información de perfiles.

**Métodos públicos importantes:**
*   `CompletableFuture<String> requestAuthorizationGrantAsync(...)`: Solicita un permiso de autorización.
*   `CompletableFuture<String> exchangeAuthGrantForTokenAsync(...)`: Intercambia el permiso por un token de acceso.
*   `GameProfile[] getGameProfiles(String oauthAccessToken)`: Recupera perfiles de jugadores usando el token de acceso.
*   `GameSessionResponse createGameSession(...)`: Inicia una nueva sesión de juego.

### `PlayerAuthentication`
Una clase de datos que contiene la información de autenticación de un jugador (UUID, Nombre de usuario).
---

## Command
**Paquete:** `com.hypixel.hytale.server.core.command.system`
### `CommandManager`
El corazón del sistema de comandos. Registra, analiza y dirige comandos al procesador relevante.

**Métodos públicos importantes:**
*   `void registerCommands()`: Registra comandos del sistema predeterminados.
*   `CommandRegistration register(AbstractCommand command)`: Registra un nuevo objeto de comando en el sistema. Se usa para agregar comandos personalizados en mods.
*   `CompletableFuture<Void> handleCommand(CommandSender sender, String commandString)`: Ejecuta una línea de comando en nombre del remitente.
*   `Map<String, AbstractCommand> getCommandRegistration()`: Devuelve un mapa de todos los comandos registrados.

### `CommandSender`
Una interfaz que representa la entidad que ejecuta el comando. Puede ser `Player` o `ConsoleSender`.

**Métodos:**
*   `void sendMessage(Message message)`: Envía un mensaje al remitente.
*   `String getName()`: Devuelve el nombre del remitente.
*   `boolean hasPermission(String permission)`: Comprueba si el remitente tiene un permiso específico.

---

## Event
**Package:** `com.hypixel.hytale.event` (y `com.hypixel.hytale.server.core.event`)

### `EventBus`
El centro del sistema basado en eventos. Permite el envío y la escucha de eventos.

**Métodos públicos importantes:**
*   `EventRegistration register(Class<T> eventClass, Consumer<T> consumer)`: Registra un listener para una clase de evento específica.
*   `IEventDispatcher dispatchFor(Class<T> eventClass)`: Devuelve un publisher para una clase de evento.

### Example Events (`server.core.event.events`)
*   `BootEvent`: Se activa cuando el servidor se inicia.
*   `ShutdownEvent`: Se activa cuando el servidor comienza a apagarse.

---

## Plugin
**Package:** `com.hypixel.hytale.server.core.plugin`

### `PluginManager`
Gestiona los plugins (Mods/Plugins) cargados en el servidor.

**Métodos públicos importantes:**
*   `List<PluginBase> getPlugins()`: Lista todos los plugins cargados y activos.
*   `PluginBase getPlugin(PluginIdentifier identifier)`: Recupera el plugin con el ID especificado.
*   `void setup()`: Inicia la fase de configuración de los plugins.
*   `void start()`: Inicia (habilita) los plugins.
*   `void shutdown()`: Detiene de forma segura los plugins.

### `PluginBase`
La clase base para todos los plugins. Esta clase se hereda al desarrollar mods (usualmente a través de `JavaPlugin`).

---

## Permissions
**Package:** `com.hypixel.hytale.server.core.permissions`

### `HytalePermissions`
Contiene constantes que definen permisos estándar (nodos de permiso) dentro del servidor.

**Constantes importantes:**
*   `COMMAND_BASE`: Permiso básico de comando (`hytale.command`).
*   `ASSET_EDITOR`: Permiso para el editor de assets.
*   `FLY_CAM`: Permiso para el uso de la cámara voladora.
*   `fromCommand(String name)`: Crea una string de permiso para un nombre de comando (por ejemplo, `hytale.command.give`).

---

## Util (Herramientas)
**Package:** `com.hypixel.hytale.server.core.util`

### `MessageUtil`
Contiene métodos auxiliares para formatear, colorear y enviar mensajes a los jugadores.

**Métodos públicos importantes:**
*   `AttributedString toAnsiString(Message message)`: Convierte un objeto de mensaje a formato ANSI para que aparezca coloreado en la consola.
*   `formatText(String text, ...)`: Reemplaza parámetros (como {0}, {name}) en el texto con sus valores.
### `NotificationUtil` (Otra herramienta examinada)
Simplifica el envío de notificaciones.