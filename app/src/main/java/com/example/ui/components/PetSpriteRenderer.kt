package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.PetEmote
import com.example.data.models.PetMoodLevel
import com.example.data.models.PetMoodState
import com.example.data.models.PetState
import com.example.data.models.PetType
import com.example.ui.theme.OsrsGold
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance 60fps Universal Pet Sprite Renderer and Mood Overlay system.
 * Redraws each RuneScape and Pokémon pet with authentic visual designs, colors,
 * features, and 60fps continuous animations, plus universal mood indicators:
 * - Falling teardrops when unhappy/sad/bored/lonely
 * - Floating hearts when happy/ecstatic
 * - Floating meat drumstick when hungry
 * - Chewing animation & delicious nom particles when eating food
 */
@Composable
fun PetSpriteRenderer(
    petType: PetType,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 80.dp,
    petState: PetState? = null,
    petMoodState: PetMoodState? = null
) {
    val isEating = petState?.currentEmote == PetEmote.EATING
    val transition = rememberInfiniteTransition(label = "pet_sprite_eating_anim")
    val chewScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (isEating) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chew_scale"
    )
    val chewRotation by transition.animateFloat(
        initialValue = if (isEating) -4f else 0f,
        targetValue = if (isEating) 4f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chew_rot"
    )

    Box(
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer {
                if (isEating) {
                    scaleX = chewScale
                    scaleY = chewScale
                    rotationZ = chewRotation
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Main 60fps Animated Pet Sprite
        when (petType) {
            // --- OSRS PETS ---
            PetType.TANGLEROOT -> ShamanicTreantSprite(sizeDp = sizeDp)
            PetType.BEAVER -> BeaverSprite(sizeDp = sizeDp)
            PetType.HERON -> HeronSprite(sizeDp = sizeDp)
            PetType.ROCK_GOLEM -> RockGolemSprite(sizeDp = sizeDp)
            PetType.GIANT_SQUIRREL -> GiantSquirrelSprite(sizeDp = sizeDp)
            PetType.ROCKY -> RockyRaccoonSprite(sizeDp = sizeDp)
            PetType.BABY_CHINCHOMPA -> BabyChinchompaSprite(sizeDp = sizeDp)
            PetType.RIFT_GUARDIAN -> RiftGuardianSprite(sizeDp = sizeDp)
            PetType.GUTHIXIAN_WISP -> GuthixianWispSprite(sizeDp = sizeDp)
            PetType.SAILING_PARROT -> SailingParrotSprite(sizeDp = sizeDp)
            PetType.PHOENIX -> PhoenixSprite(sizeDp = sizeDp)
            PetType.ABYSSAL_ORPHAN -> AbyssalOrphanSprite(sizeDp = sizeDp)
            PetType.SMOLCANO -> SmolcanoSprite(sizeDp = sizeDp)
            PetType.PET_KRAKEN -> PetKrakenSprite(sizeDp = sizeDp)
            PetType.TZREK_JAD -> TzRekJadSprite(sizeDp = sizeDp)
            PetType.BABY_MOLE -> BabyMoleSprite(sizeDp = sizeDp)
            PetType.BABY_BLACK_DRAGON -> BabyBlackDragonSprite(sizeDp = sizeDp)
            PetType.PRINCE_BLACK_DRAGON -> PrinceBlackDragonSprite(sizeDp = sizeDp)
            PetType.VORKI -> VorkiSprite(sizeDp = sizeDp)

            // --- POKÉMON STARTERS & POPULAR GEN 1-4 ---
            PetType.SABLEYE -> SableyeSprite(sizeDp = sizeDp)
            PetType.BULBASAUR, PetType.IVYSAUR, PetType.VENUSAUR -> BulbasaurLineSprite(petType, sizeDp)
            PetType.CHARMANDER, PetType.CHARMELEON, PetType.CHARIZARD -> CharmanderLineSprite(petType, sizeDp)
            PetType.SQUIRTLE, PetType.WARTORTLE, PetType.BLASTOISE -> SquirtleLineSprite(petType, sizeDp)
            PetType.PIKACHU -> PikachuSprite(sizeDp = sizeDp)
            PetType.EEVEE -> EeveeSprite(sizeDp = sizeDp)
            PetType.SNORLAX, PetType.MUNCHLAX -> SnorlaxSprite(petType, sizeDp)
            PetType.GENGAR -> GengarSprite(sizeDp = sizeDp)
            PetType.DRAGONITE -> DragoniteSprite(sizeDp = sizeDp)
            PetType.MEWTWO -> MewtwoSprite(sizeDp = sizeDp)

            PetType.TURTWIG, PetType.GROTLE, PetType.TORTERRA -> TurtwigLineSprite(petType, sizeDp)
            PetType.CHIMCHAR, PetType.MONFERNO, PetType.INFERNAPE -> ChimcharLineSprite(petType, sizeDp)
            PetType.PIPLUP, PetType.PRINPLUP, PetType.EMPOLEON -> PiplupLineSprite(petType, sizeDp)

            PetType.STARLY, PetType.STARAVIA, PetType.STARAPTOR -> StarlyLineSprite(petType, sizeDp)
            PetType.BIDOOF, PetType.BIBAREL -> BidoofLineSprite(petType, sizeDp)
            PetType.SHINX, PetType.LUXIO, PetType.LUXRAY -> ShinxLineSprite(petType, sizeDp)
            PetType.PACHIRISU -> PachirisuSprite(sizeDp = sizeDp)
            PetType.BUIZEL, PetType.FLOATZEL -> BuizelLineSprite(petType, sizeDp)
            PetType.DRIFLOON, PetType.DRIFBLIM -> DrifloonLineSprite(petType, sizeDp)
            PetType.BUNEARY, PetType.LOPUNNY -> BunearyLineSprite(petType, sizeDp)
            PetType.GIBLE, PetType.GABITE, PetType.GARCHOMP -> GibleLineSprite(petType, sizeDp)
            PetType.RIOLU, PetType.LUCARIO -> LucarioLineSprite(petType, sizeDp)
            PetType.CROAGUNK, PetType.TOXICROAK -> CroagunkLineSprite(petType, sizeDp)

            // Legendaries
            PetType.DARKRAI, PetType.ARCEUS, PetType.DIALGA, PetType.PALKIA, PetType.GIRATINA -> LegendaryPokemonSprite(petType, sizeDp)

            else -> GenericPetCanvasSprite(petType, sizeDp)
        }

        // Universal 60fps Mood Overlay - Pet mood removed per user request
    }
}

// ==========================================
// UNIVERSAL 60FPS MOOD OVERLAY COMPONENT
// ==========================================
@Composable
fun PetMoodOverlay(
    petState: PetState?,
    petMoodState: PetMoodState?,
    sizeDp: Dp
) {
    // Pet mood removed
}

// ==========================================
// OSRS PET SPRITES (CANVAS RENDERED)
// ==========================================

@Composable
fun BeaverSprite(sizeDp: Dp) {
    val transition = rememberInfiniteTransition(label = "beaver_anim")
    val tailWag by transition.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "tail"
    )
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        val brownDark = Color(0xFF4E2A10)
        val brownMid = Color(0xFF7A4822)
        val brownLight = Color(0xFFA66A38)
        val hatRed = Color(0xFFD32F2F)

        // Paddle Tail
        val tailPath = Path().apply {
            moveTo(w * 0.20f, h * 0.65f)
            quadraticTo(w * 0.02f, h * 0.70f + tailWag, w * 0.15f, h * 0.88f)
            close()
        }
        drawPath(tailPath, brownDark)

        // Main Body
        drawOval(brownMid, topLeft = Offset(w * 0.22f, h * 0.35f), size = Size(w * 0.56f, h * 0.50f))
        drawOval(brownLight, topLeft = Offset(w * 0.32f, h * 0.45f), size = Size(w * 0.36f, h * 0.35f))

        // Head
        drawCircle(brownMid, radius = w * 0.22f, center = Offset(w * 0.50f, h * 0.30f))

        // Red Lumberjack Hat
        val hatPath = Path().apply {
            moveTo(w * 0.35f, h * 0.18f)
            lineTo(w * 0.65f, h * 0.18f)
            lineTo(w * 0.60f, h * 0.08f)
            lineTo(w * 0.40f, h * 0.08f)
            close()
        }
        drawPath(hatPath, hatRed)

        // Cute Eyes & Buck Teeth
        drawCircle(Color.Black, radius = w * 0.035f, center = Offset(w * 0.42f, h * 0.28f))
        drawCircle(Color.Black, radius = w * 0.035f, center = Offset(w * 0.58f, h * 0.28f))
        drawCircle(Color.White, radius = w * 0.012f, center = Offset(w * 0.41f, h * 0.27f))
        drawCircle(Color.White, radius = w * 0.012f, center = Offset(w * 0.57f, h * 0.27f))

        // Buck teeth
        drawRect(Color.White, topLeft = Offset(w * 0.47f, h * 0.34f), size = Size(w * 0.06f, h * 0.06f))

        // Wood Log in Paws
        drawRoundRect(Color(0xFF8D5B28), topLeft = Offset(w * 0.30f, h * 0.62f), size = Size(w * 0.40f, h * 0.12f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f))
    }
}

@Composable
fun HeronSprite(sizeDp: Dp) {
    val transition = rememberInfiniteTransition(label = "heron_anim")
    val neckBob by transition.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "neck"
    )
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        val heronBlue = Color(0xFF5C7A99)
        val heronLight = Color(0xFFE0E8F0)
        val beakOrange = Color(0xFFFF9800)

        // Slender Yellow Legs
        drawLine(beakOrange, Offset(w * 0.45f, h * 0.65f), Offset(w * 0.42f, h * 0.92f), strokeWidth = w * 0.03f)
        drawLine(beakOrange, Offset(w * 0.55f, h * 0.65f), Offset(w * 0.58f, h * 0.92f), strokeWidth = w * 0.03f)

        // Body
        drawOval(heronBlue, topLeft = Offset(w * 0.35f, h * 0.45f), size = Size(w * 0.35f, h * 0.25f))

        // Curved Neck & Head
        val neckPath = Path().apply {
            moveTo(w * 0.60f, h * 0.50f)
            quadraticTo(w * 0.72f, h * 0.30f + neckBob, w * 0.55f, h * 0.18f + neckBob)
            lineTo(w * 0.45f, h * 0.18f + neckBob)
            quadraticTo(w * 0.58f, h * 0.32f + neckBob, w * 0.50f, h * 0.50f)
            close()
        }
        drawPath(neckPath, heronLight)

        // Head Feather Crest & Eye
        drawCircle(heronBlue, radius = w * 0.08f, center = Offset(w * 0.50f, h * 0.18f + neckBob))
        drawCircle(Color.Black, radius = w * 0.025f, center = Offset(w * 0.48f, h * 0.17f + neckBob))

        // Beak with Fish
        val beakPath = Path().apply {
            moveTo(w * 0.43f, h * 0.18f + neckBob)
            lineTo(w * 0.20f, h * 0.22f + neckBob)
            lineTo(w * 0.43f, h * 0.22f + neckBob)
            close()
        }
        drawPath(beakPath, beakOrange)

        // Raw Fish in Beak
        drawOval(Color(0xFF00BCD4), topLeft = Offset(w * 0.22f, h * 0.20f + neckBob), size = Size(w * 0.12f, h * 0.06f))
    }
}

@Composable
fun RockGolemSprite(sizeDp: Dp) {
    val transition = rememberInfiniteTransition(label = "golem_anim")
    val eyeGlow by transition.animateFloat(
        initialValue = 0.4f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "golem_eye"
    )
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        val rockDark = Color(0xFF3E3E3E)
        val rockMid = Color(0xFF616161)
        val rockLight = Color(0xFF9E9E9E)
        val runicPurple = Color(0xFFD500F9)

        // Layered Granite Body
        drawRect(rockDark, topLeft = Offset(w * 0.28f, h * 0.40f), size = Size(w * 0.44f, h * 0.45f))
        drawRect(rockMid, topLeft = Offset(w * 0.32f, h * 0.45f), size = Size(w * 0.36f, h * 0.35f))

        // Angular Head
        drawRect(rockLight, topLeft = Offset(w * 0.35f, h * 0.18f), size = Size(w * 0.30f, h * 0.22f))

        // Floating Rock Shoulders
        drawCircle(rockMid, radius = w * 0.10f, center = Offset(w * 0.22f, h * 0.42f))
        drawCircle(rockMid, radius = w * 0.10f, center = Offset(w * 0.78f, h * 0.42f))

        // Glowing Purple Runic Eyes
        drawRect(runicPurple.copy(alpha = eyeGlow), topLeft = Offset(w * 0.40f, h * 0.25f), size = Size(w * 0.06f, h * 0.04f))
        drawRect(runicPurple.copy(alpha = eyeGlow), topLeft = Offset(w * 0.54f, h * 0.25f), size = Size(w * 0.06f, h * 0.04f))
    }
}

@Composable
fun GiantSquirrelSprite(sizeDp: Dp) {
    val transition = rememberInfiniteTransition(label = "squirrel_anim")
    val tailSway by transition.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "squirrel_tail"
    )
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        val orangeCoat = Color(0xFFE65100)
        val creamBelly = Color(0xFFFFE0B2)
        val acornGold = Color(0xFFFFD700)

        // Giant Bushy Curled Tail
        val tailPath = Path().apply {
            moveTo(w * 0.35f, h * 0.75f)
            quadraticTo(w * 0.05f + tailSway, h * 0.30f, w * 0.30f, h * 0.15f)
            quadraticTo(w * 0.18f + tailSway, h * 0.45f, w * 0.40f, h * 0.65f)
            close()
        }
        drawPath(tailPath, orangeCoat)

        // Body & Head
        drawOval(orangeCoat, topLeft = Offset(w * 0.35f, h * 0.45f), size = Size(w * 0.38f, h * 0.42f))
        drawOval(creamBelly, topLeft = Offset(w * 0.42f, h * 0.52f), size = Size(w * 0.22f, h * 0.28f))
        drawCircle(orangeCoat, radius = w * 0.18f, center = Offset(w * 0.55f, h * 0.32f))

        // Pointy Ears
        drawCircle(orangeCoat, radius = w * 0.05f, center = Offset(w * 0.46f, h * 0.15f))
        drawCircle(orangeCoat, radius = w * 0.05f, center = Offset(w * 0.64f, h * 0.15f))

        // Eye & Shiny Golden Acorn
        drawCircle(Color.Black, radius = w * 0.03f, center = Offset(w * 0.58f, h * 0.28f))
        drawCircle(acornGold, radius = w * 0.07f, center = Offset(w * 0.68f, h * 0.58f))
    }
}

@Composable
fun RockyRaccoonSprite(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        val grayBody = Color(0xFF757575)
        val darkMask = Color(0xFF212121)

        // Striped Tail
        drawRoundRect(grayBody, topLeft = Offset(w * 0.15f, h * 0.60f), size = Size(w * 0.25f, h * 0.18f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f))
        drawRect(darkMask, topLeft = Offset(w * 0.22f, h * 0.60f), size = Size(w * 0.06f, h * 0.18f))

        // Body & Head
        drawCircle(grayBody, radius = w * 0.22f, center = Offset(w * 0.50f, h * 0.55f))
        drawCircle(grayBody, radius = w * 0.18f, center = Offset(w * 0.50f, h * 0.30f))

        // Black Bandit Mask
        drawOval(darkMask, topLeft = Offset(w * 0.36f, h * 0.25f), size = Size(w * 0.28f, h * 0.10f))
        drawCircle(Color.White, radius = w * 0.02f, center = Offset(w * 0.42f, h * 0.30f))
        drawCircle(Color.White, radius = w * 0.02f, center = Offset(w * 0.58f, h * 0.30f))

        // Stolen Coin Pouch
        drawCircle(Color(0xFF8D6E63), radius = w * 0.08f, center = Offset(w * 0.65f, h * 0.62f))
        drawCircle(Color(0xFFFFD700), radius = w * 0.03f, center = Offset(w * 0.65f, h * 0.60f))
    }
}

@Composable
fun BabyChinchompaSprite(sizeDp: Dp) {
    val transition = rememberInfiniteTransition(label = "chin_anim")
    val noseTwitch by transition.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "nose"
    )
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        val redFur = Color(0xFFD84315)
        val creamChest = Color(0xFFFFCC80)

        // Round Fluffy Chinchompa Body
        drawCircle(redFur, radius = w * 0.30f, center = Offset(w * 0.50f, h * 0.52f))
        drawCircle(creamChest, radius = w * 0.18f, center = Offset(w * 0.50f, h * 0.58f))

        // Large Cute Eyes
        drawCircle(Color.Black, radius = w * 0.045f, center = Offset(w * 0.40f, h * 0.42f))
        drawCircle(Color.Black, radius = w * 0.045f, center = Offset(w * 0.60f, h * 0.42f))
        drawCircle(Color.White, radius = w * 0.015f, center = Offset(w * 0.39f, h * 0.40f))
        drawCircle(Color.White, radius = w * 0.015f, center = Offset(w * 0.59f, h * 0.40f))

        // Twitching Pink Nose
        drawCircle(Color(0xFFFF80AB), radius = w * 0.025f, center = Offset(w * 0.50f + noseTwitch, h * 0.48f))
    }
}

@Composable
fun RiftGuardianSprite(sizeDp: Dp) {
    val transition = rememberInfiniteTransition(label = "rift_anim")
    val orbAngle by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "orb_rot"
    )
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        val obsidianDark = Color(0xFF1A237E)
        val runeCyan = Color(0xFF00E5FF)

        // Obsidian Body
        drawRect(obsidianDark, topLeft = Offset(w * 0.32f, h * 0.35f), size = Size(w * 0.36f, h * 0.50f))

        // Orbiting Rune Ring
        val rad = Math.toRadians(orbAngle.toDouble())
        val orbX = w * 0.50f + (w * 0.32f) * cos(rad).toFloat()
        val orbY = h * 0.50f + (h * 0.18f) * sin(rad).toFloat()

        drawCircle(runeCyan, radius = w * 0.06f, center = Offset(orbX, orbY))
        drawCircle(Color.White, radius = w * 0.025f, center = Offset(orbX, orbY))
    }
}

@Composable
fun GuthixianWispSprite(sizeDp: Dp) {
    val transition = rememberInfiniteTransition(label = "wisp_anim")
    val wispPulse by transition.animateFloat(
        initialValue = 0.8f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "wisp_pulse"
    )
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFB2FF59), Color(0xFF00E676), Color.Transparent)
            ),
            radius = (w * 0.35f) * wispPulse,
            center = Offset(w * 0.50f, h * 0.50f)
        )
        drawCircle(Color.White, radius = w * 0.12f, center = Offset(w * 0.50f, h * 0.50f))
    }
}

@Composable
fun SailingParrotSprite(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        // Red/Yellow/Blue Plumage Body
        drawOval(Color(0xFFD50000), topLeft = Offset(w * 0.35f, h * 0.35f), size = Size(w * 0.32f, h * 0.45f))
        drawCircle(Color(0xFFFFD600), radius = w * 0.14f, center = Offset(w * 0.50f, h * 0.28f))

        // Eyepatch & Beak
        drawRect(Color.Black, topLeft = Offset(w * 0.38f, h * 0.24f), size = Size(w * 0.10f, h * 0.08f))
        drawPath(Path().apply {
            moveTo(w * 0.55f, h * 0.26f)
            lineTo(w * 0.72f, h * 0.32f)
            lineTo(w * 0.55f, h * 0.34f)
            close()
        }, Color(0xFFFF9100))

        // Captain Pirate Hat
        drawPath(Path().apply {
            moveTo(w * 0.30f, h * 0.18f)
            lineTo(w * 0.70f, h * 0.18f)
            lineTo(w * 0.50f, h * 0.05f)
            close()
        }, Color.Black)
    }
}

@Composable
fun PhoenixSprite(sizeDp: Dp) {
    val transition = rememberInfiniteTransition(label = "phoenix_anim")
    val wingFlap by transition.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "flap"
    )
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        // Flame Wings
        val leftWing = Path().apply {
            moveTo(w * 0.40f, h * 0.45f)
            quadraticTo(w * 0.05f, h * 0.20f + wingFlap, w * 0.20f, h * 0.65f)
            close()
        }
        val rightWing = Path().apply {
            moveTo(w * 0.60f, h * 0.45f)
            quadraticTo(w * 0.95f, h * 0.20f + wingFlap, w * 0.80f, h * 0.65f)
            close()
        }
        drawPath(leftWing, Color(0xFFFF3D00))
        drawPath(rightWing, Color(0xFFFF3D00))

        // Fiery Body & Beak
        drawOval(Color(0xFFFFAB00), topLeft = Offset(w * 0.38f, h * 0.30f), size = Size(w * 0.24f, h * 0.45f))
        drawCircle(Color.White, radius = w * 0.025f, center = Offset(w * 0.48f, h * 0.28f))
    }
}

@Composable
fun AbyssalOrphanSprite(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        val voidPurple = Color(0xFF4A148C)

        // Void Head & Body
        drawCircle(voidPurple, radius = w * 0.28f, center = Offset(w * 0.50f, h * 0.40f))

        // Multiple Glowing Eyes
        drawCircle(Color(0xFFFFEA00), radius = w * 0.04f, center = Offset(w * 0.40f, h * 0.38f))
        drawCircle(Color(0xFFFFEA00), radius = w * 0.04f, center = Offset(w * 0.60f, h * 0.38f))
        drawCircle(Color(0xFFFFEA00), radius = w * 0.03f, center = Offset(w * 0.50f, h * 0.28f))

        // Squiggling Tentacles
        drawLine(voidPurple, Offset(w * 0.35f, h * 0.65f), Offset(w * 0.25f, h * 0.90f), strokeWidth = w * 0.05f)
        drawLine(voidPurple, Offset(w * 0.50f, h * 0.65f), Offset(w * 0.50f, h * 0.92f), strokeWidth = w * 0.05f)
        drawLine(voidPurple, Offset(w * 0.65f, h * 0.65f), Offset(w * 0.75f, h * 0.90f), strokeWidth = w * 0.05f)
    }
}

@Composable
fun SmolcanoSprite(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        // Basalt Rock Cone
        val volcanoPath = Path().apply {
            moveTo(w * 0.30f, h * 0.25f)
            lineTo(w * 0.70f, h * 0.25f)
            lineTo(w * 0.85f, h * 0.85f)
            lineTo(w * 0.15f, h * 0.85f)
            close()
        }
        drawPath(volcanoPath, Color(0xFF212121))

        // Glowing Magma Crater
        drawOval(Color(0xFFFF3D00), topLeft = Offset(w * 0.32f, h * 0.22f), size = Size(w * 0.36f, h * 0.08f))
    }
}

@Composable
fun PetKrakenSprite(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        // Giant Kraken Head
        drawCircle(Color(0xFFE65100), radius = w * 0.28f, center = Offset(w * 0.50f, h * 0.38f))
        drawCircle(Color.Black, radius = w * 0.04f, center = Offset(w * 0.42f, h * 0.38f))
        drawCircle(Color.Black, radius = w * 0.04f, center = Offset(w * 0.58f, h * 0.38f))

        // Wriggling Tentacles
        for (i in 0..4) {
            val tx = w * (0.20f + i * 0.15f)
            drawLine(Color(0xFFEF6C00), Offset(tx, h * 0.60f), Offset(tx, h * 0.88f), strokeWidth = w * 0.06f, cap = StrokeCap.Round)
        }
    }
}

@Composable
fun TzRekJadSprite(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        // Obsidian Body
        drawRect(Color(0xFF1C1B1B), topLeft = Offset(w * 0.25f, h * 0.35f), size = Size(w * 0.50f, h * 0.45f))

        // Red Lava Spikes
        val spikePath = Path().apply {
            moveTo(w * 0.30f, h * 0.35f)
            lineTo(w * 0.20f, h * 0.15f)
            lineTo(w * 0.40f, h * 0.35f)
            moveTo(w * 0.70f, h * 0.35f)
            lineTo(w * 0.80f, h * 0.15f)
            lineTo(w * 0.60f, h * 0.35f)
        }
        drawPath(spikePath, Color(0xFFD50000))

        // Glowing Lava Eyes & Mouth
        drawCircle(Color(0xFFFF6D00), radius = w * 0.04f, center = Offset(w * 0.42f, h * 0.45f))
        drawCircle(Color(0xFFFF6D00), radius = w * 0.04f, center = Offset(w * 0.58f, h * 0.45f))
        drawRect(Color(0xFFFFD600), topLeft = Offset(w * 0.42f, h * 0.58f), size = Size(w * 0.16f, h * 0.08f))
    }
}

@Composable
fun BabyMoleSprite(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        drawOval(Color(0xFF4E342E), topLeft = Offset(w * 0.25f, h * 0.35f), size = Size(w * 0.50f, h * 0.50f))
        drawCircle(Color(0xFFF8BBD0), radius = w * 0.06f, center = Offset(w * 0.50f, h * 0.48f)) // Pink Nose
        drawCircle(Color.Black, radius = w * 0.03f, center = Offset(w * 0.40f, h * 0.42f))
        drawCircle(Color.Black, radius = w * 0.03f, center = Offset(w * 0.60f, h * 0.42f))
    }
}

@Composable
fun BabyBlackDragonSprite(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        // Dark Dragon Body & Wings
        drawOval(Color(0xFF212121), topLeft = Offset(w * 0.30f, h * 0.38f), size = Size(w * 0.40f, h * 0.42f))
        drawCircle(Color(0xFF212121), radius = w * 0.16f, center = Offset(w * 0.50f, h * 0.28f))

        // Red Wings
        drawPath(Path().apply {
            moveTo(w * 0.35f, h * 0.38f)
            lineTo(w * 0.10f, h * 0.25f)
            lineTo(w * 0.25f, h * 0.55f)
            close()
        }, Color(0xFFB71C1C))

        drawCircle(Color(0xFFFF6D00), radius = w * 0.035f, center = Offset(w * 0.56f, h * 0.26f)) // Fiery Eye
    }
}

@Composable
fun PrinceBlackDragonSprite(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        drawOval(Color(0xFF212121), topLeft = Offset(w * 0.28f, h * 0.45f), size = Size(w * 0.44f, h * 0.40f))

        // 3 Heads
        drawCircle(Color(0xFF212121), radius = w * 0.10f, center = Offset(w * 0.32f, h * 0.28f))
        drawCircle(Color(0xFF212121), radius = w * 0.11f, center = Offset(w * 0.50f, h * 0.22f))
        drawCircle(Color(0xFF212121), radius = w * 0.10f, center = Offset(w * 0.68f, h * 0.28f))

        // Golden Crowns on heads
        drawRect(Color(0xFFFFD700), topLeft = Offset(w * 0.44f, h * 0.08f), size = Size(w * 0.12f, h * 0.06f))
    }
}

@Composable
fun VorkiSprite(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        // Skeletal Teal Dragon
        drawOval(Color(0xFF00695C), topLeft = Offset(w * 0.30f, h * 0.38f), size = Size(w * 0.40f, h * 0.42f))

        // Bone Ribs
        for (i in 0..2) {
            drawLine(Color.White, Offset(w * 0.35f, h * (0.45f + i * 0.08f)), Offset(w * 0.65f, h * (0.45f + i * 0.08f)), strokeWidth = w * 0.04f)
        }
        drawCircle(Color(0xFF00E5FF), radius = w * 0.04f, center = Offset(w * 0.50f, h * 0.28f)) // Frost Eye
    }
}

// ==========================================
// POKÉMON SPRITES (CANVAS RENDERED)
// ==========================================

@Composable
fun BulbasaurLineSprite(petType: PetType, sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        val bodyColor = Color(0xFF26A69A)
        val spotColor = Color(0xFF004D40)

        // Quadruped Body
        drawOval(bodyColor, topLeft = Offset(w * 0.25f, h * 0.40f), size = Size(w * 0.50f, h * 0.40f))
        drawCircle(bodyColor, radius = w * 0.18f, center = Offset(w * 0.50f, h * 0.32f))

        // Spots & Red Eyes
        drawCircle(spotColor, radius = w * 0.04f, center = Offset(w * 0.35f, h * 0.48f))
        drawCircle(Color(0xFFD50000), radius = w * 0.035f, center = Offset(w * 0.42f, h * 0.30f))
        drawCircle(Color(0xFFD50000), radius = w * 0.035f, center = Offset(w * 0.58f, h * 0.30f))

        // Plant Bulb / Flower on back
        val plantColor = if (petType == PetType.VENUSAUR) Color(0xFFE91E63) else Color(0xFF4CAF50)
        drawCircle(plantColor, radius = w * 0.16f, center = Offset(w * 0.50f, h * 0.22f))
    }
}

@Composable
fun CharmanderLineSprite(petType: PetType, sizeDp: Dp) {
    val transition = rememberInfiniteTransition(label = "char_anim")
    val flameFlicker by transition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "flame"
    )
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        val orangeColor = if (petType == PetType.CHARMELEON) Color(0xFFC62828) else Color(0xFFFF6D00)

        // Body & Cream Belly
        drawOval(orangeColor, topLeft = Offset(w * 0.30f, h * 0.38f), size = Size(w * 0.40f, h * 0.45f))
        drawOval(Color(0xFFFFECB3), topLeft = Offset(w * 0.38f, h * 0.48f), size = Size(w * 0.24f, h * 0.30f))
        drawCircle(orangeColor, radius = w * 0.18f, center = Offset(w * 0.50f, h * 0.28f))

        // Charizard Wings
        if (petType == PetType.CHARIZARD) {
            drawPath(Path().apply {
                moveTo(w * 0.35f, h * 0.38f)
                lineTo(w * 0.08f, h * 0.20f)
                lineTo(w * 0.25f, h * 0.55f)
                close()
            }, Color(0xFF00897B))
        }

        // Tail Flame
        drawCircle(Color(0xFFFF3D00), radius = (w * 0.08f) * flameFlicker, center = Offset(w * 0.22f, h * 0.70f))
        drawCircle(Color(0xFFFFD600), radius = (w * 0.04f) * flameFlicker, center = Offset(w * 0.22f, h * 0.70f))
    }
}

@Composable
fun SquirtleLineSprite(petType: PetType, sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        val turtleBlue = Color(0xFF29B6F6)
        val shellBrown = Color(0xFF6D4C41)

        // Brown Shell & Blue Body
        drawOval(shellBrown, topLeft = Offset(w * 0.28f, h * 0.40f), size = Size(w * 0.44f, h * 0.42f))
        drawCircle(turtleBlue, radius = w * 0.18f, center = Offset(w * 0.50f, h * 0.28f))

        // Blastoise Cannons
        if (petType == PetType.BLASTOISE) {
            drawRect(Color(0xFF78909C), topLeft = Offset(w * 0.25f, h * 0.22f), size = Size(w * 0.08f, h * 0.18f))
            drawRect(Color(0xFF78909C), topLeft = Offset(w * 0.67f, h * 0.22f), size = Size(w * 0.08f, h * 0.18f))
        }
    }
}

@Composable
fun PikachuSprite(sizeDp: Dp) {
    val transition = rememberInfiniteTransition(label = "pika_anim")
    val tailEarRotate by transition.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pika_ear"
    )
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        val pikaYellow = Color(0xFFFFEB3B)
        val cheekRed = Color(0xFFFF1744)

        // Pointed Ears
        val leftEar = Path().apply {
            moveTo(w * 0.42f, h * 0.25f)
            lineTo(w * 0.25f + tailEarRotate, h * 0.05f)
            lineTo(w * 0.35f, h * 0.22f)
            close()
        }
        val rightEar = Path().apply {
            moveTo(w * 0.58f, h * 0.25f)
            lineTo(w * 0.75f - tailEarRotate, h * 0.05f)
            lineTo(w * 0.65f, h * 0.22f)
            close()
        }
        drawPath(leftEar, pikaYellow)
        drawPath(rightEar, pikaYellow)
        drawCircle(Color.Black, radius = w * 0.03f, center = Offset(w * 0.27f, h * 0.08f))
        drawCircle(Color.Black, radius = w * 0.03f, center = Offset(w * 0.73f, h * 0.08f))

        // Body & Head
        drawOval(pikaYellow, topLeft = Offset(w * 0.30f, h * 0.42f), size = Size(w * 0.40f, h * 0.45f))
        drawCircle(pikaYellow, radius = w * 0.20f, center = Offset(w * 0.50f, h * 0.32f))

        // Red Cheeks & Eyes
        drawCircle(cheekRed, radius = w * 0.045f, center = Offset(w * 0.36f, h * 0.36f))
        drawCircle(cheekRed, radius = w * 0.045f, center = Offset(w * 0.64f, h * 0.36f))
        drawCircle(Color.Black, radius = w * 0.035f, center = Offset(w * 0.42f, h * 0.30f))
        drawCircle(Color.Black, radius = w * 0.035f, center = Offset(w * 0.58f, h * 0.30f))
        drawCircle(Color.White, radius = w * 0.012f, center = Offset(w * 0.41f, h * 0.29f))
        drawCircle(Color.White, radius = w * 0.012f, center = Offset(w * 0.57f, h * 0.29f))

        // Zigzag Lightning Tail
        val tailPath = Path().apply {
            moveTo(w * 0.28f, h * 0.70f)
            lineTo(w * 0.15f, h * 0.58f)
            lineTo(w * 0.22f, h * 0.50f)
            lineTo(w * 0.10f, h * 0.35f)
            lineTo(w * 0.22f, h * 0.38f)
            close()
        }
        drawPath(tailPath, pikaYellow)
    }
}

@Composable
fun EeveeSprite(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        val eeveeBrown = Color(0xFF8D6E63)
        val creamFluff = Color(0xFFFFF8E1)

        drawOval(eeveeBrown, topLeft = Offset(w * 0.32f, h * 0.42f), size = Size(w * 0.36f, h * 0.42f))
        drawCircle(creamFluff, radius = w * 0.16f, center = Offset(w * 0.50f, h * 0.42f)) // Fluffy Collar
        drawCircle(eeveeBrown, radius = w * 0.18f, center = Offset(w * 0.50f, h * 0.28f))

        // Big Long Ears
        drawOval(eeveeBrown, topLeft = Offset(w * 0.20f, h * 0.08f), size = Size(w * 0.12f, h * 0.24f))
        drawOval(eeveeBrown, topLeft = Offset(w * 0.68f, h * 0.08f), size = Size(w * 0.12f, h * 0.24f))
    }
}

@Composable
fun SnorlaxSprite(petType: PetType, sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        val snorlaxTeal = Color(0xFF00695C)
        val creamBelly = Color(0xFFFFF8E1)

        drawCircle(snorlaxTeal, radius = w * 0.35f, center = Offset(w * 0.50f, h * 0.52f))
        drawOval(creamBelly, topLeft = Offset(w * 0.28f, h * 0.35f), size = Size(w * 0.44f, h * 0.45f))

        // Closed Sleeping Eyes & Ears
        drawLine(Color.Black, Offset(w * 0.40f, h * 0.32f), Offset(w * 0.46f, h * 0.32f), strokeWidth = w * 0.03f)
        drawLine(Color.Black, Offset(w * 0.54f, h * 0.32f), Offset(w * 0.60f, h * 0.32f), strokeWidth = w * 0.03f)
    }
}

@Composable
fun GengarSprite(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        val gengarPurple = Color(0xFF4A148C)

        // Spiky Shadow Body
        drawCircle(gengarPurple, radius = w * 0.32f, center = Offset(w * 0.50f, h * 0.45f))

        // Evil Red Eyes & Grin
        drawCircle(Color(0xFFD50000), radius = w * 0.05f, center = Offset(w * 0.38f, h * 0.38f))
        drawCircle(Color(0xFFD50000), radius = w * 0.05f, center = Offset(w * 0.62f, h * 0.38f))
        drawArc(Color.White, 0f, 180f, true, topLeft = Offset(w * 0.38f, h * 0.48f), size = Size(w * 0.24f, h * 0.15f))
    }
}

@Composable
fun DragoniteSprite(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        val dragonOrange = Color(0xFFFF8F00)
        drawOval(dragonOrange, topLeft = Offset(w * 0.30f, h * 0.38f), size = Size(w * 0.40f, h * 0.45f))
        drawOval(Color(0xFFFFF8E1), topLeft = Offset(w * 0.36f, h * 0.46f), size = Size(w * 0.28f, h * 0.32f))
        drawCircle(dragonOrange, radius = w * 0.18f, center = Offset(w * 0.50f, h * 0.28f))
    }
}

@Composable
fun MewtwoSprite(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        drawOval(Color(0xFFE0E0E0), topLeft = Offset(w * 0.32f, h * 0.38f), size = Size(w * 0.36f, h * 0.42f))
        drawCircle(Color(0xFF7B1FA2), radius = w * 0.08f, center = Offset(w * 0.30f, h * 0.70f)) // Purple Tail
        drawCircle(Color(0xFFE0E0E0), radius = w * 0.16f, center = Offset(w * 0.50f, h * 0.26f))
    }
}

@Composable
fun TurtwigLineSprite(petType: PetType, sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        drawOval(Color(0xFF5D4037), topLeft = Offset(w * 0.25f, h * 0.42f), size = Size(w * 0.50f, h * 0.40f))
        drawCircle(Color(0xFF8BC34A), radius = w * 0.18f, center = Offset(w * 0.50f, h * 0.30f))
        drawCircle(Color(0xFF4CAF50), radius = w * 0.06f, center = Offset(w * 0.50f, h * 0.12f)) // Head Sprout
    }
}

@Composable
fun ChimcharLineSprite(petType: PetType, sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        drawCircle(Color(0xFFFF6D00), radius = w * 0.24f, center = Offset(w * 0.50f, h * 0.45f))
        drawCircle(Color(0xFFFF6D00), radius = w * 0.18f, center = Offset(w * 0.50f, h * 0.28f))
        drawCircle(Color(0xFFFF3D00), radius = w * 0.07f, center = Offset(w * 0.25f, h * 0.60f)) // Flaming Tail
    }
}

@Composable
fun PiplupLineSprite(petType: PetType, sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        val penguinBlue = Color(0xFF0288D1)
        drawOval(penguinBlue, topLeft = Offset(w * 0.30f, h * 0.42f), size = Size(w * 0.40f, h * 0.42f))
        drawCircle(penguinBlue, radius = w * 0.18f, center = Offset(w * 0.50f, h * 0.28f))
        drawCircle(Color.White, radius = w * 0.12f, center = Offset(w * 0.50f, h * 0.28f))
        drawCircle(Color(0xFFFFB300), radius = w * 0.04f, center = Offset(w * 0.50f, h * 0.30f)) // Beak
    }
}

@Composable
fun StarlyLineSprite(petType: PetType, sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        drawCircle(Color(0xFF616161), radius = w * 0.22f, center = Offset(w * 0.50f, h * 0.45f))
        drawCircle(Color.White, radius = w * 0.06f, center = Offset(w * 0.50f, h * 0.32f))
    }
}

@Composable
fun BidoofLineSprite(petType: PetType, sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        drawOval(Color(0xFF795548), topLeft = Offset(w * 0.25f, h * 0.35f), size = Size(w * 0.50f, h * 0.50f))
        drawRect(Color.White, topLeft = Offset(w * 0.46f, h * 0.52f), size = Size(w * 0.08f, h * 0.08f)) // Big teeth
    }
}

@Composable
fun ShinxLineSprite(petType: PetType, sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        drawCircle(Color(0xFF0288D1), radius = w * 0.22f, center = Offset(w * 0.50f, h * 0.45f))
        drawCircle(Color(0xFFFFEB3B), radius = w * 0.04f, center = Offset(w * 0.22f, h * 0.65f)) // Star Tail
    }
}

@Composable
fun PachirisuSprite(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        drawCircle(Color.White, radius = w * 0.24f, center = Offset(w * 0.50f, h * 0.45f))
        drawCircle(Color(0xFFFFEB3B), radius = w * 0.04f, center = Offset(w * 0.38f, h * 0.42f))
    }
}

@Composable
fun BuizelLineSprite(petType: PetType, sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        drawOval(Color(0xFFFB8C00), topLeft = Offset(w * 0.30f, h * 0.40f), size = Size(w * 0.40f, h * 0.42f))
        drawCircle(Color(0xFFFFD54F), radius = w * 0.14f, center = Offset(w * 0.50f, h * 0.36f)) // Collar
    }
}

@Composable
fun DrifloonLineSprite(petType: PetType, sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        drawCircle(Color(0xFF7B1FA2), radius = w * 0.28f, center = Offset(w * 0.50f, h * 0.40f))
        drawRect(Color(0xFFFFEB3B), topLeft = Offset(w * 0.44f, h * 0.38f), size = Size(w * 0.12f, h * 0.12f)) // Yellow Tape
    }
}

@Composable
fun BunearyLineSprite(petType: PetType, sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        drawOval(Color(0xFF8D6E63), topLeft = Offset(w * 0.32f, h * 0.42f), size = Size(w * 0.36f, h * 0.42f))
        drawCircle(Color(0xFFFFF8E1), radius = w * 0.08f, center = Offset(w * 0.28f, h * 0.18f)) // Fluffy ear tip
    }
}

@Composable
fun GibleLineSprite(petType: PetType, sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        drawOval(Color(0xFF1565C0), topLeft = Offset(w * 0.28f, h * 0.35f), size = Size(w * 0.44f, h * 0.48f))
        drawOval(Color(0xFFD32F2F), topLeft = Offset(w * 0.35f, h * 0.48f), size = Size(w * 0.30f, h * 0.28f)) // Red belly
    }
}

@Composable
fun LucarioLineSprite(petType: PetType, sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        drawOval(Color(0xFF0288D1), topLeft = Offset(w * 0.32f, h * 0.38f), size = Size(w * 0.36f, h * 0.44f))
        drawRect(Color.Black, topLeft = Offset(w * 0.38f, h * 0.24f), size = Size(w * 0.24f, h * 0.08f)) // Mask
    }
}

@Composable
fun CroagunkLineSprite(petType: PetType, sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        drawOval(Color(0xFF1976D2), topLeft = Offset(w * 0.30f, h * 0.38f), size = Size(w * 0.40f, h * 0.42f))
        drawCircle(Color(0xFFFB8C00), radius = w * 0.06f, center = Offset(w * 0.30f, h * 0.42f)) // Poison Sac
    }
}

@Composable
fun LegendaryPokemonSprite(petType: PetType, sizeDp: Dp) {
    val transition = rememberInfiniteTransition(label = "legend_anim")
    val legendPulse by transition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "legend_pulse"
    )
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        val auraColor = when (petType) {
            PetType.DARKRAI -> Color(0xFF311B92)
            PetType.ARCEUS -> Color(0xFFFFD700)
            PetType.DIALGA -> Color(0xFF00E5FF)
            PetType.PALKIA -> Color(0xFFFF4081)
            else -> Color(0xFFD500F9)
        }

        drawCircle(
            brush = Brush.radialGradient(colors = listOf(auraColor, Color.Transparent)),
            radius = (w * 0.42f) * legendPulse,
            center = Offset(w * 0.50f, h * 0.50f)
        )
        drawCircle(auraColor, radius = w * 0.22f, center = Offset(w * 0.50f, h * 0.45f))
    }
}

@Composable
fun SableyeSprite(sizeDp: Dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "sableye_anim_60fps")

    val floatY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sableye_float"
    )

    val gemGlint by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sableye_gem_glint"
    )

    Canvas(modifier = Modifier.size(sizeDp).offset(y = floatY.dp)) {
        val w = size.width
        val h = size.height

        // Dark Purple Shadow Body
        drawCircle(
            color = Color(0xFF4A148C),
            radius = w * 0.32f,
            center = Offset(w * 0.50f, h * 0.50f)
        )

        // Pointy Ears
        val leftEar = Path().apply {
            moveTo(w * 0.32f, h * 0.35f)
            lineTo(w * 0.18f, h * 0.12f)
            lineTo(w * 0.42f, h * 0.28f)
            close()
        }
        drawPath(leftEar, color = Color(0xFF38006B))

        val rightEar = Path().apply {
            moveTo(w * 0.68f, h * 0.35f)
            lineTo(w * 0.82f, h * 0.12f)
            lineTo(w * 0.58f, h * 0.28f)
            close()
        }
        drawPath(rightEar, color = Color(0xFF38006B))

        // Diamond Gem Eyes (Cyan / Ice Blue)
        val eyeRadius = w * 0.08f * gemGlint
        val leftEyeCenter = Offset(w * 0.38f, h * 0.46f)
        val rightEyeCenter = Offset(w * 0.62f, h * 0.46f)

        // Left Diamond Eye
        drawCircle(Color(0xFF00E5FF), radius = eyeRadius, center = leftEyeCenter)
        drawCircle(Color.White, radius = eyeRadius * 0.4f, center = Offset(leftEyeCenter.x - 2f, leftEyeCenter.y - 2f))

        // Right Diamond Eye
        drawCircle(Color(0xFF00E5FF), radius = eyeRadius, center = rightEyeCenter)
        drawCircle(Color.White, radius = eyeRadius * 0.4f, center = Offset(rightEyeCenter.x - 2f, rightEyeCenter.y - 2f))

        // Sharp Tooth Smile
        val mouthPath = Path().apply {
            moveTo(w * 0.38f, h * 0.62f)
            lineTo(w * 0.44f, h * 0.68f)
            lineTo(w * 0.50f, h * 0.62f)
            lineTo(w * 0.56f, h * 0.68f)
            lineTo(w * 0.62f, h * 0.62f)
        }
        drawPath(mouthPath, color = Color.White, style = Stroke(width = 2.dp.toPx()))

        // Red Ruby Gem on Chest
        drawCircle(Color(0xFFFF1744), radius = w * 0.06f * gemGlint, center = Offset(w * 0.50f, h * 0.70f))
    }
}

@Composable
fun GenericPetCanvasSprite(petType: PetType, sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        drawCircle(Color(0xFF4A301C), radius = w * 0.30f, center = Offset(w * 0.50f, h * 0.48f))
        drawCircle(Color.White, radius = w * 0.05f, center = Offset(w * 0.42f, h * 0.42f))
        drawCircle(Color.White, radius = w * 0.05f, center = Offset(w * 0.58f, h * 0.42f))
        drawCircle(Color.Black, radius = w * 0.025f, center = Offset(w * 0.42f, h * 0.42f))
        drawCircle(Color.Black, radius = w * 0.025f, center = Offset(w * 0.58f, h * 0.42f))
    }
}
