package com.example.justdoit;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.justdoit.config.Config;
import com.example.justdoit.dto.zadachi.ZadachaItemDTO;
import com.example.justdoit.network.RetrofitClient;
import com.example.justdoit.zadacha.OnItemClickZadacha;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<ZadachaItemDTO> taskList = new ArrayList<>();
    private OnSelectionChangedListener selectionChangedListener;

    private OnItemClickZadacha onEditListener;

    public TaskAdapter(List<ZadachaItemDTO> list, OnSelectionChangedListener listener, OnItemClickZadacha onEditListener) {
        taskList = list;
        this.selectionChangedListener = listener;
        this.onEditListener = onEditListener;
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
        ZadachaItemDTO item = taskList.get(position);
        holder.taskText.setText(item.getName());
        holder.taskCheckBox.setChecked(item.isSelected());
        Glide.with(holder.itemView.getContext())
                .load(Config.IMAGES_URL + "200_" + item.getImage())
                .into(holder.taskImage);

        holder.taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
            if (selectionChangedListener != null) {
                selectionChangedListener.onSelectionChanged(hasSelectedItems());
            }
        });

        holder.editButton.setOnClickListener(x -> onEditListener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public boolean hasSelectedItems() {
        for (ZadachaItemDTO item : taskList) {
            if (item.isSelected()) return true;
        }
        return false;
    }

    public void deleteSelectedItems(Runnable onSuccess) {
        List<Long> toDelete = new ArrayList<>();
        for (ZadachaItemDTO item : taskList) {
            if (item.isSelected()) {
                toDelete.add((long) item.getId());
            }
        }

        if (toDelete.isEmpty()) return;

        RetrofitClient.getInstance().getZadachiApi().deleteRange(toDelete)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            if (onSuccess != null) onSuccess.run();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
    }




    public interface OnSelectionChangedListener {
        void onSelectionChanged(boolean hasSelected);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskText;
        CheckBox taskCheckBox;
        ImageView taskImage;
        Button editButton;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskText = itemView.findViewById(R.id.taskText);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
            taskImage = itemView.findViewById(R.id.taskImage);
            editButton = itemView.findViewById(R.id.edit_btn);
        }
    }
}

