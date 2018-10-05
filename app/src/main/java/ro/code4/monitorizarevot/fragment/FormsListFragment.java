package ro.code4.monitorizarevot.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

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

        // TODO dynamically add forms and create layout (Grid?) https://github.com/code4romania/monitorizare-vot-android/issues/50
        Map<String, Integer> forms = new HashMap<>();
        forms.put("A", R.id.tile_form_A);
        forms.put("B", R.id.tile_form_B);
        forms.put("C1", R.id.tile_form_C1);
        forms.put("C2", R.id.tile_form_C2);
        forms.put("C3", R.id.tile_form_C3);
        forms.put("C4", R.id.tile_form_C4);
        forms.put("C5", R.id.tile_form_C5);
        forms.put("C6", R.id.tile_form_C6);
        forms.put("D", R.id.tile_form_D);

        for(final String formId : forms.keySet()) {
            rootView.findViewById(forms.get(formId)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showForm(Data.getInstance().getForm(formId));
                }
            });
        }

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
