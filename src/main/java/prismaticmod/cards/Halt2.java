package prismaticmod.cards;

import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import prismaticmod.util.CardStats;
import theprismatic.ThePrismatic;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.player;

public class Halt2 extends BaseCard {
    public static final String ID = makeID("Halt"); //makeID adds the mod ID, so the final ID will be something like "modID:MyCard"
    private static final CardStats info = new CardStats(
            ThePrismatic.Enums.Purple, //The card color. If you're making your own character, it'll look something like this. Otherwise, it'll be CardColor.RED or something similar for a basegame character color.
            CardType.SKILL, //The type. ATTACK/SKILL/POWER/CURSE/STATUS
            CardRarity.COMMON, //Rarity. BASIC is for starting cards, then there's COMMON/UNCOMMON/RARE, and then SPECIAL and CURSE. SPECIAL is for cards you only get from events. Curse is for curses, except for special curses like Curse of the Bell and Necronomicurse.
            CardTarget.SELF, //The target. Single target is ENEMY, all enemies is ALL_ENEMY. Look at cards similar to what you want to see what to use.
            0 //The card's base cost. -1 is X cost, -2 is no cost for unplayable cards like curses, or Reflex.
    );
    //These will be used in the constructor. Technically you can just use the values directly,
    //but constants at the top of the file are easy to adjust.

    public Halt2() {
        super(ID, info); //Pass the required information to the BaseCard constructor.
        this.block = this.baseBlock = 3;
        this.magicNumber = this.baseMagicNumber = 7;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        if (player.stance.ID.equals("Neutral")) {
            addToBot(new GainBlockAction(p, this.block));
        } else {
            addToBot(new GainBlockAction(p, this.block + this.magicNumber));
        }
    }
    public void applyPowers() {
        this.baseBlock += 4 + this.timesUpgraded * 2;
        this.baseMagicNumber = this.baseBlock;
        super.applyPowers();
        this.magicNumber = this.block;
        this.isMagicNumberModified = this.isBlockModified;
        this.baseBlock -= 4 + this.timesUpgraded * 2;
        super.applyPowers();
    }

    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBlock(1);
            this.baseMagicNumber = this.baseBlock + 4 + this.timesUpgraded * 2;
            this.upgradedMagicNumber = this.upgradedBlock;
        }
    }
}
