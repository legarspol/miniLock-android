package com.uliamar.minilock.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uliamar.minilock.R;
import com.uliamar.minilock.minilocklib.Util;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class RecipientInput extends LinearLayout implements TextWatcher {
  @Bind(R.id.input) EditText inputView;
  @Bind(R.id.cross) View crossView;
  @Bind(R.id.label) TextView labelView;
  private String ownerMiniLockID;
  private OnRecipientInputTextChanged listener;
  private int colorLabelError;
  private int colorLabel;

  public RecipientInput(Context context) {
    this(context, null);
  }

  public RecipientInput(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RecipientInput(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    colorLabel = getResources().getColor(R.color.recipientLabel);
    colorLabelError = getResources().getColor(R.color.recipientLabelInvalid);
    View v = inflate(context, R.layout.customview_recipient, this);
    if (!isInEditMode()) {
      ButterKnife.bind(this, v);
      crossView.setVisibility(GONE);
      inputView.addTextChangedListener(this);
    }
  }


  public void setOnTextChangedListener(OnRecipientInputTextChanged listener) {
    this.listener = listener;
  }

  @OnClick(R.id.cross)
  void onCrossClick() {
    inputView.setText("");
    crossView.setVisibility(GONE);
  }

  public void setOwnerMiniLockID(String ownerMiniLockID) {
    this.ownerMiniLockID = ownerMiniLockID;
    refreshView();
  }

  public boolean isValidID() {
    String id = inputView.getText().toString();
    if (id.length() == 0) return true;
    return Util.validateID(id);
  }

  public String getMiniLockID() {
    return inputView.getText().toString();
  }

  public void setMiniLockID(String id) {
    inputView.setText(id);
    refreshView();
  }

  @Override
  public void afterTextChanged(Editable s) {
    refreshView();
  }

  private void refreshView() {
    if (isInEditMode())
      return;
    String string = inputView.getText().toString();
    if (string.length() == 0) {
      crossView.setVisibility(GONE);
      labelView.setVisibility(INVISIBLE);
    } else {
      if (crossView.getVisibility() == GONE) {
        crossView.setVisibility(VISIBLE);
      }

      if (isValidID()) {
        labelView.setBackgroundColor(colorLabel);
        if (ownerMiniLockID != null && ownerMiniLockID.equals(string)) {
          labelView.setVisibility(VISIBLE);
          labelView.setText(R.string.recipient_label_me);
        } else {
          labelView.setVisibility(INVISIBLE);
        }
      } else {
        labelView.setBackgroundColor(colorLabelError);
        labelView.setVisibility(VISIBLE);
        labelView.setText(R.string.recipient_label_invalid);
      }
    }

    if (listener != null) {
      listener.textHasChanged();
    }
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {

  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {

  }

  public interface OnRecipientInputTextChanged {
    void textHasChanged();
  }
}