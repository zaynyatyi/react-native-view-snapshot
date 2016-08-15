package com.github.jsierles.reactnativeviewsnapshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.DisplayMetrics;

import com.facebook.react.bridge.GuardedAsyncTask;
import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.NativeViewHierarchyManager;
import com.facebook.react.uimanager.UIBlock;
import com.facebook.react.uimanager.UIImplementation;
import com.facebook.react.uimanager.UIManagerModule;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class ViewSnapshotterModule extends ReactContextBaseJavaModule {
  private final ReactApplicationContext reactContext;

  public ViewSnapshotterModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "ViewSnapshotter";
  }

  @ReactMethod
  public void saveSnapshotToPath(int tag, ReadableMap options, Promise promise) {
    ReactApplicationContext context = getReactApplicationContext();
    String filePath = options.hasKey("filePath") ? options.getString("filePath") : null;
    if (filePath == null) {
      throw new JSApplicationIllegalArgumentException("Output file should be specified");
    }
    String format = options.hasKey("format") ? options.getString("format") : "png";
    Bitmap.CompressFormat compressFormat =
      format.equals("png")
        ? Bitmap.CompressFormat.PNG
        : format.equals("jpg")||format.equals("jpeg")
        ? Bitmap.CompressFormat.JPEG
        : format.equals("webm")
        ? Bitmap.CompressFormat.WEBP
        : null;
    if (compressFormat == null) {
      throw new JSApplicationIllegalArgumentException("Unsupported image format: " + format);
    }
    double quality = options.hasKey("quality") ? options.getDouble("quality") : 1.0;
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    Integer width = options.hasKey("width") ? (int)(displayMetrics.density * options.getDouble("width")) : null;
    Integer height = options.hasKey("height") ? (int)(displayMetrics.density * options.getDouble("height")) : null;
    try {
      File outputFile = new File(filePath);
      UIManagerModule uiManager = this.reactContext.getNativeModule(UIManagerModule.class);
      uiManager.addUIBlock(new ViewSnapshotter(tag, compressFormat, quality, width, height, outputFile, promise));
    }
    catch (Exception e) {
      promise.reject(ViewSnapshotter.ERROR_UNABLE_TO_SNAPSHOT, "Failed to snapshot view tag " + tag);
    }
  }
}
