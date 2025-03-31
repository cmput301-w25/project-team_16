/**
 * AddImage is a Fragment that allows users to preview and update a selected image
 * either from the camera or gallery before confirming it.
 *
 * It supports:
 * - Displaying a previously selected image or a newly selected one.
 * - Launching the camera or gallery to choose a new image.
 * - Applying a rounded corners transformation using Glide.
 * - Returning the final image URI to the parent fragment using FragmentResult API.
 *
 * Usage:
 * This fragment is typically used as part of the mood event creation flow,
 * where the user wants to attach an image to their mood event.
 */

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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    }

    /**
     * Creates a new instance of the AddImage fragment.
     *
     * @param type The type of image selection (e.g., "Camera" or "Gallery")
     * @return A new instance of AddImage fragment
     */
    public static AddImage newInstance(String type) {
        AddImage fragment = new AddImage();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, type);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initializes the fragment and retrieves arguments.
     * Sets up the image URI from either new selection or existing image.
     *
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     */
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

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     * Sets the activity title to "Image Preview".
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     * @return The View for the fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getActivity() != null) {
            getActivity().setTitle("Image Preview");
        }
        return inflater.inflate(R.layout.fragment_add_image, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned.
     * Sets up UI components and click listeners for image update and confirmation.
     *
     * @param view The View returned by onCreateView()
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null) {
            getActivity().setTitle("Image Preview");
        }

        imageView = requireView().findViewById(R.id.addMoodImage);
        textView = requireView().findViewById(R.id.noImageText);
        textView.setVisibility(View.GONE);

        Button updateButton = view.findViewById(R.id.updateImageButton);
        Button confirmButton = view.findViewById(R.id.confirmImageButton);

        if (imageUri != null) {
            loadImage(imageUri);
        } else if (oldImageUri != null) {
            loadImage(oldImageUri);
        }

        updateButton.setOnClickListener(v -> handleImageUpdate());
        confirmButton.setOnClickListener(v -> confirmSelection());

        updateButton.setOnClickListener(v -> {
            Animation fadeAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in);
            updateButton.startAnimation(fadeAnimation);
            handleImageUpdate();
        });

        confirmButton.setOnClickListener(v -> {
            Animation fadeAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in);
            confirmButton.startAnimation(fadeAnimation);
            confirmSelection();
        });

    }
    /**
     * Handles the image update process based on the selection type.
     * Launches either the camera or gallery picker based on the type parameter.
     */
    private void handleImageUpdate() {
        if (Objects.equals(type, "Camera")) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + System.currentTimeMillis() + ".jpg");
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MoodApp");

            imageUri = requireContext().getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
            );

            if (imageUri != null) {
                takePhotoLauncher.launch(imageUri);
            }
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        }
    }
    /**
     * Confirms the image selection and returns the result to the parent fragment.
     * Uses FragmentResult API to communicate the selected image URI.
     */
    private void confirmSelection() {
        result.putParcelable("uri", imageUri != null ? imageUri : oldImageUri);
        getParentFragmentManager().setFragmentResult("image_result", result);
        getParentFragmentManager().popBackStack();
    }
    /**
     * Loads and displays an image from the given URI.
     * Applies rounded corners transformation using Glide.
     *
     * @param uri The URI of the image to load
     */
    private void loadImage(Uri uri) {
        imageView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
        Glide.with(this)
                .load(uri)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(20)))
                .into(imageView);
    }


    /**
     * ActivityResultLauncher for picking images from the gallery.
     * Handles the result of the image picker and updates the UI accordingly.
     */
    private ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
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

    /**
     * ActivityResultLauncher for taking photos with the camera.
     * Handles the result of the camera capture and updates the UI accordingly.
     */
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