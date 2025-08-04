package com.example.gamer;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import androidx.recyclerview.widget.LinearLayoutManager;

public class MainActivity extends AppCompatActivity {


    GridLayout board;
    BottomNavigationView bottom;
    MoveAdapter moveAdapter;


    RecyclerView recyclerView;
    Dialog customDialog;
    private List<ImageButton> highlightedButtons = new ArrayList<>();
    private List<String> movesList = new ArrayList<>();
    private List<ImageButton> played = new ArrayList<>();
    ImageButton selectedButton = null;
    String selectedPosition = null;
    //String fen = "8/1P6/7k/8/8/8/8/4K3 w - - 0 2";
    String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    //String fen = "rnbqkbnr/pppppppp/8/8/6P1/5P2/PPPPP2P/RNBQKBNR b KQkq - 0 2";

    private Map<String, Integer> dictionary = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView); // First initialize
        moveAdapter = new MoveAdapter(movesList);       // Then create adapter
        recyclerView.setAdapter(moveAdapter);           // Then set adapter
        recyclerView.setHasFixedSize(true);             // Optional for performance
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)); // Horizontal scroll
        moveAdapter.notifyDataSetChanged();

        board = findViewById(R.id.chessBoard);
        bottom = findViewById(R.id.bottomNavigation);
        dictionary.put("r", R.drawable.black_rook);
        dictionary.put("n", R.drawable.black_horse);
        dictionary.put("b", R.drawable.black_bishop);
        dictionary.put("q", R.drawable.black_queen);
        dictionary.put("k", R.drawable.black_king);
        dictionary.put("p", R.drawable.black_pawn);
        dictionary.put("P", R.drawable.white_pawn);
        dictionary.put("R", R.drawable.white_rook);
        dictionary.put("N", R.drawable.white_horse);
        dictionary.put("B", R.drawable.white_bishop);
        dictionary.put("Q", R.drawable.white_queen);
        dictionary.put("K", R.drawable.white_king);

        createChessBoard();
        addImage();

        bottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.back){
                    finishAffinity();
                    return true;
                }
                return true;
            }
        });
    }

    private void createChessBoard() {
        int size = getResources().getDisplayMetrics().widthPixels / 8;

        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();
        PyObject module = py.getModule("timepass");

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                ImageButton button = new ImageButton(this);
                SetImage( button, row, col, size);

                button.setTag(R.id.position_tag, new int[]{row, col});

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        PyObject result = module.callAttr("check_move", fen);

                        boolean isWhite = result.toBoolean();

                        if (isWhite == false) return;

                        ViewParent parent = v.getParent();
                        // If the user taps a highlighted square → move piece
                        if (highlightedButtons.contains(v)) {

                            // Perform human move first (on UI thread)
                            change_place((ImageButton) v, module, () -> {
                                // Only starts after player's move is completely finished

                                new Thread(() -> {
                                    runOnUiThread(() -> {
                                        // Get AI prediction
                                        String aiMove = aiPrediction(module);
                                        movesList.add(aiMove);
                                        moveAdapter.notifyItemInserted(movesList.size() - 1);
                                        recyclerView.scrollToPosition(movesList.size() - 1);  // Optional: auto-scroll to latest

                                        // Parse move
                                        String first = aiMove.substring(0, 2);
                                        String second = aiMove.substring(2, 4);

                                        // Apply move to board
                                        move(aiMove, module);

                                        // Highlight
                                        int index1 = positiontosquare(first);
                                        int index2 = positiontosquare(second);
                                        ImageButton btn1 = (ImageButton) board.getChildAt(index1);
                                        ImageButton btn2 = (ImageButton) board.getChildAt(index2);
                                        btn1.setBackgroundColor(Color.YELLOW);
                                        btn2.setBackgroundColor(Color.YELLOW);
                                        played.add(btn1);
                                        played.add(btn2);
                                    });
                                }).start();
                            });

                        }

                        clearHighlights();
                        setHighlightedButtons((ImageButton) v, module);
                    }
                });
            }
        }
    }

    private String getSquareFromPosition(int row, int col) {
        char file = (char) ('a' + col);           // 'a' to 'h'
        int rank = 8 - row;                       // 8 (top) to 1 (bottom)
        return "" + file + rank;                  // e.g., "g2"
    }

    private int positiontosquare(String position){
        int col = position.charAt(0) - 'a';
        int row = 8 - Character.getNumericValue(position.charAt(1));

        int index = row * 8 + col;

        return index;
    }

    private void clearHighlights() {
        for (ImageButton b : highlightedButtons) {
            int[] pos = (int[]) b.getTag(R.id.position_tag);
            int row = pos[0], col = pos[1];
            b.setBackgroundColor((row + col) % 2 == 0 ? Color.WHITE : getResources().getColor(R. color. green));
        }
        highlightedButtons.clear();
    }

    private void playedHighlights() {
        for (ImageButton b : played) {
            int[] pos = (int[]) b.getTag(R.id.position_tag);
            int row = pos[0], col = pos[1];
            b.setBackgroundColor((row + col) % 2 == 0 ? Color.WHITE : getResources().getColor(R. color. green));
        }
        played.clear();
    }

    private void HighligthSquare(String i){ // e.g., "g2g4"
        String toSquare = i.substring(2, 4); // "g4"

        int index = positiontosquare(toSquare);

        ImageButton destButton = (ImageButton) board.getChildAt(index);
        destButton.setBackgroundColor(Color.GRAY);  // highlight

        highlightedButtons.add(destButton);
    }

    private void setHighlightedButtons(ImageButton v,PyObject module){
        int[] pos = (int[]) v.getTag(R.id.position_tag);
        String square = getSquareFromPosition(pos[0], pos[1]);

        selectedButton = (ImageButton) v;
        selectedPosition = square;

        //Toast.makeText(MainActivity.this,"hii", Toast.LENGTH_SHORT).show();

        PyObject result = module.callAttr("get_valid_moves", fen, square);

        List<PyObject> moveObjects = result.asList();

        // Highlight valid destination squares
        for (PyObject move : moveObjects) {
            String position = move.toString(); // e.g., "g2g4"
            HighligthSquare(position);
        }
    }

    private String settleImage(String f) {
        StringBuilder total = new StringBuilder();
        String boardLayout = f.split(" ")[0]; // Get only the board part of the FEN

        for (String rank : boardLayout.split("/")) {
            for (char c : rank.toCharArray()) {
                if (Character.isDigit(c)) {
                    int emptySquares = Character.getNumericValue(c);
                    for (int i = 0; i < emptySquares; i++) {
                        total.append(".");
                    }
                } else {
                    total.append(c);
                }
            }
        }
        return total.toString();
    }

    private void SetImage(ImageButton button, int row, int col, int size){
        if ((row + col) % 2 == 0) {
            button.setBackgroundColor(Color.WHITE);
            //getResources().getColor(R. color. green)
        } else {
            button.setBackgroundColor(getResources().getColor(R. color. green));
        }
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = size;
        params.height = size;
        button.setLayoutParams(params);

        board.addView(button);
    }

    private void addImage(){
        String code = settleImage(fen);

        for(int i=0; i<64;i++){
            ImageButton button = (ImageButton) board.getChildAt(i);
            button.setImageDrawable(null);
            char piece = code.charAt(i);
            if (dictionary.containsKey(String.valueOf(piece))) {
                int id = dictionary.get(String.valueOf(piece));
                button.setImageResource(id);
                button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        }
    }


    private void move(String square, PyObject module){
        PyObject result = module.callAttr("make_move", fen, square);
        fen = result.toString();

        addImage();
        // Clear highlights
        clearHighlights();
        selectedButton = null;
        selectedPosition = null;
    }

    private String aiPrediction(PyObject module) {

        PyObject isGameOver = module.callAttr("is_game_over", fen);
        if (isGameOver.toBoolean()) {
            Toast.makeText(this, "Game Over!", Toast.LENGTH_LONG).show();
            return "0";
        }

        // Load single evaluation (optional)
        float[][][][] input = FenConverter.fenToTensor(fen);
        ModelRunner model = new ModelRunner(this);
        float score = model.evaluatePosition(input);

        // Get all legal moves and their tensors
        PyObject allMoves = module.callAttr("get_all_tensor", fen);

        List<float[][][][]> tensorInputs = new ArrayList<>();
        List<String> moveStrings = new ArrayList<>();

        for (PyObject pair : allMoves.asList()) {
            List<PyObject> tuple = pair.asList();
            PyObject move = tuple.get(0);
            PyObject tensorList = tuple.get(1);

            List<PyObject> tensorPy = tensorList.asList();
            float[] flatTensor = new float[tensorPy.size()];
            for (int i = 0; i < tensorPy.size(); i++) {
                flatTensor[i] = tensorPy.get(i).toFloat();
            }

            // Reshape to [1][14][8][8]
            float[][][][] reshaped = new float[1][14][8][8];
            for (int p = 0; p < 14; p++) {
                for (int r = 0; r < 8; r++) {
                    for (int c = 0; c < 8; c++) {
                        reshaped[0][p][r][c] = flatTensor[p * 64 + r * 8 + c];
                    }
                }
            }
            moveStrings.add(move.toString());
            tensorInputs.add(reshaped);
        }

        boolean isWhiteTurn = fen.split(" ")[1].equals("w"); // Detect side to move
        String bestMove = model.predict(tensorInputs, moveStrings, isWhiteTurn);

        //Log.d("AI", "Best move: " + bestMove);
        return bestMove;
    }

    private void change_place(ImageButton v, PyObject module, Runnable onComplete) {
        if (selectedButton != null && selectedPosition != null) {

            PyObject isGameOver = module.callAttr("is_game_over", fen);
            if (isGameOver.toBoolean()) {
                Toast.makeText(this, "Game Over!", Toast.LENGTH_LONG).show();
                return;
            }

            int[] pos = (int[]) v.getTag(R.id.position_tag);
            int index = pos[0] * 8 + pos[1];

            PyObject isPawn = module.callAttr("is_pawn_at", fen, selectedPosition);
            boolean isP = isPawn.toBoolean();

            if (index < 8 && index >= 0 && isP) {
                String square = getSquareFromPosition(pos[0],pos[1]);
                String po = selectedPosition+square;
                // It's a promotion, wait for player to choose

                promotion(po, module, onComplete);
                return;
            }

            // Normal move
            String square = getSquareFromPosition(pos[0], pos[1]);
            String uci_move = selectedPosition + square;
            PyObject result = module.callAttr("make_move", fen, uci_move);
            fen = result.toString();

            addImage();
            movesList.add(uci_move);
            moveAdapter.notifyItemInserted(movesList.size() - 1);
            recyclerView.scrollToPosition(movesList.size() - 1);  // Optional: auto-scroll to latest

            clearHighlights();
            playedHighlights();
            selectedButton = null;
            selectedPosition = null;

            // Now safely trigger the callback
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    private void promotion(String po, PyObject module, Runnable onComplete) {
        customDialog = new Dialog(MainActivity.this);
        customDialog.setContentView(R.layout.options);
        customDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        customDialog.setCancelable(false);
        customDialog.show();

        Button queen = customDialog.findViewById(R.id.queen);
        Button horse = customDialog.findViewById(R.id.horse);
        Button bishop = customDialog.findViewById(R.id.bishop);
        Button rook = customDialog.findViewById(R.id.rook);

        if (queen == null || horse == null || bishop == null || rook == null) {
            return;
        }

        View.OnClickListener listener = v -> {
            String promotion = "q";
            if (v.getId() == R.id.horse) promotion = "n";
            else if (v.getId() == R.id.bishop) promotion = "b";
            else if (v.getId() == R.id.rook) promotion = "r";

            //String square = getSquareFromPosition(targetPos[0], targetPos[1]);
            String uci_move = po + promotion;

            // Directly call Python and update UI on main thread
            PyObject result = module.callAttr("make_move", fen, uci_move);
            fen = result.toString();

            addImage();
            movesList.add(uci_move);
            moveAdapter.notifyItemInserted(movesList.size() - 1);
            recyclerView.scrollToPosition(movesList.size() - 1);  // Optional: auto-scroll to latest


            clearHighlights();
            playedHighlights();
            selectedButton = null;
            selectedPosition = null;
            customDialog.dismiss();

            // Safely call the next step
            if (onComplete != null) {
                onComplete.run();
            }
        };

        queen.setOnClickListener(listener);
        horse.setOnClickListener(listener);
        bishop.setOnClickListener(listener);
        rook.setOnClickListener(listener);
    }
}