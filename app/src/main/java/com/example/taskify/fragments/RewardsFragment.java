package com.example.taskify.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.taskify.R;
import com.example.taskify.activities.MainActivity;
import com.example.taskify.adapters.RewardAdapter;
import com.example.taskify.databinding.FragmentStreamBinding;
import com.example.taskify.models.Reward;
import com.example.taskify.models.TaskifyUser;
import com.example.taskify.design.VerticalSpaceItemDecoration;
import com.example.taskify.util.ParseUtil;
import com.parse.ParseUser;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RewardsFragment extends Fragment {

    private final static String TAG = "RewardsFragment";
    private FragmentStreamBinding binding;
    private RewardAdapter adapter;
    private List<Reward> rewards;

    // Required empty public constructor
    public RewardsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStreamBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rewards = ((MainActivity)requireActivity()).rewards;
        adapter = new RewardAdapter(getActivity(), rewards);

        binding.recyclerViewStream.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.recyclerViewStream.setLayoutManager(linearLayoutManager);
        VerticalSpaceItemDecoration dividerItemDecoration = new VerticalSpaceItemDecoration();
        binding.recyclerViewStream.addItemDecoration(dividerItemDecoration);

        TaskifyUser user = (TaskifyUser) ParseUser.getCurrentUser();

        if (user.isParent() || user.isSolo()) {
            binding.floatingActionButtonCreate.setVisibility(View.VISIBLE);
            binding.floatingActionButtonCreate.setOnClickListener(v ->
                    Navigation.findNavController(v).navigate(R.id.action_rewards_to_rewardCreateFragment));
        }
        else {
            binding.floatingActionButtonCreate.setVisibility(View.GONE);
        }

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(true);
            rewards.clear();
            ParseUtil.queryRewards(getContext(), user, rewards, adapter);
            binding.swipeRefreshLayout.setRefreshing(false);
        });

        NavController navController = NavHostFragment.findNavController(this);
        // We use a String here, but any type that can be put in a Bundle is supported
        MutableLiveData<Reward> createRewardLiveData = Objects.requireNonNull(navController.getCurrentBackStackEntry())
                .getSavedStateHandle()
                .getLiveData("reward");
        createRewardLiveData.observe(getViewLifecycleOwner(), reward -> {
            if (reward == null) {
                Log.i(TAG, "No reward returned");
                return;
            }
            if (!rewards.contains(reward)) {
                rewards.add(reward);
            }
            Collections.sort(rewards);
            adapter.notifyDataSetChanged();
            binding.recyclerViewStream.smoothScrollToPosition(adapter.getItemCount()-1);
        });
    }
}