# 仙 DaoEngine: Path to Immortality - Herní Manuál

Vítejte v herním manuálu pro **DaoEngine**. Tento dokument vás provede instalací hry, hloubkovými mechanikami a cestou vaší postavy od prostého smrtelníka až k nebeskému vládci.

---

## 1. Úvod a Epický Příběh: Střepy Věčnosti

Svět DaoEngine není obyčejným světem. Je to **"Vězeňská sféra"**, vytvořená prastarým artefaktem, aby zachytila esence nejmocnějších bytostí, které kdy kráčely po cestě Dao. Nebe je zde roztříštěné a čas plyne v nekonečných cyklech.

### Legenda o Panteonu DaoEngine
Tento svět je hřbitovem bohů. Každá stele, kterou najdete, nese otisk duše jedné z legend, které formovaly historii multivesmíru. Tyto fragmenty jsou rozděleny do několika proudů síly:

- **Ctihodní (Venerables)**:
    - **Fang Yuan (Great Love)**: Jehož bezohledná touha po věčném životě a pohrdání emocemi jsou legendární. Věří, že neexistují věční nepřátelé, jen věčné benefity.
    - **Spectral Soul**: Krvelačný stín, jehož cesta byla dlážděna dušemi miliard smrtelníků i kultivátorů. Jeho šílenost a touha po krvi uhasila i slunce.
    - **Paradise Earth & Red Lotus**: První přináší klid a laskavost, druhý věčné výčitky a snahu změnit minulost skrze řeku času.

- **Prastaří (The Ancients)**:
    - **Tu Si & Xuan Luo**: Poslední z linie Prastarých bohů a Dao, jejichž těla nesla váhu hvězd a jejichž krev je esencí samotné existence.
    - **Ta Ji & Chi Hu**: Zástupci Prastarých ďáblů a klanu Obřích démonů, kteří věří pouze v hrubou sílu a drcení nebes pěstmi.

- **Vzpurní a Vyvolení (The Defiers)**:
    - **Wang Lin**: Bůh smrti, který prošel cestou pomsty a samoty, aby dokázal, že jeho život patří jemu, ne nebesům.
    - **Situ Nan**: Šílený génius a vězeň magické perly, který vás bude urážet, dokud se nenaučíte skutečnou kultivaci.
    - **All Seer**: Šachista osudu, který vidí každé padající listí jako součást své hry.

- **Tragické duše**:
    - **Bo Qing**: Génius meče, jehož meč dokázal rozpůlit 5 regionů a 2 nebesa. Byl považován za nejsilnější hned pod Ctihodnými (Venerables).
    - **Bai Ning Bing**: Mladý genius posedlý soubojem, který zvolil velkolepý, byť krátký život v mrazu, místo šedi věčnosti.
    - **Zhou Yi**: Věčně čekající v dešti na tu, kterou ztratil, ztělesnění neochvějné lásky.
    - **Fang Zheng**: Stín svého bratra, zmítaný spalující žárlivostí a touhou dokázat svou cenu.

- **Aspekty chaosu**:
    - **Greed**: Bezedný hlad, který chce pohltit nebesa i zemi a nikdy nebude nasycen.
    - **Difuz (Void Glutton)**: Požírač světů, jehož hlad je černou dírou, která chce pohltit slunce, hvězdy i samotný čas.

### Vaše role: Ten, kdo popírá osud
Vy začínáte jako nikdo v **Mortal Valley**. Vaším průvodcem je **Senior Han**, který kdysi sloužil v nebeských síních, a **Elder Mo**, kovář snažící se ukovat zbraň schopnou protnout osud. Musíte projít skrze **Nine Bridges** (Devět mostů) pod dohledem **Seniora Wanga** a postavit se zkouškám **Sword Saint Shena**, abyste dokázali, že vaše vůle je dostatečně silná.

Vaším skutečným cílem není konečný boss, ale **věčná cesta**. Nasbírejte dostatek Qi a Realm Tokenů, abyste mohli plynule přecházet do stále mocnějších říší (**Realms**). Každý nový svět je nebezpečnější, nepřátelé silnější a tribulace ničivější. Vaším úkolem je "pošlapávat nebesa" (Trample the Heavens) v nekonečném cyklu kultivace a stát se legendou, jejíž jméno bude jednou vytesáno do steles pro budoucí generace kultivujících.

![Postava hráče stojící před obří Spirit stele v mlze](doc_screens/spirit_stele_screen.png)

---

## 2. Instalace a spuštění

### Požadavky
- **Java JDK 21** nebo novější (běží na JavaFX).
- **Maven** (pro sestavení).
- **RAM**: Minimálně 2 GB volné paměti.

### Spuštění hry
1. Stáhněte/naklonujte repozitář.
2. V kořenovém adresáři otevřete terminál a spusťte: `mvn javafx:run`.
3. Hra se automaticky sestaví a spustí hlavní menu.

---

## 3. Hlavní menu
Po spuštění se zobrazí hlavní menu s interaktivním pozadím:
- **New Game**: Start nové cesty (přepíše dočasná data).
- **Load Game**: Výběr z 5 pozic uložených na disku.
- **Lexicon**: Encyklopedie světa (předměty, nepřátelé, recepty).
- **Exit**: Ukončení aplikace.

![Hlavní menu hry](doc_screens/main_menu_screen.png)

---

## 4. Ovládání hry
Hra využívá plynulé ovládání kombinující klávesnici a kurzor myši:

| Akce | Klávesa | Popis |
| :--- | :--- | :--- |
| **Pohyb** | `W`, `A`, `S`, `D` | Pohyb ve čtyřech směrech. |
| **Základní útok** | `LMB` (Levé t.) | Útok zbraní směrem ke kurzoru. |
| **Aktivní technika** | `RMB` (Pravé t.) | Aktivuje speciální techniku (stojí Qi). |
| **Meditace** | `Mezerník` | Čerpá Qi z okolí. Během meditace nelze chodit. |
| **Interakce** | `E` | NPC, sbírání věcí, aktivace bran a stele. |
| **Inventář** | `I` | Správa předmětů a crafting. |
| **Deník úkolů** | `Q` | Seznam aktivních a splněných úkolů. |
| **Mapa světa** | `M` | Zobrazení celé mapy ve velkém. |
| **Kultivace** | `C` | Menu s detaily o vašem postupu a ranku. |
| **Průlom** | `B` | Rychlá klávesa pro pokus o Breakthrough. |
| **Použít hotbar** | `F` | Použije předmět ve vybraném slotu hotbaru. |
| **Sloty hotbaru** | `1` až `5` | Přepínání mezi předměty v hotbaru. |
| **Pauza / Menu** | `Esc` | Menu pauzy a ukládání. |

> **Poznámka k dialogům**: Během rozhovoru s NPC můžete pokračovat klávesou `E` nebo kliknutím. Pokud máte na výběr více možností, lze je vybrat buď kliknutím přímo na text, nebo číselnými klávesami `1`, `2`, `3` atd.

---

## 5. Herní mechaniky

### 5.1 Kultivace a Qi
Vaše postava má dva hlavní ukazatele: **HP (Zdraví)** a **Qi (Energie)**.
- **Získávání Qi**: Meditací (držení `Mezerníku`), zabíjením nepřátel nebo konzumací pilulek.
- **Průlom (Breakthrough)**: Po dosažení maximální kapacity Qi stiskněte `B`. Průlom trvale zvýší vaše statistiky (**HP**, **Síla**, **Obrana**) a okamžitě vás **plně vyléčí**.

![Menu kultivace a průlomu (Breakthrough)](doc_screens/cultivation_screen.png)

### 5.2 Svět a Biomy
DaoEngine generuje světy dynamicky. Každý biom má své unikátní vlastnosti:
- **Forest (Lesní říše)**: Harmonická a uklidňující krajina plná zeleně. Je to nejčastější prostředí, kde začíná vaše cesta za nesmrtelností.
- **Ice (Ledová tundra)**: Pustina pokrytá věčným ledem a sněhem. Vizuálně chladné prostředí, které podtrhuje osamělost cesty kultivujícího.
- **Fire (Vulkanické hlubiny)**: Temný svět prozářený žárem tekuté lávy. Toto prostředí působí nejvíce nebezpečně a tvoří dramatickou kulisu pro vaše souboje.

![Srovnání vizuálů různých biomů](doc_screens/bioms_example_screen.png)

### 5.3 Nebeské Tribulace
V určitých intervalech (sledovat časovač v HUDu) nebo při průlomu na vás nebesa sešlou blesky.
- **Varování**: Před dopadem blesku se na zemi objeví nenápadná stopa. Jakmile se objeví, musíte se co nejrychleji vzdálit.
- **Taktika**: Blesky zraňují i nepřátele, můžete je do zón nalákat!

![Varovná zóna na zemi a dopadající blesk](doc_screens/lightning_strike_screen.png)

---

## 6. Bojové techniky (Skills)
Můžete mít aktivní pouze jednu techniku. Nové se učíte ze **Skill Booků**.
- **Fiery Palm**: Vějíř ohnivých koulí.
- **Void Sword Slash**: Průrazný meč s vysokým poškozením.
- **Thunder Clap**: Bleskový výboj s extrémní rychlostí.
- **Soul Devourer**: Obří koule energie ničící vše v cestě.

![Hráč útočící na Spirit Bat](doc_screens/attack_bat_screen.png)

---

## 7. Cíle levelu a Postup
Každý level (Realm) má omezený čas a lze jej dokončit dvěma způsoby:
1. **Cesta moci (Survival)**: Přežijte do vypršení časovače a porazte všechny vlny **Nebeské Tribulace**.
2. **Cesta artefaktu (Realm Token)**: Najděte **Realm Token** a přineste jej k bráně **Gate of Realms**.

![Modře zářící portál Gate of Realms](doc_screens/realm_gate_screen.png)

---

## 8. Uživatelské rozhraní (HUD)
1. **Levý horní roh**: Pruhy HP a Qi. Pod nimi **Quest Log a Tribulation Timer**.
2. **Pravý horní roh**: Minimapa.
3. **Střed dole**: Hotbar se sloty 1-5.
4. **Pravý dolní roh vedle hotbaru**: Ikona aktivní techniky a cooldown.

![Celkový pohled na HUD s popisky jednotlivých prvků](doc_screens/full_hud_screen.png)

### 8.1 Legenda k mapě
Minimapa (vpravo nahoře) a velká mapa (`M`) využívají barevné kódování pro rychlou orientaci v terénu:

**Barvy terénu (Dlaždice):**
- **Zelená**: Tráva a volný terén vhodný k pohybu.
- **Modrá**: Voda (řeky a jezera).
- **Tyrkysová**: Spirit Veins – ideální místa pro meditaci se zvýšeným ziskem Qi.
- **Šedá**: Zdi, skály a neprostupné překážky.
- **Hnědá**: Mosty umožňující přechod přes vodu.

**Ikony entit (Body na mapě):**
- **Bílý bod**: Vaše aktuální pozice.
- **Červený bod**: Nepřátelé (monstra a strážci).
- **Zlatý bod**: Předměty ležící na zemi připravené k sebrání.
- **Tyrkysový bod**: NPC postavy a Spirit Steles.
- **Velký tyrkysový bod**: Gate of Realms (Brána k postupu do dalšího světa).

![Pohled na mapu světa s legendou](doc_screens/map_screen.png)

---

## 9. NPC, Stele a Dialogy
- **NPC**: Můžete s nimi mluvit (`E`), získávat úkoly nebo s nimi obchodovat.
- **Spirit steles**: Monumenty poskytující **trvalé bonusy** k statistikám.
- **Dialogy**: Výběr možností provádíte kliknutím nebo klávesami `1`-`9`.

![Okno rozhovoru s NPC a výběr z více možností](doc_screens/npc_int_screen.png)

---

## 10. Inventář a Crafting
V inventáři (`I`) můžete vyrábět předměty:
- **Recepty**: Seznam v pravé části inventáře.
- **Materiály**: Iron Ore, Spirit Herb, Magic Essence atd.
- **Manipulace (Drag & Drop)**: Předměty můžete v inventáři libovolně přesouvat držením levého tlačítka myši. Tímto způsobem je lze přemisťovat i do slotů hotbaru (1-5).
- **Vyhazování**: Předmět vyhodíte na zem tak, že jej přetáhnete mimo okno inventáře na hrací plochu nebo na ikonu koše.
- **Trash**: Předměty lze trvale zničit přetažením na ikonu koše v pravém dolním rohu.

![Otevřený inventář s ukázkou craftingu](doc_screens/inv_crafting_screen.png)

---

## 11. Kniha znalostí (Lexicon)
Vaše osobní encyklopedie dostupná z menu:
- **Bestiář**: Statistiky a chování všech objevených monster.
- **Atlas předmětů**: Detaily o každém nalezeném itemu.
- **Recepty**: Přehled všech vašich řemeslných znalostí.
- **Kultivace**: Přehled všech kultivačních úrovní.

![Karta v Lexiconu s detailním popisem nepřítele](doc_screens/lexicon_screen.png)

---

## 12. Ukládání a Konec hry
- **Ukládání**: V menu pauzy (`Esc`) vyberte slot 1-5. Ukládání je asynchronní.
- **Smrt**: Po smrti uvidíte obrazovku "DEFEAT". Můžete načíst poslední save.
- **Doporučení**: Hru ukládejte často! Bez uložení přicházíte při smrti o veškerý postup v levelu.

![Menu ukládání a načítání hry](doc_screens/save_game_screen.png)
![Načítání uložené pozice](doc_screens/load_game_screen.png)
![Obrazovka smrti (Death Screen)](doc_screens/death_screen.png)

---

## 13. FAQ
**Hra se mi seká, co mám dělat?**
- Zkuste v `game_config.json` snížit cílové FPS nebo vypnout debug mód.

**Kde najdu savy?**
- Savy jsou uloženy ve složce `/saves` ve formátu `.json`.

---

© 2026 DaoEngine <3
