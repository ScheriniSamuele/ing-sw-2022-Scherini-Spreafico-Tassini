package it.polimi.ingsw.model.exceptions;

public class EmptyCloudException extends TryAgainException {

    public EmptyCloudException(String message) {
        super(message);
    }
}
