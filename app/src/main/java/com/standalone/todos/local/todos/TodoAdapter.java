package com.standalone.todos.local.todos;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.standalone.core.adapter.AbsAdapter;
import com.standalone.todos.databinding.ItemTodoBinding;

import java.util.List;

public class TodoAdapter extends AbsAdapter<Todo, TodoAdapter.ViewHolder> {
    public static class AdapterException extends RuntimeException {
        public AdapterException(String message) {
            super(message);
        }
    }

    private final AppCompatActivity activity;
    private final TodoDao dao;

    public TodoAdapter(AppCompatActivity activity) {
        this.activity = activity;
        this.dao = new TodoDao();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addItem(Todo todo) {
        itemList.add(todo);
        notifyDataSetChanged();
        dao.create(todo);
    }

    public Todo getItemById(long id) {
        try {
            return itemList.get(getPosById(id));
        } catch (AdapterException e) {
            return null;
        }
    }

    public List<Todo> getAllItems() {
        return itemList;
    }

    public void updateItem(long id, Todo todo) {
        dao.update(id, todo);
        try {
            int pos = getPosById(id);
            itemList.set(pos, todo);
            notifyItemChanged(pos);
        } catch (AdapterException e) {
            // ignore
        }
    }

    public void deleteItem(long id) {
        dao.delete(id);
        try {
            int pos = getPosById(id);
            itemList.remove(pos);
            notifyItemRemoved(pos);
        } catch (AdapterException e) {
            // ignore
        }
    }

    private int getPosById(long id) {
        Todo existingItem = itemList.stream().filter(t -> id == t.getId()).findFirst().orElse(null);
        ;
        if (existingItem == null) throw new AdapterException("Index out of range");

        return itemList.indexOf(existingItem);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final ItemTodoBinding itemBinding = ItemTodoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), activity);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTodoBinding itemBinding;

        public ViewHolder(@NonNull ItemTodoBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }

        public void bind(Todo todo, AppCompatActivity parent) {
            itemBinding.tvContent.setText(todo.getContent());
            itemBinding.tvUpdatedAt.setText(todo.getUpdatedAt());

            itemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO: handle touched event
                }
            });
        }
    }
}
