package com.example.gamer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MoveAdapter extends RecyclerView.Adapter<MoveAdapter.MoveViewHolder> {

    private List<String> moveList;

    public MoveAdapter(List<String> moveList) {
        this.moveList = moveList;
    }

    @NonNull
    @Override
    public MoveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_move, parent, false);
        return new MoveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoveViewHolder holder, int position) {
        String move = moveList.get(position);
        holder.moveText.setText((position + 1) + ". " + move);
    }

    @Override
    public int getItemCount() {
        return moveList.size();
    }

    public static class MoveViewHolder extends RecyclerView.ViewHolder {
        TextView moveText;

        public MoveViewHolder(@NonNull View itemView) {
            super(itemView);
            moveText = itemView.findViewById(R.id.moveText);
        }
    }

    // Optional: Notify UI when new move is added
    public void addMove(String move) {
        moveList.add(move);
        notifyItemInserted(moveList.size() - 1);
    }
}
