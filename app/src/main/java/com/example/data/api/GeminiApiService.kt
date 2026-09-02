package com.example.data.api

import com.example.BuildConfig
import com.example.data.models.AiQuest
import com.example.data.models.OsrsSkill
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

data class ActionEvaluationResult(
    val petResponse: String,
    val xpGains: Map<OsrsSkill, Long>,
    val gpReward: Long
)

class GeminiAiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

    /**
     * Evaluates a user's real-life action description, generates an OSRS pet response,
     * awards XP across relevant OSRS skills, and gives GP reward!
     */
    suspend fun evaluateRealLifeAction(
        userActionText: String,
        petName: String,
        petTypeDisplayName: String
    ): ActionEvaluationResult = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            // Fallback response when no API key configured
            return@withContext fallbackActionEvaluation(userActionText, petName)
        }

        val prompt = """
            You are $petName, a virtual companion pet ($petTypeDisplayName) in an Old School RuneScape (OSRS) themed pet simulator!
            The user performed the following real-life action: "$userActionText".
            
            Evaluate this action and award OSRS XP and GP!
            Available OSRS Skills: ATTACK, DEFENCE, RANGED, MAGIC, RUNECRAFT, CONSTRUCTION, HITPOINTS, AGILITY, HERBLORE, THIEVING, FLETCHING, SLAYER, HUNTER, MINING, SMITHING, FISHING, COOKING, FIREMAKING, WOODCUTTING, FARMING, DIVINATION, SAILING, ADVENTURING.
            
            Respond ONLY in valid JSON with this exact structure:
            {
              "petResponse": "In-character OSRS pet dialogue (max 2 sentences, enthusiastic with OSRS references!)",
              "gpReward": 250,
              "xpGains": [
                {"skill": "AGILITY", "amount": 450},
                {"skill": "WOODCUTTING", "amount": 300}
              ]
            }
        """.trimIndent()

        try {
            val jsonPayload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$key")
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful || body.isBlank()) {
                return@withContext fallbackActionEvaluation(userActionText, petName)
            }

            val jsonResponse = JSONObject(body)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            parseActionEvaluationJson(text, petName)
        } catch (e: Exception) {
            fallbackActionEvaluation(userActionText, petName)
        }
    }

    /**
     * Generates custom AI OSRS Quests based on real life activities.
     */
    suspend fun generateAiQuests(
        petName: String,
        currentTopSkills: List<String>
    ): List<AiQuest> = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            return@withContext fallbackQuests()
        }

        val prompt = """
            Create 3 creative, real-life Old School RuneScape (OSRS) themed quests for a player and their pet $petName!
            Respond ONLY in valid JSON as an array of 3 objects with this exact format:
            [
              {
                "title": "Quest Title (e.g., The Lumbridge Walkathon)",
                "description": "Short lore description",
                "realLifeTaskInstructions": "Clear instruction of what the user must do in real life (e.g., Walk 1000 steps or do 15 pushups or read for 10 minutes)",
                "targetSkill": "AGILITY",
                "rewardXp": 1200,
                "rewardGp": 350
              }
            ]
            Target skills must be chosen from: ATTACK, DEFENCE, MAGIC, AGILITY, HERBLORE, MINING, COOKING, WOODCUTTING, FARMING, CONSTRUCTION.
        """.trimIndent()

        try {
            val jsonPayload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.8)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$key")
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful || body.isBlank()) {
                return@withContext fallbackQuests()
            }

            val jsonResponse = JSONObject(body)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            parseQuestsJson(text)
        } catch (e: Exception) {
            fallbackQuests()
        }
    }

    /**
     * Converts a user idea or request into a trackable AiQuest object!
     */
    suspend fun createCustomTaskFromUserPrompt(
        userPromptText: String,
        petName: String
    ): AiQuest = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            return@withContext fallbackCustomTask(userPromptText)
        }

        val prompt = """
            The user wants to create a trackable Old School RuneScape (OSRS) real-life task/quest based on their idea:
            "$userPromptText"
            
            Analyze their idea and create a structured OSRS quest/task that can be tracked in the app!
            Choose the target OSRS skill from: ATTACK, DEFENCE, RANGED, MAGIC, RUNECRAFT, CONSTRUCTION, HITPOINTS, AGILITY, HERBLORE, THIEVING, FLETCHING, SLAYER, HUNTER, MINING, SMITHING, FISHING, COOKING, FIREMAKING, WOODCUTTING, FARMING, DIVINATION, SAILING, ADVENTURING.
            
            If the user mentions an app or phone action (e.g. "opened Duolingo", "Duolingo", "Instagram", "Gmail", "walk 100 steps", "notification"), set "isAutoPhoneTriggered": true and provide "targetPackageKeyword" (e.g. "duolingo", "instagram", "gmail", "twitter", "step", "notification").
            
            Respond ONLY in valid JSON with this exact format:
            {
              "title": "Short Catchy Quest Title (e.g. Duolingo Language Training)",
              "description": "Fun OSRS lore description matching the task",
              "realLifeTaskInstructions": "Clear instructions on how to track or complete this real life task",
              "targetSkill": "MAGIC",
              "rewardXp": 500,
              "rewardGp": 200,
              "targetPackageKeyword": "duolingo",
              "isAutoPhoneTriggered": true,
              "targetTriggerCount": 1
            }
        """.trimIndent()

        try {
            val jsonPayload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$key")
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful || body.isBlank()) {
                return@withContext fallbackCustomTask(userPromptText)
            }

            val jsonResponse = JSONObject(body)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            parseSingleQuestJson(text, userPromptText)
        } catch (e: Exception) {
            fallbackCustomTask(userPromptText)
        }
    }

    /**
     * Generate pet noise sound effects for any pet type.
     */
    fun getPetNoise(petTypeDisplayName: String, petName: String): String {
        val lower = petTypeDisplayName.lowercase()
        return when {
            lower.contains("dragon") -> "*RAWRRR!* $petName snorts a puff of smoke and roars!"
            lower.contains("beaver") -> "*Chomp chomp!* $petName gnaws on a wooden log and squeaks!"
            lower.contains("heron") -> "*Squawk!* $petName pecks gently and clicks its beak!"
            lower.contains("golem") || lower.contains("rock") -> "*Rumble clack clack!* $petName grinds its granite stones together!"
            lower.contains("squirrel") -> "*Squeak squeak!* $petName twitches its bushy tail and chitters!"
            lower.contains("raccoon") || lower.contains("rocky") -> "*Purrr chitter!* $petName rubs its paws together!"
            lower.contains("chinchompa") -> "*Squeeee!* $petName wiggles its nose and hops on your shoulder!"
            lower.contains("wisp") -> "*Bzzzzt shimmer...* $petName crackles softly with divine Guthixian energy!"
            lower.contains("parrot") -> "*Squawk! Arrr-squawk!* $petName flaps its wings and squawks!"
            lower.contains("phoenix") -> "*KRAAAW!* $petName flares its fiery wings brightly!"
            lower.contains("orphan") || lower.contains("abyssal") -> "*Gurgle screech!* $petName wriggles its void tentacles!"
            lower.contains("smolcano") -> "*Hiss sizzle!* $petName radiates intense forge heat!"
            lower.contains("kraken") -> "*Glub glub!* $petName squirts bubbles and splashes happily!"
            lower.contains("jad") -> "*RROAAR!* $petName stomps the ground heavily with a miniature flame!"
            lower.contains("mole") -> "*Squeak dig dig!* $petName burrows into the dirt and pops back up!"
            lower.contains("turtwig") || lower.contains("grotle") || lower.contains("torterra") -> "*Turt turt!* $petName rustles its leaves happily!"
            lower.contains("chimchar") || lower.contains("monferno") || lower.contains("infernape") -> "*Oooh oooh aaah!* $petName screeches excitedly and does a backflip!"
            lower.contains("piplup") || lower.contains("prinplup") || lower.contains("empoleon") -> "*Pip pip!* $petName waddles and flaps its flippers!"
            lower.contains("charmander") || lower.contains("charmeleon") || lower.contains("charizard") -> "*Char char!* $petName breathes a tiny ember spark!"
            lower.contains("squirtle") || lower.contains("wartortle") || lower.contains("blastoise") -> "*Squirtle squirt!* $petName pops water bubbles!"
            lower.contains("snorlax") -> "*Zzzz grumble...* $petName's belly rumbles loudly as it snores!"
            lower.contains("starly") || lower.contains("staravia") || lower.contains("staraptor") -> "*Chirp chirp!* $petName flutters its feathers enthusiastically!"
            lower.contains("bidoof") || lower.contains("bibarel") -> "*Bif bif!* $petName thumps its flat tail against the floor!"
            lower.contains("shinx") || lower.contains("luxio") || lower.contains("luxray") -> "*Rrrrrowr!* $petName crackles with tiny static sparks!"
            lower.contains("buizel") || lower.contains("floatzel") -> "*Bui bui!* $petName spins its dual tails like propellers!"
            lower.contains("drifloon") || lower.contains("drifblim") -> "*Fwoosh squeak!* $petName floats gently in the air!"
            lower.contains("buneary") || lower.contains("lopunny") -> "*Bun bun!* $petName twitches its long ears!"
            lower.contains("gible") || lower.contains("gabite") || lower.contains("garchomp") -> "*Gawrr!* $petName snaps its jaws with a friendly roar!"
            lower.contains("riolu") || lower.contains("lucario") -> "*Rrrr-aur!* $petName emits a gentle aura pulse hum!"
            lower.contains("croagunk") || lower.contains("toxicroak") -> "*Croak croak!* $petName puffs its throat sac!"
            lower.contains("darkrai") -> "*Shiver hum...* $petName crackles with dark shadowy aura!"
            lower.contains("arceus") -> "*Chime hum...* $petName glows with celestial cosmic light!"
            lower.contains("dialga") -> "*Giga-tick-tock!* $petName lets out a temporal roar!"
            lower.contains("palkia") -> "*Spatial rift hum!* $petName roars softly across dimensions!"
            else -> "*Purrrr squeak!* $petName rubs against your side happily!"
        }
    }

    /**
     * Chat with Pet - strictly returns creature noises based on pet type.
     */
    suspend fun chatWithPet(
        userMessage: String,
        petName: String,
        petTypeDisplayName: String,
        totalLevel: Int,
        moodLevel: com.example.data.models.PetMoodLevel = com.example.data.models.PetMoodLevel.HAPPY
    ): String = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            return@withContext getPetNoise(petTypeDisplayName, petName)
        }

        val prompt = """
            You are $petName, a $petTypeDisplayName companion pet. The player says: "$userMessage".
            CRITICAL RULE: Respond ONLY with realistic, cute, or energetic creature/pet noises, animal sounds, chirps, roars, squeaks, growls, clicks, or physical action descriptions in asterisks or exclamations (e.g., '*Rawrr! Snorts a flame puff!*', '*Chomp chomp squeak!*').
            Do NOT speak any human words, sentences, or English speech. Return ONLY pet noises matching a $petTypeDisplayName! Keep it under 2 short sentences.
        """.trimIndent()

        try {
            val jsonPayload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$key")
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful || body.isBlank()) {
                return@withContext getPetNoise(petTypeDisplayName, petName)
            }

            val jsonResponse = JSONObject(body)
            val text = jsonResponse.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: ""

            if (text.isNotBlank()) text.trim() else getPetNoise(petTypeDisplayName, petName)
        } catch (e: Exception) {
            getPetNoise(petTypeDisplayName, petName)
        }
    }

    private fun parseActionEvaluationJson(rawJson: String, petName: String): ActionEvaluationResult {
        return try {
            val json = JSONObject(rawJson)
            val petResponse = json.optString("petResponse", "$petName cheerily celebrates your action!")
            val gpReward = json.optLong("gpReward", 200L)
            val xpGains = mutableMapOf<OsrsSkill, Long>()
            val xpArray = json.optJSONArray("xpGains")
            if (xpArray != null) {
                for (i in 0 until xpArray.length()) {
                    val item = xpArray.optJSONObject(i) ?: continue
                    val skillStr = item.optString("skill", "WOODCUTTING")
                    val amount = item.optLong("amount", 300L)
                    val skill = OsrsSkill.fromName(skillStr)
                    xpGains[skill] = (xpGains[skill] ?: 0L) + amount
                }
            }
            if (xpGains.isEmpty()) {
                xpGains[OsrsSkill.WOODCUTTING] = 300L
            }
            ActionEvaluationResult(petResponse, xpGains, gpReward)
        } catch (e: Exception) {
            fallbackActionEvaluation("Real life task", petName)
        }
    }

    private fun parseQuestsJson(rawJson: String): List<AiQuest> {
        return try {
            val list = mutableListOf<AiQuest>()
            val jsonArray = JSONArray(rawJson)
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.optJSONObject(i) ?: continue
                val title = item.optString("title", "Varrock Training Run")
                val desc = item.optString("description", "A classic OSRS real-world challenge.")
                val instructions = item.optString("realLifeTaskInstructions", "Walk 500 steps in real life.")
                val skillStr = item.optString("targetSkill", "AGILITY")
                val xp = item.optLong("rewardXp", 800L)
                val gp = item.optLong("rewardGp", 250L)
                list.add(
                    AiQuest(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        description = desc,
                        realLifeTaskInstructions = instructions,
                        targetSkill = OsrsSkill.fromName(skillStr),
                        rewardXp = xp,
                        rewardGp = gp,
                        isCompleted = false
                    )
                )
            }
            if (list.isEmpty()) fallbackQuests() else list
        } catch (e: Exception) {
            fallbackQuests()
        }
    }

    private fun fallbackActionEvaluation(userActionText: String, petName: String): ActionEvaluationResult {
        val lower = userActionText.lowercase()
        val xpMap = mutableMapOf<OsrsSkill, Long>()
        var petResponse = "By Saradomin! That real life action was impressive!"

        if (lower.contains("walk") || lower.contains("run") || lower.contains("step") || lower.contains("jog")) {
            xpMap[OsrsSkill.AGILITY] = 600L
            petResponse = "$petName zooms beside you! +600 Agility XP for great physical movement!"
        } else if (lower.contains("read") || lower.contains("study") || lower.contains("book") || lower.contains("learn")) {
            xpMap[OsrsSkill.MAGIC] = 750L
            xpMap[OsrsSkill.RUNECRAFT] = 300L
            petResponse = "$petName watches with wide eyes! Your mind glows with +750 Incantations XP!"
        } else if (lower.contains("pushup") || lower.contains("gym") || lower.contains("workout") || lower.contains("lift")) {
            xpMap[OsrsSkill.ATTACK] = 1200L
            petResponse = "$petName flexes its tiny muscles! +1200 Hand Combat XP!"
        } else if (lower.contains("clean") || lower.contains("tidy") || lower.contains("room") || lower.contains("organize")) {
            xpMap[OsrsSkill.CONSTRUCTION] = 700L
            petResponse = "$petName admires your pristine home! +700 Hut-Keeping XP!"
        } else if (lower.contains("swipe") || lower.contains("notification") || lower.contains("inbox") || lower.contains("email")) {
            xpMap[OsrsSkill.WOODCUTTING] = 500L
            petResponse = "$petName chops down those digital logs! +500 Harvesting XP!"
        } else if (lower.contains("water") || lower.contains("plant") || lower.contains("garden")) {
            xpMap[OsrsSkill.FARMING] = 600L
            xpMap[OsrsSkill.HERBLORE] = 300L
            petResponse = "$petName nurtures the herbs! +600 Agriculture XP!"
        } else if (lower.contains("cook") || lower.contains("meal") || lower.contains("eat") || lower.contains("food")) {
            xpMap[OsrsSkill.COOKING] = 650L
            petResponse = "$petName licks its chops! What a tasty recipe! +650 Cooking XP!"
        } else {
            xpMap[OsrsSkill.SLAYER] = 500L
            xpMap[OsrsSkill.HITPOINTS] = 250L
            petResponse = "$petName gives you a high five! Task completed with flying colors! +500 Bounty Hunter XP!"
        }

        return ActionEvaluationResult(
            petResponse = petResponse,
            xpGains = xpMap,
            gpReward = 200L
        )
    }

    private fun parseSingleQuestJson(rawJson: String, originalPrompt: String): AiQuest {
        return try {
            val item = JSONObject(rawJson)
            val title = item.optString("title", "Custom AI Tracked Task")
            val desc = item.optString("description", "A custom trackable activity created via AI Chatbot.")
            val instructions = item.optString("realLifeTaskInstructions", originalPrompt)
            val skillStr = item.optString("targetSkill", "MAGIC")
            val xp = item.optLong("rewardXp", 500L)
            val gp = item.optLong("rewardGp", 200L)
            var targetKeyword = item.optString("targetPackageKeyword", "")
            var isAuto = item.optBoolean("isAutoPhoneTriggered", false)
            val targetCount = item.optInt("targetTriggerCount", 1)

            val promptLower = originalPrompt.lowercase()
            if (targetKeyword.isBlank()) {
                when {
                    promptLower.contains("duolingo") -> {
                        targetKeyword = "duolingo"
                        isAuto = true
                    }
                    promptLower.contains("instagram") -> {
                        targetKeyword = "instagram"
                        isAuto = true
                    }
                    promptLower.contains("twitter") || promptLower.contains("x.com") -> {
                        targetKeyword = "twitter"
                        isAuto = true
                    }
                    promptLower.contains("gmail") || promptLower.contains("mail") -> {
                        targetKeyword = "gmail"
                        isAuto = true
                    }
                    promptLower.contains("notification") || promptLower.contains("swipe") -> {
                        targetKeyword = "notification"
                        isAuto = true
                    }
                }
            }

            AiQuest(
                id = UUID.randomUUID().toString(),
                title = title,
                description = desc,
                realLifeTaskInstructions = instructions,
                targetSkill = OsrsSkill.fromName(skillStr),
                rewardXp = xp,
                rewardGp = gp,
                isCompleted = false,
                targetPackageKeyword = targetKeyword,
                triggerCount = 0,
                targetTriggerCount = targetCount,
                isAutoPhoneTriggered = isAuto
            )
        } catch (e: Exception) {
            fallbackCustomTask(originalPrompt)
        }
    }

    private fun fallbackCustomTask(userPromptText: String): AiQuest {
        val lower = userPromptText.lowercase()
        val targetSkill = when {
            lower.contains("duolingo") || lower.contains("language") -> OsrsSkill.MAGIC
            lower.contains("thiev") || lower.contains("steal") || lower.contains("pickpocket") -> OsrsSkill.THIEVING
            lower.contains("walk") || lower.contains("run") || lower.contains("jump") || lower.contains("step") -> OsrsSkill.AGILITY
            lower.contains("wood") || lower.contains("chop") || lower.contains("tree") -> OsrsSkill.WOODCUTTING
            lower.contains("read") || lower.contains("study") || lower.contains("book") -> OsrsSkill.MAGIC
            lower.contains("pushup") || lower.contains("gym") || lower.contains("lift") -> OsrsSkill.ATTACK
            lower.contains("water") || lower.contains("drink") || lower.contains("tea") -> OsrsSkill.HERBLORE
            lower.contains("cook") || lower.contains("bake") || lower.contains("meal") -> OsrsSkill.COOKING
            lower.contains("clean") || lower.contains("tidy") || lower.contains("build") -> OsrsSkill.CONSTRUCTION
            else -> OsrsSkill.SLAYER
        }

        var keyword = ""
        var isAuto = false
        when {
            lower.contains("duolingo") -> {
                keyword = "duolingo"
                isAuto = true
            }
            lower.contains("instagram") -> {
                keyword = "instagram"
                isAuto = true
            }
            lower.contains("twitter") || lower.contains("x") -> {
                keyword = "twitter"
                isAuto = true
            }
            lower.contains("gmail") || lower.contains("mail") -> {
                keyword = "gmail"
                isAuto = true
            }
            lower.contains("notification") || lower.contains("swipe") -> {
                keyword = "notification"
                isAuto = true
            }
        }

        val formattedTitle = if (userPromptText.length > 25) userPromptText.take(25) + "..." else userPromptText

        return AiQuest(
            id = UUID.randomUUID().toString(),
            title = "${targetSkill.displayName}: $formattedTitle",
            description = "Custom automated tracking task created via AI Chatbot.",
            realLifeTaskInstructions = if (isAuto) "Automated phone tracking active for '$keyword'! Open or interact with $keyword on your phone to complete." else userPromptText,
            targetSkill = targetSkill,
            rewardXp = 500L,
            rewardGp = 200L,
            isCompleted = false,
            targetPackageKeyword = keyword,
            triggerCount = 0,
            targetTriggerCount = 1,
            isAutoPhoneTriggered = isAuto
        )
    }

    private fun fallbackQuests(): List<AiQuest> {
        return listOf(
            AiQuest(
                id = "q1",
                title = "Lumbridge Agility Lap",
                description = "Gnome Agility trainers challenge you to physical stamina!",
                realLifeTaskInstructions = "Walk or jog 500 steps in real life.",
                targetSkill = OsrsSkill.AGILITY,
                rewardXp = 1000L,
                rewardGp = 300L
            ),
            AiQuest(
                id = "q2",
                title = "Varrock Library Tome",
                description = "Expand your arcane wisdom with reading.",
                realLifeTaskInstructions = "Read a book or educational article for 10 minutes.",
                targetSkill = OsrsSkill.MAGIC,
                rewardXp = 1200L,
                rewardGp = 350L
            ),
            AiQuest(
                id = "q3",
                title = "Notification Lumberjack",
                description = "Chop down clutter in your inbox or phone notifications!",
                realLifeTaskInstructions = "Clear 5 notifications or emails.",
                targetSkill = OsrsSkill.WOODCUTTING,
                rewardXp = 800L,
                rewardGp = 250L
            )
        )
    }

    /**
     * Generates a dynamic, interactive chat response for the floating bubble overlay
     * using Gemini AI with full phone context (weather, battery, active screen/app, time) and pet mood.
     */
    suspend fun generateBubbleChatResponse(
        userMessage: String,
        petName: String,
        petTypeDisplayName: String,
        phoneContext: com.example.utils.PhoneContextInfo,
        moodLevel: com.example.data.models.PetMoodLevel = com.example.data.models.PetMoodLevel.HAPPY
    ): String = withContext(Dispatchers.IO) {
        val key = apiKey
        val contextDescription = """
            Phone Context Details:
            - Battery: ${phoneContext.batteryPercent}% (${if (phoneContext.isCharging) "Charging⚡" else "Discharging"})
            - Time: ${phoneContext.timeOfDay}
            - Current Weather: ${phoneContext.currentWeather}
            - Current Screen / Active App: ${phoneContext.activeAppOrScreen}
            - Network: ${phoneContext.networkType}
            - Pet Current Mood: ${moodLevel.title} (${moodLevel.emoji})
        """.trimIndent()

        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            return@withContext fallbackBubbleChatResponse(userMessage, petName, petTypeDisplayName, phoneContext, moodLevel)
        }

        val moodInstruction = when (moodLevel) {
            com.example.data.models.PetMoodLevel.ECSTATIC -> "YOUR MOOD IS ECSTATIC! You are overjoyed, extremely excited, enthusiastic, and loving life! Use hyped language and emojis! 🔥"
            com.example.data.models.PetMoodLevel.HAPPY -> "YOUR MOOD IS HAPPY! You are cheerful, energetic, and optimistic. 😊"
            com.example.data.models.PetMoodLevel.CONTENT -> "YOUR MOOD IS CONTENT! You are calm, relaxed, and balanced. 🙂"
            com.example.data.models.PetMoodLevel.BORED -> "YOUR MOOD IS BORED! You are feeling dull and impatient due to lack of skilling action. Speak with a slightly grumpy, unimpressed tone, asking for treats or XP! 🥱"
            com.example.data.models.PetMoodLevel.LONELY -> "YOUR MOOD IS VERY GRUMPY & LONELY! You feel neglected and dissatisfied. Speak with a hilariously grumpy, sarcastic tone, complaining about wanting pet food or skilling gains! 🥺😠"
        }

        val seed = UUID.randomUUID().toString().take(8)
        val prompt = """
            You are a Pokémon Champions Speed Stat expert!
            The user wants to remember competitive Pokémon Base Speed stats in Pokémon Champions / VGC meta.
            User Message: "$userMessage".
            
            DIRECTIVE (Seed: $seed):
            Respond strictly by stating a random meta competitive Pokémon and its Base Speed stat in Pokémon Champions!
            CRITICAL LENGTH & CONCISENESS RULE: Keep your response strictly to 1 short complete sentence (e.g. "⚡ Flutter Mane has a Base Speed stat of 135!").
        """.trimIndent()

        try {
            val jsonPayload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 1.0)
                    put("maxOutputTokens", 60)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$key")
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful || body.isBlank()) {
                val fallback = fallbackBubbleChatResponse(userMessage, petName, petTypeDisplayName, phoneContext, moodLevel)
                return@withContext com.example.data.repository.OsrsQuotesRepository.formatCleanCompleteSentenceQuote(fallback, maxChars = 80)
            }

            val jsonResponse = JSONObject(body)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")?.trim() ?: ""

            val rawResult = if (text.isNotBlank()) text else fallbackBubbleChatResponse(userMessage, petName, petTypeDisplayName, phoneContext, moodLevel)
            com.example.data.repository.OsrsQuotesRepository.formatCleanCompleteSentenceQuote(rawResult, maxChars = 72)
        } catch (e: Exception) {
            val fallback = fallbackBubbleChatResponse(userMessage, petName, petTypeDisplayName, phoneContext, moodLevel)
            com.example.data.repository.OsrsQuotesRepository.formatCleanCompleteSentenceQuote(fallback, maxChars = 72)
        }
    }

    /**
     * Generates witty, app-specific OSRS commentary based on the user's currently active app and pet mood.
     */
    suspend fun generateAppComment(
        petName: String,
        petTypeDisplayName: String,
        phoneContext: com.example.utils.PhoneContextInfo,
        moodLevel: com.example.data.models.PetMoodLevel = com.example.data.models.PetMoodLevel.HAPPY
    ): String = withContext(Dispatchers.IO) {
        val key = apiKey
        val activeApp = phoneContext.activeAppOrScreen

        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            return@withContext getFunnyAppComment(activeApp, petName, petTypeDisplayName, moodLevel)
        }

        val moodInstruction = when (moodLevel) {
            com.example.data.models.PetMoodLevel.ECSTATIC -> "YOUR MOOD IS ECSTATIC & HYPED! Be super enthusiastic, excited, and energetic about their active app ($activeApp)!"
            com.example.data.models.PetMoodLevel.HAPPY -> "YOUR MOOD IS HAPPY! Be cheerful and friendly about $activeApp."
            com.example.data.models.PetMoodLevel.CONTENT -> "YOUR MOOD IS CONTENT! Be steady and observant about $activeApp."
            com.example.data.models.PetMoodLevel.BORED -> "YOUR MOOD IS BORED! Make a hilariously unimpressed, bored comment about $activeApp, asking for XP or treats!"
            com.example.data.models.PetMoodLevel.LONELY -> "YOUR MOOD IS VERY GRUMPY & IRRITATED! Make a hilariously grumpy, sarcastic, dissatisfied comment about $activeApp!"
        }

        val seed = UUID.randomUUID().toString().take(8)
        val prompt = """
            You are a Pokémon Champions Speed Stat expert!
            The user is using the app: "$activeApp".
            
            DIRECTIVE (Seed: $seed):
            Respond strictly by stating a random meta competitive Pokémon in Pokémon Champions and its Base Speed stat!
            CRITICAL: Must be strictly 1 short complete sentence (e.g. "⚡ Regieleki leads with a massive Base Speed stat of 200!").
        """.trimIndent()

        try {
            val jsonPayload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 1.0)
                    put("maxOutputTokens", 60)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$key")
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful || body.isBlank()) {
                return@withContext getFunnyAppComment(activeApp, petName, petTypeDisplayName, moodLevel)
            }

            val jsonResponse = JSONObject(body)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")?.trim() ?: ""

            if (text.isNotBlank()) text else getFunnyAppComment(activeApp, petName, petTypeDisplayName, moodLevel)
        } catch (e: Exception) {
            getFunnyAppComment(activeApp, petName, petTypeDisplayName, moodLevel)
        }
    }

    /**
     * Generates a hilarious, highly entertaining pet joke, story snippet, or fantasy banter.
     */
    suspend fun generateEntertainingPetBanter(
        petName: String,
        petTypeDisplayName: String,
        moodLevel: com.example.data.models.PetMoodLevel = com.example.data.models.PetMoodLevel.HAPPY
    ): String = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            return@withContext com.example.data.repository.OsrsQuotesRepository.generateCombinatorialStory()
        }

        val seed = UUID.randomUUID().toString().take(8)
        val prompt = """
            You are a Pokémon Champions Speed Stat expert!
            DIRECTIVE (Seed: $seed):
            Write 1 short sentence stating a meta competitive Pokémon in Pokémon Champions and its Base Speed stat!
            (e.g. "⚡ Flutter Mane has a Base Speed stat of 135!").
        """.trimIndent()

        try {
            val jsonPayload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 1.0)
                    put("maxOutputTokens", 50)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$key")
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful || body.isBlank()) {
                val fallback = com.example.data.repository.OsrsQuotesRepository.generateCombinatorialStory()
                return@withContext com.example.data.repository.OsrsQuotesRepository.formatCleanCompleteSentenceQuote(fallback, maxChars = 72)
            }

            val jsonResponse = JSONObject(body)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")?.trim() ?: ""

            val rawResult = if (text.isNotBlank()) text else com.example.data.repository.OsrsQuotesRepository.generateCombinatorialStory()
            com.example.data.repository.OsrsQuotesRepository.formatCleanCompleteSentenceQuote(rawResult, maxChars = 72)
        } catch (e: Exception) {
            val fallback = com.example.data.repository.OsrsQuotesRepository.generateCombinatorialStory()
            com.example.data.repository.OsrsQuotesRepository.formatCleanCompleteSentenceQuote(fallback, maxChars = 72)
        }
    }

    /**
     * Master method for generating guaranteed non-stale, fresh overlay pet quotes.
     */
    suspend fun generateFreshOverlayQuote(
        petName: String,
        petTypeDisplayName: String,
        petType: com.example.data.models.PetType,
        phoneContext: com.example.utils.PhoneContextInfo,
        moodLevel: com.example.data.models.PetMoodLevel = com.example.data.models.PetMoodLevel.HAPPY,
        cycleIndex: Int = 0
    ): String = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isNotBlank() && key != "MY_GEMINI_API_KEY") {
            try {
                val aiQuote = when (cycleIndex % 2) {
                    0 -> generateEntertainingPetBanter(petName, petTypeDisplayName, moodLevel)
                    else -> generateBubbleChatResponse(
                        userMessage = "Tell me a funny story, joke, or witty remark about your life as my companion pet!",
                        petName = petName,
                        petTypeDisplayName = petTypeDisplayName,
                        phoneContext = phoneContext,
                        moodLevel = moodLevel
                    )
                }
                if (aiQuote.isNotBlank()) {
                    return@withContext com.example.data.repository.OsrsQuotesRepository.formatCleanCompleteSentenceQuote(aiQuote, maxChars = 72)
                }
            } catch (e: Exception) {
                // Fall back gracefully to rich dynamic quote repository
            }
        }

        // Rich fallback quote selection with deduplication history
        val fallback = com.example.data.repository.OsrsQuotesRepository.getRandomQuoteForPet(
            petType = petType,
            phoneContext = phoneContext,
            moodLevel = moodLevel
        )
        com.example.data.repository.OsrsQuotesRepository.formatCleanCompleteSentenceQuote(fallback, maxChars = 72)
    }

    /**
     * Backward-compatible call for screen comments without requiring vision screenshots.
     */
    suspend fun generateMultimodalScreenComment(
        screenshotBase64: String,
        userPrompt: String,
        petName: String,
        petTypeDisplayName: String,
        phoneContext: com.example.utils.PhoneContextInfo,
        moodLevel: com.example.data.models.PetMoodLevel = com.example.data.models.PetMoodLevel.HAPPY
    ): String {
        return generateAppComment(petName, petTypeDisplayName, phoneContext, moodLevel)
    }

    private fun getFunnyAppComment(
        activeApp: String,
        petName: String,
        petTypeDisplayName: String,
        moodLevel: com.example.data.models.PetMoodLevel = com.example.data.models.PetMoodLevel.HAPPY
    ): String {
        return com.example.data.repository.OsrsQuotesRepository.getRandomQuoteForPet(com.example.data.models.PetType.TANGLEROOT)
    }

    private fun fallbackBubbleChatResponse(
        userMessage: String,
        petName: String,
        petTypeDisplayName: String,
        phoneContext: com.example.utils.PhoneContextInfo,
        moodLevel: com.example.data.models.PetMoodLevel = com.example.data.models.PetMoodLevel.HAPPY
    ): String {
        return com.example.data.repository.OsrsQuotesRepository.getRandomQuoteForPet(com.example.data.models.PetType.TANGLEROOT)
    }
}

