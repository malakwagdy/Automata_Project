package com.example.automata_project;

public class DFA {
    private enum State {
        START,
        ZERO_ONES,
        ONE_ONE,
        TWO_ONES
    }

    private String input;
    private String message;
    private State currentState = State.START;

    public void setInput(String input) {
        this.input = input;
    }

    public String getMessage() {
        return message;
    }

    public String getCurrentState() {
        return currentState == null ? null : currentState.name();
    }

    public boolean isAccepted(String input) {
        currentState = State.START;

        if (input == null) {
            message = "String Rejected: null input";
            return false;
        }

        for (int i = 0; i < input.length(); i++) {
            char symbol = input.charAt(i);
            currentState = transition(currentState, symbol);

            if (currentState == null) {
                message = "Invalid character: " + symbol;
                return false;
            }
        }

        boolean accepted = currentState == State.ZERO_ONES;
        message = accepted ? "String Accepted!" : "String Rejected!";
        return accepted;
    }

    public boolean isAccepted() {
        return isAccepted(this.input);
    }

    private State transition(State currentState, char symbol) {
        switch (currentState) {
            case START:
                if (symbol == '0') {
                    return State.ZERO_ONES;
                }
                if (symbol == '1') {
                    return State.ONE_ONE;
                }
                return null;
            case ZERO_ONES:
                if (symbol == '0') {
                    return State.ZERO_ONES;
                }
                if (symbol == '1') {
                    return State.ONE_ONE;
                }
                return null;
            case ONE_ONE:
                if (symbol == '0') {
                    return State.ONE_ONE;
                }
                if (symbol == '1') {
                    return State.TWO_ONES;
                }
                return null;
            case TWO_ONES:
                if (symbol == '0') {
                    return State.TWO_ONES;
                }
                if (symbol == '1') {
                    return State.START;
                }
                return null;
            default:
                return null;
        }
    }
}