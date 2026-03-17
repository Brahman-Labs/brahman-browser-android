path = 'app/src/main/java/com/brahmanlabs/browser/MainActivity.kt'
with open(path, 'r') as f: c = f.read()

# Add toolbarTopOffset if missing
if 'toolbarTopOffset' not in c:
    c = c.replace(
        '    private var toolbarHidden = false',
        '    private var toolbarHidden = false\n    private var toolbarTopOffset = 0'
    )
    print("✅ Added toolbarTopOffset variable")
else:
    # It exists but maybe in wrong place — check if it's before forceShowToolbars usage
    print("toolbarTopOffset exists, checking...")
    idx = c.find('toolbarTopOffset')
    print(f"First occurrence at char {idx}")
    print(c[idx-100:idx+100])

with open(path, 'w') as f: f.write(c)
