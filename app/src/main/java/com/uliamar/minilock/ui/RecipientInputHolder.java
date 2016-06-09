package com.uliamar.minilock.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class RecipientInputHolder extends LinearLayout implements RecipientInput
    .OnRecipientInputTextChanged {

  private static final String STATE_RECIPIENT_LIST = "STATE_RECIPIENT_LIST";
  public static final int NORMAL_INPUT_NUMBER = 3;
  private static final String STATE_FATHER = "STATE_FATHER";
  private String ownerMinilockId;
  private boolean isCreating = true;

  public RecipientInputHolder(Context context) {
    this(context, null);
  }

  public RecipientInputHolder(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RecipientInputHolder(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setOrientation(VERTICAL);
    for (int i = 0; i < NORMAL_INPUT_NUMBER; i++) {
      addView(createASon());
    }
    isCreating = false;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    isCreating = true;
    Bundle bundle = (Bundle) state;
    super.onRestoreInstanceState(bundle.getParcelable(STATE_FATHER));
    ArrayList<String> list = bundle.getStringArrayList(STATE_RECIPIENT_LIST);
    while (getChildCount() != 0) {
      removeViewAt(0);
    }
    for (String s : list) {
      RecipientInput recipientInput = createASon();
      recipientInput.setMiniLockID(s);
      addView(recipientInput);
    }
    for (int i = getChildCount(); i < NORMAL_INPUT_NUMBER; ++i) {
      addView(createASon());
    }
    isCreating = false;
  }

  @Override
  protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
    dispatchFreezeSelfOnly(container);
  }

  @Override
  protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
    dispatchThawSelfOnly(container);
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Bundle bundle = new Bundle();
    bundle.putParcelable(STATE_FATHER, super.onSaveInstanceState());
    List<String> strings = new ArrayList<>();
    for (int i = 0; i < getChildCount(); ++i) {
      RecipientInput input = (RecipientInput) getChildAt(i);
      strings.add(input.getMiniLockID()); //remove empty
    }
    bundle.putStringArrayList(STATE_RECIPIENT_LIST, (ArrayList<String>) strings);
    return bundle;
  }

  public void setOwnerMinilockId(String ownerMinilockId) {
    this.ownerMinilockId = ownerMinilockId;
    for (int i = 0; i < getChildCount(); ++i) {
      ((RecipientInput) getChildAt(i)).setOwnerMiniLockID(ownerMinilockId);
    }
  }


  @Override
  public void textHasChanged() {
    if (isCreating)
      return;
    boolean areAllDirty = true;
    for (int i = 0; i < getChildCount(); ++i) {
      RecipientInput input = (RecipientInput) getChildAt(i);
      if (input.getMiniLockID().length() == 0) {
        areAllDirty = false;
        break;
      }
    }
    if (areAllDirty && getChildCount() != 1) {
      addView(createASon());
    }

  }

  private RecipientInput createASon() {
    RecipientInput newRecipient = new RecipientInput(getContext());
    newRecipient.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.WRAP_CONTENT));
    newRecipient.setOwnerMiniLockID(ownerMinilockId);
    newRecipient.setOnTextChangedListener(this);
    return newRecipient;
  }

  public void setInitialState() {
    RecipientInput r = (RecipientInput) getChildAt(0);
    r.setMiniLockID(ownerMinilockId);
  }
}
