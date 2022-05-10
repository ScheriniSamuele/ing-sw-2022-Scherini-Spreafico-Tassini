package it.polimi.ingsw.model.exceptions;

import it.polimi.ingsw.exceptions.NonExistentColorException;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Wizard;
import it.polimi.ingsw.utils.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NonExistentColorExceptionTest {

    @Test
    public void exceptionTest(){

        Game g1 = new Game(2, new Constants(2));
        Player p1 = new Player(Wizard.BLUE_WIZARD, "Matteo", g1.getConstants());
        g1.addPlayer(p1);
        g1.setCurrentPlayer(p1);

        assertThrows(NonExistentColorException.class,
                () -> p1.getSchool().getTable("BLACK"));

    }

}