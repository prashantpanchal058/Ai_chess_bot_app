package com.example.gamer;

import java.util.ArrayList;
import java.util.List;

public class MoveDecoder {
    private static final String[] files = {"a", "b", "c", "d", "e", "f", "g", "h"};

    public static List<String> generateAllUciMoves() {
        List<String> moves = new ArrayList<>();
        for (int from = 0; from < 64; from++) {
            for (int to = 0; to < 64; to++) {
                if (from == to) continue;
                String fromSquare = files[from % 8] + (8 - from / 8);
                String toSquare = files[to % 8] + (8 - to / 8);
                moves.add(fromSquare + toSquare);
            }
        }
        return moves;
    }

    public static String decodeMoveIndex(int index) {
        List<String> allMoves = generateAllUciMoves();
        if (index < allMoves.size()) {
            return allMoves.get(index);
        } else {
            return "invalid";
        }
    }

    public static int argMax(float[] scores) {
        int bestIndex = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[bestIndex]) {
                bestIndex = i;
            }
        }
        return bestIndex;
    }
}
