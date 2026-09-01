# Fabric Resource Loader v1

## System Properties

| Property                                                  | Type    | Default                                                      | Description                                                                     |
|:----------------------------------------------------------|:--------|:-------------------------------------------------------------|:--------------------------------------------------------------------------------|
| `fabric.resource_loader.debug.pack.dump_from_in_memory`   | boolean | `true` in development environments, or `false` otherwise     | Automatically dumps any in-memory resource packs.                               |
| `fabric.resource_loader.pack.virtual_async_threads`       | int     | Available machine threads divided by 2, with a minimum of 1. | The amount of worker threads for async virtual resources.                       |
| `fabric.resource_loader.debug.profile_resource_reloaders` | boolean | `false`                                                      | Profiles and prints to the console the profiling results of resource reloaders. |
| `fabric.resource_loader.debug.reloaders_order`            | boolean | `false`                                                      | Prints to console the application order of resource reloaders.                  |
