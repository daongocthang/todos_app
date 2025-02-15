package com.standalone.todos;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.standalone.core.adapter.utils.DialogUtil;
import com.standalone.core.adapter.utils.InputValidator;
import com.standalone.core.adapter.utils.ViewUtil;
import com.standalone.core.ext.ApiService;
import com.standalone.core.persistent.SetPreferences;
import com.standalone.todos.databinding.DialogFormBinding;
import com.standalone.todos.local.todos.Todo;
import com.standalone.todos.local.todos.TodoAdapter;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FormDialog extends BottomSheetDialogFragment implements Callback {
    public static final String TAG = FormDialog.class.getSimpleName();
    public static final String BUNDLE_NAME = "id";
    private DialogFormBinding binding;
    private boolean canUpdate;
    private long todoId;

    private final TodoAdapter adapter;
    private DataEvent dataEvent;
    private ApiService<Todo> apiService;

    public FormDialog(TodoAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, com.standalone.core.R.style.AppTheme_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogFormBinding.inflate(inflater, container, false);
        Dialog dialog = getDialog();
        assert dialog != null;
        Window window = dialog.getWindow();
        assert window != null;
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Activity activity = getActivity();
        assert activity != null;

        dataEvent = new DataEvent();
        apiService = new ApiService<>();

        final Bundle bundle = getArguments();
        canUpdate = bundle != null && bundle.containsKey(BUNDLE_NAME);
        if (canUpdate) {
            todoId = bundle.getLong(BUNDLE_NAME);
            Todo todo = adapter.getItemById(todoId);
            printOnUiThread(activity, todo);
        } else {
            binding.btDelete.setVisibility(View.GONE);
        }

        binding.btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    createOrUpdate();
                    dismiss();
                } catch (InputValidator.ValidationError e) {
                    // ignore
                }
            }
        });

        binding.btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showConfirm(activity, activity.getString(R.string.confirm_delete), new DialogUtil.OnConfirmListener() {
                    @Override
                    public void onConfirm() {

                        requestDelete(todoId);
                        dismiss();
                    }
                });
            }
        });

        binding.btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof OnDismissListener) {
            ((OnDismissListener) activity).onDismiss(dialog);
        }
    }

    private void requestDelete(long id) {
        adapter.deleteItem(todoId);
        try {
            apiService.set(this).delete(id);
        } catch (Exception e) {
            dataEvent.put(DataEvent.DELETED, String.valueOf(id));
        }
    }

    private void requestInsert(Todo todo) {
        adapter.addItem(todo);
        try {
            apiService.set(this).insert(todo);
        } catch (Exception e) {
            dataEvent.put(DataEvent.CREATED, String.valueOf(todo.getId()));
        }
    }

    private void requestUpdate(long id, Todo todo) {
        adapter.updateItem(todoId, todo);
        try {
            apiService.set(this).update(id, todo);
        } catch (Exception e) {
            //ignore
        }
    }

    private void createOrUpdate() throws InputValidator.ValidationError {
        InputValidator.getInstance().validate(binding.edContent).notEmpty();
        String content = ViewUtil.getText(binding.edContent);
        Todo todo = new Todo(content);
        //create or Update
        if (canUpdate) {

            requestUpdate(todoId, todo);
        } else {

            requestInsert(todo);
        }
    }

    private void printOnUiThread(@NonNull Activity activity, Todo todo) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.edContent.setText(todo.getContent());
            }
        });
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        throw new RuntimeException(e);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

    }

    public interface OnDismissListener {
        void onDismiss(DialogInterface dialogInterface);
    }
}
