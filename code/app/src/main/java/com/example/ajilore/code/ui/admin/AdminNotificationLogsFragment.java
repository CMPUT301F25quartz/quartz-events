package com.example.ajilore.code.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ajilore.code.R;
import com.example.ajilore.code.adapters.AdminLogsAdapter;
import com.example.ajilore.code.controllers.AdminController;
import com.example.ajilore.code.models.NotificationLog;

import java.util.List;

public class AdminNotificationLogsFragment extends Fragment {

    private RecyclerView rvLogs;
    private AdminLogsAdapter adapter;
    private AdminController controller;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_logs, container, false);

        rvLogs = view.findViewById(R.id.rv_logs);
        EditText etSearch = view.findViewById(R.id.et_search);
        ImageButton btnBack = view.findViewById(R.id.btn_back);

        controller = new AdminController();
        adapter = new AdminLogsAdapter(requireContext());

        rvLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvLogs.setAdapter(adapter);

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });

        loadLogs();
        return view;
    }

    private void loadLogs() {
        controller.fetchNotificationLogs(new AdminController.DataCallback<List<NotificationLog>>() {
            @Override
            public void onSuccess(List<NotificationLog> data) {
                if(isAdded()) {
                    adapter.setLogs(data);
                    if(data.isEmpty()) Toast.makeText(getContext(), "No logs found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                if(isAdded()) Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}