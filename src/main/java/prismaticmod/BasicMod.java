package prismaticmod;

import basemod.AutoAdd;
import basemod.BaseMod;
import basemod.abstracts.CustomRelic;
import basemod.interfaces.*;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.relics.*;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import prismaticmod.cards.BaseCard;
import prismaticmod.util.GeneralUtils;
import prismaticmod.util.KeywordInfo;
import prismaticmod.util.TextureLoader;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.Patcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.localization.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scannotation.AnnotationDB;
import theprismatic.ThePrismatic;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static theprismatic.ThePrismatic.Enums.CARD_COLOR;

@SpireInitializer
public class BasicMod implements
        EditRelicsSubscriber,
        EditCardsSubscriber,
        EditCharactersSubscriber,
        EditStringsSubscriber,
        EditKeywordsSubscriber,
        PostInitializeSubscriber {
    public static ModInfo info;
    public static String modID; //Edit your pom.xml to change this
    static { loadModInfo(); }
    public static final Logger logger = LogManager.getLogger(modID); //Used to output to the console.
    private static final String resourcesFolder = "prismaticmod";

    private static final String BG_ATTACK = characterPath("cardback/bg_attack.png");
    private static final String BG_ATTACK_P = characterPath("cardback/bg_attack_p.png");
    private static final String BG_SKILL = characterPath("cardback/bg_skill.png");
    private static final String BG_SKILL_P = characterPath("cardback/bg_skill_p.png");
    private static final String BG_POWER = characterPath("cardback/bg_power.png");
    private static final String BG_POWER_P = characterPath("cardback/bg_power_p.png");
    private static final String ENERGY_ORB = characterPath("cardback/energy_orb.png");
    private static final String ENERGY_ORB_P = characterPath("cardback/energy_orb_p.png");
    private static final String SMALL_ORB = characterPath("cardback/small_orb.png");
    private static final Color cardColor = new Color(255f/255f, 255f/255f, 255f/255f, 1f);
    //red, green, blue, alpha. alpha is transparency, which should just be 1.
    private static final String CHAR_SELECT_BUTTON = characterPath("select/button.png");
    private static final String CHAR_SELECT_PORTRAIT = characterPath("select/portrait.png");

    //This is used to prefix the IDs of various objects like cards and relics,
    //to avoid conflicts between different mods using the same name for things.
    public static String makeID(String id) {
        return modID + ":" + id;
    }

    //This will be called by ModTheSpire because of the @SpireInitializer annotation at the top of the class.
    public static void initialize() {
        new BasicMod();

        BaseMod.addColor(CARD_COLOR, cardColor,
                BG_ATTACK, BG_SKILL, BG_POWER, ENERGY_ORB,
                BG_ATTACK_P, BG_SKILL_P, BG_POWER_P, ENERGY_ORB_P,
                SMALL_ORB);

    }

    public BasicMod() {
        BaseMod.subscribe(this); //This will make BaseMod trigger all the subscribers at their appropriate times.
        logger.info(modID + " subscribed to BaseMod.");
    }

    @Override
    public void receivePostInitialize() {
        //This loads the image used as an icon in the in-game mods menu.
        Texture badgeTexture = TextureLoader.getTexture(imagePath("badge.png"));
        //Set up the mod information displayed in the in-game mods menu.
        //The information used is taken from your pom.xml file.
        BaseMod.registerModBadge(badgeTexture, info.Name, GeneralUtils.arrToString(info.Authors), info.Description, null);
    }

    /*----------Localization----------*/

    //This is used to load the appropriate localization files based on language.
    private static String getLangString()
    {
        return Settings.language.name().toLowerCase();
    }
    private static final String defaultLanguage = "eng";

    public static final Map<String, KeywordInfo> keywords = new HashMap<>();

    @Override
    public void receiveEditStrings() {
        /*
            First, load the default localization.
            Then, if the current language is different, attempt to load localization for that language.
            This results in the default localization being used for anything that might be missing.
            The same process is used to load keywords slightly below.
        */
        loadLocalization(defaultLanguage); //no exception catching for default localization; you better have at least one that works.
        if (!defaultLanguage.equals(getLangString())) {
            try {
                loadLocalization(getLangString());
            }
            catch (GdxRuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadLocalization(String lang) {
        //While this does load every type of localization, most of these files are just outlines so that you can see how they're formatted.
        //Feel free to comment out/delete any that you don't end up using.
        BaseMod.loadCustomStringsFile(CardStrings.class,
                localizationPath(lang, "CardStrings.json"));
        BaseMod.loadCustomStringsFile(CharacterStrings.class,
                localizationPath(lang, "CharacterStrings.json"));
        BaseMod.loadCustomStringsFile(EventStrings.class,
                localizationPath(lang, "EventStrings.json"));
        BaseMod.loadCustomStringsFile(OrbStrings.class,
                localizationPath(lang, "OrbStrings.json"));
        BaseMod.loadCustomStringsFile(PotionStrings.class,
                localizationPath(lang, "PotionStrings.json"));
        BaseMod.loadCustomStringsFile(PowerStrings.class,
                localizationPath(lang, "PowerStrings.json"));
        BaseMod.loadCustomStringsFile(RelicStrings.class,
                localizationPath(lang, "RelicStrings.json"));
        BaseMod.loadCustomStringsFile(UIStrings.class,
                localizationPath(lang, "UIStrings.json"));
    }

    @Override
    public void receiveEditKeywords()
    {
        Gson gson = new Gson();
        String json = Gdx.files.internal(localizationPath(defaultLanguage, "Keywords.json")).readString(String.valueOf(StandardCharsets.UTF_8));
        KeywordInfo[] keywords = gson.fromJson(json, KeywordInfo[].class);
        for (KeywordInfo keyword : keywords) {
            keyword.prep();
            registerKeyword(keyword);
        }

        if (!defaultLanguage.equals(getLangString())) {
            try
            {
                json = Gdx.files.internal(localizationPath(getLangString(), "Keywords.json")).readString(String.valueOf(StandardCharsets.UTF_8));
                keywords = gson.fromJson(json, KeywordInfo[].class);
                for (KeywordInfo keyword : keywords) {
                    keyword.prep();
                    registerKeyword(keyword);
                }
            }
            catch (Exception e)
            {
                logger.warn(modID + " does not support " + getLangString() + " keywords.");
            }
        }
    }

    private void registerKeyword(KeywordInfo info) {
        BaseMod.addKeyword(modID.toLowerCase(), info.PROPER_NAME, info.NAMES, info.DESCRIPTION);
        if (!info.ID.isEmpty())
        {
            keywords.put(info.ID, info);
        }
    }

    //These methods are used to generate the correct filepaths to various parts of the resources folder.
    public static String localizationPath(String lang, String file) {
        return resourcesFolder + "/localization/" + lang + "/" + file;
    }

    public static String imagePath(String file) {
        return resourcesFolder + "/images/" + file;
    }
    public static String characterPath(String file) {
        return resourcesFolder + "/images/character/" + file;
    }
    public static String powerPath(String file) {
        return resourcesFolder + "/images/powers/" + file;
    }
    public static String relicPath(String file) {
        return resourcesFolder + "/images/relics/" + file;
    }


    //This determines the mod's ID based on information stored by ModTheSpire.
    private static void loadModInfo() {
        Optional<ModInfo> infos = Arrays.stream(Loader.MODINFOS).filter((modInfo)->{
            AnnotationDB annotationDB = Patcher.annotationDBMap.get(modInfo.jarURL);
            if (annotationDB == null)
                return false;
            Set<String> initializers = annotationDB.getAnnotationIndex().getOrDefault(SpireInitializer.class.getName(), Collections.emptySet());
            return initializers.contains(BasicMod.class.getName());
        }).findFirst();
        if (infos.isPresent()) {
            info = infos.get();
            modID = info.ID;
        }
        else {
            throw new RuntimeException("Failed to determine mod info/ID based on initializer.");
        }
    }

    @Override
    public void receiveEditCharacters() {
        BaseMod.addCharacter(new ThePrismatic(),
                CHAR_SELECT_BUTTON, CHAR_SELECT_PORTRAIT, ThePrismatic.Enums.Prismatic);
    }

    @Override
    public void receiveEditCards() {
        new AutoAdd(modID) //Loads files from this mod
                .packageFilter(BaseCard.class) //In the same package as this class
                .setDefaultSeen(true) //And marks them as seen in the compendium
                .cards(); //Adds the cards
    }
    public void receiveEditRelics()
    {
        // This finds and adds all relics inheriting from CustomRelic that are in the same package
        // as MyRelic, keeping all as unseen except those annotated with @AutoAdd.Seen
        new AutoAdd(modID)
                .any(CustomRelic.class, (info, relic) -> {
                    BaseMod.addRelicToCustomPool(relic, CARD_COLOR);
                    if (info.seen) {
                        UnlockTracker.markRelicAsSeen(relic.relicId);
                    }
                });
        /*
        BaseMod.addRelicToCustomPool(new Abacus(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Akabeko(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Anchor(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new AncientTeaSet(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new ArtOfWar(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Astrolabe(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new BagOfMarbles(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new BagOfPreparation(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new BirdFacedUrn(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new BlackStar(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new BloodVial(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new BloodyIdol(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new BlueCandle(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Boot(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new BottledFlame(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new BottledLightning(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new BottledTornado(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new BronzeScales(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new BustedCrown(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Calipers(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new CallingBell(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new CaptainsWheel(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new Cauldron(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new CentennialPuzzle(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new CeramicFish(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new ChemicalX(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new ClockworkSouvenir(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new CoffeeDripper(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Courier(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new CultistMask(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new CursedKey(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new DarkstonePeriapt(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new DeadBranch(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new DollysMirror(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new DreamCatcher(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new DuVuDoll(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new Ectoplasm(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new EmptyCage(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Enchiridion(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new EternalFeather(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new FaceOfCleric(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new FossilizedHelix(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new FrozenEgg2(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new FrozenEye(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new FusionHammer(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new GamblingChip(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Ginger(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Girya(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new GoldenIdol(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new GremlinHorn(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new GremlinMask(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new HandDrill(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new HappyFlower(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new HornCleat(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new IceCream(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new IncenseBurner(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new InkBottle(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new JuzuBracelet(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Kunai(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Lantern(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new LetterOpener(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new LizardTail(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Mango(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new MarkOfTheBloom(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Matryoshka(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new MawBank(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new MealTicket(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new MeatOnTheBone(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new MedicalKit(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new MembershipCard(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new MercuryHourglass(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new MoltenEgg2(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new MummifiedHand(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new MutagenicStrength(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Necronomicon(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new NeowsLament(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new NilrysCodex(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new NlothsGift(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new NlothsMask(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Nunchaku(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new OddlySmoothStone(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new OddMushroom(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new OldCoin(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Omamori(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new OrangePellets(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Orichalcum(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new OrnamentalFan(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Orrery(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new PandorasBox(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Pantograph(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new PeacePipe(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Pear(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new PenNib(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new PhilosopherStone(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Pocketwatch(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new PotionBelt(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new PrayerWheel(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new PreservedInsect(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new PrismaticShard(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new QuestionCard(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new RedMask(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new RegalPillow(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new RunicDome(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new RunicPyramid(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new SacredBark(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Shovel(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Shuriken(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new SingingBowl(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new SlaversCollar(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Sling(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new SmilingMask(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new SneckoEye(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Sozu(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new SpiritPoop(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new SsserpentHead(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new StoneCalendar(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new StrangeSpoon(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Strawberry(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new StrikeDummy(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Sundial(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new ThreadAndNeedle(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new TinyChest(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new TinyHouse(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Toolbox(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Torii(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new ToxicEgg2(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new ToyOrnithopter(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new TungstenRod(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Turnip(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new UnceasingTop(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Vajra(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new VelvetChoker(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Waffle(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new WarPaint(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new WarpedTongs(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Whetstone(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new WhiteBeast(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new WingBoots(), CARD_COLOR);
        */
        BaseMod.addRelicToCustomPool(new HoveringKite(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new NinjaScroll(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new PaperCrane(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new RingOfTheSerpent(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new SnakeRing(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new SneckoSkull(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new TheSpecimen(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Tingsha(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new ToughBandages(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new TwistedFunnel(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new WristBlade(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new BlackBlood(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Brimstone(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new BurningBlood(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new ChampionsBelt(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new CharonsAshes(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new MagicFlower(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new MarkOfPain(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new PaperFrog(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new RedSkull(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new RunicCube(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new SelfFormingClay(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new CrackedCore(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new DataDisk(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new EmotionChip(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new FrozenCore(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new GoldPlatedCables(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new Inserter(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new NuclearBattery(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new RunicCapacitor(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new SymbioticVirus(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new CloakClasp(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Damaru(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new GoldenEye(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new HolyWater(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new Melange(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new PureWater(), CARD_COLOR);
        //BaseMod.addRelicToCustomPool(new VioletLotus(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new TeardropLocket(), CARD_COLOR);
        BaseMod.addRelicToCustomPool(new Duality(), CARD_COLOR);
    }
}
