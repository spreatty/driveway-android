package org.openauto.webviewauto;

import com.google.android.apps.auto.sdk.CarActivity;
import com.google.android.apps.auto.sdk.CarActivityService;

public class CarService extends CarActivityService {
    public Class<? extends CarActivity> getCarActivity() {
        return WebViewAutoActivity.class;
    }
}
