# Fabric Tag API (v1)

This module contains APIs for working with data pack tags.

## Tag entry removals

*Tag entry removals* may be used to remove entries from a tag.

These may be used to remove values from gameplay facing tags, to exclude specific entries from
referenced tags from being applied via a tag's `values` field, or to just remove unwanted values.

All tag files contain an additional field: `fabric:remove`, which is an array of entries you
wish to remove, following the same syntax as the `values` field.

See the module javadoc for more information about tag entry removals.
