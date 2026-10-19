#!/usr/bin/env python3
"""Check vendored CC0 art against the pinned provenance manifest."""
from pathlib import Path
import hashlib,json
root=Path(__file__).resolve().parents[1]
manifest=json.loads((root/'third_party/superpowers/manifest.json').read_text())
assert manifest['license']=='CC0-1.0'
for entry in manifest['assets']:
    data=(root/'core/src/main/assets'/entry['file']).read_bytes()
    assert hashlib.sha256(data).hexdigest()==entry['sha256'],entry['file']
    assert not entry['modified']
assert (root/'core/src/main/assets/home/superpowers/LICENSE.txt').read_bytes()==(root/'third_party/superpowers/LICENSE.txt').read_bytes()
print(f"Verified {len(manifest['assets'])} unmodified CC0 art files and bundled license")
