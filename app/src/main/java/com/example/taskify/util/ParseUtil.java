package com.example.taskify.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.taskify.R;
import com.example.taskify.activities.MainActivity;
import com.example.taskify.models.Reward;
import com.example.taskify.models.Task;
import com.example.taskify.models.TaskifyUser;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

// Contains utility variables and methods used in the app.
public class ParseUtil {

    private static final String TAG = "ParseUtil";

    // Returns the user-friendly error message that accompanies a ParseException.
    public static String parseExceptionToErrorText(ParseException e) {
        // fullErrorMessage in the format of: "com.parse.ParseRequest$ParseRequestException: [error message]."
        // sometimes, the error may be "com.parse.ParseRequest$ParseRequestException: java.lang.IllegalArgumentException: [error message]."
        // The function extracts and capitalizes the first letter of the user-friendly part of the error message,
        // which always begins 2 characters after the last colon.
        String fullErrorMessage = e.toString();
        int indexOfColon = fullErrorMessage.lastIndexOf(":");
        return fullErrorMessage.substring(indexOfColon+2, indexOfColon+3).toUpperCase()
                .concat(fullErrorMessage.substring(indexOfColon + 3));
    }

    public static void save(ParseObject object, Context context, String TAG, String successMessage, String errorMessage) {
        object.saveInBackground(e -> {
            if (e != null) {
                if (errorMessage != null) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, errorMessage, e);
                }
                else {
                    Toast.makeText(context, context.getString(R.string.error_default_message), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, context.getString(R.string.error_default_message), e);
                }
            }
            else {
                if (successMessage != null) {
                    Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, successMessage);
                }
            }
        });
    }

    public static void setPhoto(ImageView imageView, TaskifyUser user, Drawable defaultPhoto) {
        try {
            ParseFile photoFile = ((TaskifyUser) user.fetch()).getProfilePhoto();
            if (photoFile != null) {
                photoFile.getDataInBackground((data, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error getting profile photo.");
                        imageView.setImageDrawable(defaultPhoto);
                        return;
                    }
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    imageView.setImageBitmap(bitmap);
                });
            } else {
                imageView.setImageDrawable(defaultPhoto);
            }
        }
        catch (ParseException e) {
            Log.e(TAG, "Error getting user.");
            imageView.setImageDrawable(defaultPhoto);
        }
    }

    public static TaskifyUser queryUser(String username) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(TaskifyUser.KEY_USERNAME, username);
        try {
            List<ParseUser> parseUsers = query.find();
            if (parseUsers.size() == 0) {
                Log.e(TAG, String.format("No user found with username \"%s\".", username));
                return null;
            }
            if (parseUsers.size() != 1) {
                Log.e(TAG, String.format("Multiple users found with username \"%s\".", username));
                return null;
            }
            return (TaskifyUser) parseUsers.get(0);
        }
        catch (ParseException e) {
            Log.e(TAG, ParseUtil.parseExceptionToErrorText(e), e);
            return null;
        }
    }

    public static void queryRewards(Context context, TaskifyUser user, List<Reward> rewards, RecyclerView.Adapter adapter) {
        ParseQuery<Reward> query;
        if (user == null) {
            Toast.makeText(context, context.getString(R.string.error_default_message), Toast.LENGTH_SHORT).show();
            Log.e(TAG, context.getString(R.string.error_default_message));
            return;
        }
        if (user.isParent()) {
            List<TaskifyUser> children = ((MainActivity)context).associatedUsers;
            List<ParseQuery<Reward>> queries = new ArrayList<>();
            for (TaskifyUser child : children) {
                ParseQuery<Reward> tempQuery = ParseQuery.getQuery(Reward.class);
                tempQuery.whereEqualTo(Reward.KEY_USERS, child);
                queries.add(tempQuery);
            }
            if (queries.isEmpty()) {
                return;
            }
            query = ParseQuery.or(queries);
        }
        else {
            query = ParseQuery.getQuery(Reward.class);
            query = query.include(Reward.KEY_USERS);
            query.whereEqualTo(Reward.KEY_USERS, user);
        }
        query.addAscendingOrder(Reward.KEY_POINTS_VALUE);
        query.findInBackground((queryRewards, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting posts", e);
            } else {
                for (Reward reward : queryRewards) {
                    Log.i(TAG, "Reward Name: " + reward.getRewardName() + ", assigned to: " + reward.getUsers().toString());
                }
                rewards.addAll(queryRewards);
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    public static void queryTasks(Context context, TaskifyUser user, List<Task> tasks, RecyclerView.Adapter adapter) {
        ParseQuery<Task> query = ParseQuery.getQuery(Task.class);
        query = query.include(Task.KEY_USERS);
        if (user == null) {
            Toast.makeText(context, context.getString(R.string.error_default_message), Toast.LENGTH_SHORT).show();
            Log.e(TAG, context.getString(R.string.error_default_message));
            return;
        }
        if (user.isParent()) {
            List<TaskifyUser> children = ((MainActivity)context).associatedUsers;
            List<ParseQuery<Task>> queries = new ArrayList<>();
            for (TaskifyUser child : children) {
                ParseQuery<Task> tempQuery = ParseQuery.getQuery(Task.class);
                tempQuery.whereEqualTo(Task.KEY_USERS, child);
                queries.add(tempQuery);
            }
            if (queries.isEmpty()) {
                return;
            }
            query = ParseQuery.or(queries);
        }
        else {
            query.whereEqualTo(Task.KEY_USERS, user);
        }
        query.findInBackground((queryTasks, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting posts", e);
            }
            else {
                tasks.addAll(queryTasks);
                for (Task task : tasks) {
                    Log.i(TAG, "Task Name: " + task.getTaskName() + ", assigned to: " + task.getUsers().toString());
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                    Log.i(TAG, "Adapter updated.");
                }
                if (!user.isParent()) {
                    TimeUtil.cancelAlarms(context);
                    TimeUtil.startAlarms(context, tasks);
                }
            }
        });
    }


}
