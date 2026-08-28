import zipfile
with zipfile.ZipFile('log5.zip') as z:
    for name in z.namelist():
        if '6_Build' in name:
            content = z.read(name).decode('utf-8','replace')
            for ln in content.split('\n'):
                s = ln.strip()
                if s.startswith('e:') or s.startswith('w:') or 'ERROR:' in s:
                    print(s[:500])
