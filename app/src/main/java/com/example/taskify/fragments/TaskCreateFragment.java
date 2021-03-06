package com.example.taskify.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.taskify.R;
import com.example.taskify.activities.MainActivity;
import com.example.taskify.adapters.AssignChildAdapter;
import com.example.taskify.databinding.FragmentTaskCreateBinding;
import com.example.taskify.models.Alarm;
import com.example.taskify.models.Task;
import com.example.taskify.models.TaskifyUser;
import com.example.taskify.util.ParseUtil;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class TaskCreateFragment extends DialogFragment {

    public final static String TAG = "TaskCreateFragment";
    private FragmentTaskCreateBinding binding;
    protected FragmentActivity activity;

    // Required empty public constructor
    public TaskCreateFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTaskCreateBinding.inflate(inflater, container, false);
        Objects.requireNonNull(getDialog()).getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TaskifyUser user = (TaskifyUser) ParseUser.getCurrentUser();
        List<TaskifyUser> children = ((MainActivity)activity).associatedUsers;
        AssignChildAdapter assignChildAdapter = new AssignChildAdapter(activity, children);
        if (user.isSolo()) {
            binding.textViewAssignToText.setVisibility(View.GONE);
            binding.recyclerViewAssignChild.setVisibility(View.GONE);
        }
        else {
            binding.textViewAssignToText.setVisibility(View.VISIBLE);
            binding.recyclerViewAssignChild.setAdapter(assignChildAdapter);
            binding.recyclerViewAssignChild.setLayoutManager(new LinearLayoutManager(activity));
        }

        binding.checkBoxSetRecurringTrue.setOnCheckedChangeListener((buttonView, isChecked) ->
                binding.layoutCheckBoxSetRecurringWeekdays.setVisibility(isChecked? View.VISIBLE : View.GONE));

        binding.buttonCancel.setOnClickListener(v -> dismiss());

        binding.buttonConfirm.setOnClickListener(v -> {
            String taskName = binding.editTextTaskName.getText().toString();
            if (taskName.isEmpty()) {
                Toast.makeText(activity, getString(R.string.error_empty_task_name_message), Toast.LENGTH_SHORT).show();
                return;
            }
            int pointsValue;
            try {
                pointsValue = Integer.parseInt(binding.editTextPoints.getText().toString());
                if (pointsValue < 0) throw new IllegalArgumentException();
            }
            catch (NumberFormatException ne) {
                Toast.makeText(activity, getString(R.string.error_empty_points_message), Toast.LENGTH_SHORT).show();
                return;
            }
            catch (IllegalArgumentException ie) {
                Toast.makeText(activity, getString(R.string.error_negative_points_message), Toast.LENGTH_SHORT).show();
                return;
            }
            List<ParseUser> selectedChildren;
            if (user.isSolo()) {
                selectedChildren = new ArrayList<>();
                selectedChildren.add(user);
            }
            else {
                selectedChildren = assignChildAdapter.getSelectedChildren();
                if (selectedChildren.isEmpty()) {
                    Toast.makeText(activity, getString(R.string.error_empty_child_message), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Alarm alarm;
            Date date = new Date();
            date.setHours(binding.timePicker.getHour());
            date.setMinutes(binding.timePicker.getMinute());
            boolean recurring = binding.checkBoxSetRecurringTrue.isChecked();
            if (recurring) {
                List<Boolean> recurringWeekdays = Arrays.asList(
                        binding.checkBoxSetRecurringSun.isChecked(),
                        binding.checkBoxSetRecurringMon.isChecked(),
                        binding.checkBoxSetRecurringTue.isChecked(),
                        binding.checkBoxSetRecurringWed.isChecked(),
                        binding.checkBoxSetRecurringThu.isChecked(),
                        binding.checkBoxSetRecurringFri.isChecked(),
                        binding.checkBoxSetRecurringSat.isChecked()
                );
                alarm = new Alarm(date, true, recurringWeekdays);
            }
            else {
                alarm = new Alarm(date, false);
            }
            ParseUtil.save(alarm, activity, TAG, null, null);

            Task task = new Task(taskName, pointsValue, alarm, selectedChildren);
            ParseUtil.save(task, activity, TAG, getString(R.string.success_save_task_message), getString(R.string.error_save_task_message));

            Objects.requireNonNull(NavHostFragment.findNavController(this).getPreviousBackStackEntry()).getSavedStateHandle().set("task", task);
            dismiss();
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentActivity){
            activity = (FragmentActivity)context;
        }
    }

}