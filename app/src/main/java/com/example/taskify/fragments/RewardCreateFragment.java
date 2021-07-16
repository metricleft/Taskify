package com.example.taskify.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.taskify.R;
import com.example.taskify.databinding.FragmentRewardCreateBinding;
import com.example.taskify.models.Reward;
import com.example.taskify.models.TaskifyUser;
import com.example.taskify.util.ParseUtil;
import com.example.taskify.util.PhotoUtil;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RewardCreateFragment extends DialogFragment {

    public final static String TAG = "RewardCreateFragment";
    public final static int REQUEST_IMAGE_CAPTURE = 2;
    private FragmentRewardCreateBinding binding;
    protected FragmentActivity activity;
    private Reward reward;

    public interface RewardCreateDialogListener {
        void onFinishRewardCreateDialog(Reward reward);
    }

    // Required empty public constructor
    public RewardCreateFragment() {}

    public static RewardCreateFragment newInstance() {
        RewardCreateFragment fragment = new RewardCreateFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRewardCreateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // CodePath tutorial
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photoFile = PhotoUtil.getPhotoFileUri(activity, PhotoUtil.DEFAULT_PHOTO_FILE_NAME);
                Uri fileProvider = FileProvider.getUriForFile(activity, activity.getResources().getString(R.string.uri_fileprovider_authority), photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

                // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
                // So as long as the result is not null, it's safe to use the intent.
                if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                    // Start the image capture intent to take photo
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        };

        binding.imageViewPhoto.setOnClickListener(onClickListener);
        binding.floatingActionButtonCamera.setOnClickListener(onClickListener);

        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        binding.buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rewardName = binding.editTextRewardName.getText().toString();
                if (rewardName.isEmpty()) {
                    Toast.makeText(activity, "Reward name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                int pointsValue;
                try {
                    pointsValue = Integer.parseInt(binding.editTextPoints.getText().toString());
                    if (pointsValue < 0) throw new IllegalArgumentException();
                }
                catch (NumberFormatException ne) {
                    Toast.makeText(activity, "Points cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                TaskifyUser user = (TaskifyUser) ParseUser.getCurrentUser();
                if (binding.imageViewPhoto.getDrawable() == null) {
                    // Does not require a photo to be uploaded.
                    reward = new Reward(rewardName, pointsValue, null, user);
                    ParseUtil.save(reward, activity, TAG, "Reward saved successfully!", "Error while saving reward.");
                    returnReward();
                }
                else {
                    File photoFile = PhotoUtil.getPhotoFileUri(activity, PhotoUtil.DEFAULT_PHOTO_FILE_NAME);
                    ParseFile parseFile = new ParseFile(photoFile);
                    parseFile.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                reward = new Reward(rewardName, pointsValue, parseFile, user);
                                ParseUtil.save(reward, activity, TAG, "Reward saved successfully!", "Error while saving reward.");
                                if (photoFile != null && photoFile.delete()) {
                                    Log.i(TAG, "Photo deletion successful.");
                                }
                                else {
                                    Log.e(TAG, "Photo deletion unsuccessful.");
                                }
                                returnReward();
                            }
                            else {
                                Toast.makeText(activity, ParseUtil.parseExceptionToErrorText(e), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error creating parseFile from taken image", e);
                            }
                        }
                    });
                }
            }
        });
    }

    private void returnReward() {
        //RewardCreateDialogListener listener = (RewardCreateDialogListener) activity;
        //listener.onFinishRewardCreateDialog(reward);
        Intent intent = new Intent();
        intent.putExtra("reward", Parcels.wrap(reward));
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentActivity){
            activity = (FragmentActivity)context;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                // Codepath tutorial
                File photoFile = PhotoUtil.getPhotoFileUri(activity, PhotoUtil.DEFAULT_PHOTO_FILE_NAME);
                // by this point we have the camera photo on disk
                Bitmap takenImage = PhotoUtil.rotateBitmapOrientation(photoFile.getAbsolutePath());
                // See BitmapScaler.java: https://gist.github.com/nesquena/3885707fd3773c09f1bb
                Bitmap resizedBitmap = PhotoUtil.scaleToFitWidth(takenImage, PhotoUtil.DEFAULT_WIDTH);

                // Configure byte output stream
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                // Compress the image further
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                try {
                    FileOutputStream fos = new FileOutputStream(photoFile);
                    // Write the bytes of the bitmap to file
                    fos.write(bytes.toByteArray());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Load the resized image into a preview
                binding.imageViewPhoto.setImageBitmap(resizedBitmap);
            } else { // Result was a failure
                Toast.makeText(activity, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }


}