package fr.roro.fightforsub.game.team;

import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.server.v1_8_R3.EnumChatFormat;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;

/**
 * This file is a part of FightForSub project.
 *
 * @author roro1506_HD
 */
public class TeamNameHider extends ScoreboardTeam {

    public TeamNameHider() {
        super(null, "");
    }

    @Override
    public String getName() {
        return "NameHider";
    }

    @Override
    public String getDisplayName() {
        return "NameHider";
    }

    @Override
    public Collection<String> getPlayerNameSet() {
        return new ArrayList<>();
    }

    @Override
    public String getPrefix() {
        return "";
    }

    @Override
    public String getSuffix() {
        return "";
    }

    @Override
    public boolean allowFriendlyFire() {
        return true;
    }

    @Override
    public boolean canSeeFriendlyInvisibles() {
        return false;
    }

    @Override
    public EnumNameTagVisibility getNameTagVisibility() {
        return EnumNameTagVisibility.NEVER;
    }

    @Override
    public EnumNameTagVisibility j() {
        return EnumNameTagVisibility.NEVER;
    }

    @Override
    public int packOptionData() {
        return 1;
    }

    @Override
    public EnumChatFormat l() {
        return EnumChatFormat.WHITE;
    }
}
