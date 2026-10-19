#!/usr/bin/env python3
"""Use Apple's sips to generate required icon sizes from the user-supplied original."""
import json,subprocess,pathlib
root=pathlib.Path(__file__).resolve().parents[1]
icons=root/'ios/assets/Assets.xcassets/AppIcon.appiconset'
source=root/'docs/images/shattered-assist-logo.jpg'
contents=json.loads((icons/'Contents.json').read_text())
for item in contents['images']:
    if not item.get('filename'): continue
    pixels=round(float(item['size'].split('x')[0])*float(item['scale'].rstrip('x')))
    subprocess.run(['sips','-s','format','png','-z',str(pixels),str(pixels),str(source),'--out',str(icons/item['filename'])],check=True,stdout=subprocess.DEVNULL)
print('Generated icon sizes from original user logo.')
