package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

data class QuestSaga(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val questIds: List<String>,
    val highlightDescription: String,
    val category: String = "Major Canonical Sagas"
)

object QuestRelationshipData {

    val ALL_SAGAS = listOf(
        QuestSaga(
            id = "saga_elven",
            title = "Crystal Sanctuary & Prismatic Spirits",
            subtitle = "From Emerald Weald to the Crystal Worldtree",
            emoji = "💎",
            questIds = listOf(
                "quest_plague_city",
                "quest_biohazard",
                "quest_underground_pass",
                "quest_regicide",
                "quest_roving_elves",
                "quest_mournings_end_part_1",
                "quest_mournings_end_part_2",
                "quest_song_of_the_elves"
            ),
            highlightDescription = "Unravel the shadow plague deception in Emerald Weald, journey through the treacherous Nether Abyss Pass, unite the Prismatic Spirit Guardians, and restore the legendary Crystal Sanctuary of Light!"
        ),
        QuestSaga(
            id = "saga_morytania",
            title = "Shadow Nether & Spectral Sovereigns",
            subtitle = "From Mist Boundary to the Dark Spire Citadel",
            emoji = "🦇",
            questIds = listOf(
                "quest_restless_ghost",
                "quest_priest_in_peril",
                "quest_nature_spirit",
                "quest_creature_of_fenkenstrain",
                "quest_in_search_of_myreque",
                "quest_in_aid_of_myreque",
                "quest_darkness_of_hallowvale",
                "quest_taste_of_hope",
                "quest_sins_of_the_father",
                "quest_night_at_the_theatre"
            ),
            highlightDescription = "Breach the ancient ward into the cursed Shadow Nether, join the underground Spirit Resistance, forge the Sunfire Blisterwood Relic, and banish the Shadow Sovereign Drakan!"
        ),
        QuestSaga(
            id = "saga_dragonkin",
            title = "Ancient Wyverns & Apex Dragon Spirits",
            subtitle = "From Isle of Ashes to the Primordial Dragon Forge",
            emoji = "🐲",
            questIds = listOf(
                "quest_cooks_assistant",
                "quest_knights_sword",
                "quest_dragon_slayer_1",
                "quest_heroes_quest",
                "quest_legends_quest",
                "quest_dragon_slayer_2"
            ),
            highlightDescription = "Conquer the ancient green wyvern spirit on Isle of Ashes, earn commendations in the Sovereign Guilds, and battle the colossal Apex Dragon Spirit Galvek atop the Primordial Forge!"
        ),
        QuestSaga(
            id = "saga_mahjarrat",
            title = "Great Desert Sands & Ancient Primordial Spirits",
            subtitle = "From Cascading Oasis to the Sunken Pyramids of Power",
            emoji = "🏜️",
            questIds = listOf(
                "quest_restless_ghost",
                "quest_waterfall_quest",
                "quest_tourist_trap",
                "quest_temple_of_ikov",
                "quest_desert_treasure_1",
                "quest_desert_treasure_2"
            ),
            highlightDescription = "Uncover the lost elemental prism diamonds across the Great Desert, master Ancient Spirit Incantations at the Sunken Pyramids, and confront the four Primordial Demigod Spirits in the Lost Vaults!"
        ),
        QuestSaga(
            id = "saga_gnome",
            title = "Elder Canopy & Primate Spirit Totems",
            subtitle = "From Sacred Worldtree Canopy to Primate Isle",
            emoji = "🌴",
            questIds = listOf(
                "quest_tree_gnome_village",
                "quest_grand_tree",
                "quest_monkey_madness_1",
                "quest_monkey_madness_2"
            ),
            highlightDescription = "Defend the sacred Elder Worldtree and Spirit Roots, infiltrate the wild Primate Isle with spirit greegree charms, and foil the corrupted warlord's primal titan army!"
        ),
        QuestSaga(
            id = "saga_culinary",
            title = "Feast of Spirits & Grand Shaman Banquet",
            subtitle = "From Hearth Kitchen to the Astral Spirit Feast",
            emoji = "🍲",
            questIds = listOf(
                "quest_cooks_assistant",
                "quest_fishing_contest",
                "quest_goblin_diplomacy",
                "quest_big_chompy_bird_hunting",
                "quest_recipe_for_disaster_part_1",
                "quest_recipe_for_disaster_full"
            ),
            highlightDescription = "Prepare celestial feasts for ancient spirits, free council dignitaries from stasis charms, and conquer the rogue Feast Glutton Spirit to unlock the legendary Shaman Master Gauntlets!"
        ),
        QuestSaga(
            id = "saga_fremennik",
            title = "Northern Isles & Totem Warrior Clan",
            subtitle = "From Rellek Elder Council to the Glacial Isle of Stone",
            emoji = "❄️",
            questIds = listOf(
                "quest_fremennik_trials",
                "quest_fremennik_isles",
                "quest_fremennik_exiles"
            ),
            highlightDescription = "Pass the 12 sacred initiation rites of the Northern Elder Council, aid the twin island chieftains against the Frost Titan King, and slay the Glacial Basilisk to forge the Totem Crest Crown!"
        ),
        QuestSaga(
            id = "saga_troll",
            title = "Mountain Titans & Alpine Shaman Herbalist",
            subtitle = "From Crag Summit to the Alpine Spirit Patches",
            emoji = "🏔️",
            questIds = listOf(
                "quest_death_plateau",
                "quest_troll_stronghold",
                "quest_edgars_ruse",
                "quest_my_arms_big_adventure",
                "quest_making_friends_with_my_arm"
            ),
            highlightDescription = "Infiltrate Crag Titan Stronghold to liberate captured scholars, help the adventurous mountain titan cultivate sacred alpine spirit herb patches, and establish peace across the frosty peaks!"
        ),
        QuestSaga(
            id = "saga_fairies",
            title = "Astral Spirit Glade & Fairy Rings",
            subtitle = "From Luminous Moonlit Glade to the Astral Spirit Network",
            emoji = "🧚",
            questIds = listOf(
                "quest_lost_city",
                "quest_nature_spirit",
                "quest_fairy_tale_1",
                "quest_fairy_tale_2",
                "quest_fairy_tale_3"
            ),
            highlightDescription = "Discover the hidden portal to the moon-lit fairy realm of Zanaris, rescue the Astral Queen from the root blight, and unlock the global teleport network of ancient Spirit Rings!"
        ),
        QuestSaga(
            id = "saga_temple_knights",
            title = "Order of Radiant Light & High Templars",
            subtitle = "From Shadow Citadel Watch to the High Round Table",
            emoji = "🛡️",
            questIds = listOf(
                "quest_black_knights_fortress",
                "quest_recruitment_drive",
                "quest_wanted",
                "quest_slug_menace",
                "quest_kings_ransom"
            ),
            highlightDescription = "Infiltrate and sabotage the Shadow Citadel, pass the sacred trials of the Order of Radiant Light, and defend the High Round Table in the Champion Waves!"
        ),
        QuestSaga(
            id = "saga_druidic",
            title = "Sacred Herbalism & Shamanic Purification",
            subtitle = "From Sacred Stone Circle to Shilo Spirit Grove",
            emoji = "🌿",
            questIds = listOf(
                "quest_druidic_ritual",
                "quest_jungle_potion",
                "quest_shilo_village",
                "quest_one_small_favour"
            ),
            highlightDescription = "Purify the ancient stone circle to master Sacred Herbalism, cleanse the jungle blight from tropical islands, and revitalize the sacred Shilo Spirit Village!"
        ),
        QuestSaga(
            id = "saga_elemental",
            title = "Elemental Matrix & Soul Infusion",
            subtitle = "From Primordial Kiln to Mind Matrix Attunement",
            emoji = "⚙️",
            questIds = listOf(
                "quest_elemental_workshop_1",
                "quest_elemental_workshop_2"
            ),
            highlightDescription = "Operate the ancient subterranean machinery beneath Seers' Grove to forge elemental spirit shields, mind helmets, and elemental armor sets!"
        ),
        QuestSaga(
            id = "saga_shadow",
            title = "Shadow Whispers & Cursed Desert Necropolis",
            subtitle = "From Sunken Mirage Citadel to the Ancient Tomb of Spirits",
            emoji = "💀",
            questIds = listOf(
                "quest_prince_ali_rescue",
                "quest_icthlarins_little_helper",
                "quest_contact",
                "quest_beneath_cursed_sands"
            ),
            highlightDescription = "Rescue the Desert Emissary from captivity, investigate the high priest intrigue of the Sunken Oasis, and brave the ancient Necropolis to unearth primordial guardian relics!"
        ),
        QuestSaga(
            id = "saga_shaman_kanto",
            title = "Shaman Path: Forest Realm",
            subtitle = "From Spirit Valley to Celestial Summit",
            emoji = "🌲",
            questIds = listOf(
                "tl_kanto_1_pallet_route_1",
                "tl_kanto_4_pewter_gym",
                "tl_kanto_7_cerulean_gym",
                "tl_kanto_11_vermilion_gym",
                "tl_kanto_14_celadon_gym",
                "tl_kanto_18_fuchsia_gym",
                "tl_kanto_20_saffron_gym",
                "tl_kanto_23_cinnabar_gym",
                "tl_kanto_24_viridian_gym",
                "tl_kanto_30_champion"
            ),
            highlightDescription = "Trek across the 10 Sacred Chapters of the Forest Realm, claim all 8 Spirit Obelisks, defeat the High Spirits, and become the League Champion!",
            category = "Shaman Path Series"
        ),
        QuestSaga(
            id = "saga_shaman_johto",
            title = "Shaman Path: River Realm",
            subtitle = "From Springside Trail to the Frost Peak Summit",
            emoji = "🌊",
            questIds = listOf(
                "tl_johto_1_new_bark",
                "tl_johto_3_violet_gym",
                "tl_johto_5_azalea_gym",
                "tl_johto_6_goldenrod_gym",
                "tl_johto_8_ecruteak_gym",
                "tl_johto_9_olivine_gym",
                "tl_johto_11_blackthorn_gym",
                "tl_johto_16_e4_lance",
                "tl_johto_17_champion_red"
            ),
            highlightDescription = "Follow the mystic rivers of Johto, conquer the Elder Tree Tower and Blackthorn Dragon Shrine, and duel the Legendary Champion atop Mt. Silver!",
            category = "Shaman Path Series"
        ),
        QuestSaga(
            id = "saga_shaman_hoenn",
            title = "Shaman Path: Volcanic Realm",
            subtitle = "From Littleroot Shore to Ever Grande Summit",
            emoji = "🌋",
            questIds = listOf(
                "tl_hoenn_1_littleroot",
                "tl_hoenn_3_rustboro_gym",
                "tl_hoenn_5_dewford_gym",
                "tl_hoenn_6_mauville_gym",
                "tl_hoenn_8_lavaridge_gym",
                "tl_hoenn_10_petalburg_gym",
                "tl_hoenn_12_fortree_gym",
                "tl_hoenn_14_mossdeep_gym",
                "tl_hoenn_16_sootopolis_gym",
                "tl_hoenn_22_champion_steven"
            ),
            highlightDescription = "Traverse the volcanic landscapes and oceanic routes of Hoenn, awaken the Primordial Weather Spirits at Sky Pillar, and defeat Champion Steven!",
            category = "Shaman Path Series"
        ),
        QuestSaga(
            id = "saga_shaman_sinnoh",
            title = "Shaman Path: Spirit Realm",
            subtitle = "From Twinleaf Haven to Spear Pillar Apex",
            emoji = "✨",
            questIds = listOf(
                "tl_sinnoh_1_twinleaf",
                "tl_sinnoh_3_oreburgh_gym",
                "tl_sinnoh_5_eterna_gym",
                "tl_sinnoh_7_veilstone_gym",
                "tl_sinnoh_8_pastoria_gym",
                "tl_sinnoh_10_hearthome_gym",
                "tl_sinnoh_12_canalave_gym",
                "tl_sinnoh_14_snowpoint_gym",
                "tl_sinnoh_16_sunyshore_gym",
                "tl_sinnoh_22_champion_cynthia"
            ),
            highlightDescription = "Journey across the ancient myths of Sinnoh, climb Mt. Coronet to the Spear Pillar, quell the Space-Time Primordials, and challenge Champion Cynthia!",
            category = "Shaman Path Series"
        )
    )

    // Build a master quest lookup across both OSRS quests and Trainer League quests
    fun getAllMasterQuests(): List<OsrsQuest> {
        val osrs = OsrsQuestData.QUESTS
        val kanto = TrainerLeagueData.KANTO_QUESTS
        val johto = TrainerLeagueData.JOHTO_QUESTS
        val hoenn = TrainerLeagueData.HOENN_QUESTS
        val sinnoh = TrainerLeagueData.SINNOH_QUESTS
        return (osrs + kanto + johto + hoenn + sinnoh).distinctBy { it.id }
    }

    fun getQuestById(questId: String): OsrsQuest? {
        return getAllMasterQuests().find { it.id == questId }
            ?: OsrsQuestData.QUESTS.find { it.id == questId }
            ?: TrainerLeagueData.getQuestById(questId)
    }

    // Direct downstream unlock mapping: Quest ID -> List of Quest IDs that require this quest
    fun getDirectUnlockMap(): Map<String, List<String>> {
        val allQuests = getAllMasterQuests()
        val unlockMap = mutableMapOf<String, MutableList<String>>()

        for (quest in allQuests) {
            for (reqId in quest.reqQuestIds) {
                unlockMap.getOrPut(reqId) { mutableListOf() }.add(quest.id)
            }
        }
        return unlockMap
    }

    // Direct prerequisite mapping: Quest ID -> List of Quest IDs that this quest requires
    fun getDirectPrereqMap(): Map<String, List<String>> {
        val allQuests = getAllMasterQuests()
        return allQuests.associate { it.id to it.reqQuestIds }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestRelationshipDialog(
    viewModel: PetViewModel,
    onDismiss: () -> Unit
) {
    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val activeQuestExpedition by viewModel.activeQuestExpedition.collectAsStateWithLifecycle()
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()

    val completedSet = petState.completedQuestIds.toSet()
    val allQuests = remember { QuestRelationshipData.getAllMasterQuests() }
    val directUnlockMap = remember { QuestRelationshipData.getDirectUnlockMap() }
    val directPrereqMap = remember { QuestRelationshipData.getDirectPrereqMap() }

    var selectedSagaIndex by remember { mutableIntStateOf(0) }
    var selectedQuestId by remember { mutableStateOf<String?>("quest_dragon_slayer_1") }
    var searchQuery by remember { mutableStateOf("") }
    var viewMode by remember { mutableStateOf("FLOW") } // "FLOW" or "MATRIX" or "EXPLORER"

    val currentSaga = QuestRelationshipData.ALL_SAGAS[selectedSagaIndex.coerceIn(0, QuestRelationshipData.ALL_SAGAS.size - 1)]

    // If search query is entered, match quests across the entire game
    val searchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else {
            allQuests.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.difficulty.displayName.contains(searchQuery, ignoreCase = true) ||
                (it.unlockedFeatures.any { feat -> feat.contains(searchQuery, ignoreCase = true) })
            }
        }
    }

    // Quests in current active view
    val displayQuests: List<OsrsQuest> = remember(selectedSagaIndex, searchQuery, searchResults) {
        if (searchQuery.isNotBlank()) {
            searchResults
        } else {
            currentSaga.questIds.mapNotNull { QuestRelationshipData.getQuestById(it) }
        }
    }

    val selectedQuest = remember(selectedQuestId) {
        selectedQuestId?.let { QuestRelationshipData.getQuestById(it) }
    }

    // Calculate progression stats for current saga
    val sagaTotalQuests = displayQuests.size
    val sagaCompletedQuests = displayQuests.count { completedSet.contains(it.id) }
    val sagaTotalQp = displayQuests.sumOf { it.questPoints }
    val sagaEarnedQp = displayQuests.filter { completedSet.contains(it.id) }.sumOf { it.questPoints }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = OsrsLeatherDark,
            border = BorderStroke(2.dp, OsrsGold),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // --- TOP HEADER BAR ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2B1F14),
                            border = BorderStroke(1.dp, OsrsGold),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🌳", fontSize = 20.sp)
                            }
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Quest Line & Dependency Visualizer",
                                    color = OsrsTextYellow,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF1E3A20),
                                    border = BorderStroke(1.dp, Color(0xFF4CAF50))
                                ) {
                                    Text(
                                        text = "$sagaCompletedQuests/$sagaTotalQuests Done",
                                        color = Color(0xFF81C784),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Track quest prerequisite chains, branches, and unlocks",
                                color = OsrsParchment.copy(alpha = 0.85f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("❌", fontSize = 14.sp)
                    }
                }

                // --- SAGA SELECTOR CHIPS ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuestRelationshipData.ALL_SAGAS.forEachIndexed { index, saga ->
                        val isSelected = selectedSagaIndex == index && searchQuery.isBlank()
                        val questList = saga.questIds.mapNotNull { QuestRelationshipData.getQuestById(it) }
                        val doneCount = questList.count { completedSet.contains(it.id) }
                        val isFinished = questList.isNotEmpty() && doneCount == questList.size

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedSagaIndex = index
                                searchQuery = ""
                                if (questList.isNotEmpty()) {
                                    selectedQuestId = questList.first().id
                                }
                            },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(saga.emoji, fontSize = 12.sp)
                                    Text(
                                        text = saga.title.split("&").first().trim(),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isFinished) {
                                        Text("✅", fontSize = 10.sp)
                                    } else {
                                        Text("($doneCount/${questList.size})", fontSize = 9.5.sp, color = if (isSelected) Color.Black else OsrsGold)
                                    }
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OsrsGold,
                                selectedLabelColor = Color.Black,
                                containerColor = OsrsLeatherMedium,
                                labelColor = Color.White
                            )
                        )
                    }
                }

                // --- SEARCH BAR & VIEW TOGGLES ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("quest_tree_search_input"),
                        placeholder = { Text("Search any quest or unlock (e.g. Dragon Slayer, Prifddinas)...", color = Color.Gray, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OsrsGold,
                            unfocusedBorderColor = Color(0xFF4A3B32),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // View Mode Switcher: Flowchart vs Matrix
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF241910),
                        border = BorderStroke(1.dp, Color(0xFF5C473A)),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Button(
                                onClick = { viewMode = "FLOW" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (viewMode == "FLOW") OsrsGold else Color.Transparent,
                                    contentColor = if (viewMode == "FLOW") Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("🔗 Flow Tree", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewMode = "MATRIX" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (viewMode == "MATRIX") OsrsGold else Color.Transparent,
                                    contentColor = if (viewMode == "MATRIX") Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("📋 Matrix", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // --- SAGA HIGHLIGHT CARD ---
                if (searchQuery.isBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E1610),
                        border = BorderStroke(1.dp, Color(0xFF5C473A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(currentSaga.emoji, fontSize = 24.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${currentSaga.title}: ${currentSaga.subtitle}",
                                    color = OsrsGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                                Text(
                                    text = currentSaga.highlightDescription,
                                    color = OsrsParchment.copy(alpha = 0.85f),
                                    fontSize = 9.5.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "⭐ $sagaEarnedQp / $sagaTotalQp QP",
                                    color = OsrsTextYellow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                                val pct = if (sagaTotalQuests > 0) (sagaCompletedQuests.toFloat() / sagaTotalQuests.toFloat()) else 0f
                                Text(
                                    text = "${(pct * 100).toInt()}% Progress",
                                    color = if (pct >= 1f) Color(0xFF81C784) else Color.White,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                // --- MAIN GRAPHICAL CONTENT AREA ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF130E0A))
                        .border(1.dp, Color(0xFF3E2D20), RoundedCornerShape(8.dp))
                ) {
                    if (viewMode == "FLOW") {
                        QuestFlowTreeView(
                            quests = displayQuests,
                            completedSet = completedSet,
                            selectedQuestId = selectedQuestId,
                            onSelectQuest = { selectedQuestId = it }
                        )
                    } else {
                        QuestMatrixView(
                            quests = displayQuests,
                            completedSet = completedSet,
                            selectedQuestId = selectedQuestId,
                            directUnlockMap = directUnlockMap,
                            onSelectQuest = { selectedQuestId = it }
                        )
                    }
                }

                // --- BOTTOM SELECTED QUEST INSPECTOR ---
                selectedQuest?.let { quest ->
                    QuestDetailInspectorCard(
                        quest = quest,
                        completedSet = completedSet,
                        directUnlockMap = directUnlockMap,
                        directPrereqMap = directPrereqMap,
                        activeExpeditionId = activeQuestExpedition?.quest?.id,
                        onSelectOtherQuest = { selectedQuestId = it },
                        onStartExpedition = {
                            viewModel.startQuestExpedition(quest)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuestFlowTreeView(
    quests: List<OsrsQuest>,
    completedSet: Set<String>,
    selectedQuestId: String?,
    onSelectQuest: (String) -> Unit
) {
    if (quests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No quests found matching query", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(quests) { index, quest ->
            val isCompleted = completedSet.contains(quest.id)
            val isSelected = selectedQuestId == quest.id
            val prereqsMet = quest.reqQuestIds.all { completedSet.contains(it) }
            val unlocksOthers = quests.any { other -> other.reqQuestIds.contains(quest.id) }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive Quest Node Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isSelected -> Color(0xFF382300)
                        isCompleted -> Color(0xFF18311B)
                        prereqsMet -> Color(0xFF241A12)
                        else -> Color(0xFF19120C)
                    },
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        when {
                            isSelected -> OsrsGold
                            isCompleted -> Color(0xFF4CAF50)
                            prereqsMet -> Color(0xFFFFB300)
                            else -> Color(0xFF4A3A30)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectQuest(quest.id) }
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isCompleted) Color(0xFF2E7D32) else Color(0xFF2C1E14),
                                    border = BorderStroke(1.dp, if (isCompleted) Color(0xFF81C784) else Color(0xFF5C473A)),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(quest.iconEmoji, fontSize = 16.sp)
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "#${index + 1} ${quest.name}",
                                            color = if (isCompleted) Color(0xFF81C784) else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when (quest.difficulty) {
                                                QuestDifficulty.NOVICE -> Color(0xFF1B5E20)
                                                QuestDifficulty.INTERMEDIATE -> Color(0xFF004D40)
                                                QuestDifficulty.EXPERIENCED -> Color(0xFF0D47A1)
                                                QuestDifficulty.MASTER -> Color(0xFF4A148C)
                                                QuestDifficulty.GRANDMASTER -> Color(0xFFB71C1C)
                                            }
                                        ) {
                                            Text(
                                                text = quest.difficulty.displayName,
                                                color = Color.White,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }

                                        Text(
                                            text = "⭐ ${quest.questPoints} QP",
                                            color = OsrsTextYellow,
                                            fontSize = 9.sp
                                        )

                                        if (quest.recCombatLevel > 3) {
                                            Text(
                                                text = "⚔️ Cb ${quest.recCombatLevel}",
                                                color = OsrsParchment.copy(alpha = 0.8f),
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Status Chip
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when {
                                    isCompleted -> Color(0xFF1B5E20)
                                    prereqsMet -> Color(0xFF5D4037)
                                    else -> Color(0xFF261814)
                                },
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isCompleted -> Color(0xFF81C784)
                                        prereqsMet -> Color(0xFFFFB300)
                                        else -> Color(0xFF8B3A3A)
                                    }
                                )
                            ) {
                                Text(
                                    text = when {
                                        isCompleted -> "🟢 Completed"
                                        prereqsMet -> "🟡 Ready"
                                        else -> "🔒 Prerequisites Needed"
                                    },
                                    color = when {
                                        isCompleted -> Color(0xFFA5D6A7)
                                        prereqsMet -> Color(0xFFFFE082)
                                        else -> Color(0xFFFFAB91)
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Unlock & Prerequisite Summary Line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (quest.reqQuestIds.isNotEmpty()) {
                                Text(
                                    text = "Requires ${quest.reqQuestIds.size} preceding quest(s)",
                                    color = if (prereqsMet) Color(0xFFA5D6A7) else Color(0xFFFFCC80),
                                    fontSize = 9.sp
                                )
                            } else {
                                Text(
                                    text = "🌱 Root Starter Quest (No Prereqs)",
                                    color = Color(0xFFA5D6A7),
                                    fontSize = 9.sp
                                )
                            }

                            if (quest.unlockedFeatures.isNotEmpty()) {
                                Text(
                                    text = "🔑 ${quest.unlockedFeatures.first()}",
                                    color = OsrsGold,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Directed Graphic Connector Arrow
                if (index < quests.size - 1) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Canvas(modifier = Modifier.size(width = 24.dp, height = 18.dp)) {
                            val isLinkActive = isCompleted
                            val lineColor = if (isLinkActive) Color(0xFF4CAF50) else Color(0xFF6B5643)

                            // Vertical center line
                            drawLine(
                                color = lineColor,
                                start = Offset(size.width / 2, 0f),
                                end = Offset(size.width / 2, size.height - 4f),
                                strokeWidth = 3f,
                                pathEffect = if (!isLinkActive) PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f) else null
                            )

                            // Downward arrow head
                            val path = Path().apply {
                                moveTo(size.width / 2 - 5f, size.height - 6f)
                                lineTo(size.width / 2, size.height)
                                lineTo(size.width / 2 + 5f, size.height - 6f)
                            }
                            drawPath(
                                path = path,
                                color = lineColor,
                                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = if (isCompleted) "✓ Unlocks Next" else "▼ Unlocks Next",
                            fontSize = 8.5.sp,
                            color = if (isCompleted) Color(0xFF81C784) else Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestMatrixView(
    quests: List<OsrsQuest>,
    completedSet: Set<String>,
    selectedQuestId: String?,
    directUnlockMap: Map<String, List<String>>,
    onSelectQuest: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(quests) { quest ->
            val isCompleted = completedSet.contains(quest.id)
            val isSelected = selectedQuestId == quest.id
            val downstreamUnlocks = directUnlockMap[quest.id] ?: emptyList()

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) Color(0xFF382300) else Color(0xFF1E150F),
                border = BorderStroke(1.dp, if (isSelected) OsrsGold else Color(0xFF4A382A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onSelectQuest(quest.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(quest.iconEmoji, fontSize = 16.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = quest.name,
                                color = if (isCompleted) Color(0xFF81C784) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                            Text(
                                text = "Requires ${quest.reqQuestIds.size} quests • Unlocks ${downstreamUnlocks.size} quests",
                                color = OsrsParchment.copy(alpha = 0.75f),
                                fontSize = 9.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isCompleted) Color(0xFF1B5E20) else Color(0xFF2C1C14),
                        border = BorderStroke(1.dp, if (isCompleted) Color(0xFF81C784) else Color(0xFF5C473A))
                    ) {
                        Text(
                            text = if (isCompleted) "🟢 Done" else "⭐ ${quest.questPoints} QP",
                            color = if (isCompleted) Color(0xFFA5D6A7) else OsrsTextYellow,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestDetailInspectorCard(
    quest: OsrsQuest,
    completedSet: Set<String>,
    directUnlockMap: Map<String, List<String>>,
    directPrereqMap: Map<String, List<String>>,
    activeExpeditionId: String?,
    onSelectOtherQuest: (String) -> Unit,
    onStartExpedition: () -> Unit
) {
    val isCompleted = completedSet.contains(quest.id)
    val isActive = activeExpeditionId == quest.id
    val prereqsMet = quest.reqQuestIds.all { completedSet.contains(it) }
    val downstreamIds = directUnlockMap[quest.id] ?: emptyList()

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF231810),
        border = BorderStroke(1.5.dp, OsrsGold),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Title & Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(quest.iconEmoji, fontSize = 18.sp)
                    Column {
                        Text(
                            text = quest.name,
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                        Text(
                            text = "Difficulty: ${quest.difficulty.displayName} • Cb Lv. ${quest.recCombatLevel} • ⭐ ${quest.questPoints} QP",
                            color = OsrsParchment.copy(alpha = 0.8f),
                            fontSize = 9.5.sp
                        )
                    }
                }

                if (!isCompleted) {
                    Button(
                        onClick = onStartExpedition,
                        enabled = prereqsMet && !isActive,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (prereqsMet) Color(0xFF2E7D32) else Color(0xFF3E2723),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = if (isActive) "🏃 Questing..." else if (prereqsMet) "🏃 Start Quest" else "🔒 Locked",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = quest.description,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 10.sp,
                lineHeight = 13.sp
            )

            // PREREQUISITES REQUIRED
            if (quest.reqQuestIds.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "🔒 Required Preceding Quests to Unlock This Quest:",
                        color = Color(0xFFFFB74D),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quest.reqQuestIds.forEach { reqId ->
                            val reqQuest = QuestRelationshipData.getQuestById(reqId)
                            val isReqDone = completedSet.contains(reqId)

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isReqDone) Color(0xFF1B381E) else Color(0xFF2E1912),
                                border = BorderStroke(1.dp, if (isReqDone) Color(0xFF4CAF50) else Color(0xFFFF8A80)),
                                modifier = Modifier.clickable { onSelectOtherQuest(reqId) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(if (isReqDone) "✅" else "🔒", fontSize = 10.sp)
                                    Text(
                                        text = reqQuest?.name ?: reqId,
                                        color = if (isReqDone) Color(0xFFA5D6A7) else Color(0xFFFFAB91),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // DOWNSTREAM QUESTS UNLOCKED
            if (downstreamIds.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "🔓 Quests Directly Unlocked by Completing This Quest (${downstreamIds.size}):",
                        color = Color(0xFF81C784),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        downstreamIds.forEach { downId ->
                            val downQuest = QuestRelationshipData.getQuestById(downId)
                            val isDownDone = completedSet.contains(downId)

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isDownDone) Color(0xFF1B381E) else Color(0xFF1F2B38),
                                border = BorderStroke(1.dp, if (isDownDone) Color(0xFF4CAF50) else Color(0xFF42A5F5)),
                                modifier = Modifier.clickable { onSelectOtherQuest(downId) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(downQuest?.iconEmoji ?: "📜", fontSize = 10.sp)
                                    Text(
                                        text = downQuest?.name ?: downId,
                                        color = if (isDownDone) Color(0xFFA5D6A7) else Color(0xFF90CAF9),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // UNLOCKED FEATURES & PERKS
            if (quest.unlockedFeatures.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🌟 Unlocks:", color = OsrsGold, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = quest.unlockedFeatures.joinToString(" • "),
                        color = Color(0xFFFFF59D),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
