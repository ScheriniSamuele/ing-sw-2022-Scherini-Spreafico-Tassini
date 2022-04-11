package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.FullTableException;
import it.polimi.ingsw.model.exceptions.NonExistentColorException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TableTest{

    @Test
    public void testAddStudent() throws NonExistentColorException {

        Player p1 = new Player(Wizard.PINK_WIZARD, "Ludo");

        try {
            p1.getSchool().getTable("YELLOW").addStudent(new Student(Color.YELLOW), p1);
            p1.getSchool().getTable("GREEN").addStudent(new Student(Color.GREEN), p1);
            p1.getSchool().getTable("PINK").addStudent(new Student(Color.PINK), p1);
        }
        catch(FullTableException e){}

        assertEquals(1, p1.getCoinsWallet());

        try {
            p1.getSchool().getTable("YELLOW").addStudent(new Student(Color.YELLOW), p1);
            p1.getSchool().getTable("YELLOW").addStudent(new Student(Color.YELLOW), p1);
        }
        catch(FullTableException e){}

        assertEquals(2, p1.getCoinsWallet());

    }

    @Test
    public void testCoinCheck() throws NonExistentColorException {

        Player p1 = new Player(Wizard.PINK_WIZARD, "Ludo");

        //we should start with one coin
        assertEquals(1, p1.getCoinsWallet());

        try {
            p1.getSchool().getTable("YELLOW").addStudent(new Student(Color.YELLOW), p1);
            p1.getSchool().getTable("YELLOW").addStudent(new Student(Color.YELLOW), p1);
            p1.getSchool().getTable("YELLOW").addStudent(new Student(Color.YELLOW), p1);
        }
        catch(FullTableException e){}

        //one coin should be added
        assertEquals(2, p1.getCoinsWallet());

        try {
            p1.getSchool().getTable("PINK").addStudent(new Student(Color.PINK), p1);
        }
        catch(FullTableException e){}

        //no coin should be added
        assertEquals(2, p1.getCoinsWallet());
    }
}