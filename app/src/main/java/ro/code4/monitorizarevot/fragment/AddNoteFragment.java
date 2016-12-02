package ro.code4.monitorizarevot.fragment;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import ro.code4.monitorizarevot.BaseFragment;
import ro.code4.monitorizarevot.R;
import ro.code4.monitorizarevot.db.Data;
import ro.code4.monitorizarevot.net.model.Note;
import ro.code4.monitorizarevot.widget.FileSelectorButton;
import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.MediaOptions;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;
import vn.tungdx.mediapicker.utils.MediaUtils;

import static android.app.Activity.RESULT_OK;

public class AddNoteFragment extends BaseFragment {
    private static final String ARGS_QUESTION_ID = "QuestionId";
    private static final int PERMISSIONS_READ_EXTERNAL_STORAGE = 1001;
    private static final int REQUEST_MEDIA = 100;
    private Integer questionId;
    private EditText description;
    private FileSelectorButton fileSelectorButton;
    private Button send;
    private MediaItem mediaItem;

    public static AddNoteFragment newInstance() {
        return new AddNoteFragment();
    }

    public static AddNoteFragment newInstance(Integer idIntrebare) {
        AddNoteFragment fragment = new AddNoteFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARGS_QUESTION_ID, idIntrebare);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            questionId = getArguments().getInt(ARGS_QUESTION_ID);
            Toast.makeText(getActivity(), getArguments().getInt(ARGS_QUESTION_ID) + "", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_note, container, false);

        description = (EditText) rootView.findViewById(R.id.note_description);
        fileSelectorButton = (FileSelectorButton) rootView.findViewById(R.id.note_file_selector);
        send = (Button) rootView.findViewById(R.id.button_continue);

        fileSelectorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    openMediaPicker();
                } else {
                    requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                            PERMISSIONS_READ_EXTERNAL_STORAGE);
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote(mediaItem);
                navigateBack();
            }
        });

        disableSend();

        return rootView;
    }

    @Override
    public String getTitle() {
        return getString(R.string.title_note);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_READ_EXTERNAL_STORAGE: {
                if (hasGrantedPermission(grantResults)) {
                    openMediaPicker();
                } else {
                    navigateBack();
                    Toast.makeText(getActivity(), "Permisiunea este necesară pentru a putea selecta o resursă", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void openMediaPicker() {
        MediaOptions.Builder optionsBuilder = new MediaOptions.Builder();
        optionsBuilder.canSelectBothPhotoVideo();
        MediaPickerActivity.open(AddNoteFragment.this, REQUEST_MEDIA, optionsBuilder.build());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA && resultCode == RESULT_OK) {
            List<MediaItem> mediaSelectedList = MediaPickerActivity
                    .getMediaItemSelected(data);
            if (mediaSelectedList != null && mediaSelectedList.size() > 0) {
                mediaItem = mediaSelectedList.get(0);
                enableSend();
                fileSelectorButton.setText(getFileName(mediaItem));
            }
        }
    }

    private void enableSend() {
        send.setEnabled(true);
    }

    private void disableSend() {
        send.setEnabled(false);
    }

    private void saveNote(MediaItem item) {
        Note note = new Note();
        note.setUriPath(item.getPathOrigin(getActivity()));
        note.setBranchNumber(123);
        note.setCountyCode("AB");
        note.setDescription(description.getText().toString());
        note.setQuestionId(questionId);

        Data.getInstance().saveNote(note);
    }

    private String getFileName(MediaItem item) {
        String path = item.getPathOrigin(getActivity());
        return path.substring(path.lastIndexOf("/")+1);
    }
}