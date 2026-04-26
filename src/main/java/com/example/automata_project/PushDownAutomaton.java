package com.example.automata_project;
import java.util.Stack;

public class PushDownAutomaton {
    // stack alphabet = {L,#}
    // input alphabet = {a,b}
    private String input;

    public void setInput(String input) {
        this.input = input;
    }

    public boolean isAccepted(String input){
        if (input == null) {
            return false;
        }

        Stack<Character> stack = new Stack<>();
        stack.push('#');
        boolean seenB = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == 'a') {
                if (seenB) {
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
                    return false; // more b's than a's
                }
            }
            else {
                return false; // invalid symbol
            }
        }

        // Accept empty string (n = 0) and all strings of the form a^n b^n.
        return stack.size() == 1;
    }

    public boolean isAccepted() {
        return isAccepted(this.input);
    }
}
