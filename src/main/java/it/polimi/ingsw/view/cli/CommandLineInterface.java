package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.controller.ClientController;
import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.GameBoard;
import it.polimi.ingsw.observers.ViewObservable;
import it.polimi.ingsw.utils.ANSIConstants;
import it.polimi.ingsw.view.View;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * This class offers a User Interface via terminal. It is an implementation of the {@link View}.
 */

public class CommandLineInterface extends ViewObservable implements View {

    private final PrintStream out;

    /**
     * CommandLineInterface constructor.
     */

    public CommandLineInterface() {
        this.out = System.out;
    }

    /**
     * Reads a line from the standard input.
     *
     * @return the string read from the input.
     * @throws ExecutionException if the input stream thread is interrupted.
     */

    private String readLine() throws ExecutionException{
        FutureTask<String> futureTask = new FutureTask<>(new InputReadTask());
        Thread inputThread = new Thread(futureTask);
        inputThread.start();
        String input = null;
        try {
            input = futureTask.get();
        }
        catch (InterruptedException e) {
            futureTask.cancel(true);
            Thread.currentThread().interrupt();
        }
        return input;
    }

    /**
     * This method shows the client the Eriantys logo and starts the method chain to connect to the server.
     */

    public void init(){
        out.println("""
                   ('-.  _  .-')             ('-.         .-') _  .-') _                 .-')   \s
                 _(  OO)( \\( -O )           ( OO ).-.    ( OO ) )(  OO) )               ( OO ). \s
                (,------.,------.  ,-.-')   / . --. /,--./ ,--,' /     '._  ,--.   ,--.(_)---\\_)\s
                 |  .---'|   /`. ' |  |OO)  | \\-.  \\ |   \\ |  |\\ |'--...__)  \\  `.'  / /    _ | \s
                 |  |    |  /  | | |  |  \\.-'-'  |  ||    \\|  | )'--.  .--'.-')     /  \\  :` `. \s
                (|  '--. |  |_.' | |  |(_/ \\| |_.'  ||  .     |/    |  |  (OO  \\   /    '..`''.)\s
                 |  .--' |  .  '.',|  |_.'  |  .-.  ||  |\\    |     |  |   |   /  /\\_  .-._)   \\\s
                 |  `---.|  |\\  \\(_|  |     |  | |  ||  | \\   |     |  |   `-./  /.__) \\       /\s
                 `------'`--' '--' `--'     `--' `--'`--'  `--'     `--'     `--'       `-----' \s""");

        out.println("Welcome to Eriantys!\n");
        askServerData();
    }

    /**
     * Asks the client information about what server they want to try to connect to.
     */

    @Override
    public void askServerData(){
        String address;
        String port;

        boolean addressIsValid;
        boolean portIsValid;

        String defaultAddress = "127.0.0.1";
        int defaultPort = 12345;

        out.println("Please enter the following values to connect to the server [type enter to use default values]");
        do{
            out.println("Enter the server IP address [ default = "+defaultAddress+" ] : ");
            try {
                addressIsValid = true;
                address = readLine();
                if(address.equals("")) address = defaultAddress;
                else if(!ClientController.isValidAddress(address)){
                    addressIsValid = false;
                    out.println("The address is not valid, please enter the value again: ");
                }
            }
            catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }while(!addressIsValid);

        do{
            out.println("Enter the server port [ default = "+defaultPort+" ] : ");
            try {
                portIsValid = true;
                port = readLine();
                if(port.equals("")) port = String.valueOf(defaultPort);
                else if(!ClientController.isValidPort(port)) {
                    portIsValid = false;
                    out.println("The port is not valid, please enter the value again: ");
                }
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }while(!portIsValid);

        int finalPortValue = Integer.parseInt(port);
        String finalAddress = address;

        notifyObserver(viewObserver -> viewObserver.onUpdateServerData(finalAddress, finalPortValue));
    }

    /**
     * Asks the client which nickname they want to use.
     * If it is unique, the client joins the lobby; otherwise, they are asked to choose a different nickname.
     */

    @Override
    public void askNickname() {
        out.println("Enter your nickname [must be unique] : ");
        try {
            String nickname = readLine();
            while (nickname.equalsIgnoreCase("")){
                out.println("Please enter a valid nickname [must be unique] : ");
                nickname = readLine();
            }
            String finalNickname = nickname;
            notifyObserver(viewObserver -> viewObserver.onUpdateNickname(finalNickname));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Asks the client if they want to create or join a game.
     */

    @Override
    public void askCreateOrJoin() {
        out.println("Choose whether to create a new game or to join an existing one [ type CREATE to create or JOIN to join ] :");
        String choice;
        try {
            do {
                choice = readLine();
                if(!choice.equalsIgnoreCase("CREATE") && !choice.equalsIgnoreCase("JOIN"))
                    out.println("Please enter a valid choice [ type CREATE to create or JOIN to join ] : ");
            }while(!choice.equalsIgnoreCase("CREATE") && !choice.equalsIgnoreCase("JOIN"));
            String finalChoice = choice;
            notifyObserver(viewObserver -> viewObserver.onUpdateCreateOrJoin(finalChoice));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Asks the client about the game info: number of players and game mode.
     */

    @Override
    public void askGameInfo() {
        out.println("Please insert all the required parameters in order to create a new game: ");
        try {
            int gameNumber = 0;
            do{
                try {
                    out.println("Type the number (ID) of the game you want to create [ game number must be unique and greater than zero ]: ");
                    gameNumber = Integer.parseInt(readLine());
                    if(gameNumber <= 0)
                        out.println("The ID must be greater than zero!");
                }
                catch(NumberFormatException e){
                    out.println("Please type a valid number.");
                }
            }while(gameNumber <= 0);
            out.println("Choose the type of game you want to play [ For Expert mode type EXPERT, for Normal mode type NORMAL ] : ");
            String str = readLine().toUpperCase();
            while (!str.equals("EXPERT") && !str.equals("NORMAL")){
                    out.println("The given input is not correct, please retry. " +
                        "\nChoose the type of game you want to play [ For Expert mode type EXPERT, for normal mode type NORMAL ] : ");
                str = readLine().toUpperCase();
            }
            boolean expertMode = str.equals("EXPERT");
            int numOfPlayers = 0;
            do {
                try {
                    out.println("Enter the desired number of players [ 2 or 3 ] : ");
                    numOfPlayers = Integer.parseInt(readLine());
                    if (numOfPlayers != 2 && numOfPlayers != 3)
                        out.println("Please enter a valid choice.");
                }
                catch(NumberFormatException e){
                    out.println("Please type a valid number.");
                }
            }while(numOfPlayers != 2 && numOfPlayers != 3);
            int finalNumOfPlayers = numOfPlayers;
            int finalGameNumber = gameNumber;
            notifyObserver(viewObserver -> viewObserver.onUpdateGameInfo(finalGameNumber, expertMode, finalNumOfPlayers));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Asks the client the gameID (which must be unique).
     * If it is unique a new game is created, else they are asked to choose a different gameID.
     */

    @Override
    public void askGameNumber() {
        try {
            int gameNumber = 0;
            do {
                try {
                    out.println("Type the number of the game you want to join: ");
                    gameNumber = Integer.parseInt(readLine());
                    if(gameNumber <= 0)
                        out.println("The game ID must be greater than zero!");
                }
                catch(NumberFormatException e){
                    out.println("Please type a valid number.");
                }
            }while(gameNumber <= 0);
            int finalGameNumber = gameNumber;
            notifyObserver(viewObserver -> viewObserver.onUpdateGameNumber(finalGameNumber));
        }
        catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Asks the client which WizardID they want to choose (must be unique).
     * If it is unique the client is added to the game, else they are asked to choose a different Wizard.
     */

    @Override
    public void askWizardID() {
        out.println("Choose your wizard for this game between [ BLUE_WIZARD | YELLOW_WIZARD | GREEN_WIZARD | PINK_WIZARD ] : ");
        out.println("(The wizard must be unique for each player)");
        try {
            String wizard = readLine().toUpperCase();
            while (!wizard.equals("BLUE_WIZARD") && !wizard.equals("PINK_WIZARD")
                    && !wizard.equals("YELLOW_WIZARD") && !wizard.equals("GREEN_WIZARD")){
                out.println("The given input is not correct, please retry. \nChoose between [ BLUE_WIZARD | PINK_WIZARD | YELLOW_WIZARD | GREEN_WIZARD ]: ");
                wizard = readLine().toUpperCase();
            }
            String finalWizard = wizard.toUpperCase();
            notifyObserver(viewObserver -> viewObserver.onUpdateWizardID(finalWizard));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException ignored){}
    }

    /**
     * Asks the client which Assistant Card they want to play.
     */

    @Override
    public void askAssistantCard() {
        out.println("Select the Assistant Card you want to play: ");
        try {
            String assistantCard = readLine().toUpperCase();
            notifyObserver(viewObserver -> viewObserver.onUpdateAssistantCard(assistantCard));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException ignored){}
    }

    /**
     * Asks the client where they want to move the student (on an island or on the correspondent table).
     */

    @Override
    public void askMoveStudent(){

        try {
            String choice;
            do{
                out.println("Type ISLAND if you want to move a student to an Island, TABLE if you want to move it to its Table:");
                choice = readLine().toUpperCase();
                if(!choice.equals("ISLAND") && !choice.equals("TABLE")) {
                    out.println("The given input is not correct, please try again.");
                }
            }while (!choice.equals("ISLAND") && !choice.equals("TABLE"));

            if(choice.equals("ISLAND")) {
                out.println("Which student do you want to move to an Island? Please type a valid color " +
                        "[YELLOW, BLUE, GREEN, RED, PINK]:");
                String color;
                do {
                    color = readLine().toUpperCase();
                }
                while(isColorInvalid(color));

                int islandID = 0;
                do {
                    try {
                        out.println("Towards which island? Please type a valid number:");
                        islandID = Integer.parseInt(readLine());
                        if (islandID < 1 || islandID > 12) {
                           out.println("The given input is not correct, please try again.");
                        }
                    }
                    catch(NumberFormatException e){
                        out.println("Please type a valid number.");
                    }
                }while (islandID < 1 || islandID > 12);
                String finalColor = color;
                int finalIslandID = islandID;
                notifyObserver(viewObserver -> viewObserver.onUpdateIslandStudentMove(finalColor, finalIslandID));
            }

            if(choice.equals("TABLE")){
                out.println("Which student do you want to move to its Table? Please type a valid color " +
                        "[YELLOW, BLUE, GREEN, RED, PINK]:");
                String color;
                do {
                    color = readLine().toUpperCase();
                }
                while(isColorInvalid(color));
                String finalColor = color;
                notifyObserver(viewObserver -> viewObserver.onUpdateTableStudentMove(finalColor));
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException ignored){}
    }

    /**
     * Asks the client how many steps they wish to move Mother Nature of.
     */

    @Override
    public void askMotherNatureSteps() {

        try {
            int steps = 0;
            do {
                try {
                    out.println("How many steps do you wish Mother Nature has to move of? Please type a valid number:");
                    String string = readLine();
                    if(string != null) {
                        steps = Integer.parseInt(string);
                        if (steps < 1 || steps > 7)
                            out.println("The given input is not correct, please try again.");
                    }
                    else{
                        throw new NullPointerException();
                    }
                }
                catch(NumberFormatException e){
                    out.println("Please type a valid number.");
                }
            }while (steps < 1 || steps > 7);
            int finalSteps = steps;
            notifyObserver(viewObserver -> viewObserver.onUpdateMotherNatureSteps(finalSteps));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        catch (NullPointerException ignored){}
    }

    /**
     * Asks the client which cloud they want to choose.
     */

    @Override
    public void askCloud() {

        try {
            int cloudID = -1;
            do {
                try {
                    out.println("Which cloud do you want to pick students from? Please type a valid number:");
                    String string = readLine();
                    if(string != null) {
                        cloudID = Integer.parseInt(string) - 1;
                        if (cloudID < 0 || cloudID > 2)
                            out.println("The given input is not correct, please try again.");
                    }
                    else throw new NullPointerException();
                }
                catch(NumberFormatException e){
                    out.println("Please type a valid number.");
                }
            }while(cloudID < 0 || cloudID > 2);
            int finalCloudID = cloudID;
            notifyObserver(viewObserver -> viewObserver.onUpdateCloudChoice(finalCloudID));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException ignored){}
    }

    /**
     * Asks the client which Character Card they want to play (Expert mode exclusive).
     */

    @Override
    public void askCharacterCard() {

        try {
            int characterCardID = 0;
            do {
                try {
                    out.println("Which Character Card do you want to use? Please type a valid number:");
                    characterCardID = Integer.parseInt(readLine());
                    if (characterCardID < 1 || characterCardID > 12)
                        out.println("The given input is not correct, please try again.");
                }
                catch(NumberFormatException e){
                    out.println("Please type a valid number.");
                }
            }while(characterCardID < 1 || characterCardID > 12);
            int finalCharacterCardID = characterCardID;
            switch (characterCardID) {
                case 2, 4, 6, 8 -> notifyObserver(viewObserver -> viewObserver.onUpdateCharacterCard(finalCharacterCardID));
                case 3, 5 -> {
                    int par = 0;
                    do {
                        try {
                            out.println("Please enter a valid parameter (number):");
                            par = Integer.parseInt(readLine());
                        } catch (NumberFormatException e) {
                            out.println("Please type a valid number.");
                        }
                    }while(par <= 0);
                    int finalPar = par;
                    notifyObserver(viewObserver -> viewObserver.onUpdateCharacterCardInt(finalCharacterCardID, finalPar));
                }
                case 9, 11, 12 -> {
                    out.println("Please enter a valid parameter (color):");
                    String color;
                    do {
                        color = readLine().toUpperCase();
                    }
                    while(isColorInvalid(color));
                    String par = color;
                    notifyObserver(viewObserver -> viewObserver.onUpdateCharacterCardString(finalCharacterCardID, par));
                }
                case 1 -> {
                    out.println("Please enter a valid parameter (color):");
                    String color;
                    do {
                        color = readLine().toUpperCase();
                    }
                    while(isColorInvalid(color));
                    String par1 = color;
                    int par2 = 0;
                    do{
                        try {
                            out.println("Please enter a valid parameter (islandID number):");
                            par2 = Integer.parseInt(readLine());
                        } catch (NumberFormatException e) {
                            out.println("Please type a valid number.");
                        }
                    } while (par2 < 1 || par2 > 12);
                    int finalPar = par2;
                    notifyObserver(viewObserver -> viewObserver.onUpdateCharacterCardStringInt(finalCharacterCardID, par1, finalPar));
                }
                case 7 -> {
                    out.println("Please enter a valid list of colors (0, 2, 4 or 6 colors), STOP to end the list.");
                    ArrayList<String> par = new ArrayList<>();
                    while (true) {
                        if (par.size() == 6)
                            break;
                        out.println("Please enter the color of the hall student (STOP to end):");
                        String par1 = readLine().toUpperCase();
                        if (par1.equals("STOP"))
                            break;
                        String color1;
                        do {
                            color1 = readLine().toUpperCase();
                        }
                        while(isColorInvalid(color1));
                        par.add(color1);
                        out.println("Please enter the color of the card student:");
                        String color2;
                        do {
                            color2 = readLine().toUpperCase();
                        }
                        while(isColorInvalid(color2));
                        par.add(color2);
                    }
                    notifyObserver(viewObserver -> viewObserver.onUpdateCharacterCardArrayListString(finalCharacterCardID, par));
                }
                case 10 -> {
                    out.println("Please enter a valid list of colors (0, 2 or 4 colors), STOP to end the list.");
                    ArrayList<String> par = new ArrayList<>();
                    while (true) {
                        if (par.size() == 4)
                            break;
                        out.println("Please enter the color of the hall student (STOP to end):");
                        String par1 = readLine().toUpperCase();
                        if (par1.equals("STOP"))
                            break;
                        String color1;
                        do {
                            color1 = readLine().toUpperCase();
                        }
                        while(isColorInvalid(color1));
                        par.add(color1);
                        out.println("Please enter the color of the table student:");
                        String color2;
                        do {
                            color2 = readLine().toUpperCase();
                        }
                        while(isColorInvalid(color2));
                        par.add(color2);
                    }
                    notifyObserver(viewObserver -> viewObserver.onUpdateCharacterCardArrayListString(finalCharacterCardID, par));
                }
                default -> {} // should never enter here
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Asks the client if they want to play a CharacterCard or to move a student (Expert mode exclusive).
     */

    @Override
    public void askAction() {
        out.println("What do you want to do? Please type STUDENT to move a student, CARD to play a Character Card: ");
        try {
            String choice = readLine().toUpperCase();
            while (!choice.equals("STUDENT") && !choice.equals("CARD")){
                out.println("The given input is not correct, please try again. \n" +
                        "Type STUDENT to move a student, CARD to play a character card: ");
                choice = readLine().toUpperCase();
            }
            String finalChoice = choice;
            notifyObserver(viewObserver -> viewObserver.onUpdateActionChoice(finalChoice));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException ignored){}
    }

    /**
     * Shows a generic message to the client.
     *
     * @param message the message that will be shown to the client.
     */

    @Override
    public void showGenericMessage(String message) {
        out.println(message);
    }

    /**
     * Shows to the client the existing games they may join.
     */

    @Override
    public void showExistingGames(Map<Integer, GameController> existingGames) {
        if(existingGames.isEmpty())
            out.println("No games have been created yet.");
        else {
            out.println("Existing games list: ");
            for(Map.Entry<Integer, GameController> entry : existingGames.entrySet()) {
                Integer key = entry.getKey();
                GameController value = entry.getValue();
                out.print("- "+key+": ");
                List<String> players = value.getGameQueue();
                for(int i = 0; i < players.size() - 1; i++){
                    out.print(players.get(i) + ", ");
                }
                try {
                    if (players.size() == value.getGame().getPlayersNumber())
                        out.println(players.get(players.size() - 1) + " (FULL)");
                    else
                        out.println(players.get(players.size() - 1) + " (WAITING FOR PLAYERS TO JOIN)");
                }catch (IndexOutOfBoundsException e){out.println("WAITING FOR PLAYERS TO JOIN");}
            }
        }
        askCreateOrJoin();
    }

    /**
     * Shows the client in which round phase they are.
     *
     * @param isActionPhase {@code true} if the current player is about to play the Action Phase, {@code false}
     *        otherwise.
     */

    @Override
    public void showPhaseUpdate(boolean isActionPhase) {
        clearInterface();
        if(isActionPhase)
            out.println(ANSIConstants.ANSI_BOLD + "------ ACTION PHASE ------" + ANSIConstants.ANSI_RESET);
        else
            out.println(ANSIConstants.ANSI_BOLD + "------ PLANNING PHASE ------" + ANSIConstants.ANSI_RESET);
    }

    /**
     * Shows the client what happened on the {@link GameBoard}.
     *
     * @param s the update to notify.
     */

    @Override
    public void showUpdateMessage(String s) {
        out.println(s);
    }

    /**
     * Shows to the client the latest Assistant Cards played, the Game Board and the players' order.
     *
     * @param game the game whose status needs to be shown.
     */

    @Override
    public void showGameStatusFirstActionPhase(Game game) {
        clearInterface();
        game.showAssistantCardsPlayed();
        game.showGameBoard();
        game.showPlayersOrder();
    }

    /**
     * Shows the current Game Board to the client.
     *
     * @param game the game whose status needs to be shown.
     */

    @Override
    public void showGameStatus(Game game) {
        clearInterface();
        game.showGameBoard();
    }

    /**
     * Shows to the client the AssistantCards they can play.
     *
     * @param game the game whose status needs to be shown.
     */

    public void showDeck(Game game) {
        clearInterface();
        game.showDeck();
    }

    /**
     * Shows a disconnection message to the client.
     *
     * @param message the message that will be shown to the client.
     */

    @Override
    public void showDisconnectionMessage(String message) {
        clearInterface();
        out.println(message);
    }

    /**
     * Closes the client's app.
     */

    @Override
    public void quit() {
        out.println("To play another game, please restart the app. Thank you for playing!");
        System.exit(0);
    }

    /**
     * Sets the view user's nickname. This method is not used by the CLI.
     *
     * @param nickname the nickname chosen by the client.
     */

    @Override
    public void setNickname(String nickname) {
    }

    /**
     * Clears the interface.
     */

    public void clearInterface(){
        out.flush();
    }

    /**
     * Checks if the given input is valid.
     *
     * @param color the string to check.
     * @return {@code true} if the input is valid, {@code false} otherwise.
     */

    private boolean isColorInvalid(String color){
        if (!color.equals("YELLOW") && !color.equals("BLUE") && !color.equals("GREEN")
                && !color.equals("RED") && !color.equals("PINK")) {
            out.println("The given input is not correct, please try again. \n" +
                    "Please type a valid color [YELLOW, BLUE, GREEN, RED, PINK]:");
            return true;
        }
        else return false;
    }

}
