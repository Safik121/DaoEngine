# 2. Testovací scénáře

Tato kapitola obsahuje detailní návrh testovacích scénářů, analýzu vstupů a procesní testy pro klíčové funkce **DaoEngine**.

## 2.1 Testy vstupů (Input Testing)
Pro analýzu byla vybrána kritická metoda `SaveManager.save(SaveData data, int slot)`, která zajišťuje perzistenci herního stavu. Tato metoda je netriviální, protože kombinuje validaci parametrů s externí I/O operací a serializací.

### 2.1.1 Analýza ekvivalentních tříd (EC)
Ekvivalentní třídy rozdělují definiční obor vstupů na skupiny, které by se měly z pohledu systému chovat stejně.

| Parametr | Třída (EC) | Definice | Typ | Očekávané chování |
| :--- | :--- | :--- | :--- | :--- |
| **slot** | EC1 | {1, 2, 3, 4, 5} | Platná | Úspěšné uložení do slotu. |
| | EC2 | {..., -1, 0} | Neplatná | Vyhození `IllegalArgumentException`. |
| | EC3 | {6, 7, ...} | Neplatná | Vyhození `IllegalArgumentException`. |
| **data** | EC4 | Validní `SaveData` objekt | Platná | Správná serializace do JSON. |
| | EC5 | `null` | Neplatná | Vyhození `NullPointerException` nebo custom exception. |
| | EC6 | `SaveData` s nekonzistentním stavem | Platná (z pohledu typu) | Serializace proběhne, ale logika aplikace může selhat při načítání. |

### 2.1.2 Mezní hodnoty (Boundary Value Analysis - BVA)
Mezní hodnoty testují hranice mezi ekvivalentními třídami, kde je nejvyšší pravděpodobnost chyby (např. záměna `<` za `<=`).

| Parametr | Mez | Hodnota | Testovaný stav |
| :--- | :--- | :--- | :--- |
| **slot** | Dolní hranice | 1 | Platné minimum |
| | Těsně pod dolní | 0 | Neplatné (hranice EC2) |
| | Horní hranice | 5 | Platné maximum |
| | Těsně nad horní | 6 | Neplatné (hranice EC3) |

### 2.1.3 Pairwise Testing
Technika Pairwise Testing zajišťuje, že jsou otestovány všechny kombinace dvojic vstupních parametrů, což statisticky odhalí většinu chyb.

| ID | Data (P1) | Slot (P2) | Typ testu | Očekávaný výsledek |
| :--- | :--- | :--- | :--- | :--- |
| **T01** | Validní | 1 (Min) | Pozitivní | Úspěšné uložení |
| **T02** | Validní | 5 (Max) | Pozitivní | Úspěšné uložení |
| **T03** | Validní | 0 (Invalid) | Negativní | `IllegalArgumentException` |
| **T04** | `null` | 1 | Negativní | `NullPointerException` |
| **T05** | `null` | 6 (Invalid) | Negativní | Priorita validace slotu |
| **T06** | Nekonzistentní | 3 (Mid) | Pozitivní | Serializace (systém je robustní) |
| **T07** | Validní | 100 | Negativní | `IllegalArgumentException` |

---

## 2.2 Testy průchodů (Process Testing)
V této sekci analyzujeme dva klíčové procesy v aplikaci pomocí procesních diagramů a techniky TDL 2 (Test Design Language level 2 - pokrytí všech hran).

### 2.2.1 Proces A: Dokončení questu (Quest Completion)
Tento proces popisuje interakci mezi smrtí nepřítele a progresivním systémem.

```mermaid
graph TD
    A[Začátek: Nepřítel zabit] --> B[CombatManager detekuje smrt]
    B --> C[QuestManager.notifyEvent 'KILL', enemyId]
    C --> D{Má hráč aktivní quest na tento cíl?}
    D -- Ne --> E[Konec: Žádná změna]
    D -- Ano --> F[Quest.addProgress 1]
    F --> G{Je quest dokončen?}
    G -- Ne --> H[Zobrazení notifikace o postupu]
    G -- Ano --> I[QuestManager.completeQuest]
    I --> J[Přidání odměn do inventáře]
    I --> K[Nastavení WorldState flagu]
    K --> L[Zobrazení notifikace o dokončení]
    L --> M[Konec]
```

**Testovací průchody (TDL 2):**
1.  **P1 (Negative path)**: Zabití nepřítele, který není v žádném questu. (Hrany: A-B, B-C, C-D, D-E)
2.  **P2 (Partial progress)**: Zabití nepřítele, inkrementace counteru, ale quest pokračuje. (Hrany: D-F, F-G, G-H, H-M)
3.  **P3 (Complete path)**: Poslední zabití vedoucí k dokončení a předání odměn. (Hrany: G-I, I-J, J-K, K-L, L-M)

### 2.2.2 Proces B: Soubojový cyklus (Combat Cycle)
Proces popisující aplikaci poškození a stavových efektů při zásahu.

```mermaid
graph TD
    S[Start: Projektil zasáhl cíl] --> Hit[CombatManager.applyDamage]
    Hit --> Calc[Výpočet Damage dle Atributů]
    Calc --> SE{Má projektil efekt?}
    SE -- Ano --> AddE[StatusEffectManager.addEffect]
    SE -- Ne --> RedHP[Snížení HP LivingEntity]
    AddE --> RedHP
    RedHP --> Dead{HP <= 0?}
    Dead -- Ano --> Notify[Zavolání OnDeath Eventu]
    Dead -- Ne --> End[Konec cyklu]
    Notify --> End
```

**Testovací průchody (TDL 2):**
1.  **P4 (Simple Hit)**: Zásah bez efektu, cíl přežije. (Hrany: S-Hit, Hit-Calc, Calc-SE, SE-RedHP, RedHP-Dead, Dead-End)
2.  **P5 (Effect Hit)**: Zásah s efektem (např. oheň), cíl přežije. (Hrany: SE-AddE, AddE-RedHP)
3.  **P6 (Lethal Hit)**: Zásah, který sníží HP na 0 a vyvolá smrt. (Hrany: Dead-Notify, Notify-End)

---

## 2.3 Detailní testovací scénáře (Manual Test Cases)
Následující scénáře jsou určeny pro manuální verifikaci komplexních funkcí.

### Scénář DS-01: Perzistence herního stavu (Save/Load)
| Atribut | Hodnota |
| :--- | :--- |
| **ID** | DS-01 |
| **Priorita** | Kritická |
| **Modul** | SaveManager / PlayState |
| **Předpoklad** | Aplikace v PlayState, dostupný volný slot 1. |

**Kroky:**
1. Hráč zabije 2 nepřátele a získá 100 XP.
2. Hráč sebere předmět "Void Shard" (ověření inventáře).
3. Hráč otevře menu a uloží hru do slotu 1.
4. Hráč změní pozici (podejde o 200px doprava).
5. Hráč načte hru ze slotu 1.

**Očekávaný výsledek:**
- Hráč má po načtení přesně 100 XP.
- "Void Shard" je v inventáři.
- Hráč se vrátil na pozici, kde hru ukládal (ne na novou pozici z kroku 4).
- Nepřátelé jsou stále mrtví (stav světa perzistoval).

### Scénář DS-02: Interakce Questu a Inventáře
| Atribut | Hodnota |
| :--- | :--- |
| **ID** | DS-02 |
| **Priorita** | Vysoká |
| **Modul** | QuestManager / Inventory |
| **Předpoklad** | Hráč má aktivní quest "Sběr bylin" (potřeba 3 ks). |

**Kroky:**
1. Hráč sebere 1. bylinu.
2. Hráč sebere 2. bylinu.
3. Hráč zahodí 1 bylinu z inventáře.
4. Hráč sebere 3. bylinu.

**Očekávaný výsledek:**
- Po kroku 2 je progres questu 2/3.
- Po kroku 3 musí progres klesnout na 1/3 (pokud je quest vázán na stav inventáře) NEBO zůstat 2/3 (pokud je vázán na akci sběru). *Pozn: DaoEngine používá akci sběru.*
- Po kroku 4 je progres 3/3 a quest je označen jako splněný.
