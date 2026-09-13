package mage.view;

import mage.game.Game;
import mage.game.command.Phenomenon;
import mage.players.PlayableObjectStats;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** Public command-object view of a Phenomenon. */
public class PhenomenonView implements CommandObjectView, Serializable {

    protected UUID id;
    protected String name;
    protected String imageFileName = "";
    protected int imageNumber;
    protected String expansionSetCode = "";
    protected List<String> rules;
    protected PlayableObjectStats playableStats = new PlayableObjectStats();

    public PhenomenonView(Phenomenon phenomenon, Game game) {
        this.id = phenomenon.getId();
        this.name = phenomenon.getName();
        this.imageFileName = phenomenon.getImageFileName();
        this.imageNumber = phenomenon.getImageNumber();
        this.expansionSetCode = phenomenon.getExpansionSetCode();
        this.rules = phenomenon.getAbilities().getRules(game, phenomenon);
    }

    @Override
    public String getExpansionSetCode() {
        return expansionSetCode;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getImageFileName() {
        return imageFileName;
    }

    @Override
    public int getImageNumber() {
        return imageNumber;
    }

    @Override
    public List<String> getRules() {
        return rules;
    }

    @Override
    public boolean isPlayable() {
        return playableStats.getPlayableAmount() > 0;
    }

    @Override
    public void setPlayableStats(PlayableObjectStats playableStats) {
        this.playableStats = playableStats;
    }

    @Override
    public PlayableObjectStats getPlayableStats() {
        return playableStats;
    }

    @Override
    public boolean isChoosable() {
        return false;
    }

    @Override
    public void setChoosable(boolean isChoosable) {
    }

    @Override
    public boolean isSelected() {
        return false;
    }

    @Override
    public void setSelected(boolean isSelected) {
    }
}
