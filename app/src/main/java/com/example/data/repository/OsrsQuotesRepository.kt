package com.example.data.repository

import com.example.data.models.OsrsSkill
import com.example.data.models.PetMoodLevel
import com.example.data.models.PetType
import com.example.utils.PhoneContextInfo
import java.util.Calendar
import kotlin.random.Random

object OsrsQuotesRepository {

    private val recentQuotesHistory = ArrayDeque<String>(200)

    // Pokémon Champions & Meta Competitive Pokémon Base Speed Stats
    val MASTER_PHRASES: List<String> = listOf(
        "⚡ Flutter Mane has a Base Speed stat of 135!",
        "⚡ Regieleki leads all competitive Speed tiers with a Base Speed stat of 200!",
        "⚡ Deoxys (Speed Forme) holds a supreme Base Speed stat of 180!",
        "⚡ Ninjask blazes ahead with a Base Speed stat of 160!",
        "⚡ Pheromosa blitzes the field with a Base Speed stat of 151!",
        "⚡ Calyrex (Shadow Rider) commands a massive Base Speed stat of 150!",
        "⚡ Hisuian Electrode bolts in with a Base Speed stat of 150!",
        "⚡ Zacian (Crowned Sword) dashes in with a Base Speed stat of 148!",
        "⚡ Dragapult zips through meta Speed tiers with a Base Speed stat of 142!",
        "⚡ Iron Bundle blazes ahead with a Base Speed stat of 136!",
        "⚡ Chien-Pao rocks a high meta Base Speed stat of 135!",
        "⚡ Koraidon hits the battlefield with a Base Speed stat of 135!",
        "⚡ Miraidon charges in with a Base Speed stat of 135!",
        "⚡ Aerodactyl soars at a Base Speed stat of 130!",
        "⚡ Jolteon volt-switches at a Base Speed stat of 130!",
        "⚡ Mewtwo holds a legendary Base Speed stat of 130!",
        "⚡ Eternatus beams down with a Base Speed stat of 130!",
        "⚡ Zamazenta (Crowned Shield) defends at a Base Speed stat of 128!",
        "⚡ Talonflame gale-wings at a Base Speed stat of 126!",
        "⚡ Weavile ice-shards with a Base Speed stat of 125!",
        "⚡ Kilowattrel wind-powers in with a Base Speed stat of 125!",
        "⚡ Iron Boulder rushes ahead with a Base Speed stat of 124!",
        "⚡ Meowscarada leaps in with a Base Speed stat of 123!",
        "⚡ Noivern booms at a Base Speed stat of 123!",
        "⚡ Greninja water-shurikens in with a Base Speed stat of 122!",
        "⚡ Tornadus (Therian Forme) flies at a Base Speed stat of 121!",
        "⚡ Sneasler dire-claws in with a Base Speed stat of 120!",
        "⚡ Palkia (Origin Forme) warps space with a Base Speed stat of 120!",
        "⚡ Roaring Moon sweeps with a Base Speed stat of 119!",
        "⚡ Iron Valiant strikes fast with a Base Speed stat of 116!",
        "⚡ Whimsicott sets Tailwind with a Base Speed stat of 116!",
        "⚡ Starmie spins at a Base Speed stat of 115!",
        "⚡ Raikou thunders at a Base Speed stat of 115!",
        "⚡ Serperior leaf-storms with a Base Speed stat of 113!",
        "⚡ Tornadus (Incarnate Forme) sets Tailwind at a Base Speed stat of 111!",
        "⚡ Maushold population-bombs with a Base Speed stat of 111!",
        "⚡ Gengar Shadow Balls with a Base Speed stat of 110!",
        "⚡ Latios mist-balls with a Base Speed stat of 110!",
        "⚡ Latias luster-purges with a Base Speed stat of 110!",
        "⚡ Lugia aeroblasts with a Base Speed stat of 110!",
        "⚡ Ogerpon (Teal / Hearthflame / Wellspring / Cornerstone) has a Base Speed stat of 110!",
        "⚡ Iron Moth fiery-dances at a Base Speed stat of 110!",
        "⚡ Alolan Ninetales sets Aurora Veil at a Base Speed stat of 109!",
        "⚡ Walking Wake glides at a Base Speed stat of 109!",
        "⚡ Iron Jugulis air-slashes at a Base Speed stat of 108!",
        "⚡ Enamorus (Incarnate Forme) floats at a Base Speed stat of 106!",
        "⚡ Munkidori toxic-chains at a Base Speed stat of 106!",
        "⚡ Iron Leaves psyblade-strikes at a Base Speed stat of 104!",
        "⚡ Garchomp outspeeds 100s with its iconic Base Speed stat of 102!",
        "⚡ Chi-Yu flares up with a Base Speed stat of 100!",
        "⚡ Volcarona quiver-dances at a Base Speed stat of 100!",
        "⚡ Salamence dragon-dances at a Base Speed stat of 100!",
        "⚡ Zapdos thunderbolts at a Base Speed stat of 100!",
        "⚡ Galarian Zapdos thunderous-kicks at a Base Speed stat of 100!",
        "⚡ Entei sacred-fires at a Base Speed stat of 100!",
        "⚡ Palafin (Hero Form) transforms and holds a Base Speed stat of 100!",
        "⚡ Fezandipiti lashes out with a Base Speed stat of 99!",
        "⚡ Hydreigon draco-meteors with a Base Speed stat of 98!",
        "⚡ Iron Crown hovers at a Base Speed stat of 98!",
        "⚡ Urshifu (Rapid Strike) punches in with a Base Speed stat of 97!",
        "⚡ Urshifu (Single Strike) strikes with a Base Speed stat of 97!",
        "⚡ Lunala shines with a Base Speed stat of 97!",
        "⚡ Solgaleo sunsteel-strikes with a Base Speed stat of 97!",
        "⚡ Rayquaza rules the skies with a Base Speed stat of 95!",
        "⚡ Kyurem (Black / White) strikes with a Base Speed stat of 95!",
        "⚡ Galarian Articuno freezing-glares at a Base Speed stat of 95!",
        "⚡ Indeedee (Male) sets Psychic Terrain at a Base Speed stat of 95!",
        "⚡ Tinkaton gigaton-hammers at a Base Speed stat of 94!",
        "⚡ Landorus (Therian Forme) flies in with a Base Speed stat of 91!",
        "⚡ Gouging Fire blazes in with a Base Speed stat of 91!",
        "⚡ Kyogre expands seas with a Base Speed stat of 90!",
        "⚡ Groudon expands land with a Base Speed stat of 90!",
        "⚡ Reshiram blue-flares at a Base Speed stat of 90!",
        "⚡ Zekrom bolt-strikes at a Base Speed stat of 90!",
        "⚡ Ho-Oh sacred-fires at a Base Speed stat of 90!",
        "⚡ Dialga (Origin Forme) roars time at a Base Speed stat of 90!",
        "⚡ Annihilape rage-fists with a Base Speed stat of 90!",
        "⚡ Hisuian Arcanine raging-bulls at a Base Speed stat of 90!",
        "⚡ Moltres fire-blasts at a Base Speed stat of 90!",
        "⚡ Galarian Moltres fiery-wraths at a Base Speed stat of 90!",
        "⚡ Great Tusk spins in with a Base Speed stat of 87!",
        "⚡ Baxcalibur glides in with a Base Speed stat of 87!",
        "⚡ Glimmora hazard-stacks at a Base Speed stat of 86!",
        "⚡ Rillaboom plays Grassy Glide at a Base Speed stat of 85!",
        "⚡ Archaludon Electro-Shots at a Base Speed stat of 85!",
        "⚡ Suicune Tailwind-boosts at a Base Speed stat of 85!",
        "⚡ Articuno ice-beams at a Base Speed stat of 85!",
        "⚡ Kommo-o Clangorous Souls at a Base Speed stat of 85!",
        "⚡ Ceruledge bitter-blades at a Base Speed stat of 85!",
        "⚡ Indeedee (Female) sets Follow Me at a Base Speed stat of 85!",
        "⚡ Overqwil barb-barrages at a Base Speed stat of 85!",
        "⚡ Quaquaval aqua-steps at a Base Speed stat of 85!",
        "⚡ Gholdengo Make It Rains with a Base Speed stat of 84!",
        "⚡ Tatsugiri commands Dondozo with a Base Speed stat of 82!",
        "⚡ Gyarados waterfall-strikes at a Base Speed stat of 81!",
        "⚡ Dragonite extremespeeds at a Base Speed stat of 80!",
        "⚡ Gardevoir hyper-voices at a Base Speed stat of 80!",
        "⚡ Gallade psycho-cutters at a Base Speed stat of 80!",
        "⚡ Goodra dragon-pulses at a Base Speed stat of 80!",
        "⚡ Okidogi upper-curts at a Base Speed stat of 80!",
        "⚡ Basculegion wave-crashes at a Base Speed stat of 78!",
        "⚡ Heatran erupts with a Base Speed stat of 77!",
        "⚡ Necrozma (Dusk Mane) strikes with a Base Speed stat of 77!",
        "⚡ Raging Bolt thunders with a Base Speed stat of 75!",
        "⚡ Armarouge expands force with a Base Speed stat of 75!",
        "⚡ Iron Thorns dragon-dances at a Base Speed stat of 72!",
        "⚡ Breloom Spores at a Base Speed stat of 70!",
        "⚡ Wo-Chien tablets-of-ruin at a Base Speed stat of 70!",
        "⚡ Metagross bullet-punches at a Base Speed stat of 70!",
        "⚡ Houndstone last-respects at a Base Speed stat of 68!",
        "⚡ Corviknight Brave Birds at a Base Speed stat of 67!",
        "⚡ Skeledirge Torch Songs at a Base Speed stat of 66!",
        "⚡ Pelipper sets Rain at a Base Speed stat of 65!",
        "⚡ Orthworm shed-tails at a Base Speed stat of 65!",
        "⚡ Alolan Sandslash icicle-spears at a Base Speed stat of 65!",
        "⚡ Tyranitar sand-streams at a Base Speed stat of 61!",
        "⚡ Incineroar Fake Out-pivots at a Base Speed stat of 60!",
        "⚡ Grimmsnarl sets Screens at a Base Speed stat of 60!",
        "⚡ Farigiraf Armor-Tails at a Base Speed stat of 60!",
        "⚡ Porygon2 Eviolite-tanks at a Base Speed stat of 60!",
        "⚡ Hisuian Goodra shelter-defends at a Base Speed stat of 60!",
        "⚡ Primarina hyper-voices at a Base Speed stat of 60!",
        "⚡ Ursaluna (Bloodmoon) blasts Hyper Voice at a Base Speed stat of 52!",
        "⚡ Sinistcha matcha-gotchas at a Base Speed stat of 52!",
        "⚡ Kingambit Kowtow Cleaves at a Base Speed stat of 50!",
        "⚡ Iron Hands Drain Punches at a Base Speed stat of 50!",
        "⚡ Calyrex (Ice Rider) dominates Trick Room at a Base Speed stat of 50!",
        "⚡ Ursaluna (Normal) facade-smashes at a Base Speed stat of 50!",
        "⚡ Enamorus (Therian Forme) tanks at a Base Speed stat of 46!",
        "⚡ Ting-Lu tanks with a Base Speed stat of 45!",
        "⚡ Gastrodon storm-drains at a Base Speed stat of 39!",
        "⚡ Clefairy Follow Me-redirects at a Base Speed stat of 35!",
        "⚡ Dondozo Wave Crashes at a Base Speed stat of 35!",
        "⚡ Toxapex baneful-bunkers at a Base Speed stat of 35!",
        "⚡ Bronzong Gyro Balls in Trick Room at a Base Speed stat of 33!",
        "⚡ Amoonguss Spores in Trick Room at a Base Speed stat of 30!",
        "⚡ Snorlax Belly Dumps in Trick Room at a Base Speed stat of 30!",
        "⚡ Hatterene Trick Rooms at a Base Speed stat of 29!",
        "⚡ Dusclops Will-O-Wisps at a Trick Room Base Speed stat of 25!",
        "⚡ Torkoal Erupts in Sun & Trick Room at a Base Speed stat of 20!"
    )

    /**
     * Generates a random Pokémon Champions Base Speed stat entry.
     */
    fun generateCombinatorialStory(): String {
        return MASTER_PHRASES.random()
    }

    /**
     * Returns 5 special daily phrases dynamically seeded by current day.
     */
    fun getDailySpecialPhrases(): List<String> {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        val seed = (year * 366 + dayOfYear).toLong()
        val random = Random(seed)

        return MASTER_PHRASES.shuffled(random).take(10)
    }

    /**
     * Synthesizes a dynamic, highly entertaining quote.
     */
    fun generateDynamicSynthesizedQuote(
        petType: PetType,
        phoneContext: PhoneContextInfo? = null,
        moodLevel: PetMoodLevel = PetMoodLevel.HAPPY
    ): String {
        val emojis = listOf("✨", "🔥", "🌿", "⚔️", "🐉", "🛡️", "🔮", "🌊", "👑", "⚡", "🌟", "🐾", "💎")
        val emoji = emojis.random()
        val quote = generateCombinatorialStory()
        val result = "$emoji $quote"
        rememberQuote(result)
        return result
    }

    /**
     * Returns a fresh, deduplicated quote ensuring phrases are not repeated.
     */
    fun getRandomQuoteForPet(
        petType: PetType,
        phoneContext: PhoneContextInfo? = null,
        moodLevel: PetMoodLevel = PetMoodLevel.HAPPY
    ): String {
        val dailySpecials = getDailySpecialPhrases()
        val combined = (petType.quotes + dailySpecials + MASTER_PHRASES.shuffled()).distinct()

        // Filter out quotes that are in recent history
        val candidates = combined.filterNot { recentQuotesHistory.contains(it) }

        val chosen = if (candidates.isNotEmpty()) {
            candidates.random()
        } else {
            generateCombinatorialStory()
        }

        rememberQuote(chosen)
        return chosen
    }

    private fun rememberQuote(quote: String) {
        if (recentQuotesHistory.size >= 180) {
            recentQuotesHistory.removeFirst()
        }
        recentQuotesHistory.addLast(quote)
    }

    fun isRecentQuote(quote: String): Boolean {
        return recentQuotesHistory.contains(quote)
    }

    /**
     * Ensures quotes fit within 2 compact display lines and never exceed max 72 characters or get cut off mid-word.
     */
    fun formatCleanCompleteSentenceQuote(raw: String, maxChars: Int = 72): String {
        if (raw.isBlank()) return "⚡ Flutter Mane has a Base Speed stat of 135!"

        // 1. Clean up line breaks, excess whitespace, and wrapping quotation marks
        var clean = raw.trim()
            .replace("\r\n", " ")
            .replace("\n", " ")
            .replace("  ", " ")
            .trim()

        // Remove speech bubble emoji or quote symbols if present
        if (clean.startsWith("💬")) {
            clean = clean.substring(1).trim()
        }
        if ((clean.startsWith("\"") && clean.endsWith("\"")) || (clean.startsWith("“") && clean.endsWith("”"))) {
            clean = clean.substring(1, clean.length - 1).trim()
        }
        if (clean.startsWith("\"") || clean.startsWith("“")) {
            clean = clean.substring(1).trim()
        }
        if (clean.endsWith("\"") || clean.endsWith("”")) {
            clean = clean.substring(0, clean.length - 1).trim()
        }

        if (clean.isBlank()) return "⚡ Flutter Mane has a Base Speed stat of 135!"

        // 2. If clean string fits within maxChars, ensure clean ending symbol and return
        if (clean.length <= maxChars) {
            val lastChar = clean.last()
            val cleanEndsWithEmoji = clean.endsWith("✨") || clean.endsWith("🔥") || 
                    clean.endsWith("💖") || clean.endsWith("🟢") || clean.endsWith("💬") ||
                    clean.endsWith("💎") || clean.endsWith("🪵") || clean.endsWith("🙈") ||
                    clean.endsWith("⛵") || clean.endsWith("🍖") || clean.endsWith("🪓")
            return if (lastChar == '.' || lastChar == '!' || lastChar == '?' || lastChar == ')' || cleanEndsWithEmoji) {
                clean
            } else {
                "$clean!"
            }
        }

        // 3. String is longer than maxChars: extract the first complete sentence ending with . ! or ?
        val sentenceEnds = listOf('.', '!', '?')
        var bestEndIndex = -1

        for (i in 10 until clean.length.coerceAtMost(maxChars)) {
            if (clean[i] in sentenceEnds) {
                val isNextBoundary = (i + 1 >= clean.length) || clean[i + 1].isWhitespace() || clean[i + 1] in sentenceEnds
                if (isNextBoundary) {
                    bestEndIndex = i
                }
            }
        }

        if (bestEndIndex != -1) {
            return clean.substring(0, bestEndIndex + 1).trim()
        }

        // 4. If no sentence boundary within maxChars, trim to the last full word before maxChars - 1 (never mid-word)
        val searchArea = clean.substring(0, maxChars - 1)
        val lastSpace = searchArea.lastIndexOf(' ')
        if (lastSpace >= 10) {
            val wordTrimmed = searchArea.substring(0, lastSpace).trim()
            val lastChar = wordTrimmed.lastOrNull() ?: '!'
            val endSymbol = if (lastChar in sentenceEnds || lastChar == ')') "" else "!"
            return "$wordTrimmed$endSymbol"
        }

        return searchArea.trim()
    }
}


