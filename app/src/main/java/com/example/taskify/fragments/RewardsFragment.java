package com.example.taskify.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.taskify.activities.MainActivity;
import com.example.taskify.adapters.RewardAdapter;
import com.example.taskify.databinding.FragmentRewardsBinding;
import com.example.taskify.models.Reward;
import com.example.taskify.models.TaskifyUser;
import com.example.taskify.util.ParseUtil;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.Collections;
import java.util.List;

public class RewardsFragment extends Fragment {

    private final static int KEY_REWARD_CREATE_FRAGMENT = 1;
    private FragmentRewardsBinding binding;
    private RewardAdapter adapter;
    private List<Reward> rewards;

    // Required empty public constructor
    public RewardsFragment() {}

    public static RewardsFragment newInstance() {
        RewardsFragment fragment = new RewardsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRewardsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rewards = MainActivity.rewards;

        adapter = new RewardAdapter(getActivity(), rewards);

        binding.recyclerViewRewardsStream.setAdapter(adapter);
        binding.recyclerViewRewardsStream.setLayoutManager(new LinearLayoutManager(getActivity()));

        TaskifyUser user = (TaskifyUser) ParseUser.getCurrentUser();

        if (!user.isParent()) {
            binding.floatingActionButtonCreateReward.setVisibility(View.GONE);
        }
        else {
            binding.floatingActionButtonCreateReward.setOnClickListener(v -> {
                RewardCreateFragment rewardCreateFragment = RewardCreateFragment.newInstance();
                rewardCreateFragment.setTargetFragment(RewardsFragment.this, KEY_REWARD_CREATE_FRAGMENT);
                rewardCreateFragment.show(getActivity().getSupportFragmentManager().beginTransaction(), "fragment_reward_create");
            });
        }

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(true);
            rewards.clear();
            ParseUtil.queryRewards(getContext(), user, rewards, adapter);
            binding.swipeRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == KEY_REWARD_CREATE_FRAGMENT && resultCode == Activity.RESULT_OK) {
            Reward reward = Parcels.unwrap(data.getExtras().getParcelable("reward"));

            rewards.add(reward);
            Collections.sort(rewards);
            adapter.notifyDataSetChanged();
            binding.recyclerViewRewardsStream.smoothScrollToPosition(adapter.getItemCount()-1);
        }
    }
}