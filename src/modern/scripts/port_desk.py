"""Mechanical GuiDesk / GuiBook data adaptation from the complete 0aad501 tree."""
from pathlib import Path
import re
import sys
import json

root = Path(__file__).resolve().parents[3]
legacy_root = Path(sys.argv[1])
legacy = legacy_root / 'src/main/java/com/lulan/shincolle'
values = (legacy / 'reference/Values.java').read_text()
ids = (legacy / 'reference/ID.java').read_text()

def block(source, start):
    begin = source.index('{', start)
    end, depth = begin + 1, 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[begin:end]

def constant(group, name):
    section = block(ids, ids.index('class ' + group))
    m = re.search(r'\b' + name + r'\s*=\s*(-?\d+)\s*;', section)
    assert m, (group, name)
    return m[1]

def clean(source):
    source = re.sub(r'/\*.*?\*/', '', source, flags=re.S)
    source = re.sub(r'//[^\n]*', '', source)
    return '\n'.join(line.expandtabs(4).rstrip() for line in source.splitlines() if line.strip())

def convert(source):
    return re.sub(r'ID\.(\w+)\.(\w+)', lambda m: constant(m[1], m[2]), source)

def write(relative, source):
    target = root / relative
    source = '\n'.join(line.expandtabs(4).rstrip() for line in source.splitlines()) + '\n'
    if '--check' in sys.argv:
        assert target.read_text() == source, target
        print(target.name + ': mechanical parity passed')
    else:
        target.write_text(source)

book_body = convert(clean(block(values, re.search(r'\bBookList\s*=', values).start())[2:-2]))
book_body = re.sub(r'\bput\(', 'data.put(', book_body)
lists = {}
for name in ['ShipBookList', 'EnemyBookList']:
    content = convert(clean(block(values, values.index(name + ' ='))))
    lists[name] = ', '.join(re.findall(r'add\(\(int\)\s*(\d+)\)', content))

icons = []
for icon, registry, name, meta in re.findall(r'put\(ID.Icon.(\w+),\s*new ItemStack\((\w+)\.(\w+)(?:,\s*1,\s*(\d+))?\)\)',
                                           block(values, values.index('ItemIconMap ='))):
    meta = int(meta or '0')
    if registry in ['Items', 'Blocks']:
        path = {'WOOL': 'WHITE_WOOL', 'MAGMA': 'MAGMA_BLOCK', 'DYE': 'LAPIS_LAZULI',
                'POTIONITEM': 'POTION'}.get(name, name).lower()
        path = 'minecraft:' + path
    else:
        path = 'shincolle:' + name.lower() + (str(meta) if meta else '')
        if name == 'ShipSpawnEgg':
            path = 'shincolle:' + {0: 'smallegg', 1: 'largeegg'}.get(meta, 'shipegg' + str(meta))
    icons.append(f'            case {constant("Icon", icon)} -> "{path}";')

write('src/modern/java/com/lulan/shincolle/menu/LegacyDeskBookData.java', '''package com.lulan.shincolle.menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LegacyDeskBookData {
    public static final int[] PAGE_LIMIT = {1, 28, 6, 20, 26, 19, 4};
    public static final List<Integer> ShipBookList = List.of(''' + lists['ShipBookList'] + ''');
    public static final List<Integer> EnemyBookList = List.of(''' + lists['EnemyBookList'] + ''');
    public static final Map<Integer, List<int[]>> BookList = books();
    private LegacyDeskBookData() {}
    private static Map<Integer, List<int[]>> books() {
        Map<Integer, List<int[]>> data = new HashMap<>();
''' + book_body + '''
        return Collections.unmodifiableMap(data);
    }
    public static ItemStack icon(int id) {
        String key = switch (id) {
''' + '\n'.join(icons) + '''
            default -> null;
        };
        return key == null ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(key)));
    }
}
''')

helper = (legacy / 'utility/GuiHelper.java').read_text()
table = clean(block(helper, helper.index('{//gui 3:')))
write('src/modern/java/com/lulan/shincolle/menu/LegacyDeskState.java', '''package com.lulan.shincolle.menu;

public final class LegacyDeskState {
    private static final int[][][] BUTTONS = ''' + table + ''';
    public int function, chapter, page, zoom;
    public static int buttonAt(int panel, int x, int y) {
        if (panel < 0 || panel >= BUTTONS.length) return -1;
        for (int i = 0; i < BUTTONS[panel].length; i++) {
            int[] b = BUTTONS[panel][i];
            if (x >= b[0] && x <= b[2] && y >= b[1] && y <= b[3]) return i;
        }
        return -1;
    }
    public void selectFunction(int button) {
        if (button >= 0 && button < 4) function = function == button + 1 ? 0 : button + 1;
    }
    public void bookClick(int button, int mouseButton) {
        if (button < 0 || button > 8) return;
        if (button > 1) { chapter = button - 2; page = 0; }
        else {
            int step = mouseButton == 0 ? 1 : 10;
            page = Math.max(0, Math.min(LegacyDeskBookData.PAGE_LIMIT[chapter], page + (button == 0 ? -step : step)));
        }
    }
    public static boolean opensRadarInventory(int selected, int clicked, int count) {
        return clicked >= 0 && clicked < count && selected == clicked;
    }
    public static int radarPointOffset(int index, int count) {
        return Math.min(Math.max(0, index), Math.max(0, count - 5));
    }
}
''')

for source in (legacy_root / 'src/main/resources/assets/shincolle/textures/gui').rglob('*.png'):
    if source.name.startswith('GuiDesk') or 'book' in source.parts:
        rel = str(source.relative_to(legacy_root / 'src/main/resources')).lower()
        target = root / 'src/modern/resources' / rel
        if '--check' in sys.argv:
            assert target.read_bytes() == source.read_bytes(), target
        else:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(source.read_bytes())

for modern, old_locale in [('en_us', 'en_US'), ('zh_cn', 'zh_CN'), ('zh_tw', 'zh_TW')]:
    target = root / f'src/modern/resources/assets/shincolle/lang/{modern}.json'
    language = json.loads(target.read_text())
    language = {key: val for key, val in language.items() if not key.startswith('gui.shincolle.desk.')}
    for line in (legacy_root / f'src/main/resources/assets/shincolle/lang/{old_locale}.lang').read_text().splitlines():
        if '=' in line and not line.startswith('#'):
            key, val = line.split('=', 1)
            if any(key.startswith('gui.shincolle:' + prefix) for prefix in ['book.', 'radar.', 'team.', 'general.', 'target.']):
                language[key] = val
    if '--check' not in sys.argv:
        target.write_text(json.dumps(language, ensure_ascii=False, indent=2) + '\n')
    else:
        assert json.loads(target.read_text()) == language, target
if '--check' in sys.argv:
    print('Original desk/book language data and texture byte parity: passed')
