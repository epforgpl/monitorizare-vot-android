package ro.code4.monitorizarevot.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ro.code4.monitorizarevot.App;
import ro.code4.monitorizarevot.BaseFragment;
import ro.code4.monitorizarevot.R;
import ro.code4.monitorizarevot.db.Data;
import ro.code4.monitorizarevot.net.NetworkService;
import ro.code4.monitorizarevot.net.model.Note;
import ro.code4.monitorizarevot.observable.GeneralSubscriber;
import ro.code4.monitorizarevot.util.FileUtils;
import ro.code4.monitorizarevot.util.NetworkUtils;
import ro.code4.monitorizarevot.widget.FileSelectorButton;

import static android.app.Activity.RESULT_OK;

public class AddNoteFragment extends BaseFragment {
    private static final String ARGS_QUESTION_ID = "QuestionId";
    private static final int PERMISSIONS_WRITE_EXTERNAL_STORAGE_VIDEO = 1001;
    private static final int PERMISSIONS_WRITE_EXTERNAL_STORAGE_PHOTO = 1002;
    private static final int PERMISSIONS_READ_EXTERNAL_STORAGE = 1003;
    private static final int TAKE_PHOTO = 100;
    private static final int RECORD_MOVIE = 101;
    private static final int PICK_MEDIA = 102;

    private Integer questionId;
    private EditText description;
    private FileSelectorButton photoButton;
    private FileSelectorButton pickFileButton;
    private FileSelectorButton recordVideoButton;

    private File mFile;

    public static AddNoteFragment newInstance() {
        return new AddNoteFragment();
    }

    public static AddNoteFragment newInstance(Integer questionId) {
        AddNoteFragment fragment = new AddNoteFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARGS_QUESTION_ID, questionId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            questionId = getArguments().getInt(ARGS_QUESTION_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_note, container, false);

        description = rootView.findViewById(R.id.note_description);
        photoButton = rootView.findViewById(R.id.note_take_photo);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    takePhoto();
                } else {
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            PERMISSIONS_WRITE_EXTERNAL_STORAGE_PHOTO);
                }
            }
        });

        pickFileButton = rootView.findViewById(R.id.note_pick_file);
        pickFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    pickMedia();
                } else {
                    requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                            PERMISSIONS_READ_EXTERNAL_STORAGE);
                }
            }
        });

        recordVideoButton = rootView.findViewById(R.id.note_record_video);
        recordVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    recordVideo();
                } else {
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            PERMISSIONS_WRITE_EXTERNAL_STORAGE_VIDEO);
                }
            }
        });

        rootView.findViewById(R.id.button_continue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (description.getText().toString().length() == 0 && mFile == null) {
                    Toast.makeText(getActivity(), getString(R.string.invalid_note), Toast.LENGTH_SHORT).show();
                } else {
                    saveNote();
                    Toast.makeText(getActivity(), getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
                    navigateBack();
                }
            }
        });

        return rootView;
    }

    @Override
    public String getTitle() {
        return getString(R.string.title_note);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_WRITE_EXTERNAL_STORAGE_PHOTO: {
                if (hasGrantedPermission(grantResults)) {
                    takePhoto();
                } else {
                    Toast.makeText(App.getContext(), R.string.error_permission_external_storage, Toast.LENGTH_LONG).show();
                }
                break;
            }
            case PERMISSIONS_WRITE_EXTERNAL_STORAGE_VIDEO: {
                if (hasGrantedPermission(grantResults)) {
                    recordVideo();
                } else {
                    Toast.makeText(App.getContext(), R.string.error_permission_external_storage, Toast.LENGTH_LONG).show();
                }
                break;
            }
            case PERMISSIONS_READ_EXTERNAL_STORAGE: {
                if (hasGrantedPermission(grantResults)) {
                    pickMedia();
                } else {
                    Toast.makeText(App.getContext(), R.string.error_permission_external_storage, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            File file = createMediaFile("IMG_", ".jpg", Environment.DIRECTORY_PICTURES);

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri photoUri = FileProvider.getUriForFile(getContext(),
                        "ro.code4.monitorizarevot.fileprovider",
                        file);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            } else {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            }
            mFile = file;
            startActivityForResult(takePictureIntent, TAKE_PHOTO);
        }
    }

    private void recordVideo() {
        Intent recordIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (recordIntent.resolveActivity(getContext().getPackageManager()) != null) {
            File file = createMediaFile("VID_", ".mp4", Environment.DIRECTORY_PICTURES);

            if (file != null) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri photoUri = FileProvider.getUriForFile(getContext(),
                            "ro.code4.monitorizarevot.fileprovider",
                            file);
                    recordIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                } else {
                    recordIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                }
                recordIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                mFile = file;
                startActivityForResult(recordIntent, RECORD_MOVIE);
            }
        }
    }

    private void pickMedia() {
        if (Build.VERSION.SDK_INT < 19) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/* video/*");
            startActivityForResult(intent, PICK_MEDIA);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "video/*"});
            startActivityForResult(intent, PICK_MEDIA);
        }
    }

    private File createMediaFile(String prefix, String suffix, String folder) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = prefix + timeStamp + suffix;
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(folder), "Observations");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        return new File(storageDir, imageFileName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PHOTO && resultCode == RESULT_OK) {
            if (mFile != null) {
                photoButton.setText(mFile.getName());
                pickFileButton.clearText();
                recordVideoButton.clearText();
                galleryAddMedia();
            }
        } else if (requestCode == RECORD_MOVIE && resultCode == RESULT_OK) {
            if (mFile != null) {
                recordVideoButton.setText(mFile.getName());
                photoButton.clearText();
                pickFileButton.clearText();
                galleryAddMedia();
            }
        }
        else if (requestCode == PICK_MEDIA && resultCode == RESULT_OK) {
            Uri imagePath = data.getData();
            if (imagePath != null) {
                String filePath = FileUtils.getPath(getContext(), imagePath);
                if (filePath != null) {
                    File file = new File(filePath);
                    pickFileButton.setText(file.getName());
                    photoButton.clearText();
                    recordVideoButton.clearText();
                    mFile = file;
                } else {
                    Toast.makeText(App.getContext(), R.string.error_permission_external_storage, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void galleryAddMedia() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(mFile);
        mediaScanIntent.setData(contentUri);
        getContext().sendBroadcast(mediaScanIntent);
    }

    private void saveNote() {
        Note note = Data.getInstance().saveNote(
                mFile != null ? mFile.getAbsolutePath() : null,
                description.getText().toString(),
                questionId);
        syncCurrentNote(note);
    }

    private void syncCurrentNote(Note note){
        if(NetworkUtils.isOnline(getActivity())){
            NetworkService.syncCurrentNote(note).startRequest(new GeneralSubscriber());
        }
    }
}
