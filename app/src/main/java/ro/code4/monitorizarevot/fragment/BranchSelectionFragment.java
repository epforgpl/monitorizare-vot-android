package ro.code4.monitorizarevot.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ro.code4.monitorizarevot.BaseFragment;
import ro.code4.monitorizarevot.R;
import ro.code4.monitorizarevot.db.Data;
import ro.code4.monitorizarevot.db.Preferences;
import ro.code4.monitorizarevot.net.model.District;

public class BranchSelectionFragment extends BaseFragment {
    private Spinner districtsSpinner1;
    private Spinner districtsSpinner2;
    private Spinner districtsSpinner3; // level3
    private EditText branchNumber;
    private District selectedDistrict;
    private boolean areYouSureOfBranch = false;

    public static BranchSelectionFragment newInstance() {
        return new BranchSelectionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_branch_selection, container, false);

        // TODO make it dynamic and dependent on the API data? #62
        districtsSpinner1 = rootView.findViewById(R.id.branch_selector_district_level1);
        districtsSpinner2 = rootView.findViewById(R.id.branch_selector_district_level2);
        districtsSpinner3 = rootView.findViewById(R.id.branch_selector_county);
        branchNumber = rootView.findViewById(R.id.branch_number_input);

        // load data
        List<District> topDistricts = Data.getInstance().getDistricts(1);
        if (topDistricts.isEmpty()) {
            loadDataFromJson();
            topDistricts = Data.getInstance().getDistricts(1);
        }

        if (Preferences.hasBranch()) {
            String selectedDistrictId = Preferences.getCountyCode();

            District l3 = Data.getInstance().getDistrict(selectedDistrictId);
            District l2 = Data.getInstance().getDistrictParent(l3);
            District l1 = Data.getInstance().getDistrictParent(l2);

            setOptions(districtsSpinner1, topDistricts, l1, false);
            setOptions(districtsSpinner2, Data.getInstance().getDistrictsOf(l1.getId()), l2, false);
            setOptions(districtsSpinner3, Data.getInstance().getDistrictsOf(l2.getId()), l3, false);

            selectedDistrict = l3;

            branchNumber.setText(String.valueOf(Preferences.getBranchNumber()));

            districtsSpinner2.setEnabled(true);
            districtsSpinner3.setEnabled(true);
            branchNumber.setEnabled(true);

        } else {
            setOptions(districtsSpinner1, topDistricts,
                    Data.getInstance().getDistrict("140000"), true);

            districtsSpinner2.setEnabled(true);
            districtsSpinner3.setEnabled(false);
            branchNumber.setEnabled(false);
        }


        setItemsSelectedListener(districtsSpinner1, (d) -> {
            districtsSpinner2.setEnabled(true);
            districtsSpinner3.setEnabled(false);
            branchNumber.setEnabled(false);

            selectedDistrict = null;
            branchNumber.setText("");

            setOptions(districtsSpinner2, Data.getInstance().getDistrictsOf(d.getId()));
        });

        setItemsSelectedListener(districtsSpinner2, (e) -> {
            districtsSpinner3.setEnabled(true);
            branchNumber.setEnabled(false);

            selectedDistrict = null;
            branchNumber.setText("");

            setOptions(districtsSpinner3, Data.getInstance().getDistrictsOf(e.getId()));
        });

        setItemsSelectedListener(districtsSpinner3, (f) -> {
            branchNumber.setEnabled(true);

            selectedDistrict = f;
            branchNumber.setText("");
        });

        setContinueButton(rootView.findViewById(R.id.button_continue));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private interface OnSelectedDistrict {
        void onSelected(District d);
    }

    private void setOptions(Spinner dropdown, List<District> options) {
        setOptions(dropdown, options, null, false);
    }

    private void setOptions(Spinner dropdown, List<District> options, District selected, boolean fireListeners) {
        ArrayAdapter<District> countyAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.support_simple_spinner_dropdown_item, options);

        dropdown.setAdapter(countyAdapter);
        if (selected != null) {
            // use animate false in order not to fire listeners
            if (fireListeners)
                dropdown.setSelection(options.indexOf(selected));
            else
                dropdown.setSelection(options.indexOf(selected), fireListeners);
        }
    }

    private void setItemsSelectedListener(Spinner dropdown, final OnSelectedDistrict listener) {
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                District d = (District) dropdown.getSelectedItem();

                listener.onSelected(d);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setContinueButton(View button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedDistrict == null) {
                    Toast.makeText(getActivity(), R.string.invalid_branch_county, Toast.LENGTH_SHORT).show();
                } else if (branchNumber.getText().toString().length() == 0) {
                    Toast.makeText(getActivity(), R.string.invalid_branch_number, Toast.LENGTH_SHORT).show();
                } else if (getBranchNumber() <= 0) {
                    Toast.makeText(getActivity(), R.string.invalid_branch_number, Toast.LENGTH_SHORT).show();
                } else if (getBranchNumber() > selectedDistrict.getBranchesCount()) {
                    if (!areYouSureOfBranch) { // TODO hack for Poland;  to resolve in #48 and #74
                        Toast.makeText(getActivity(), "Czy na pewno to poprawny numer komisji? Sprawdź dokładnie!", Toast.LENGTH_SHORT).show(); // hack for Poland
                        areYouSureOfBranch = true;
                    } else {
                        persistSelection();
                        navigateTo(BranchDetailsFragment.newInstance());
                    }
                    // Toast.makeText(getActivity(), getBranchExceededError(), Toast.LENGTH_SHORT).show();
                } else {
                    persistSelection();
                    navigateTo(BranchDetailsFragment.newInstance());
                }
            }
        });
    }

    @Override
    public String getTitle() {
        return getString(R.string.title_branch_selection);
    }

    @Override
    public boolean withMenu() {
        return false;
    }

    private void persistSelection() {
        District d = (District) districtsSpinner3.getSelectedItem();

        Preferences.saveCountyCode(d.getId());
        Preferences.saveBranchNumber(getBranchNumber());
    }

    public int getBranchNumber() {
        try {
            return Integer.parseInt(branchNumber.getText().toString());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String getBranchExceededError() {
        return getString(R.string.invalid_branch_number_max,
                selectedDistrict.getTitle(), String.valueOf(selectedDistrict.getBranchesCount()));
    }

    // TODO make it dynamic and dependent on the API data? #62
    private void loadDataFromJson() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    getActivity().getAssets().open("districts.jsonlines")))) {

            Map<String, District> districts = new HashMap<>();

            String line;
            while ((line = br.readLine()) != null) {
                JSONObject obj = new JSONObject(line);
                District d;
                switch(obj.getString("type")) {
                    case "wojewodztwo":
                        d = new District("" + obj.getInt("teryt"), 1,
                                obj.getString("title"), null);
                        districts.put(d.getId(), d);
                        break;

                    case "powiat":
                        d = new District("" + obj.getInt("teryt"), 2,
                                obj.getString("title"), "" + obj.getInt("teryt_parent"));
                        districts.put(d.getId(), d);
                        break;

                    case "gmina":
                        d = new District("" + obj.getInt("teryt"), 3,
                                obj.getString("title"), "" + obj.getInt("teryt_parent"));
                        districts.put(d.getId(), d);
                        break;

                    case "district":
                        //fields: teryt_parent, number, place, accessible
                        districts.get("" + obj.getInt("teryt_parent")).incrementBranchesCount();
                        break;
                }
            }

            Data.getInstance().saveDistricts(new ArrayList<>(districts.values()));

        } catch (IOException e) {
            Toast.makeText(getActivity(), "Wystąpił błąd: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (JSONException e) {
            Toast.makeText(getActivity(), "Wystąpił błąd: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
