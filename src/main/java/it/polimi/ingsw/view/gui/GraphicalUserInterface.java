package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.observers.ViewObservable;
import it.polimi.ingsw.view.View;
import javafx.application.Platform;

import java.util.List;

public class GraphicalUserInterface extends ViewObservable implements View {

    private final SceneController sceneController;

    public GraphicalUserInterface() {
        this.sceneController = new SceneController();
    }

    public SceneController getSceneController() {
        return sceneController;
    }

    @Override
    public void askNickname() {
        Platform.runLater(()-> sceneController.changeRootPane(observers, "nickname_scene.fxml"));
    }

    @Override
    public void askCreateOrJoin() {
        //TODO: do every method in gui
    }

    @Override
    public void askGameInfo() {

    }

    @Override
    public void askGameNumber() {

    }

    @Override
    public void askWizardID() {

    }

    @Override
    public void askAssistantCard() {

    }

    @Override
    public void askMoveStudent() {

    }

    @Override
    public void askMotherNatureSteps() {

    }

    @Override
    public void askCloud() {

    }

    @Override
    public void askCharacterCard() {

    }

    @Override
    public void askAction() {

    }

    @Override
    public void showGenericMessage(String message) {

    }

    @Override
    public void showGameStatusFirstActionPhase(Game game) {

    }

    @Override
    public void showGameStatus(Game game) {

    }

    @Override
    public void showDeck(Game game) {

    }
}
