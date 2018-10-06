package ro.code4.monitorizarevot.adapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ro.code4.monitorizarevot.constants.Sync;
import ro.code4.monitorizarevot.db.Data;
import ro.code4.monitorizarevot.net.NetworkService;
import ro.code4.monitorizarevot.net.model.BranchDetails;
import ro.code4.monitorizarevot.net.model.BranchQuestionAnswer;
import ro.code4.monitorizarevot.net.model.Note;
import ro.code4.monitorizarevot.net.model.Question;
import ro.code4.monitorizarevot.net.model.QuestionAnswer;
import ro.code4.monitorizarevot.net.model.ResponseAnswerContainer;
import ro.code4.monitorizarevot.net.model.response.VersionResponse;
import ro.code4.monitorizarevot.observable.ObservableListener;
import ro.code4.monitorizarevot.util.FormUtils;
import ro.code4.monitorizarevot.util.Logify;

import static ro.code4.monitorizarevot.util.AuthUtils.createSyncAccount;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        init();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        init();
    }

    private void init() {

    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Logify.d("SyncAdapter", "performing sync");

        if (extras.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, false)) {
            doUpload();
        } else {
            doSync();
        }
    }

    private void doUpload(){
        postBranchDetails();
        postQuestionAnswers();
        postNotes();
    }

    private void doSync() {
        doUpload();
        getFormsDefinition();
    }

    private void postBranchDetails(){
        List<BranchDetails> branchDetailsList = Data.getInstance().getUnsyncedList(BranchDetails.class);
        for (BranchDetails branchDetails : branchDetailsList) {
            try{
                NetworkService.postBranchDetails(branchDetails);
                Data.getInstance().markSynced(branchDetails);
            } catch (IOException e) {
                e.printStackTrace(); // TODO why silencing errors?
            }
        }
    }

    private void postQuestionAnswers() {
        try{
            List<QuestionAnswer> questionAnswers = new ArrayList<>();

            for(String formCode : Data.getInstance().getFormVersions().keySet()) {
                getAnswersFromForm(formCode, questionAnswers);
            }

            NetworkService.postQuestionAnswer(new ResponseAnswerContainer(questionAnswers));
        }catch (IOException e){
            e.printStackTrace(); // TODO why silencing errors?
        }
    }

    private void postNotes() {
        List<Note> notes = Data.getInstance().getNotes();
        for (Note note : notes) {
            try {
                NetworkService.postNote(note);
                Data.getInstance().deleteNote(note);
            } catch (IOException e) {
                e.printStackTrace(); // TODO why silencing errors?
            }
        }
    }

    private void getAnswersFromForm(String formCode, List<QuestionAnswer> questionAnswers) {
        if(formCode != null){
            List<Question> questionList = FormUtils.getAllQuestions(formCode);
            for (Question question : questionList) {
                if(!question.isSynced()){
                    for (BranchQuestionAnswer branchQuestionAnswer : Data.getInstance().getCityBranchPerQuestion(question.getId())) {
                        QuestionAnswer questionAnswer = new QuestionAnswer(branchQuestionAnswer, formCode);
                        questionAnswers.add(questionAnswer);
                    }
                }
            }
        }
    }

    private void getFormsDefinition() {
        try {
            // for demo to be able to read asset files
            NetworkService.setAssetManager(getContext().getAssets());

            VersionResponse versionResponse = NetworkService.doGetFormVersion();
            Map<String, Integer> existingFormVersion = Data.getInstance().getFormVersions();

            if(existingFormVersion == null || !existingFormVersion.equals(versionResponse.getVersions())) {
                Data.getInstance().deleteAnswersAndNotes();
                // TODO why we define formVersion for each form if in the end we reload everything?
                // question or option IDs might have changed.. that's a question what we promise from the API
                // if we should reload everything then it would be good to have one endpoint

                getForms(versionResponse.getVersions());
            }
        } catch (IOException e){
            e.printStackTrace(); // TODO why silencing errors?
        }
    }

    private void getForms(Map<String, Integer> formVersion) {
        FormDefinitionSubscriber subscriber = new FormDefinitionSubscriber(formVersion, formVersion.size());

        // for demo to be able to read asset files
        NetworkService.setAssetManager(getContext().getAssets());

        for(String formCode : formVersion.keySet()) {
            NetworkService.doGetForm(formCode).startRequest(subscriber);
        }
    }

    public static void requestSync(Context context) {
        ContentResolver.requestSync(createSyncAccount(context), Sync.AUTHORITY, getBundle(false));
    }

    public static void requestUploadSync(Context context) {
        if (ContentResolver.getMasterSyncAutomatically()) {
            ContentResolver.requestSync(createSyncAccount(context), Sync.AUTHORITY, getBundle(true));
        }
    }

    @NonNull
    private static Bundle getBundle(boolean isUpload) {
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, isUpload);
        return extras;
    }

    private class FormDefinitionSubscriber extends ObservableListener<Boolean> {
        private final Map<String, Integer> formVersion;
        private final int numberOfRequests;
        private int successCount = 0;

        FormDefinitionSubscriber(Map<String, Integer> formVersion, int numberOfRequests) {
            this.formVersion = formVersion;
            this.numberOfRequests = numberOfRequests;
        }

        @Override
        public void onSuccess() {
            if (successCount == numberOfRequests) {
                Data.getInstance().saveFormsVersion(formVersion);
            }
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace(); // TODO don't silence errors! #43
        }

        @Override
        public void onNext(Boolean aBoolean) {
            successCount++;
        }
    }
}
