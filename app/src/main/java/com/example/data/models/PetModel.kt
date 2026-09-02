package com.example.data.models

enum class PetType(
    val rawDisplayName: String,
    val description: String,
    val defaultQuote: String,
    val iconSymbol: String,
    val primarySkill: OsrsSkill,
    val quotes: List<String> = listOf(defaultQuote),
    val stage: Int = 1, // 1 = Base/1st stage (Unlockable/Adoptable), 2 = 2nd stage (Evolution only), 3 = 3rd stage (Evolution only), 0 = OSRS/Standalone pet
    val evolutionLevelReq: Int = 0,
    val evolvesToName: String? = null,
    val evolvesFromName: String? = null,
    val unlockCostGp: Long = 5000L
) {
    // --- SHAMAN SPIRIT ANIMAL COMPANIONS ---
    TANGLEROOT(
        "Spirit Treant", "An ancient elder treant spirit who loves nature and farming!", "Grow tall and flourish, adventurer!", "🌳", OsrsSkill.FARMING,
        listOf("Grow tall and flourish, adventurer!", "My roots feel grounded with nature energy!", "Spirit agriculture is sacred! Have you checked your herb patches?", "Nature spirits bless our harvest today!"),
        stage = 0
    ),
    BEAVER(
        "Spirit Beaver", "Loves harvesting timber and building river totems!", "Sacred Timber is life! Timber!", "🦫", OsrsSkill.WOODCUTTING,
        listOf("Sacred Timber is life! Timber!", "Chop chop! Ancient logs yield maximum spirit energy!", "Totem building is an art form! Let's chop more timber!"),
        stage = 0
    ),
    HERON(
        "Spirit Crane", "Patient fisher spirit of the sacred river!", "Squawk! A fine catch today!", "🦩", OsrsSkill.FISHING,
        listOf("Squawk! A fine catch today!", "Fishing at the sacred falls is so serene!", "Patience pays off! Spear at the ready!"),
        stage = 0
    ),
    ROCK_GOLEM(
        "Stone Spirit Golem", "Solid as granite and loves harvesting crystals!", "Smash the rocks! Harvest spirit crystals!", "🗿", OsrsSkill.SMITHING,
        listOf("Smash the rocks! Harvest spirit crystals!", "Solid as granite! The sacred cavern is our home!", "Clang clang! Shaman robes boost our spirit energy!"),
        stage = 0
    ),
    GIANT_SQUIRREL(
        "Wind Spirit Squirrel", "Nippy wind-walking acrobat!", "Stash those acorns! Keep wind walking!", "🐿️", OsrsSkill.AGILITY,
        listOf("Stash those acorns! Keep wind walking!", "Treetop running is my favorite agility test!", "Marks of Wind collected!"),
        stage = 0
    ),
    ROCKY(
        "Shadow Spirit Raccoon", "Master of stealth and shadow stalking!", "Shh... gather the spirit coins!", "🦝", OsrsSkill.THIEVING,
        listOf("Shh... gather the spirit coins!", "Just snagged 500 coins with shadow dexterity!", "Steal from the shadow beasts, give to our Shaman fund!"),
        stage = 0
    ),
    BABY_CHINCHOMPA(
        "Fire Spirit Chinchompa", "Furry spirit tracker creature!", "Squeak squeak! Watch the spirit traps!", "🐭", OsrsSkill.HUNTER,
        listOf("Squeak squeak! Watch the spirit traps!", "Tracking beasts yields maximum spirit XP!", "Careful! Don't drop the sacred traps!"),
        stage = 0
    ),
    RIFT_GUARDIAN(
        "Runic Spirit Guardian", "Elemental guardian of the totem altars!", "The altars hum with magical spirit power!", "🔮", OsrsSkill.RUNECRAFT,
        listOf("The altars hum with magical totem power!", "Nature, Cosmic, Astral, Soul... all spirit runes carved!", "Our essence turns into pure totem XP!"),
        stage = 0
    ),
    GUTHIXIAN_WISP(
        "Nature Spirit Wisp", "Divine memory wisp pet from ancestral energy pools!", "Shimmering divine spirit energy flows through us!", "✨", OsrsSkill.DIVINATION,
        listOf("Shimmering divine spirit energy flows through us!", "Harvesting memories from the spirit rift!", "Spirit communication energy siphoned cleanly!", "Ancestral memory balance restored!"),
        stage = 0
    ),
    SAILING_PARROT(
        "Spirit Macaw", "Fearless seafaring macaw companion ready to navigate uncharted oceans!", "Ahoy matey! Set sail for spirit island treasures!", "🦜", OsrsSkill.SAILING,
        listOf("Ahoy matey! Set sail for spirit island treasures!", "Squawk! Full speed ahead through the spirit swell!", "Chart the uncharted waters, Captain!", "Anchors aweigh! Maximum Spirit Voyaging gainz!"),
        stage = 0
    ),
    PHOENIX(
        "Sacred Firebird", "Fiery avian of the sacred hearth flames!", "Burn bright like the sacred hearth!", "🔥", OsrsSkill.FIREMAKING,
        listOf("Burn bright like the sacred hearth!", "Sacred flames warm our spirits!", "From the ashes, we rise with master flame skill!"),
        stage = 0
    ),
    ABYSSAL_ORPHAN(
        "Void Spirit", "Creepy void spirit guardian!", "Spirit master at your service!", "👾", OsrsSkill.SLAYER,
        listOf("Creepy void spirit at your service!", "Purification task complete! What corrupt beast is next?", "Spirit blade drop confirmed!"),
        stage = 0
    ),
    SMOLCANO(
        "Magma Spirit", "Miniature volcanic totem forging spirit!", "Forged in the deep magma hearth!", "🌋", OsrsSkill.SMITHING,
        listOf("Forged in the deep magma hearth!", "Forge heat rising! Totem bars ready!"),
        stage = 0
    ),
    PET_KRAKEN(
        "Ocean Kraken Spirit", "Tentacled spirit master of the deep seas!", "Glub glub! Ready for spear fishing!", "🐙", OsrsSkill.FISHING,
        listOf("Glub glub! Master of the deep ocean spirits!", "Ocean spirit blessing acquired!"),
        stage = 0
    ),
    TZREK_JAD(
        "Flame Beast Spirit", "Miniature flame beast! Watch your ancestral chants!", "Rroaaar! Switch your spirit wards!", "👹", OsrsSkill.MAGIC,
        listOf("Rroaaar! Switch your spirit wards! Spirit or Physical?!", "You conquered the sacred trials! Here's a Flame Mantle flex!"),
        stage = 0
    ),
    BABY_MOLE(
        "Earth Burrower Spirit", "Underground digging burrower spirit!", "Digging tunnels for your shrine sanctuary!", "🦡", OsrsSkill.CONSTRUCTION,
        listOf("Underground digging burrower spirit!", "Sacred earth cavern claw collector!"),
        stage = 0
    ),
    BABY_BLACK_DRAGON(
        "Spirit Drake", "A tiny spirit dragon with fiery ambition!", "Rawr! Feed me some roasted spirit fish!", "🐉", OsrsSkill.ATTACK,
        listOf("Rawr! Feed me some roasted spirit fish!", "My spirit dragonfire breath is getting stronger!"),
        stage = 0
    ),
    PRINCE_BLACK_DRAGON(
        "Grand Dragon Spirit", "Royal three-headed spirit dragon beast!", "Bow before the King of Spirit Dragons!", "🐉", OsrsSkill.ATTACK,
        listOf("Bow before the King of Spirit Dragons!", "Three heads are better than one for battle!"),
        stage = 0
    ),
    VORKI(
        "Frost Dragon Spirit", "Skeletal frost spirit dragon!", "Ice and undead dragon breath!", "❄️", OsrsSkill.RANGED,
        listOf("Skeletal frost dragon breath activated!", "Glacier frost and dragonfire power!"),
        stage = 0
    ),

    // --- ANIMAL SPIRITS & ELEMENTAL GUARDIANS ---
    BULBASAUR(
        "Spirit Seed Turtle", "Grass spirit seed guardian!", "Seed seed!", "🌱", OsrsSkill.FARMING,
        listOf("Seed seed!", "Vine Whip in the herb garden!"),
        stage = 1, evolutionLevelReq = 16, evolvesToName = "IVYSAUR"
    ),
    IVYSAUR(
        "Spirit Vine Turtle", "Evolved Vine Spirit!", "Vine vine!", "🌿", OsrsSkill.FARMING,
        listOf("Vine vine!", "Bud swelling in the sunlight!"),
        stage = 2, evolutionLevelReq = 32, evolvesToName = "VENUSAUR", evolvesFromName = "BULBASAUR"
    ),
    VENUSAUR(
        "Spirit Blossom Sovereign", "Final form Flower Sovereign!", "Blossom blossom!", "🌺", OsrsSkill.FARMING,
        listOf("Blossom blossom!", "Solar Beam at full power!"),
        stage = 3, evolvesFromName = "IVYSAUR"
    ),

    CHARMANDER(
        "Spirit Flame Lizard", "Fire spirit lizard companion!", "Flame flame!", "🔥", OsrsSkill.FIREMAKING,
        listOf("Flame flame!", "Tail flame burning bright!"),
        stage = 1, evolutionLevelReq = 16, evolvesToName = "CHARMELEON"
    ),
    CHARMELEON(
        "Spirit Flare Dragonet", "Evolved Flare Dragonet!", "Flare flare!", "🌋", OsrsSkill.FIREMAKING,
        listOf("Fierce flame slash!"),
        stage = 2, evolutionLevelReq = 36, evolvesToName = "CHARIZARD", evolvesFromName = "CHARMANDER"
    ),
    CHARIZARD(
        "Spirit Inferno Wyvern", "Final form Inferno Wyvern!", "Rroaaar! Flamethrower!", "🐲", OsrsSkill.FIREMAKING,
        listOf("Rroaaar! Flamethrower incinerates timber!"),
        stage = 3, evolvesFromName = "CHARMELEON"
    ),

    SQUIRTLE(
        "Spirit Tide Turtle", "Water spirit tide turtle!", "Tide squad!", "🐢", OsrsSkill.FISHING,
        listOf("Tide squad assemble!", "Water Jet blast!"),
        stage = 1, evolutionLevelReq = 16, evolvesToName = "WARTORTLE"
    ),
    WARTORTLE(
        "Spirit Torrent Turtle", "Evolved Torrent Spirit!", "Torrent torrent!", "🌊", OsrsSkill.FISHING,
        listOf("Feathery ears and tail swishing in ocean currents!"),
        stage = 2, evolutionLevelReq = 36, evolvesToName = "BLASTOISE", evolvesFromName = "SQUIRTLE"
    ),
    BLASTOISE(
        "Spirit Ocean Sovereign", "Final form Ocean Hydro Sovereign!", "Hydro Blast!", "🌊", OsrsSkill.FISHING,
        listOf("Hydro Cannons locked and loaded!"),
        stage = 3, evolvesFromName = "WARTORTLE"
    ),

    PIKACHU(
        "Spirit Thunder Mouse", "Thunder spirit mouse companion!", "Pika pika! Lightning burst!", "⚡", OsrsSkill.MAGIC,
        listOf("Pika pika!", "Thunderbolt 100,000 Volts!"),
        stage = 1
    ),
    EEVEE(
        "Spirit Mystery Fox", "Mystery spirit fox with boundless forms!", "Fox fox!", "🦊", OsrsSkill.ADVENTURING,
        listOf("Fox fox!", "Adaptability speed boost!"),
        stage = 1
    ),
    SNORLAX(
        "Spirit Slumber Bear", "Sleeping spirit bear who loves campfire feasts!", "Snooooze... zzz...", "🐻", OsrsSkill.COOKING,
        listOf("Snooooze... zzz...", "Rest restores Life Energy!"),
        stage = 2, evolvesFromName = "MUNCHLAX"
    ),
    GENGAR(
        "Spirit Shadow Specter", "Ghostly shadow pranker!", "Hehehe! Shadow magic time!", "👻", OsrsSkill.THIEVING,
        listOf("Hehehe! Shadow prank!", "Shadow Ball launched!"),
        stage = 3
    ),
    DRAGONITE(
        "Spirit Sky Seraph", "Sky seraph dragon capable of swift flight!", "Sky seraph ready for battle!", "🐲", OsrsSkill.AGILITY,
        listOf("Sky seraph airborne!", "Outrage spirit power!"),
        stage = 3
    ),
    MEWTWO(
        "Spirit Astral Monarch", "Astral monarch spirit!", "My psychic aura is unmatched!", "🔮", OsrsSkill.MAGIC,
        listOf("My psychic aura is unmatched!", "Psystrike ready!"),
        stage = 1
    ),

    // --- GEN 2 POKÉMON ---
    CHIKORITA(
        "Chikorita (Gen 2)", "Johto Leaf starter Pokémon!", "Chiko chiko!", "🍃", OsrsSkill.HERBLORE,
        listOf("Chiko chiko!", "Razor Leaf slash!"),
        stage = 1, evolutionLevelReq = 16, evolvesToName = "BAYLEEF"
    ),
    BAYLEEF(
        "Bayleef (Gen 2)", "Evolved Leaf Pokémon!", "Bayleef aroma!", "🌿", OsrsSkill.HERBLORE,
        listOf("Spicy aroma wafts from neck buds!"),
        stage = 2, evolutionLevelReq = 32, evolvesToName = "MEGANIUM", evolvesFromName = "CHIKORITA"
    ),
    MEGANIUM(
        "Meganium (Gen 2)", "Final form Herb Pokémon!", "Meganium breath!", "🌺", OsrsSkill.HERBLORE,
        listOf("Soothing aroma restores companion vitality!"),
        stage = 3, evolvesFromName = "BAYLEEF"
    ),

    CYNDAQUIL(
        "Cyndaquil (Gen 2)", "Johto Fire Mouse starter Pokémon!", "Cyn cyn!", "🐭", OsrsSkill.FIREMAKING,
        listOf("Cyn cyn!", "Back flames ignited!"),
        stage = 1, evolutionLevelReq = 14, evolvesToName = "QUILAVA"
    ),
    QUILAVA(
        "Quilava (Gen 2)", "Evolved Volcano Pokémon!", "Quilava flame!", "🔥", OsrsSkill.FIREMAKING,
        listOf("Flame Wheel spinning!"),
        stage = 2, evolutionLevelReq = 36, evolvesToName = "TYPHLOSION", evolvesFromName = "CYNDAQUIL"
    ),
    TYPHLOSION(
        "Typhlosion (Gen 2)", "Final form Volcano Pokémon!", "Eruption heat wave!", "🌋", OsrsSkill.FIREMAKING,
        listOf("Superheated fur creates explosive blasts!"),
        stage = 3, evolvesFromName = "QUILAVA"
    ),

    TOTODILE(
        "Totodile (Gen 2)", "Johto Big Jaw Water starter Pokémon!", "Toto toto!", "🐊", OsrsSkill.FISHING,
        listOf("Toto toto!", "Bite attack!"),
        stage = 1, evolutionLevelReq = 18, evolvesToName = "CROCONAW"
    ),
    CROCONAW(
        "Croconaw (Gen 2)", "Evolved Big Jaw Pokémon!", "Croconaw crunch!", "🐊", OsrsSkill.FISHING,
        listOf("48 sharp fangs snap firmly!"),
        stage = 2, evolutionLevelReq = 30, evolvesToName = "FERALIGATR", evolvesFromName = "TOTODILE"
    ),
    FERALIGATR(
        "Feraligatr (Gen 2)", "Final form Jaw Pokémon!", "Feraligatr bite!", "🦖", OsrsSkill.FISHING,
        listOf("Massive jaws overpower deep sea catches!"),
        stage = 3, evolvesFromName = "CROCONAW"
    ),

    TOGEPI(
        "Togepi (Gen 2)", "Spike Ball Pokémon bringing good fortune!", "Puri puri!", "🥚", OsrsSkill.HITPOINTS,
        listOf("Puri puri!", "Metronome wagging finger!"),
        stage = 1, evolutionLevelReq = 18, evolvesToName = "TOGETIC"
    ),
    TOGETIC(
        "Togetic (Gen 2)", "Happiness Pokémon floating on air!", "Toge toge!", "🕊️", OsrsSkill.HITPOINTS,
        listOf("Spreads joy and good fortune!"),
        stage = 2, evolutionLevelReq = 35, evolvesToName = "TOGEKISS", evolvesFromName = "TOGEPI"
    ),
    TOGEKISS(
        "Togekiss (Gen 2)", "Jubilee Pokémon bringing blessing and peace!", "Jubilee blessing!", "✨", OsrsSkill.HITPOINTS,
        listOf("Graceful wings shower sweet blessings!"),
        stage = 3, evolvesFromName = "TOGETIC"
    ),

    // --- GEN 3 POKÉMON ---
    TREECKO(
        "Treecko (Gen 3)", "Hoenn Wood Gecko starter Pokémon!", "Treecko leaf blade slash!", "🦎", OsrsSkill.WOODCUTTING,
        listOf("Treecko leaf blade slash!", "Wood gecko agility!"),
        stage = 1, evolutionLevelReq = 16, evolvesToName = "GROVYLE"
    ),
    GROVYLE(
        "Grovyle (Gen 3)", "Evolved Wood Gecko!", "Grovyle leaf blade!", "🍃", OsrsSkill.WOODCUTTING,
        listOf("Leaping between giant redwoods with swift blade cuts!"),
        stage = 2, evolutionLevelReq = 36, evolvesToName = "SCEPTILE", evolvesFromName = "TREECKO"
    ),
    SCEPTILE(
        "Sceptile (Gen 3)", "Final form Forest Pokémon!", "Sceptile dragon pulse!", "🌲", OsrsSkill.WOODCUTTING,
        listOf("Leaves on arms slice down magic trees in one strike!"),
        stage = 3, evolvesFromName = "GROVYLE"
    ),

    TORCHIC(
        "Torchic (Gen 3)", "Hoenn Chick Fire starter Pokémon!", "Torchic burning bright!", "🐥", OsrsSkill.FIREMAKING,
        listOf("Torchic burning bright!", "Fire spin vortex!"),
        stage = 1, evolutionLevelReq = 16, evolvesToName = "COMBUSKEN"
    ),
    COMBUSKEN(
        "Combusken (Gen 3)", "Evolved Young Fowl Pokémon!", "Combusken double kick!", "🐓", OsrsSkill.FIREMAKING,
        listOf("Kick strikes deliver 10 kicks per second!"),
        stage = 2, evolutionLevelReq = 36, evolvesToName = "BLAZIKEN", evolvesFromName = "TORCHIC"
    ),
    BLAZIKEN(
        "Blaziken (Gen 3)", "Final form Blaze Pokémon!", "Blaziken sky uppercut!", "🔥", OsrsSkill.FIREMAKING,
        listOf("Flames spurt from wrists as kicks clear thirty story buildings!"),
        stage = 3, evolvesFromName = "COMBUSKEN"
    ),

    MUDKIP(
        "Mudkip (Gen 3)", "Hoenn Mud Fish Water starter Pokémon!", "So i herd u liek mudkips!", "🐸", OsrsSkill.FISHING,
        listOf("So i herd u liek mudkips!", "Mud slap!"),
        stage = 1, evolutionLevelReq = 16, evolvesToName = "MARSHTOMP"
    ),
    MARSHTOMP(
        "Marshtomp (Gen 3)", "Evolved Mud Fish!", "Marshtomp mud shot!", "🐊", OsrsSkill.FISHING,
        listOf("Sturdy hind legs power through thick sticky mud!"),
        stage = 2, evolutionLevelReq = 36, evolvesToName = "SWAMPERT", evolvesFromName = "MUDKIP"
    ),
    SWAMPERT(
        "Swampert (Gen 3)", "Final form Mud Fish!", "Swampert muddy water!", "🌊", OsrsSkill.FISHING,
        listOf("Arms hard as rock batter down massive boulders!"),
        stage = 3, evolvesFromName = "MARSHTOMP"
    ),

    SABLEYE(
        "Sableye (Gen 3)", "Dark/Ghost Darkness Pokémon with sparkling gem eyes!", "Gems and shadows shine bright!", "💎", OsrsSkill.THIEVING,
        listOf("Gems and shadows shine bright!", "Feasts on raw gemstones in dark caverns!", "Shadow sneak strike!", "Gem eyes glinting in the dark!"),
        stage = 1
    ),

    // --- GEN 4 POKÉMON (EVERY 1ST STAGE UNLOCKABLE, 2ND & 3RD EVOLVED INTO) ---
    // Starters
    TURTWIG(
        "Turtwig (Gen 4)", "Sinnoh Tiny Leaf starter Pokémon!", "Turtwig shell growing strong!", "🐢", OsrsSkill.FARMING,
        listOf("Turtwig shell growing strong!", "Razor leaf!", "Synthesis sunshine!"),
        stage = 1, evolutionLevelReq = 18, evolvesToName = "GROTLE"
    ),
    GROTLE(
        "Grotle (Gen 4)", "Sinnoh Grove Pokémon!", "Grotle forest shell!", "🌳", OsrsSkill.FARMING,
        listOf("Grotle forest shell absorbing sunlight!", "Berries grow on shell bushes!"),
        stage = 2, evolutionLevelReq = 32, evolvesToName = "TORTERRA", evolvesFromName = "TURTWIG"
    ),
    TORTERRA(
        "Torterra (Gen 4)", "Sinnoh Continent Pokémon!", "Torterra earthquake impact!", "🏞️", OsrsSkill.FARMING,
        listOf("Small Pokémon migrate onto Torterra's back!", "Frenzy Plant unleashed!"),
        stage = 3, evolvesFromName = "GROTLE"
    ),

    CHIMCHAR(
        "Chimchar (Gen 4)", "Sinnoh Chimp Fire starter Pokémon!", "Chimchar flaming tail!", "🐒", OsrsSkill.AGILITY,
        listOf("Chimchar flaming tail!", "Flame wheel!", "Taunt agile leap!"),
        stage = 1, evolutionLevelReq = 14, evolvesToName = "MONFERNO"
    ),
    MONFERNO(
        "Monferno (Gen 4)", "Sinnoh Playful Flame Pokémon!", "Monferno fiery aerial kicks!", "🐵", OsrsSkill.AGILITY,
        listOf("Monferno fiery aerial kicks!", "Mach punch combo!"),
        stage = 2, evolutionLevelReq = 36, evolvesToName = "INFERNAPE", evolvesFromName = "CHIMCHAR"
    ),
    INFERNAPE(
        "Infernape (Gen 4)", "Sinnoh Flame Master Pokémon!", "Infernape Flare Blitz!", "🔥", OsrsSkill.AGILITY,
        listOf("Infernape Flare Blitz blitzes all obstacles!", "Crown of fire burns bright!"),
        stage = 3, evolvesFromName = "MONFERNO"
    ),

    PIPLUP(
        "Piplup (Gen 4)", "Sinnoh Penguin Water starter Pokémon!", "Piplup proud water burst!", "🐧", OsrsSkill.FISHING,
        listOf("Piplup proud water burst!", "Bubble beam!", "Peck strike!"),
        stage = 1, evolutionLevelReq = 16, evolvesToName = "PRINPLUP"
    ),
    PRINPLUP(
        "Prinplup (Gen 4)", "Sinnoh Penguin Pride Pokémon!", "Prinplup wing slap!", "🐧", OsrsSkill.FISHING,
        listOf("Prinplup wing slap cleaves through thick ice!"),
        stage = 2, evolutionLevelReq = 36, evolvesToName = "EMPOLEON", evolvesFromName = "PIPLUP"
    ),
    EMPOLEON(
        "Empoleon (Gen 4)", "Sinnoh Emperor Trident Pokémon!", "Empoleon Hydro Cannon!", "👑", OsrsSkill.FISHING,
        listOf("Three horns on beak symbolize master strength!", "Slices through icebergs effortlessly!"),
        stage = 3, evolvesFromName = "PRINPLUP"
    ),

    // Early Route & Common
    STARLY(
        "Starly (Gen 4)", "Sinnoh Starling Bird Pokémon!", "Star-ly! Chirp chirp!", "🐤", OsrsSkill.AGILITY,
        listOf("Star-ly! Chirp chirp!", "Wing attack airborne!"),
        stage = 1, evolutionLevelReq = 14, evolvesToName = "STARAVIA"
    ),
    STARAVIA(
        "Staravia (Gen 4)", "Evolved Starling Pokémon!", "Staravia flock leader!", "🦅", OsrsSkill.AGILITY,
        listOf("Flies swift through forests seeking strong prey!"),
        stage = 2, evolutionLevelReq = 34, evolvesToName = "STARAPTOR", evolvesFromName = "STARLY"
    ),
    STARAPTOR(
        "Staraptor (Gen 4)", "Final Predator Bird Pokémon!", "Staraptor Brave Bird!", "🦅", OsrsSkill.AGILITY,
        listOf("Comb crest strikes fear into foes! Brave Bird launched!"),
        stage = 3, evolvesFromName = "STARAVIA"
    ),

    BIDOOF(
        "Bidoof (Gen 4)", "Plump Beaver Pokémon!", "Bidoof bidoof! Gnaw logs!", "🦫", OsrsSkill.CONSTRUCTION,
        listOf("Bidoof bidoof! Gnaw logs!", "Sturdy teeth chew oak planks!"),
        stage = 1, evolutionLevelReq = 15, evolvesToName = "BIBAREL"
    ),
    BIBAREL(
        "Bibarel (Gen 4)", "Beaver Dam Builder Pokémon!", "Bibarel dam building!", "🪵", OsrsSkill.CONSTRUCTION,
        listOf("Builds river dams that never collapse! HM Master!"),
        stage = 2, evolvesFromName = "BIDOOF"
    ),

    KRICKETOT(
        "Kricketot (Gen 4)", "Cricket Musical Instrument Pokémon!", "Krick krick!", "🦗", OsrsSkill.FLETCHING,
        listOf("Krick krick! Antennae chime like xylophones!"),
        stage = 1, evolutionLevelReq = 10, evolvesToName = "KRICKETUNE"
    ),
    KRICKETUNE(
        "Kricketune (Gen 4)", "Cricket Conductor Pokémon!", "Delelele-woooop!", "🎻", OsrsSkill.FLETCHING,
        listOf("Delelele-woooop! Musical melody echoes!"),
        stage = 2, evolvesFromName = "KRICKETOT"
    ),

    SHINX(
        "Shinx (Gen 4)", "Flash Yellow Spark Pokémon!", "Sparking yellow fur!", "🦁", OsrsSkill.HUNTER,
        listOf("Sparking yellow fur!", "Wild charge!", "Spark electric burst!"),
        stage = 1, evolutionLevelReq = 15, evolvesToName = "LUXIO"
    ),
    LUXIO(
        "Luxio (Gen 4)", "Spark Lion Pokémon!", "Luxio electricity surge!", "⚡", OsrsSkill.HUNTER,
        listOf("Claws release high-voltage electricity!"),
        stage = 2, evolutionLevelReq = 30, evolvesToName = "LUXRAY", evolvesFromName = "SHINX"
    ),
    LUXRAY(
        "Luxray (Gen 4)", "Gleam Eyes X-Ray Vision Lion!", "Luxray X-Ray sight!", "🦁", OsrsSkill.HUNTER,
        listOf("Eyes can see through solid stone walls to track quarry!"),
        stage = 3, evolvesFromName = "LUXIO"
    ),

    BUDEW(
        "Budew (Gen 4)", "Bud Rosebud Baby Pokémon!", "Budew sweet pollen!", "🌱", OsrsSkill.HERBLORE,
        listOf("Budew sweet pollen brews fresh potion remedies!"),
        stage = 1, evolutionLevelReq = 15, evolvesToName = "ROSELIA"
    ),
    ROSELIA(
        "Roselia (Gen 4)", "Thorn Bouquet Pokémon!", "Roselia dual bouquet!", "🌹", OsrsSkill.HERBLORE,
        listOf("Dual rose bouquet shoots sharp poison darts!"),
        stage = 2, evolutionLevelReq = 30, evolvesToName = "ROSERADE", evolvesFromName = "BUDEW"
    ),
    ROSERADE(
        "Roserade (Gen 4)", "Bouquet Masquerade Dancer!", "Roserade poison whip dance!", "🥀", OsrsSkill.HERBLORE,
        listOf("Executes graceful dancer whips with lethal poison spikes!"),
        stage = 3, evolvesFromName = "ROSELIA"
    ),

    CRANIDOS(
        "Cranidos (Gen 4)", "Head Butt Fossil Pokémon!", "Cranidos skull bash!", "🦖", OsrsSkill.SMITHING,
        listOf("Hard iron skull smashes granite rocks effortlessly!"),
        stage = 1, evolutionLevelReq = 30, evolvesToName = "RAMPARDOS"
    ),
    RAMPARDOS(
        "Rampardos (Gen 4)", "Head Charge Tyrant Fossil!", "Rampardos Head Smash!", "🌋", OsrsSkill.SMITHING,
        listOf("Head Smash shatters entire mountainsides!"),
        stage = 2, evolvesFromName = "CRANIDOS"
    ),

    SHIELDON(
        "Shieldon (Gen 4)", "Shield Face Armor Fossil!", "Shieldon steel face!", "🛡️", OsrsSkill.DEFENCE,
        listOf("Sturdy facial shield repels every incoming blow!"),
        stage = 1, evolutionLevelReq = 30, evolvesToName = "BASTIODON"
    ),
    BASTIODON(
        "Bastiodon (Gen 4)", "Fortress Wall Dinosaur Fossil!", "Bastiodon impenetrable wall!", "🏰", OsrsSkill.DEFENCE,
        listOf("Impentrable fortress shield protects team mates!"),
        stage = 2, evolvesFromName = "SHIELDON"
    ),

    PACHIRISU(
        "Pachirisu (Gen 4)", "Electric Squirrel Pokémon!", "Pachi pachi! Cheeks glow!", "🐿️", OsrsSkill.AGILITY,
        listOf("Pachi pachi! Cheeks glow!", "Super Fang and Follow Me support!"),
        stage = 1
    ),

    BUIZEL(
        "Buizel (Gen 4)", "Sea Weasel Flotation Collar!", "Buizel water jet ready!", "🦦", OsrsSkill.FISHING,
        listOf("Buizel water jet ready!", "Aqua jet dash!", "Sonic boom!"),
        stage = 1, evolutionLevelReq = 26, evolvesToName = "FLOATZEL"
    ),
    FLOATZEL(
        "Floatzel (Gen 4)", "Dual Flotation Sea Weasel!", "Floatzel ocean rescue!", "🌊", OsrsSkill.FISHING,
        listOf("Dual flotation sac carries heavy loads across sea currents!"),
        stage = 2, evolvesFromName = "BUIZEL"
    ),

    CHERUBI(
        "Cherubi (Gen 4)", "Cherry Berry Twin Pokémon!", "Cherubi sweet cherry!", "🍒", OsrsSkill.FARMING,
        listOf("Small nutrient bulb tastes sweet and fruity!"),
        stage = 1, evolutionLevelReq = 25, evolvesToName = "CHERRIM"
    ),
    CHERRIM(
        "Cherrim (Gen 4)", "Blossom Sunshine Flower!", "Cherrim sunshine bloom!", "🌸", OsrsSkill.FARMING,
        listOf("Blooms brightly under intense sunshine rays!"),
        stage = 2, evolvesFromName = "CHERUBI"
    ),

    SHELLOS(
        "Shellos (Gen 4)", "Sea Slug Coastal Pokémon!", "Shellos squishy splash!", "🐌", OsrsSkill.COOKING,
        listOf("Squishy sea slug slides along sandy tides!"),
        stage = 1, evolutionLevelReq = 30, evolvesToName = "GASTRODON"
    ),
    GASTRODON(
        "Gastrodon (Gen 4)", "East & West Sea Slug!", "Gastrodon Muddy Water!", "🌊", OsrsSkill.COOKING,
        listOf("Absorbs ocean salt and sprays sticky mud currents!"),
        stage = 2, evolvesFromName = "SHELLOS"
    ),

    DRIFLOON(
        "Drifloon (Gen 4)", "Balloon Spirit Pokémon!", "Drifloon floating ghost!", "🎈", OsrsSkill.MAGIC,
        listOf("Floating softly in the twilight breeze!"),
        stage = 1, evolutionLevelReq = 28, evolvesToName = "DRIFBLIM"
    ),
    DRIFBLIM(
        "Drifblim (Gen 4)", "Airship Blimp Ghost!", "Drifblim phantom flight!", "👻", OsrsSkill.MAGIC,
        listOf("Carries passengers through nocturnal skies!"),
        stage = 2, evolvesFromName = "DRIFLOON"
    ),

    BUNEARY(
        "Buneary (Gen 4)", "Fluffy Rabbit Acrobat!", "Buneary hop hop!", "🐰", OsrsSkill.AGILITY,
        listOf("Buneary rolled-up ear strike!"),
        stage = 1, evolutionLevelReq = 20, evolvesToName = "LOPUNNY"
    ),
    LOPUNNY(
        "Lopunny (Gen 4)", "Graceful Fluffy Acrobat!", "Lopunny High Jump Kick!", "🩰", OsrsSkill.AGILITY,
        listOf("Graceful kicks dodge all incoming hazards effortlessly!"),
        stage = 2, evolvesFromName = "BUNEARY"
    ),

    GLAMEOW(
        "Glameow (Gen 4)", "Cat Feather Tail Thief!", "Glameow stealth purr!", "🐱", OsrsSkill.THIEVING,
        listOf("Hypnotizes prey with ribbon tail dances!"),
        stage = 1, evolutionLevelReq = 38, evolvesToName = "PURUGLY"
    ),
    PURUGLY(
        "Purugly (Gen 4)", "Tiger Cat Boss Pokémon!", "Purugly Body Slam!", "😼", OsrsSkill.THIEVING,
        listOf("Claims territory by binding waist with thick double tails!"),
        stage = 2, evolvesFromName = "GLAMEOW"
    ),

    CHINGLING(
        "Chingling (Gen 4)", "Bell Chime Baby Pokémon!", "Ching-chime! Ring bell!", "🔔", OsrsSkill.MAGIC,
        listOf("Ching-chime! High frequency orb sounds soothe the soul!"),
        stage = 1, evolutionLevelReq = 20, evolvesToName = "CHIMECHO"
    ),
    CHIMECHO(
        "Chimecho (Gen 4)", "Wind Chime Spirit Pokémon!", "Chimecho healing chime!", "🎐", OsrsSkill.MAGIC,
        listOf("Emitters echo ultrasonic waves that clear negative vibes!"),
        stage = 2, evolvesFromName = "CHINGLING"
    ),

    STUNKY(
        "Stunky (Gen 4)", "Skunk Spray Pokémon!", "Stunky stinky stench!", "🦨", OsrsSkill.SLAYER,
        listOf("Foul fluid spray repels wild monsters!"),
        stage = 1, evolutionLevelReq = 34, evolvesToName = "SKUNTANK"
    ),
    SKUNTANK(
        "Skuntank (Gen 4)", "Skunk Poison Blaster!", "Skuntank Flamethrower!", "☣️", OsrsSkill.SLAYER,
        listOf("Tail tip sprays noxious fluid up to 160 feet!"),
        stage = 2, evolvesFromName = "STUNKY"
    ),

    BRONZOR(
        "Bronzor (Gen 4)", "Bronze Mirror Antique!", "Bronzor ancient sheen!", "🪞", OsrsSkill.SMITHING,
        listOf("Ancient patterns inscribed on reverse bronze face!"),
        stage = 1, evolutionLevelReq = 33, evolvesToName = "BRONZONG"
    ),
    BRONZONG(
        "Bronzong (Gen 4)", "Bronze Bell Rain Maker!", "Bronzong rain dance chime!", "🔔", OsrsSkill.SMITHING,
        listOf("Brings rain clouds to nourish crops and farming patches!"),
        stage = 2, evolvesFromName = "BRONZOR"
    ),

    BONSLY(
        "Bonsly (Gen 4)", "Bonsai Rock Baby!", "Bonsly fake tears!", "🪴", OsrsSkill.SMITHING,
        listOf("Disguises as a potted tree to avoid water!"),
        stage = 1, evolutionLevelReq = 20, evolvesToName = "SUDOWOODO"
    ),

    MIME_JR(
        "Mime Jr. (Gen 4)", "Mime Baby Pokémon!", "Mime jr. mimic dance!", "🤡", OsrsSkill.MAGIC,
        listOf("Mimics actions of trainer to master illusion barrier!"),
        stage = 1, evolutionLevelReq = 18, evolvesToName = "MR_MIME"
    ),

    HAPPINY(
        "Happiny (Gen 4)", "Playhouse Egg Baby!", "Happiny round stone!", "🥚", OsrsSkill.HITPOINTS,
        listOf("Carries smooth white oval stone in pouch!"),
        stage = 1, evolutionLevelReq = 18, evolvesToName = "CHANSEY"
    ),
    CHANSEY(
        "Chansey (Gen 1)", "Egg Nurse Pokémon!", "Chansey Soft-Boiled heal!", "🥚", OsrsSkill.HITPOINTS,
        listOf("Lays nutritious egg daily to share with injured companions!"),
        stage = 2, evolutionLevelReq = 35, evolvesToName = "BLISSEY", evolvesFromName = "HAPPINY"
    ),
    BLISSEY(
        "Blissey (Gen 2)", "Happiness Nurse Specialist!", "Blissey ultimate cheer!", "💖", OsrsSkill.HITPOINTS,
        listOf("Brimming with loving compassion! Takes one bite to smile!"),
        stage = 3, evolvesFromName = "CHANSEY"
    ),

    CHATOT(
        "Chatot (Gen 4)", "Music Note Parrot Pokémon!", "Chatot mimic song!", "🦜", OsrsSkill.DIVINATION,
        listOf("Mimics human speech and rhythmic songs with note tongue!"),
        stage = 1
    ),

    SPIRITOMB(
        "Spiritomb (Gen 4)", "Forbidden Keystone 108 Spirits!", "108 souls bound within keystone!", "🔮", OsrsSkill.SLAYER,
        listOf("Bound to Odd Keystone 500 years ago for misdeeds!"),
        stage = 1
    ),

    GIBLE(
        "Gible (Gen 4)", "Land Shark Baby Dragon!", "Gible dragon bite!", "🦈", OsrsSkill.HITPOINTS,
        listOf("Gible dragon bite!", "Lurks in narrow geothermal caves!"),
        stage = 1, evolutionLevelReq = 24, evolvesToName = "GABITE"
    ),
    GABITE(
        "Gabite (Gen 4)", "Cave Shark Dragon!", "Gabite Dual Chop!", "🐊", OsrsSkill.HITPOINTS,
        listOf("Loves glittering gems! Digs for underground diamond deposits!"),
        stage = 2, evolutionLevelReq = 48, evolvesToName = "GARCHOMP", evolvesFromName = "GIBLE"
    ),
    GARCHOMP(
        "Garchomp (Gen 4)", "Mach Supersonic Dragon!", "Dragon Rush at jet speed!", "🦈", OsrsSkill.HITPOINTS,
        listOf("Flies at jet speed! Wings cut through thick mountain trees!"),
        stage = 3, evolvesFromName = "GABITE"
    ),

    MUNCHLAX(
        "Munchlax (Gen 4)", "Big Eater Baby Pokémon!", "Munch munch! Food time!", "🍖", OsrsSkill.COOKING,
        listOf("Swallows food whole without chewing! Loves feast cooking!"),
        stage = 1, evolutionLevelReq = 20, evolvesToName = "SNORLAX"
    ),

    RIOLU(
        "Riolu (Gen 4)", "Emanation Aura Baby!", "Riolu aura pulse!", "🐾", OsrsSkill.ATTACK,
        listOf("Senses emotions in the form of glowing aura waves!"),
        stage = 1, evolutionLevelReq = 25, evolvesToName = "LUCARIO"
    ),
    LUCARIO(
        "Lucario (Gen 4)", "Aura Sphere Master!", "Aura Sphere ready!", "🐺", OsrsSkill.ATTACK,
        listOf("Aura Sphere ready!", "Close combat strike!"),
        stage = 2, evolvesFromName = "RIOLU"
    ),

    HIPPOPOTAS(
        "Hippopotas (Gen 4)", "Hippo Sand Bath Pokémon!", "Hippo sand puff!", "🦛", OsrsSkill.SMITHING,
        listOf("Shrouds body in dry desert sand baths!"),
        stage = 1, evolutionLevelReq = 34, evolvesToName = "HIPPOWDON"
    ),
    HIPPOWDON(
        "Hippowdon (Gen 4)", "Heavy Sand Hippo Boss!", "Hippowdon Sand Tomb!", "🏜️", OsrsSkill.SMITHING,
        listOf("Discharges stored sand from body ports to create sandstorms!"),
        stage = 2, evolvesFromName = "HIPPOPOTAS"
    ),

    SKORUPI(
        "Skorupi (Gen 4)", "Scorpion Claws Poison!", "Skorupi pin missile!", "🦂", OsrsSkill.SLAYER,
        listOf("Burrows into dry desert sands waiting for prey!"),
        stage = 1, evolutionLevelReq = 40, evolvesToName = "DRAPION"
    ),
    DRAPION(
        "Drapion (Gen 4)", "Ogre Scorpion Boss!", "Drapion Cross Poison!", "🦂", OsrsSkill.SLAYER,
        listOf("Hooked claws exert strength to tear steel plates apart!"),
        stage = 2, evolvesFromName = "SKORUPI"
    ),

    CROAGUNK(
        "Croagunk (Gen 4)", "Toxic Mouth Frog!", "Croagunk Poison Jab!", "🐸", OsrsSkill.THIEVING,
        listOf("Puffs poison sacs on cheeks to intimidate foes!"),
        stage = 1, evolutionLevelReq = 37, evolvesToName = "TOXICROAK"
    ),
    TOXICROAK(
        "Toxicroak (Gen 4)", "Toxic Mouth Boss Frog!", "Toxicroak Sucker Punch!", "🐸", OsrsSkill.THIEVING,
        listOf("Claw tip secretes potent venom strong enough to knock out foes!"),
        stage = 2, evolvesFromName = "CROAGUNK"
    ),

    CARNIVINE(
        "Carnivine (Gen 4)", "Bug Catcher Plant!", "Carnivine Vine Whip!", "🪴", OsrsSkill.FARMING,
        listOf("Hangs from marsh tree branches with open jaws!"),
        stage = 1
    ),

    FINNEON(
        "Finneon (Gen 4)", "Wing Fish Neon!", "Finneon neon glow!", "🐟", OsrsSkill.FISHING,
        listOf("Tail fins glow bright blue under ocean moonlight!"),
        stage = 1, evolutionLevelReq = 31, evolvesToName = "LUMINEON"
    ),
    LUMINEON(
        "Lumineon (Gen 4)", "Deep Sea Neon Butterfly Fish!", "Lumineon deep glow!", "🐠", OsrsSkill.FISHING,
        listOf("Illuminates deep abyssal sea floor with four shining fins!"),
        stage = 2, evolvesFromName = "FINNEON"
    ),

    MANTYKE(
        "Mantyke (Gen 4)", "Manta Ray Baby!", "Mantyke glide wave!", "🐟", OsrsSkill.FISHING,
        listOf("Bouncing over sea ocean swells alongside Remoraa!"),
        stage = 1, evolutionLevelReq = 20, evolvesToName = "PET_KRAKEN"
    ),

    SNOVER(
        "Snover (Gen 4)", "Frost Tree Snow Abominable!", "Snover icy wind!", "❄️", OsrsSkill.WOODCUTTING,
        listOf("Grows ice berries on belly during cold winter months!"),
        stage = 1, evolutionLevelReq = 40, evolvesToName = "ABOMASNOW"
    ),
    ABOMASNOW(
        "Abomasnow (Gen 4)", "Frost Tree Snow Monster!", "Abomasnow Blizzard!", "🏔️", OsrsSkill.WOODCUTTING,
        listOf("Summons blizzards to hide in snow covered mountain peaks!"),
        stage = 2, evolvesFromName = "SNOVER"
    ),

    ROTOM(
        "Rotom (Gen 4)", "Plasma Electric Appliance Spirit!", "Rotom electro ball!", "⚡", OsrsSkill.SMITHING,
        listOf("Body composed of plasma! Inhabits electrical tools!"),
        stage = 1
    ),

    // Sinnoh Legendaries & Mythicals
    DARKRAI(
        "Darkrai (Gen 4)", "Pitch-Black mythical Pokémon of deep dreams!", "Dark Void shadow realm!", "🌑", OsrsSkill.SLAYER,
        listOf("Dark Void shadow realm!", "Bad Dreams active!"),
        stage = 1
    ),
    ARCEUS(
        "Arceus (Gen 4)", "Alpha Pokémon creator of the cosmos!", "Judgement beam from above!", "👑", OsrsSkill.HITPOINTS,
        listOf("Judgement beam from above!", "Cosmic power aura!"),
        stage = 1
    ),
    DIALGA(
        "Dialga (Gen 4)", "Temporal Legendary Time Lord!", "Roar of Time!", "⏳", OsrsSkill.RUNECRAFT,
        listOf("Roar of Time bends temporal energy!"),
        stage = 1
    ),
    PALKIA(
        "Palkia (Gen 4)", "Spatial Legendary Space Lord!", "Spacial Rend!", "🌌", OsrsSkill.MAGIC,
        listOf("Spacial Rend tear in the fabric of space!"),
        stage = 1
    ),
    GIRATINA(
        "Giratina (Gen 4)", "Renegade Distortion Realm Lord!", "Shadow Force unleashed!", "🐉", OsrsSkill.MAGIC,
        listOf("Shadow Force strikes from the Distortion Realm!"),
        stage = 1
    ),
    SHAYMIN(
        "Shaymin (Gen 4)", "Gratitude Land Form Hedgehog!", "Seed Flare bloom!", "🌺", OsrsSkill.FARMING,
        listOf("Seed Flare purifies polluted lands into lush flower gardens!"),
        stage = 1
    ),
    CRESSELIA(
        "Cresselia (Gen 4)", "Lunar Crescent Guardian!", "Lunar Dance serenity!", "🌙", OsrsSkill.MAGIC,
        listOf("Shining aurora veil dispels nightmares!"),
        stage = 1
    );

    val displayName: String
        get() = rawDisplayName.replace(Regex("\\s*\\(Gen \\d+\\)"), "").trim()

    val evolvesTo: PetType?
        get() = evolvesToName?.let { name ->
            try { PetType.valueOf(name) } catch (e: Exception) { null }
        }

    val evolvesFrom: PetType?
        get() = evolvesFromName?.let { name ->
            try { PetType.valueOf(name) } catch (e: Exception) { null }
        }

    fun getRandomQuote(): String = com.example.data.repository.OsrsQuotesRepository.getRandomQuoteForPet(this)
}

enum class PetEmote(val label: String, val animationEmoji: String) {
    IDLE("Idle", "😴"),
    HAPPY("Happy Dance", "💃"),
    EATING("Eating", "😋"),
    PLAYING("Playing", "🎮"),
    SKILLING("Training Skill", "⚒️"),
    SLEEPING("Sleeping", "💤")
}

enum class CombatStyle(val displayName: String, val iconEmoji: String, val description: String) {
    ATTACK("Hand Combat", "⚔️", "Physical hand combat strikes & melee accuracy"),
    RANGED("Blowdarts", "🏹", "Blowdart precision & speed"),
    MAGIC("Incantations", "🪄", "Sacred chants & incantations"),
    DEFENCE("Warding", "🛡️", "Spiritual protection & warding posture")
}

data class PetState(
    val petType: PetType = PetType.TANGLEROOT,
    val customName: String = "Tangleroot",
    val hunger: Int = 85, // 0 to 100
    val happiness: Int = 90, // 0 to 100
    val energy: Int = 90, // 0 to 100
    val health: Int = 100, // 0 to 100
    val coinsGp: Long = 25000L, // Initial GP
    val questPoints: Int = 0,
    val completedQuestIds: List<String> = emptyList(),
    val currentEmote: PetEmote = PetEmote.IDLE,
    val currentQuote: String = "⚔️ Ready for adventure! Train your real-world skills to level up!",
    val isMuted: Boolean = false,
    val currentOutfitId: String = "default",
    val unlockedOutfitIds: List<String> = listOf("default", "barrows_dharok", "pokemon_pikachu", "skilling_graceful"),
    val unlockedPets: List<PetType> = listOf(
        PetType.TANGLEROOT,
        PetType.BEAVER,
        PetType.HERON,
        PetType.ROCK_GOLEM,
        PetType.TURTWIG,
        PetType.CHIMCHAR,
        PetType.PIPLUP,
        PetType.GUTHIXIAN_WISP,
        PetType.SAILING_PARROT
    )
)
