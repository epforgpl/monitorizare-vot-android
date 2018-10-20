package ro.code4.monitorizarevot.observable;

import android.content.Context;
import android.widget.Toast;

public class ToastMessageSubscriber extends ObservableListener<Boolean> {

    private final Context context;
    private final String successMessage;
    private final String errorMessage;

    public ToastMessageSubscriber(Context context, String successMessage, String errorMessage) {
        this.context = context;
        this.successMessage = successMessage;
        this.errorMessage = errorMessage;
    }

    @Override
    public void onSuccess() {
        Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(Throwable e) {
        Toast.makeText(context, errorMessage + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
