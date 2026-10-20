#!/usr/bin/env python3
"""Check actual class-file Java API references against the selected RoboVM rt JAR.

The host JDK can compile APIs absent from RoboVM. Read class files directly so
the host JVM cannot substitute its own java.lang classes during verification.
RoboVM lowers java.lang.invoke bootstrap machinery during AOT; exclude it here.
"""
import argparse
import struct
import zipfile
from pathlib import Path


def read_class(data):
    position = 8

    def u2():
        nonlocal position
        value = struct.unpack_from('>H', data, position)[0]
        position += 2
        return value

    pool = [None] * u2()
    index = 1
    while index < len(pool):
        tag = data[position]
        position += 1
        if tag == 1:
            length = u2()
            # Names and descriptors use ASCII; replace modified UTF8 in strings.
            value = data[position:position + length].decode('utf8', errors='replace')
            position += length
        elif tag in (3, 4):
            value = None
            position += 4
        elif tag in (5, 6):
            value = None
            position += 8
        elif tag in (7, 8, 16, 19, 20):
            value = u2()
        elif tag in (9, 10, 11, 12, 17, 18):
            value = (u2(), u2())
        elif tag == 15:
            position += 1
            value = u2()
        else:
            raise ValueError('Unknown class constant tag: ' + str(tag))
        pool[index] = (tag, value)
        index += 2 if tag in (5, 6) else 1

    def name(index):
        return pool[pool[index][1]][1] if index else None

    u2()  # access flags
    own, parent = name(u2()), name(u2())
    parents = ([parent] if parent else []) + [name(u2()) for _ in range(u2())]

    def skip_attributes():
        nonlocal position
        for _ in range(u2()):
            u2()
            length = struct.unpack_from('>I', data, position)[0]
            position += 4 + length

    members = set()
    for kind in ('field', 'method'):
        for _ in range(u2()):
            u2()
            member, descriptor = pool[u2()][1], pool[u2()][1]
            members.add((kind, member, descriptor))
            skip_attributes()
    references = set()
    for item in pool:
        if item is not None and item[0] in (9, 10, 11):
            owner, signature = item[1]
            member, descriptor = pool[signature][1]
            references.add((name(owner), 'field' if item[0] == 9 else 'method',
                            pool[member][1], pool[descriptor][1]))
    return own, parents, members, references


def verify(runtime, roots):
    errors, checked = set(), set()
    with zipfile.ZipFile(runtime) as rt:
        available = set(rt.namelist())
        cache = {}

        def load(owner):
            if owner not in cache:
                cache[owner] = read_class(rt.read(owner + '.class'))
            return cache[owner]

        def resolves(owner, member, seen=None):
            seen = set() if seen is None else seen
            if owner in seen or owner + '.class' not in available:
                return False
            seen.add(owner)
            _, parents, members, _ = load(owner)
            if member in members:
                return True
            # Constructors are not inherited.
            return member[1] != '<init>' and any(resolves(p, member, seen) for p in parents)

        for root in roots:
            for path in Path(root).rglob('*.class'):
                own, _, _, references = read_class(path.read_bytes())
                for owner, kind, member, descriptor in references:
                    if not owner.startswith('java/') or owner.startswith('java/lang/invoke/'):
                        continue
                    checked.add((owner, kind, member, descriptor))
                    if not resolves(owner, (kind, member, descriptor)):
                        errors.add(f'{own}: unavailable {owner}.{member}{descriptor}')
    return sorted(errors), len(checked)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--runtime', required=True, type=Path)
    parser.add_argument('classes', nargs='+', type=Path)
    args = parser.parse_args()
    for root in args.classes:
        if not root.is_dir() or not any(root.rglob('*.class')):
            parser.error('No compiled classes in ' + str(root))
    errors, checked = verify(args.runtime, args.classes)
    if errors:
        raise SystemExit('\n'.join(errors))
    print(f'PASS: {checked} Java member references exist in {args.runtime.name}')
