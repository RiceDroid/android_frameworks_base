/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.util.arcana;

import static android.view.DisplayCutout.BOUNDS_POSITION_LEFT;
import static android.view.DisplayCutout.BOUNDS_POSITION_RIGHT;

import android.Manifest;
import android.content.Context;
import android.content.res.Resources;
import android.content.Intent;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.app.AlertDialog;
import android.app.IActivityManager;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.input.InputManager;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;

import com.android.internal.R;
import com.android.internal.statusbar.IStatusBarService;

import java.util.List;
import java.util.Locale;

public class ArcanaUtils {
    private static final String TAG = "ArcanaUtils";

    private static final boolean DEBUG = false;

    public static int getQSColumnsCount(Context context, int resourceCount) {
        final int QS_COLUMNS_MIN = 2;
        final Resources res = context.getResources();
        int value = QS_COLUMNS_MIN;
        if (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            value = Settings.System.getIntForUser(
                    context.getContentResolver(), "qs_layout_columns",
                    resourceCount, UserHandle.USER_CURRENT);
        } else {
            value = Settings.System.getIntForUser(
                    context.getContentResolver(), "qs_layout_columns_landscape",
                    resourceCount, UserHandle.USER_CURRENT);
        }
        return Math.max(QS_COLUMNS_MIN, value);
    }

    public static int getQuickQSColumnsCount(Context context, int resourceCount) {
        return getQSColumnsCount(context, resourceCount);
    }

    public static boolean getQSTileLabelHide(Context context) {
        return Settings.System.getIntForUser(context.getContentResolver(),
                Settings.System.QS_TILE_LABEL_HIDE,
                0, UserHandle.USER_CURRENT) != 0;
    }

    public static boolean getQSTileVerticalLayout(Context context, int defaultValue) {
        return Settings.System.getIntForUser(context.getContentResolver(),
                Settings.System.QS_TILE_VERTICAL_LAYOUT,
                defaultValue, UserHandle.USER_CURRENT) != 0;
    }
}
