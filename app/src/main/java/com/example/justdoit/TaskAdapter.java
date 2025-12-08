package com.example.justdoit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    ArrayList<String> taskList = new ArrayList<>();

    public TaskAdapter() {
        taskList.add("Зробити домашку");
        taskList.add("Купити хліб");
        taskList.add("Прибрати кімнату");
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.taskText.setText(taskList.get(position));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // щоб їздило догори і донизу
    public void swap(int from, int to) {
        Collections.swap(taskList, from, to);
        notifyItemMoved(from, to);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView taskText;
        CheckBox taskCheckBox;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskText = itemView.findViewById(R.id.taskText);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
        }
    }
}
