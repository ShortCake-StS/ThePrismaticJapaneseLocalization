package prismaticmod.cards;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.ArtifactPower;
import com.megacrit.cardcrawl.powers.EnvenomPower;
import com.megacrit.cardcrawl.powers.PoisonPower;
import com.megacrit.cardcrawl.powers.watcher.FreeAttackPower;
import prismaticmod.relics.WristBlade2;
import prismaticmod.util.CardStats;
import theprismatic.ThePrismatic;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.player;

public class Bane2 extends BaseCard {
    public static final String ID = makeID("Bane"); //makeID adds the mod ID, so the final ID will be something like "modID:MyCard"
    private static final CardStats info = new CardStats(
            ThePrismatic.Enums.Green, //The card color. If you're making your own character, it'll look something like this. Otherwise, it'll be CardColor.RED or something similar for a base game character color.
            CardType.ATTACK, //The type. ATTACK/SKILL/POWER/CURSE/STATUS
            CardRarity.COMMON, //Rarity. BASIC is for starting cards, then there's COMMON/UNCOMMON/RARE, and then SPECIAL and CURSE. SPECIAL is for cards you only get from events. Curse is for curses, except for special curses like Curse of the Bell and Necronomicurse.
            CardTarget.ENEMY, //The target. Single target is ENEMY, all enemies is ALL_ENEMY. Look at cards similar to what you want to see what to use.
            0 //The card's base cost. -1 is X cost, -2 is no cost for unplayable cards like curses, or Reflex.
    );
    //These will be used in the constructor. Technically you can just use the values directly,
    //but constants at the top of the file are easy to adjust.

    private static final int DAMAGE = 5;
    private static final int UPG_DAMAGE = 2;
    private static final int baseMagicNumber = 3;
    private static final int UPG_Number = 1;

    public Bane2() {
        super(ID, info); //Pass the required information to the BaseCard constructor.
        setDamage(DAMAGE, UPG_DAMAGE); //Sets the card's damage and how much it changes when upgraded.
        setMagic(baseMagicNumber, UPG_Number);
    }
    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        int debuffs = 0;
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
        for(AbstractPower power : m.powers){
            if(power.type == AbstractPower.PowerType.DEBUFF && !power.ID.equals("Shackled")){
                debuffs++;
            }
        }
        if(p.hasPower(EnvenomPower.POWER_ID) && !m.hasPower(ArtifactPower.POWER_ID) && !m.hasPower(PoisonPower.POWER_ID)){
            debuffs++;
        }
        if(p.hasRelic(WristBlade2.ID) && !m.hasPower(ArtifactPower.POWER_ID) && !m.hasPower(PoisonPower.POWER_ID)
                && !p.hasPower(EnvenomPower.POWER_ID) && !(this.costForTurn == 0 || this.freeToPlayOnce || player.hasPower(FreeAttackPower.POWER_ID))){
            debuffs++;
        }
        if(debuffs >= 2){
            addToBot(new ApplyPowerAction(m, p, new PoisonPower(m, p, magicNumber), magicNumber));
        }
    }
}
