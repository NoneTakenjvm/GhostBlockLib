# GhostBlockLib 1.4 — Memory & Registration Refactor

Summary of changes for consumers (e.g. SkyCloud) upgrading to this version.

## Why

A prison-server heap dump showed hundreds of `GhostBlockCuboid` instances retaining tens of GB, driven by dense per-block object storage and eager chunk allocation. This release reduces **per-cuboid memory** while keeping the public API stable.

SkyCloud lifecycle fixes (register only when owner is online, unregister on quit) cap **how many** cuboids are live. This release caps **how large** each live cuboid is. Both are needed at scale.

---

## Public API

**No breaking API changes.** Existing call sites continue to work:

- `GhostBlockManager.registerGhostBlockCuboid(cuboid)`
- `GhostBlockManager.unregisterGhostBlockCuboid(cuboid)` / `cuboid.unregister()`
- `cuboid.fill(consumer)`, `cuboid.setBlocks(...)`, `cuboid.refresh(players)`
- `cuboid.expand(...)`, `cuboid.setMin(...)`, `cuboid.setMax(...)`
- `GhostBlock`, `AsyncGhostBlockBreakEvent`, etc.

---

## Internal changes

### Compact block storage

- **Before:** Each chunk pre-allocated `GhostBlock[16][384][16]` (~98k reference slots per chunk, even when empty). Each set block stored a `GhostBlock` + `WrappedBlockState`.
- **After:** Lazy 16×16×16 **sections** with a `Material` palette (~4 KB per populated section). `WrappedBlockState` is built only when sending packets (shared cache per `Material`).

### Incremental bounds & registration

- **Before:** `register()` called `updateBounds(false)`, which cleared and rebuilt all chunks (wiping block data on re-register). `expand` / `setMin` / `setMax` unregistered, cleared, and re-registered — also wiping data.
- **After:** Bounds updates **diff** the chunk footprint: overlapping data is preserved, only removed columns are freed. Registration is **idempotent** (second `register` is a no-op).

---

## Behavior changes to be aware of

### `register` is safe to call twice

A second `registerGhostBlockCuboid()` no longer wipes block data. Consumer-side dedup (e.g. `REGISTERED_CUBOIDS`) is still fine but less critical.

### `unregister` still clears all block data

`unregister()` removes the cuboid from the spatial index and clears `cuboid.getChunks()`. Repopulate via `fill` / `reset` after re-registering — same as before.

### Expand / bound changes preserve blocks

`expand()`, `setMin()`, and `setMax()` no longer destroy populated blocks in the overlapping region. No forced re-fill after expand unless you want new blocks in the expanded area.

### Registered vs unregistered on bounds change

| State when bounds change | Spatial index |
|--------------------------|---------------|
| Registered               | Patched incrementally; stays registered |
| Unregistered             | Not added to index; expand after unregister does **not** resurrect the cuboid |

Explicit `register` is still required when an owner comes back online.

### `fill` consumer contract

`setBlocks` / `fill` reuse a single scratch `GhostBlock` for the consumer callback. Do not retain the `GhostBlock` reference passed to the consumer for later use — only call `setType()` within the callback.

---

## Storage limits

- Block data is stored as **`Material` only** (no waterlogged, facing, etc.). Suitable for simple mines (few block types per mine).
- Up to **256 distinct materials per section**; palette cost is negligible for typical mine composition.

---

## Verification

### Automated (in this repo)

```bash
mvn test
```

Covers section storage, lazy chunks, incremental bounds, and idempotent registration.

### Manual / in-game (SkyCloud)

| Flow | Check |
|------|-------|
| Owner joins → mine registers → reset | Blocks render and are mineable |
| Owner quits → rejoins | Re-register and repopulate work |
| Autominer enter/exit | Unregister/register cycle works |
| Mine expand while owner online | Existing blocks preserved; new area writable |
| Break blocks | Events fire; air syncs to client |
| Chunk reload / teleport | Ghost blocks still overlay correctly |

---

## Rollout

1. Build/install GhostBlockLib (`mvn install` or publish to private Maven).
2. Bump the dependency version in SkyCloud.
3. Deploy to staging; exercise the flows above.
4. Compare heap with pre-refactor baseline under realistic player counts.

No SkyCloud code changes are **required** unless you added workarounds for the old wipe-on-register or wipe-on-expand behaviour — those workarounds may now be redundant but are harmless if left in place.
