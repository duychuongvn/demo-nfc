package ch.smartlink.smartticketdemo.model;


public class LogModel {
    private String command;
    private String response;


    @Override
    public String toString() {
        return "Command: "+ command + "\n" + "Response: "+ response;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
