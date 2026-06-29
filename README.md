# TAN Curios Compat — NeoForge 1.21.1

Un mod de compatibilité qui permet de porter les armures de **Tough As Nails** dans les **slots secondaires de Curios**.

## ✅ Fonctionnalités

- Les armures TAN peuvent être équipées dans les slots Curios correspondants :
  - 🪖 Casques TAN → slot `head`
  - 🛡️ Plastrons TAN → slot `body`
  - 👖 Jambières TAN → slot `legs`
  - 👟 Bottes TAN → slot `feet`
- Les bonus d'armure (défense, solidité, résistance aux coups) sont **entièrement appliqués** en secondaire
- Les effets de température de TAN (isolation thermique, etc.) fonctionnent en slot Curios

## 📦 Dépendances requises

| Mod | Version |
|-----|---------|
| NeoForge | 21.1.172+ |
| Curios API | 9.1.1+1.21.1 |
| Tough As Nails | 4.2.0.1+1.21.1 |

## 🔧 Compilation

```bash
# Cloner le projet
git clone <repo>
cd tan-curios-mod

# Compiler avec Gradle
./gradlew build

# Le JAR se trouve dans build/libs/
```

## 📁 Structure du projet

```
src/main/java/com/example/tancurios/
├── TanCuriosMod.java                    # Point d'entrée du mod
└── curios/
    ├── TANCuriosItem.java               # Implémentation ICurio pour armures TAN
    ├── TANCuriosCapabilityProvider.java # Détection des items TAN
    ├── TANCuriosEventHandler.java       # Enregistrement des capabilities
    └── TANCuriosSlotHandler.java        # Validation des slots

src/main/resources/
├── META-INF/neoforge.mods.toml         # Métadonnées du mod
├── data/tancurios/
│   ├── curios/slots/                   # Définition des slots Curios
│   │   ├── body.json
│   │   ├── head.json
│   │   ├── legs.json
│   │   └── feet.json
│   └── tags/items/curios/             # Tags des items TAN pour chaque slot
│       ├── body.json
│       ├── head.json
│       ├── legs.json
│       └── feet.json
└── pack.mcmeta
```

## ⚠️ Notes importantes

### Vérifier les IDs des items TAN
Les IDs d'items dans `data/tancurios/tags/items/curios/*.json` correspondent aux items de Tough As Nails 4.x.
Si tu utilises une version différente, vérifie les IDs dans le fichier JAR de TAN avec :
```bash
unzip -l toughasnails-*.jar | grep "items/"
```

### Ajuster les versions dans `gradle.properties`
```properties
curios_version=9.1.1+1.21.1   # Vérifier sur CurseForge/Modrinth
tan_version=4.2.0.1+1.21.1    # Vérifier la dernière version TAN
```

## 🎮 En jeu

1. Ouvre l'inventaire Curios (touche par défaut : `G`)
2. Tu verras de nouveaux slots : **Head**, **Body**, **Legs**, **Feet**
3. Place une armure TAN compatible dans le slot correspondant
4. Les bonus s'appliquent en plus de l'armure principale !
