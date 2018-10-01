package ro.code4.monitorizarevot.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import ro.code4.monitorizarevot.BaseFragment;
import ro.code4.monitorizarevot.R;
import ro.code4.monitorizarevot.db.Data;
import ro.code4.monitorizarevot.db.Preferences;
import ro.code4.monitorizarevot.net.model.Form;
import ro.code4.monitorizarevot.widget.ChangeBranchBarLayout;

import static ro.code4.monitorizarevot.ToolbarActivity.BRANCH_SELECTION_BACKSTACK_INDEX;

public class FormsListFragment extends BaseFragment {
    public static FormsListFragment newInstance() {
        return new FormsListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forms_list, container, false);

        // TODO dynamically add forms https://github.com/code4romania/monitorizare-vot-android/issues/50
        rootView.findViewById(R.id.tile_form_A).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForm(Data.getInstance().getForm("A"));
            }
        });
        rootView.findViewById(R.id.tile_form_B).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForm(Data.getInstance().getForm("B"));
            }
        });
        rootView.findViewById(R.id.tile_form_C).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForm(Data.getInstance().getForm("C"));
            }
        });

        rootView.findViewById(R.id.tile_form_notes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateTo(AddNoteFragment.newInstance());
            }
        });

        setBranchBar((ChangeBranchBarLayout) rootView.findViewById(R.id.change_branch_bar));

        return rootView;
    }

    private void setBranchBar(ChangeBranchBarLayout barLayout) {
        barLayout.setBranchText(Preferences.getCountyCode() + " " + Preferences.getBranchNumber());
        barLayout.setChangeBranchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateBackUntil(BRANCH_SELECTION_BACKSTACK_INDEX);
            }
        });
    }

    private void showForm(Form form) {
        if (form != null && form.getSections() != null && form.getSections().size() > 0) {
            navigateTo(QuestionsOverviewFragment.newInstance(form.getId()));
        } else {
            Toast.makeText(getActivity(), getString(R.string.error_no_form_data), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public String getTitle() {
        return getString(R.string.title_forms_list);
    }
}
