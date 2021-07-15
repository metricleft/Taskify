package com.example.taskify.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskify.R;
import com.example.taskify.databinding.ItemRewardBinding;
import com.example.taskify.models.Reward;
import com.example.taskify.models.TaskifyUser;
import com.parse.DeleteCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.ViewHolder> {

    private final static String TAG = "RewardAdapter";

    private Context context;
    private List<Reward> rewards;

    public RewardAdapter(Context context, List<Reward> rewards) {
        this.context = context;
        this.rewards = rewards;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRewardBinding binding = ItemRewardBinding.inflate(LayoutInflater.from(context));
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reward reward = rewards.get(position);
        holder.bind(reward);
    }

    @Override
    public int getItemCount() {
        return rewards.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        ItemRewardBinding binding;

        public ViewHolder(@NonNull ItemRewardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void bind(Reward reward) {
            binding.textViewRewardName.setText(reward.getRewardName());
            String pointsValueString = String.valueOf(reward.getPointsValue()) + " " + context.getResources().getString(R.string.points_value_suffix_text);
            binding.textViewPointsValue.setText(pointsValueString);
            TaskifyUser user = (TaskifyUser) ParseUser.getCurrentUser();
            if (user.getPointsTotal() >= reward.getPointsValue()) {
                binding.checkBoxEarnedReward.setChecked(true);
            }
            else {
                binding.checkBoxEarnedReward.setChecked(false);
            }
            ParseFile rewardPhoto = reward.getRewardPhoto();
            if (rewardPhoto == null) {
                binding.imageViewRewardPhoto.setImageResource(R.drawable.ic_baseline_star_24);
            }
            else {
                rewardPhoto.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Image load unsuccessful.", e);
                        } else {
                            Bitmap rewardImageBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            binding.imageViewRewardPhoto.setImageBitmap(rewardImageBitmap);
                        }
                    }
                });

            }
        }

        @Override
        public void onClick(View v) {
            //go to details screen
            Log.i(TAG, "onClick");
        }

        @Override
        public boolean onLongClick(View v) {
            // Remove reward.
            Log.i(TAG, "onLongClick");
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return false;
            }
            Reward reward = rewards.get(position);
            reward.deleteInBackground(new DeleteCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error while removing reward", e);
                        Toast.makeText(context, "Error while removing reward.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    rewards.remove(position);
                    notifyDataSetChanged();
                    Log.i(TAG, "Reward removal was successful.");
                    Toast.makeText(context, String.format("Reward removed."), Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }
    }
}