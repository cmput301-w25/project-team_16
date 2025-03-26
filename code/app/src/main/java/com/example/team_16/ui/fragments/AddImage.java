package com.example.team_16.ui.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.team_16.R;
import com.example.team_16.models.MoodEvent;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddImage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddImage extends Fragment {
    private Uri imageUri;
    private Uri oldImageUri;

    private ImageView imageView;
    private TextView textView;

    private Bundle result = new Bundle();

    private String type;

    private static final String ARG_PARAM1 = "param1";

    public AddImage() {
        // Required empty public constructor
    }

    public static AddImage newInstance(String type) {
        AddImage fragment = new AddImage();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            type = getArguments().getString(ARG_PARAM1);
        }

        oldImageUri = getArguments().getParcelable("selectedUriOld");
        imageUri = getArguments().getParcelable("selectedUri");

        if (imageUri == null && oldImageUri != null) {
            imageUri = oldImageUri;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        imageView = requireView().findViewById(R.id.addMoodImage);
        textView = requireView().findViewById(R.id.noImageText);
        textView.setVisibility(View.GONE);

        Button updateButton = view.findViewById(R.id.updateImageButton);
        Button removeButton = view.findViewById(R.id.removeImageButton);
        Button cancelButton = view.findViewById(R.id.cancelImageButton);
        Button confirmButton = view.findViewById(R.id.confirmImageButton);

        if (imageUri != null) {
            Glide.with(this)
                    .load(imageUri)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(20)))
                    .into(imageView);
        }
        else {
            Glide.with(this)
                    .load(oldImageUri)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(20)))
                    .into(imageView);
        }

        updateButton.setOnClickListener(v -> {
            if (Objects.equals(type, "Camera")) {

                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + System.currentTimeMillis() + ".jpg");
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MoodApp");

                imageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

                takePhotoLauncher.launch(imageUri);

            }
            else {

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                pickImageLauncher.launch(intent);

            }

        });

        removeButton.setOnClickListener(v -> {
            imageUri = null;
            textView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.INVISIBLE);
        });

        cancelButton.setOnClickListener(v -> {
            result.putParcelable("uri", oldImageUri);
            getParentFragmentManager().setFragmentResult("image_result", result);
            getParentFragmentManager().popBackStack();
        });

        confirmButton.setOnClickListener(v -> {
            result.putParcelable("uri", imageUri);
            getParentFragmentManager().setFragmentResult("image_result", result);
            getParentFragmentManager().popBackStack();
        });

    }

    private ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    // Get the selected image URI.
                    imageUri = (Uri) result.getData().getData();

                    imageView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.GONE);

                    Glide.with(this)
                            .load(imageUri)
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(20)))
                            .into(imageView);
                }

            }
    );

    private final ActivityResultLauncher<Uri> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {

                if (success && imageUri != null) {

                    imageView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.GONE);

                    Glide.with(this)
                            .load(imageUri)
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(20)))
                            .into(imageView);

                }

            });


}