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
    assert set(info['UISupportedInterfaceOrientations']) == {'UIInterfaceOrientationLandscapeLeft','UIInterfaceOrientationLandscapeRight'}
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
                  'device_validation': 'pending: iPhone 16 Pro / iOS 27.0.1',
                  'features': ['Chinese draggable/hidden assist menu','8-way touch joystick','landscape safe-area layout','independent home','marriage and child growth','3-species breeding ecology','mount riding','CC0 slash animation'],
                  'external_art_commit': 'e8674a03ab4456802f71f848c4df79eccca23f7a'}
    # Leave the signed bundle untouched after codesign. Provenance lives outside it.
    target = output / 'ShatteredAssist.ipa'
    subprocess.run(['ditto', '-c', '-k', '--keepParent', str(stage / 'Payload'), str(target)], check=True)
    with zipfile.ZipFile(target) as z:
        assert z.testzip() is None
        assert any(n.endswith('messages/windows/windows_zh.properties') for n in z.namelist())
        prefix='Payload/'+app.name+'/'
        manifest=json.loads((root/'third_party/superpowers/manifest.json').read_text())
        for entry in manifest['assets']:
            assert hashlib.sha256(z.read(prefix+entry['file'])).hexdigest()==entry['sha256'],entry['file']
        labels=z.read(prefix+'messages/windows/windows_zh.properties').decode('utf-8')
        assert 'windows.wndassist.button=辅助' in labels
        assert z.read(prefix+'home/superpowers/LICENSE.txt')==(root/'third_party/superpowers/LICENSE.txt').read_bytes()
        forbidden={'HomeRenderSmoke.class','HomeWorldTest.class','issuer.pem','auth.db'}
        assert not any(pathlib.PurePosixPath(n).name in forbidden for n in z.namelist())
    source_zip = output / 'ShatteredAssist-source.zip'
    tracked = subprocess.check_output(['git', 'ls-files', '-z'], cwd=root).decode().split('\0')
    with zipfile.ZipFile(source_zip, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as source_archive:
        for relative in tracked:
            if relative and not relative.startswith('packages/'):
                source_archive.write(root / relative, 'ShatteredAssist/' + relative)
    with zipfile.ZipFile(source_zip) as source_archive:
        assert source_archive.testzip() is None
    source_digest = hashlib.sha256(source_zip.read_bytes()).hexdigest()
    (output / 'ShatteredAssist-source.zip.sha256').write_text(source_digest + '  ShatteredAssist-source.zip\n')
    provenance.update(source_zip_sha256=source_digest)
    digest = hashlib.sha256(target.read_bytes()).hexdigest()
    provenance.update(sha256=digest, bytes=target.stat().st_size)
    (output / 'ShatteredAssist.ipa.sha256').write_text(digest + '  ShatteredAssist.ipa\n')
    (output / 'build-info.json').write_text(json.dumps(provenance, indent=2) + '\n')
    print(json.dumps(provenance, indent=2))
