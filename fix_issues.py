import re

# ── Fix 1 & 3: MainActivity.kt ────────────────────────────────
path = 'app/src/main/java/com/brahmanlabs/browser/MainActivity.kt'
with open(path, 'r') as f:
    c = f.read()

# Add AppCompatDelegate import
c = c.replace(
    'import androidx.appcompat.app.AppCompatActivity',
    'import androidx.appcompat.app.AppCompatActivity\nimport androidx.appcompat.app.AppCompatDelegate'
)

# Force dark mode before super.onCreate  (fixes ALL dialogs/menus/switches)
c = c.replace(
    '    override fun onCreate(savedInstanceState: Bundle?) {\n        super.onCreate(savedInstanceState)',
    '    override fun onCreate(savedInstanceState: Bundle?) {\n        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)\n        super.onCreate(savedInstanceState)'
)

# Add dispatchTouchEvent just before onDestroy  (fixes suggestions dismiss)
dispatch = '''
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && isAddressBarFocused) {
            val abRect = android.graphics.Rect()
            addressBar.getGlobalVisibleRect(abRect)
            val spRect = android.graphics.Rect()
            suggestionsPanel.getGlobalVisibleRect(spRect)
            val x = event.rawX.toInt()
            val y = event.rawY.toInt()
            if (!abRect.contains(x, y) && !spRect.contains(x, y)) {
                hideKeyboard()
            }
        }
        return super.dispatchTouchEvent(event)
    }

'''
c = c.replace('    override fun onDestroy() {', dispatch + '    override fun onDestroy() {')

with open(path, 'w') as f:
    f.write(c)
print("✅ MainActivity.kt fixed (dark mode + suggestions dismiss)")

# ── Fix 2: ShieldsPanel.kt ────────────────────────────────────
path = 'app/src/main/java/com/brahmanlabs/browser/ShieldsPanel.kt'
with open(path, 'r') as f:
    c = f.read()

# Convert hardcoded 155px to proper dp
c = c.replace(
    'attrs.y = 155',
    'attrs.y = (context.resources.displayMetrics.density * 52f).toInt()'
)

with open(path, 'w') as f:
    f.write(c)
print("✅ ShieldsPanel.kt fixed (y position now in dp)")

print("\n🌊 All 3 fixes applied successfully!")
