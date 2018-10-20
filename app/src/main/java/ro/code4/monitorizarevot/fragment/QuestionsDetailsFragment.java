package ro.code4.monitorizarevot.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import ro.code4.monitorizarevot.BaseFragment;
import ro.code4.monitorizarevot.R;
import ro.code4.monitorizarevot.adapter.SyncAdapter;
import ro.code4.monitorizarevot.db.Data;
import ro.code4.monitorizarevot.net.NetworkService;
import ro.code4.monitorizarevot.net.model.BranchQuestionAnswer;
import ro.code4.monitorizarevot.net.model.Question;
import ro.code4.monitorizarevot.net.model.QuestionAnswer;
import ro.code4.monitorizarevot.net.model.response.ResponseAnswer;
import ro.code4.monitorizarevot.observable.ToastMessageSubscriber;
import ro.code4.monitorizarevot.presenter.QuestionsDetailsPresenter;
import ro.code4.monitorizarevot.util.FormUtils;
import ro.code4.monitorizarevot.util.NetworkUtils;
import ro.code4.monitorizarevot.util.QuestionDetailsNavigator;

public class QuestionsDetailsFragment extends BaseFragment implements QuestionDetailsNavigator {
    private static final String ARGS_FORM_ID = "FormId";
    private static final String ARGS_START_INDEX = "StartIndex";

    private List<Question> questions;
    private int currentQuestion = -1;

    private QuestionsDetailsPresenter mPresenter;

    public static QuestionsDetailsFragment newInstance(String sectionCode) {
        return newInstance(sectionCode, 0);
    }

    public static QuestionsDetailsFragment newInstance(String formId, int startIndex) {
        QuestionsDetailsFragment fragment = new QuestionsDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARGS_FORM_ID, formId);
        args.putInt(ARGS_START_INDEX, startIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.currentQuestion = getArguments().getInt(ARGS_START_INDEX, 0);
        this.questions = FormUtils.getAllQuestions(getArguments().getString(ARGS_FORM_ID));
        this.mPresenter = new QuestionsDetailsPresenter(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        showQuestion(currentQuestion);
        return rootView;
    }

    @Override
    public String getTitle() {
        return "";
    }

    private void showQuestion(int index) {
        Question question = questions.get(index);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.details_container, QuestionFragment.newInstance(
                        question.getId(),
                        index,
                        questions.size()))
                .commit();
        currentQuestion = index;
    }

    @Override
    public void onNotes() {
        navigateTo(AddNoteFragment.newInstance(questions.get(currentQuestion).getId()));
    }

    @Override
    public void onNext() {
        hideFocusedKeyboard();
        if (currentQuestion < questions.size() - 1) {
            showQuestion(currentQuestion + 1);
        } else {
            SyncAdapter.requestUploadSync(getActivity());
            navigateBack();
        }
    }

    @Override
    public void onSaveAnswerIfCompleted(ViewGroup questionContainer) {
        List<ResponseAnswer> answers = mPresenter.getAnswerIfCompleted(questionContainer);
        if (answers.size() > 0) {
            Question question = questions.get(currentQuestion);
            BranchQuestionAnswer branchQuestionAnswer = new BranchQuestionAnswer(question.getId(), answers);
            Data.getInstance().saveAnswerResponse(branchQuestionAnswer);

            syncCurrentData(branchQuestionAnswer);
        }
    }

    private void syncCurrentData(BranchQuestionAnswer branchQuestionAnswer){
        final Context context = this.getActivity().getApplicationContext();
        if(NetworkUtils.isOnline(getActivity())){
            QuestionAnswer questionAnswer = new QuestionAnswer(branchQuestionAnswer,
                    getArguments().getString(ARGS_FORM_ID));
            NetworkService.syncCurrentQuestion(questionAnswer).startRequest(
                    new ToastMessageSubscriber(context, context.getString(R.string.answer_saved), context.getString(R.string.server_error)));
        } else {
            Toast.makeText(getActivity(), context.getString(R.string.no_connection_message), Toast.LENGTH_SHORT).show();
        }
    }
}
