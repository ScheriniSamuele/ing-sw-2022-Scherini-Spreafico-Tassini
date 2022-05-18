package it.polimi.ingsw.model.charactercards;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.exceptions.IslandNotFoundException;

import java.io.Serializable;
import java.util.Optional;

public class Centaur extends CharacterCard implements Serializable {

    /*
        CHARACTER CARD DESCRIPTION:
        The centaur allows the player to trigger a special version of the islandConquerCheck method, which
        does not count into the influence the towers' effect.
    */

    public Centaur() {
        super(6, 3);
    }

    // Does not need parameters
    public void doEffect(GameExpertMode game){

        //checks if an island can be conquered without counting the towers number

        try {
            Island selectedIsland = game.getBoard().getIslands().getIslandFromID(game.getBoard().getMotherNaturePos());
            if(selectedIsland.hasVeto()) {
                selectedIsland.setVeto(false);
                game.getBoard().setNumOfVetos(game.getBoard().getNumOfVetos() + 1);
                return;
            }
            Player currentPlayer = game.getCurrentPlayer();
            Player owner = selectedIsland.getOwner();
            if(owner != null) {
                if (!owner.equals(currentPlayer)) {
                    int calcCurrent = selectedIsland.influenceCalcWithoutTowers(currentPlayer);
                    int calcOwner = selectedIsland.influenceCalcWithoutTowers(owner);
                    GameBoard.islandConquerAlgorithm(currentPlayer, selectedIsland, calcCurrent, calcOwner,
                            game.getBoard().getIslands());
                }
            }
            else{
                int calcCurrent = selectedIsland.influenceCalcWithoutTowers(currentPlayer);
                GameBoard.islandConquerAlgorithm(currentPlayer, selectedIsland, calcCurrent, 0,
                        game.getBoard().getIslands());
            }
        // it is guaranteed that IslandNotFound will never be called
        } catch (IslandNotFoundException ignored) {}

    }
}
