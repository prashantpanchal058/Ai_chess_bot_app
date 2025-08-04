package com.example.gamer;

public class FenConverter {

    public static float[][][][] fenToTensor(String fen) {
        float[][][][] tensor = new float[1][14][8][8];

        String[] parts = fen.split(" ");
        String board = parts[0];
        String turn = parts[1];
        String castling = parts[2];

        String[] rows = board.split("/");
        for (int r = 0; r < 8; r++) {
            String row = rows[r];
            int file = 0;
            for (char c : row.toCharArray()) {
                if (Character.isDigit(c)) {
                    file += c - '0';
                } else {
                    int plane = getPlaneIndex(c);
                    if (plane >= 0 && file < 8) {
                        tensor[0][plane][r][file] = 1.0f;
                    }
                    file++;
                }
            }
        }

        tensor[0][12] = fillPlane(turn.equals("w") ? 1.0f : 0.0f); // Side to move
        tensor[0][13] = fillPlane(castling.contains("K") ? 1.0f : 0.0f); // Castling (example)

        return tensor;
    }

    private static float[][] fillPlane(float value) {
        float[][] plane = new float[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                plane[i][j] = value;
            }
        }
        return plane;
    }

    private static int getPlaneIndex(char piece) {
        switch (piece) {
            case 'P': return 0;  case 'N': return 1;
            case 'B': return 2;  case 'R': return 3;
            case 'Q': return 4;  case 'K': return 5;
            case 'p': return 6;  case 'n': return 7;
            case 'b': return 8;  case 'r': return 9;
            case 'q': return 10; case 'k': return 11;
            default: return -1;
        }
    }
}
