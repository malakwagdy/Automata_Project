package com.example.automata_project;

import java.util.Stack;

public class PushDownAutomaton {
    // stack alphabet = {L,#}
    // input alphabet = {a,b}
    public final Stack<Character> stack = new Stack<>();
    private String input;
    private String message;

    public void setInput(String input) {
        this.input = input;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean isAccepted(String input){
        if (input == null) {
            message = "String Rejected: null input";
            return false;
        }

        stack.clear();
        stack.push('#');
        boolean seenB = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == 'a') {
                if (seenB) {
                    message = "String Rejected: a's after b's";
                    return false; // no 'a' after first 'b'
                }
                stack.push('L');
            }
            else if (c == 'b') {
                seenB = true;
                if (stack.peek() == 'L') {
                    stack.pop();
                }
                else {
                    message = "String Rejected: more b's than a's";
                    return false; // more b's than a's
                }
            }
            else {
                message = "Invalid character: " + c;
                return false; // invalid symbol
            }
        }

        // Accept empty string (n = 0) and all strings of the form a^n b^n.
        if (stack.size() == 1) {
            message = "String Accepted!";
            return true;
        }

        message = "String Rejected: more a's than b's";
        return false;
    }

    public boolean isAccepted() {
        return isAccepted(this.input);
    }
}
