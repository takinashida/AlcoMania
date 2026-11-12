# alcomania

Небольшой мод для **Minecraft Forge 1.20.1**, который добавляет систему опьянения.

## Что делает мод

| Опьянение | Что происходит |
|-----------|----------------|
| 0         | Всё нормально. |
| 25+       | Камера слабо шатается, появляется эффект **Sway**.|
| 50+       | Шатание усиливается|
| 75+       | Шатание максимальное|
| > 100     | Дополнительно накладывается **иссушение IV**. |

Опьянение само уменьшается на **1 единицу в секунду** (настраивается в `config/alcomania-common.toml`).

> Иссушение вешается строго выше `witherThreshold` (по умолчанию 100).
> Так как смерть наступает уже на `deathThreshold` (тоже 100), по умолчанию
> иссушение успевает только наложиться в момент смерти. Поднимите
> `deathThreshold`, например до 120, если хотите, чтобы игрок жил и страдал от него.

## Напитки

| Предмет | Опьянение | Бонус | Применений | Перезарядка |
|---------|-----------|-------|------------|-------------|
| Яблочный сидр (`alcomania:apple_cider`) | +30 | Кастомный эффект «Сидр» (Скорость II) на 30 секунд | 2 | 30 сек |
| Пиво (`alcomania:beer`) | +40 | Восстанавливает 16 HP (8 сердец) | 2 | — |
| Водка (`alcomania:vodka`) | +100 | Эффект «Водка»: каждые 5 секунд на 1 секунду подсвечивает сущности в радиусе 16 блоков (сквозь стены, только выпившему) и проигрывает звук сонара, 30 секунд | 4 | 30 сек |
| Вино (`alcomania:wine`) | +75 | Кастомный эффект «Вино»: −1 единица ко входящему урону (кроме голода и утопления) и −10% к скорости на 30 секунд | 4 | 30 сек |
| Виски (`alcomania:whisky`) | +80 | Полная неуязвимость на 10 секунд, затем гарантированная смерть | 3 | 30 сек |
| Медовуха (`alcomania:honey`) | +30 | Одноразовый «Медовый щит»: блокирует 100% урона из любых источников и исчезает после первого поглощения | 4 | 10 сек |
| Текила (`alcomania:tequila`) | +100 | Эффект «Текила» на 30 секунд: попадания снарядов накладывают горение — 1 урон каждые 2 сек. в течение 8 сек., партиклы пламени вокруг цели и −50% к входящему исцелению; новое попадание обновляет эффект. Огнестойкость не защищает, вода не останавливает эффект | 4 | — |
| Ликёр (`alcomania:liqueur`) | +50 | Эффект «Ликёр» на 6 минут: +50% к скорости добычи киркой/топором и 20% шанс двойного лута на железной/угольной руде и `jeg:brimstone_ore` | 2 | 10 мин |
| Рассол (`alcomania:rassol`) | 0 | Снимает **все** эффекты и обнуляет шкалу опьянения | 1 | 30 сек |

> **Совместимость с Survivor's Arsenal:** если установлен
> `survivorsarsenal-1.1.7_test_version.jar`, то после последнего применения
> любой алкоголь выбрасывает `survivorsarsenal:empty_glass_bottle`.
> Рассол пустую бутылку не оставляет.

Количество применений хранится **в прочности предмета**, а не в NBT.
Поэтому в инвентаре видна обычная полоска прочности: она показывает, сколько глотков осталось.
Питьё занимает **32 тика (1.6 секунды)** и использует анимацию `UseAnim.DRINK`, как у зелий.

Перезарядка напитков **не сбрасывается после смерти**: остаток кулдауна переносится
на возрождённого игрока, поэтому сразу после респавна выпить тот же напиток нельзя.

Все числа из таблицы (опьянение, применения, длительность эффектов, бонусы,
перезарядка) вынесены в конфиг.

> **Виски:** эффект длится 10 секунд и даёт полную неуязвимость. Когда эффект
> заканчивается или его снимают молоком/командой, игрок гарантированно умирает.
> В момент употребления один раз звучит плевок ламы, а во время действия
> игрок слышит сердцебиение — только он, другие игроки его не слышат.
>
> **Медовуха:** при снятии или окончании «Медового щита» играет звук
> разрушения щита.

### Совместимость с Hardcore True Darkness

Эффект **`alcomania:night_vision`** («Ночное зрение») — отдельный эффект,
который на сервере поддерживает ванильный Night Vision с короткой
длительностью и скрытой иконкой. Если Night Vision снимут, он будет добавлен
снова в ближайшем тике.

Hardcore True Darkness видит ванильный Night Vision и отключает свою темноту,
поэтому эффект работает и вместе с этим модом. `/effect clear` корректно
снимает и кастомный эффект, и поддерживаемый Night Vision.

Выдать эффект можно вручную:

```mcfunction
/effect give @s alcomania:night_vision 60 0
```

### Спектральный эффект

Добавлен отдельный эффект **`alcomania:spectral`** («Спектральный»),
сделанный по мотивам ванильного Glowing:

- игрок с этим эффектом подсвечивается бирюзовым оттенком;
- подсветка не показывает игрока сквозь стены — он виден только в прямой видимости;
- ванильный Glowing и спектральные стрелы **не изменены** и работают как раньше.

Применить эффект можно командой:

```mcfunction
/effect give @s alcomania:spectral 30 0
```

## Структура кода

```
src/main/java/net/takinashida/moonshine/
├── MoonShine.java                  — точка входа мода
├── Config.java                     — все настройки баланса и визуала
├── ModItems.java                   — регистрация предметов
├── ModEffects.java                 — регистрация кастомных эффектов
├── ModSounds.java                  — регистрация звуков mod'а
├── ModCreativeTabs.java             — добавление предметов в креатив
├── capability/
│   ├── DrunkennessCapability.java  — ключ capability
│   ├── DrunkennessInstance.java    — само значение опьянения
│   ├── DrunkennessProvider.java    — хранение и сохранение в NBT
│   └── CapabilityRegistration.java — регистрация capability
├── event/
│   ├── CapabilityAttachHandler.java — выдача capability игроку и синхронизация
│   ├── ModEvents.java              — трезвение, эффекты, смерть, иссушение
│   ├── CombatEvents.java           — вино и щит медовухи
│   ├── TequilaEvents.java          — горение и снижение исцеления от текилы
│   └── LiqueurEvents.java          — скорость добычи и двойной лут от ликёра
├── item/
│   ├── DrinkableItem.java          — общая логика питья, прочности и кулдауна
│   ├── AppleCider.java             — яблочный сидр
│   ├── BeerBottle.java             — пиво (id: alcomania:beer)
│   ├── Vodka.java                  — водка
│   ├── Wine.java                   — вино
│   ├── Whisky.java                 — виски (неуязвимость + смерть после эффекта)
│   ├── Honey.java                  — медовуха (одноразовый щит)
│   ├── Tequila.java                — текила (горение от выстрелов)
│   ├── Liqueur.java                — ликёр (скорость добычи и двойной лут)
│   └── Rassol.java                 — рассол
├── effect/
│   ├── SwayEffect.java             — эффект-маркер «Шатание»
│   ├── CiderEffect.java            — кастомный эффект «Сидр» (Скорость II)
│   ├── VodkaEffect.java            — кастомный эффект «Водка» (подсветка в радиусе)
│   ├── WineEffect.java             — кастомный эффект «Вино» (−1 урона, −10% скорости)
│   ├── WhiskyEffect.java           — эффект «Виски» (неуязвимость, смерть при снятии)
│   ├── HoneyEffect.java            — эффект «Медовый щит» (одноразовая блокировка урона)
│   ├── SpectralEffect.java         — эффект «Спектральный» (подсветка без «рентгена»)
│   ├── TequilaEffect.java          — бафф текилы
│   ├── TequilaBurnEffect.java      — горение (урон во времени, пламя + −50% исцеления)
│   ├── LiqueurEffect.java          — бафф ликёра
│   └── NightVisionEffect.java      — «Ночное зрение», не блокируется Darkness-модами
├── network/
│   ├── Networking.java             — сетевой канал
│   └── SyncDrunkennessPacket.java  — пакет «сервер → клиент»
└── client/
    ├── ClientModEvents.java        — регистрация HUD и экрана конфига
    ├── ClientConfig.java           — клиентский конфиг (видимость шкал)
    ├── MyConfigScreen.java         — экран настроек мода
    ├── ClientData.java             — клиентский кэш значения
    ├── DrunkennessHUD.java         — шкала опьянения в виде бутылки
    ├── DrunkennessBarHUD.java      — горизонтальная шкала опьянения (4 сегмента)
    ├── SpectralEffectRenderHandler.java — подсветка Spectral без «рентгена»
    ├── VodkaSightClientHandler.java — клиентская подсветка сущностей от водки
    ├── WhiskySoundClientHandler.java — сердцебиение от виски только для игрока
    └── SwayClientHandler.java      — шатание камеры и размытие
```

## Шейдер размытия (отключён)

> **Внимание:** размытие сейчас отключено — весь код, который его включает,
> закомментирован в `SwayClientHandler.java` и помечен `БЛЮР ОТКЛЮЧЁН`.
> Шатание камеры продолжает работать.

Файлы лежат в `src/main/resources/assets/alcomania/shaders/`:

```
post/blur.json       — цепочка из двух проходов (горизонталь + вертикаль)
program/blur.json    — описание программы
program/blur.vsh     — вершинный шейдер
program/blur.fsh     — фрагментный шейдер
```

**Важно:** в `post/blur.json` проходы называются `alcomania:blur`, а в `program/blur.json`
поля `vertex` и `fragment` тоже имеют namespace `alcomania:blur`. Если убрать namespace,
Minecraft будет искать файлы в `minecraft:shaders/program/`, не найдёт их и размытие
не загрузится. Сила размытия задаётся полем `Radius` (сейчас `2.0` — лёгкое размытие).

Комментарии в `.vsh`/`.fsh` специально написаны ASCII-символами: не все драйверы
принимают не-ASCII байты даже внутри комментариев GLSL.

## Сборка

```bash
gradlew build
```

Готовый мод: `build/libs/alcomania-1.5.jar`.

## Настройка

Общие игровые значения — файл `config/alcomania-common.toml`.
Видимость шкал HUD — клиентский файл `config/alcomania-client.toml`:

```toml
[hud]
# Показывать шкалу-бутылку в левом нижнем углу.
bottleVisible = true
# Показывать горизонтальную шкалу над хотбаром.
barVisible = true
# Цвет заполнения шкалы в формате RGB (0xRRGGBB).
barColor = 0xD08A2C
```

Обе шкалы и цвет горизонтальной шкалы также можно менять через кнопку
«Настройка» у alcomania в списке модов — она открывает экран `MyConfigScreen`.
Кнопка «Цвет шкалы» переключает готовую палитру, а точное значение RGB
можно вписать в `alcomania-client.toml` вручную.

Значения общего конфига сгруппированы по секциям:

```toml
[drunkenness]
# Как часто уменьшать опьянение (в тиках). 20 тиков = 1 секунда.
decayIntervalTicks = 20
# Сколько единиц опьянения убирать за один шаг.
decayAmount = 1
# Пороги эффектов и смерти.
swayThreshold = 25
slownessThreshold = 50
weaknessThreshold = 75
blindnessThreshold = 100
deathThreshold = 100
# Сегменты силы шатания: 25..49 / 50..74 / 75+.
swayTier2Threshold = 50
swayTier3Threshold = 75
# Иссушение вешается при значении СТРОГО выше this.
witherThreshold = 100
# 0 = Иссушение I, 1 = II, 2 = III, 3 = IV.
witherAmplifier = 3

[visual]
# Максимальная амплитуда шатания камеры в градусах.
swayYawDegrees = 14.0
swayPitchDegrees = 8.0
swayRollDegrees = 6.0
# Сила шатания по сегментам (доля от максимальной амплитуды).
swayTier1Strength = 0.35
swayTier2Strength = 0.70
swayTier3Strength = 1.00

[items.cider]
drunkenness = 30
uses = 2
cooldownSeconds = 30
effectDurationSeconds = 30
speedAmplifier = 1

[items.beer]
drunkenness = 40
uses = 2
cooldownSeconds = 0
healAmount = 16

[items.vodka]
drunkenness = 100
uses = 4
cooldownSeconds = 30
effectDurationSeconds = 30
glowRadius = 16

[items.wine]
drunkenness = 75
uses = 4
cooldownSeconds = 30
effectDurationSeconds = 30
damageReduction = 1
speedPenaltyPercent = 10

[items.honey]
drunkenness = 30
uses = 4
cooldownSeconds = 10

[items.tequila]
drunkenness = 100
uses = 4
cooldownSeconds = 0
effectDurationSeconds = 30
burnDurationSeconds = 8
burnIntervalSeconds = 2
burnDamage = 1
healingReductionPercent = 50

[items.liqueur]
drunkenness = 50
uses = 2
cooldownSeconds = 600
effectDurationSeconds = 360
miningSpeedBonusPercent = 50
doubleLootChancePercent = 20

[items.whisky]
drunkenness = 80
uses = 3
cooldownSeconds = 30
effectDurationSeconds = 10

[items.rassol]
uses = 1
cooldownSeconds = 30

```
