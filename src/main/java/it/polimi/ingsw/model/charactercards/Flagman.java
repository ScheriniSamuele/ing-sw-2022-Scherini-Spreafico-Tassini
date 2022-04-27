package it.polimi.ingsw.model.charactercards;

import it.polimi.ingsw.model.CharacterCard;
import it.polimi.ingsw.model.GameExpertMode;
import it.polimi.ingsw.model.exceptions.IslandNotFoundException;

public class Flagman extends CharacterCard {

    /*
        CHARACTER CARD DESCRIPTION:
        The flagman allows the player to call the islandConquerCheck method on a selected island. This method call
        doesn't prevent the player to call it another time during the normal round flow.
    */

    public Flagman() {
        super(3, 3);
    }

    // Param: islandID
    @Override
    public void doEffect(GameExpertMode game){
        // TODO: parsing input from the view
        int islandID = 1; // placeholder
        try {
            game.getBoard().islandConquerCheck(game.getCurrentPlayer(), islandID);
        }
        // the view/controller will be responsible to send the method a valid island ID
        catch(IslandNotFoundException ignored){}
    }
}
