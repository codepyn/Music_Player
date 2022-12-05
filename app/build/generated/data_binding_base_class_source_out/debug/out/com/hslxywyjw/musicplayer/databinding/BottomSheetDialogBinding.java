// Generated by view binder compiler. Do not edit!
package com.hslxywyjw.musicplayer.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.hslxywyjw.musicplayer.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class BottomSheetDialogBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final LinearLayout min15;

  @NonNull
  public final LinearLayout min30;

  @NonNull
  public final LinearLayout min60;

  private BottomSheetDialogBinding(@NonNull LinearLayout rootView, @NonNull LinearLayout min15,
      @NonNull LinearLayout min30, @NonNull LinearLayout min60) {
    this.rootView = rootView;
    this.min15 = min15;
    this.min30 = min30;
    this.min60 = min60;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static BottomSheetDialogBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static BottomSheetDialogBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.bottom_sheet_dialog, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static BottomSheetDialogBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.min_15;
      LinearLayout min15 = ViewBindings.findChildViewById(rootView, id);
      if (min15 == null) {
        break missingId;
      }

      id = R.id.min_30;
      LinearLayout min30 = ViewBindings.findChildViewById(rootView, id);
      if (min30 == null) {
        break missingId;
      }

      id = R.id.min_60;
      LinearLayout min60 = ViewBindings.findChildViewById(rootView, id);
      if (min60 == null) {
        break missingId;
      }

      return new BottomSheetDialogBinding((LinearLayout) rootView, min15, min30, min60);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}