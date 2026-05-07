# Storage Backbone: Design Decisions

## Data Model: SnippetObject

```
SnippetObject
├── id: String (UUID)
├── title: String
├── kind: TEXT | IMAGE | FILE | LINK | MIXED
├── createdAt: Long
├── updatedAt: Long
├── source: String
├── sizeBytes: Long
├── storageMode: INLINE | FILE | REFERENCE
├── previewText: String (max 500 chars, UI display only)
├── inlineContent: String? (only for INLINE mode)
├── fullContentPath: String? (file path or URI)
├── checksum: String? (SHA-256)
├── tags: List<String>
├── pinned: Boolean
├── usageCount: Int
└── lastUsedAt: Long?
```

## Storage Strategy

### Decision Table

| Content Size | Kind | Storage Mode | Where Content Lives |
|---|---|---|---|
| < 64 KB | TEXT | INLINE | `inlineContent` field in JSON metadata |
| ≥ 64 KB | TEXT | FILE | `snippets/snippet_{id}.dat` |
| Any size | IMAGE | FILE | `snippets/snippet_{id}.dat` |
| Any size | FILE | FILE | `snippets/snippet_{id}.dat` |
| N/A | LINK | REFERENCE | `fullContentPath` holds the URI |
| Any size | MIXED | FILE | `snippets/snippet_{id}.dat` |

### 64 KB Inline Threshold

- SharedPreferences + JSON is fast and simple for small snippets
- Above 64 KB, file I/O is more efficient and doesn't bloat the JSON metadata
- The threshold is a code constant, not a user-visible setting

### 100 MB MVP Test Boundary

- 100 MB is NOT a UI-enforced limit
- It is the maximum size tested in the MVP test suite
- Larger content may work but is not tested
- No artificial cap prevents saving larger content

### Never Silently Truncate

The `ClipboardGrabber` in the legacy code truncated previews to 200 characters.
In the new storage backbone:
- `previewText` is a display-only field (max 500 chars)
- Full content is always preserved in `inlineContent` or the backing file
- The UI renders previews but never loses the original

## Fallback Status

When a copy/save operation completes, the user always sees one of:

| Status | Meaning |
|---|---|
| Copied full text | Full content placed in clipboard |
| Copied file reference | File path/reference placed in clipboard |
| Saved but not copied | Content stored, clipboard not set |
| Clipboard failed | Android blocked the clipboard operation |
| Needs manual action | User must copy/paste manually |

## File Layout

```
{app-internal-storage}/
└── borderline/
    └── snippets/
        ├── metadata.json          ← snippet metadata (all fields except large content)
        ├── snippet_{id1}.dat      ← file-backed content
        ├── snippet_{id2}.dat
        └── ...
```

## Integrity

- Every snippet gets a SHA-256 checksum at save time
- `verifyIntegrity()` re-computes and compares
- Useful for detecting storage corruption after crashes/updates
