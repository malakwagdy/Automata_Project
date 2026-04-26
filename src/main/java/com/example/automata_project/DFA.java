package com.example.automata_project;

public class DFA {
    private enum State {
        ZERO_ONES_ENDS_WITH_ZERO,
        ZERO_ONES_ENDS_WITH_ONE,
        ONE_ONE_ENDS_WITH_ZERO,
        ONE_ONE_ENDS_WITH_ONE,
        TWO_ONES_ENDS_WITH_ZERO,
        TWO_ONES_ENDS_WITH_ONE
    }

    private String input;
    private String message;

    public void setInput(String input) {
        this.input = input;
    }

    public String getMessage() {
        return message;
    }

    public boolean isAccepted(String input) {
        if (input == null) {
            message = "String Rejected: null input";
            return false;
        }

        State currentState = State.ZERO_ONES_ENDS_WITH_ONE;

        for (int i = 0; i < input.length(); i++) {
            char symbol = input.charAt(i);
            currentState = transition(currentState, symbol);

            if (currentState == null) {
                message = "Invalid character: " + symbol;
                return false;
            }
        }

        boolean accepted = currentState == State.ZERO_ONES_ENDS_WITH_ZERO;
        message = accepted ? "String Accepted!" : "String Rejected!";
        return accepted;
    }

    public boolean isAccepted() {
        return isAccepted(this.input);
    }

    private State transition(State currentState, char symbol) {
        switch (currentState) {
            case ZERO_ONES_ENDS_WITH_ZERO:
            case ZERO_ONES_ENDS_WITH_ONE:
                if (symbol == '0') {
                    return State.ZERO_ONES_ENDS_WITH_ZERO;
                }
                if (symbol == '1') {
                    return State.ONE_ONE_ENDS_WITH_ONE;
                }
                return null;
            case ONE_ONE_ENDS_WITH_ZERO:
            case ONE_ONE_ENDS_WITH_ONE:
                if (symbol == '0') {
                    return State.ONE_ONE_ENDS_WITH_ZERO;
                }
                if (symbol == '1') {
                    return State.TWO_ONES_ENDS_WITH_ONE;
                }
                return null;
            case TWO_ONES_ENDS_WITH_ZERO:
            case TWO_ONES_ENDS_WITH_ONE:
                if (symbol == '0') {
                    return State.TWO_ONES_ENDS_WITH_ZERO;
                }
                if (symbol == '1') {
                    return State.ZERO_ONES_ENDS_WITH_ONE;
                }
                return null;
            default:
                return null;
        }
    }
}
