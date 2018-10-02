package ro.code4.monitorizarevot.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ro.code4.monitorizarevot.BaseFragment;
import ro.code4.monitorizarevot.R;
import ro.code4.monitorizarevot.constants.County;
import ro.code4.monitorizarevot.db.Data;
import ro.code4.monitorizarevot.db.Preferences;
import ro.code4.monitorizarevot.net.model.BranchDetails;
import ro.code4.monitorizarevot.net.model.District;

public class BranchSelectionFragment extends BaseFragment {
    private Spinner districtsSpinner1;
    private Spinner districtsSpinner2;
    private Spinner countySpinner; // level3
    private EditText branchNumber;
    private County selectedCounty;

    public static BranchSelectionFragment newInstance() {
        return new BranchSelectionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_branch_selection, container, false);

        districtsSpinner1 = rootView.findViewById(R.id.branch_selector_district_level1);
        districtsSpinner2 = rootView.findViewById(R.id.branch_selector_district_level2);
        countySpinner = rootView.findViewById(R.id.branch_selector_county);
        branchNumber = rootView.findViewById(R.id.branch_number_input);

        districtsSpinner2.setEnabled(false);
        branchNumber.setEnabled(false);

        List<District> districts = Data.getInstance().getDistricts(1);
        if (districts.isEmpty()) {
            loadDataFromJson();
        }

        // TODO
        setLevel1Dropdown(districtsSpinner1, District.extractTitles(districts));
        // setCountiesDropdown(countySpinner, District.extractTitles(districts));
        setContinueButton(rootView.findViewById(R.id.button_continue));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (Preferences.hasBranch()) {
            // TODO
            countySpinner.setSelection(County.getIndexByCountyCode(Preferences.getCountyCode()));
            branchNumber.setText(String.valueOf(Preferences.getBranchNumber()));
            branchNumber.setEnabled(true);
        }
    }

    private void setCountiesDropdown(Spinner dropdown, List<String> options) {
        ArrayAdapter<String> countyAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.support_simple_spinner_dropdown_item, County.getCountiesNames());

        dropdown.setAdapter(countyAdapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCounty = County.getCountyByIndex(position);
                branchNumber.setEnabled(true);
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
                if (selectedCounty == null) {
                    Toast.makeText(getActivity(), R.string.invalid_branch_county, Toast.LENGTH_SHORT).show();
                } else if (branchNumber.getText().toString().length() == 0) {
                    Toast.makeText(getActivity(), R.string.invalid_branch_number, Toast.LENGTH_SHORT).show();
                } else if (getBranchNumber() <= 0) {
                    Toast.makeText(getActivity(), R.string.invalid_branch_number_minus, Toast.LENGTH_SHORT).show();
                } else if (getBranchNumber() > selectedCounty.getBranchesCount()) {
                    Toast.makeText(getActivity(), getBranchExceededError(), Toast.LENGTH_SHORT).show();
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
        Preferences.saveCountyCode(County.getCountyByIndex(countySpinner.getSelectedItemPosition()).getCode());
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
                selectedCounty.getName(), String.valueOf(selectedCounty.getBranchesCount()));
    }

    private void loadDataFromJson() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                    getActivity().getAssets().open("districts.jsonlines")));

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
                        d = new District("" + obj.getInt("teryt"), 2,
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

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
