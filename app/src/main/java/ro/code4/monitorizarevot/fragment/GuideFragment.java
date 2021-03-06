package ro.code4.monitorizarevot.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import ro.code4.monitorizarevot.BaseFragment;
import ro.code4.monitorizarevot.R;

public class GuideFragment extends BaseFragment {
    public static GuideFragment newInstance(String url, String title) {
        GuideFragment f = new GuideFragment();
        f.url = url;
        f.title = title;
        return f;
    }

    private String url, title;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guide, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WebView guideView = view.findViewById(R.id.web_view_guide);
        WebSettings webSettings = guideView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        guideView.loadUrl(this.url);
    }

    @Override
    public String getTitle() {
        return title;
    }
}
