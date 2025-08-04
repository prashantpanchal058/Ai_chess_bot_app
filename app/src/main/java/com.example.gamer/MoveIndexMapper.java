package com.example.gamer;

public class MoveIndexMapper {
    // Example for demo; use your actual 1968-move list
    static String[] moveList = {/* e.g., "e2e4", "d2d4", ..., "g7g8q" */};

    public static String getMove(int index) {
        if (index >= 0 && index < moveList.length) {
            return moveList[index];
        }
        return "0000"; // illegal fallback
    }
}
