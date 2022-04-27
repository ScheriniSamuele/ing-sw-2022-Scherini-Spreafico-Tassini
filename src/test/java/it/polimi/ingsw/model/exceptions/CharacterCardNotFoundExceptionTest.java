package it.polimi.ingsw.model.exceptions;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.charactercards.Bard;
import it.polimi.ingsw.model.charactercards.Centaur;
import it.polimi.ingsw.model.charactercards.Flagman;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharacterCardNotFoundExceptionTest {

    @Test
    public void exceptionTest(){

        GameExpertMode g1 = new GameExpertMode(2);
        Player p1 = new Player(Wizard.BLUE_WIZARD, "Matteo", g1.getPlayersNumber());
        g1.addPlayer(p1);
        g1.setCurrentPlayer(p1);
        p1.setCoinsWallet(10);
        CharacterCard[] cards = new CharacterCard[Constants.CHARACTERS_NUM];
        cards[0] = new Bard();
        cards[1] = new Centaur();
        cards[2] = new Flagman();
        g1.addCharacterCards(cards);

        assertThrows(CharacterCardNotFoundException.class,
                () -> g1.playerPlaysCharacterCard(1));

    }

}