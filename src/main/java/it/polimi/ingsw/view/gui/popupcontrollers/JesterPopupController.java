package it.polimi.ingsw.view.gui.popupcontrollers;

import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.Student;
import it.polimi.ingsw.model.charactercards.Bard;
import it.polimi.ingsw.model.charactercards.Jester;
import it.polimi.ingsw.view.gui.PopupController;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

/**
 * This popup controller is specifically designed for the {@link Jester}, and controls the popup.
 */

public class JesterPopupController extends PopupController {

    @FXML
    private AnchorPane popupBox;
    private ArrayList<String> parameter;
    private List<Student> students;
    private boolean quit;

    /**
     * JesterPopupController constructor.
     *
     * @param students the list of students on the {@link Jester}.
     */

    public JesterPopupController(List<Student> students) {
        super();
        parameter = new ArrayList<>();
        this.students = students;
    }

    /**
     * Sets the choice made by the player via the popup.
     *
     * @param e the event that triggered the setting.
     */

    public void setParameter(Event e){
        for(Node node : popupBox.getChildren()) {
            if (node instanceof ChoiceBox) {
                parameter.add((String) ((ChoiceBox) node).getValue());
            }
        }
        parameter.removeIf(x -> x.equals("---"));
        if(parameter.size()%2!=0)
            parameter = null;
        getWindow().close();
    }

    /**
     * Properly initializes the popup.
     */

    @FXML
    public void initialize(){
        for(Node node : popupBox.getChildren()) {
            if (node instanceof ChoiceBox) {
                for(Color color : Color.values())
                    ((ChoiceBox) node).getItems().add(color.toString());
                ((ChoiceBox) node).getItems().add("---");
                ((ChoiceBox) node).setValue("---");
            }
            if(node instanceof Button)
                node.addEventHandler(MouseEvent.MOUSE_CLICKED, this::setParameter);
            if(node instanceof GridPane){
                int i = 0;
                for(Student student : students){
                    ImageView studentImage = new ImageView(new Image("/img/student_"+student.color().toString().toLowerCase()+".png"));
                    studentImage.setFitWidth(65);
                    studentImage.setPreserveRatio(true);
                    ((GridPane) node).add(studentImage, i, 0);
                    i++;
                }
            }
        }
    }

    /**
     * Displays the popup.
     *
     * @return an {@link ArrayList} of {@link String} representing the choice made by the player.
     */

    @Override
    public ArrayList<String> display() {

        getWindow().setTitle("Switch the students");
        getWindow().setHeight(365);
        getWindow().setWidth(400);

        Parent rootLayout = load("/fxml/jester_popup.fxml");
        Scene scene = new Scene(rootLayout, 400, 335);
        getWindow().setScene(scene);

        popupBox = (AnchorPane) rootLayout;
        initialize();

        getWindow().setOnCloseRequest(event -> {event.consume(); quit();});

        getWindow().showAndWait();

        if(quit)
            return null;
        else
            return parameter;
    }

    /**
     * Handles the closing request of the popup. Since the {@link Bard} may work even with no parameter, this is
     * needed to avoid undesired coin loss.
     */

    private void quit(){
        quit = true;
        getWindow().close();
    }

}
