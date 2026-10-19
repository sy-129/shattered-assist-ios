#!/usr/bin/env python3
"""Package the actual RoboVM build; no provisioning profile or private entitlement."""
import hashlib
import json
import os
import pathlib
import plistlib
import shutil
import subprocess
import tempfile
import zipfile

root = pathlib.Path(__file__).resolve().parents[1]
source = list((root / 'ios/build').rglob('*.ipa'))
assert len(source) == 1, f'Expected one RoboVM IPA, found {source}'
output = root / 'packages'
output.mkdir(exist_ok=True)
with tempfile.TemporaryDirectory() as temporary:
    stage = pathlib.Path(temporary)
    with zipfile.ZipFile(source[0]) as z:
        assert z.testzip() is None
        for member in z.namelist():
            assert not member.startswith('/') and '..' not in pathlib.PurePosixPath(member).parts
        z.extractall(stage)
    apps = list((stage / 'Payload').glob('*.app'))
    assert len(apps) == 1
    app = apps[0]
    info = plistlib.loads((app / 'Info.plist').read_bytes())
    assert info['CFBundleIdentifier'] == 'io.github.sy129.shatteredassist.apple'
    assert info['CFBundleDisplayName'] == 'Shattered Assist'
    binary = app / info['CFBundleExecutable']
    binary.chmod(0o755)
    archs = subprocess.check_output(['lipo', '-archs', str(binary)], text=True).strip()
    assert archs == 'arm64', archs
    # Remove profiles from upstream build inputs if any; ESign adds user's own.
    for profile in app.rglob('embedded.mobileprovision'):
        profile.unlink()
    subprocess.run(['codesign', '--force', '--deep', '--sign', '-', '--timestamp=none', str(app)], check=True)
    subprocess.run(['codesign', '--verify', '--deep', '--strict', str(app)], check=True)
    commit = os.environ.get('GITHUB_SHA', 'local')
    provenance = {'source_commit': commit, 'upstream_commit': '57a4e06a4caf162446d1c28caa7983f0493fecf0',
                  'bundle_id': info['CFBundleIdentifier'], 'architecture': archs,
                  'minimum_ios': info.get('MinimumOSVersion'), 'signing': 'ad-hoc; user must re-sign with valid certificate',
                  'device_validation': 'pending: iPhone 16 Pro / iOS 27.0.1'}
    # Leave the signed bundle untouched after codesign. Provenance lives outside it.
    target = output / 'ShatteredAssist.ipa'
    subprocess.run(['ditto', '-c', '-k', '--keepParent', str(stage / 'Payload'), str(target)], check=True)
    with zipfile.ZipFile(target) as z:
        assert z.testzip() is None
        assert any(n.endswith('messages/windows/windows_zh.properties') for n in z.namelist())
    digest = hashlib.sha256(target.read_bytes()).hexdigest()
    provenance.update(sha256=digest, bytes=target.stat().st_size)
    (output / 'ShatteredAssist.ipa.sha256').write_text(digest + '  ShatteredAssist.ipa\n')
    (output / 'build-info.json').write_text(json.dumps(provenance, indent=2) + '\n')
    print(json.dumps(provenance, indent=2))
