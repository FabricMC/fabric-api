# Fabric Tag API (v1)

This module contains APIs for working with data pack tags.

## Tag aliases

*Tag alias groups* merge tags that refer to the same set of registry entries.
The contained tags will be linked together and get the combined set of entries
of all the aliased tags in a group.

Tag alias groups can be defined in data packs in the `data/<mod namespace>/fabric/tag_alias/<registry>`
directory. `<registry>` is the path of the registry's ID, prefixed with `<registry's namespace>/` if it's
not `minecraft`.

The JSON format of tag alias groups is an object with a `tags` list containing plain tag IDs.

See the module javadoc for more information about tag aliases.
