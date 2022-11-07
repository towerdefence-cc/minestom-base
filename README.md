# minestom-base

This repo is no longer maintained. I have moved away from a core + extension system.

Instead, modules will be defined for different server elements. There will then be one repo for each server type that contains a module for the server type + defines a schema of what other modules should be loaded.

E.g
repo: `lobby`
```java
new MinestomServer.Builder()
  .addModule(KubernetesModule.class, KubernetesModule::new)
  .addModule(LobbyModule.class, LobbyModule::new)
  .build();
