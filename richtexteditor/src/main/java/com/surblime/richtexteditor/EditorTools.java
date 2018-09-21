package com.surblime.richtexteditor;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by moses on 8/30/18.
 */

public class EditorTools extends FrameLayout {
    private final int[] ids = new int[]{
            R.id.action_align_center,
            R.id.action_align_left,
            R.id.action_align_right,
            R.id.action_bg_color,
            R.id.action_blockquote,
            R.id.action_bold,
            R.id.action_heading1,
            R.id.action_heading2,
            R.id.action_heading3,
            R.id.action_heading4,
            R.id.action_heading5,
            R.id.action_heading6,
            R.id.action_indent,
            R.id.action_insert_bullets,
            R.id.action_insert_checkbox,
            R.id.action_insert_image,
            R.id.action_insert_link,
            R.id.action_insert_numbers,
            R.id.action_italic,
            R.id.action_outdent,
            R.id.action_redo,
            R.id.action_strikethrough,
            R.id.action_subscript,
            R.id.action_superscript,
            R.id.action_txt_color,
            R.id.action_underline,
            R.id.action_undo
    };
    private AlertDialog enterLinkDialog;

    private ActionListener actionListener;

    public EditorTools(@NonNull Context context) {
        super(context);
        initialize(null, 0);
    }

    public EditorTools(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs, 0);
    }

    public EditorTools(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public EditorTools(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(attrs, defStyleRes);
    }

    private void initialize(AttributeSet attributeSet, int defStyleRes) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.editor_tools, this, false);
        addView(view);

        if (attributeSet != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.EditorTools, defStyleRes, 0);

            int background = typedArray.getColor(R.styleable.EditorTools_backgroundColor, Color.TRANSPARENT);

            setBackgroundColor(background);

            int foreground = typedArray.getColor(R.styleable.EditorTools_foregroundColor, Color.BLACK);
            for (int id : ids) {
                ImageView imageView = findViewById(id);
                imageView.setColorFilter(foreground);
            }
            typedArray.recycle();
        }
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public ActionListener getActionListener() {
        return actionListener;
    }

    public void setEditor(final Editor mEditor) {
        findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.undo();
            }
        });

        findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.redo();
            }
        });

        findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBold();
            }
        });

        findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setItalic();
            }
        });

        findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setSubscript();
            }
        });

        findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setSuperscript();
            }
        });

        findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setStrikeThrough();
            }
        });

        findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setUnderline();
            }
        });

        findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(1);
            }
        });

        findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(2);
            }
        });

        findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(3);
            }
        });

        findViewById(R.id.action_heading4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(4);
            }
        });

        findViewById(R.id.action_heading5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(5);
            }
        });

        findViewById(R.id.action_heading6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(6);
            }
        });

        findViewById(R.id.action_txt_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override
            public void onClick(View v) {
                mEditor.setTextColor(isChanged ? Color.BLACK : Color.RED);
                isChanged = !isChanged;
            }
        });

        findViewById(R.id.action_bg_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override
            public void onClick(View v) {
                mEditor.setTextBackgroundColor(isChanged ? Color.TRANSPARENT : Color.YELLOW);
                isChanged = !isChanged;
            }
        });

        findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setIndent();
            }
        });

        findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setOutdent();
            }
        });

        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignLeft();
            }
        });

        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignCenter();
            }
        });

        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignRight();
            }
        });

        findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBlockquote();
            }
        });

        findViewById(R.id.action_insert_bullets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBullets();
            }
        });

        findViewById(R.id.action_insert_numbers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setNumbers();
            }
        });

        findViewById(R.id.action_insert_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertImage("http://www.1honeywan.com/dachshund/image/7.21/7.21_3_thumb.JPG",
                        "dachshund");
                if (actionListener != null) {
                    actionListener.onInsertImage();
                }
            }
        });

        findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) getContext();

                LinkDialog linkDialog = new LinkDialog();
                linkDialog.setEditor(mEditor);

                linkDialog.show(activity.getSupportFragmentManager(), "Link Dialog");
                if (actionListener != null) {
                    actionListener.onInsertLink();
                }
            }
        });
        findViewById(R.id.action_insert_checkbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertTodo();
            }
        });

        mEditor.requestFocus();
    }

    public interface ActionListener {
        void onInsertLink();

        void onInsertImage();
    }

    public static class LinkDialog extends android.support.v4.app.DialogFragment {

        private EditText linkText;
        private EditText titleText;
        private Button okButton;
        private Button cancelButton;
        private Editor editor;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.dialog_add_link, container, false);
        }

        @Override
        public void onStart() {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            super.onStart();
        }

        public void setEditor(Editor editor) {
            this.editor = editor;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            linkText = view.findViewById(R.id.linkText);
            titleText = view.findViewById(R.id.linkTitle);
            okButton = view.findViewById(R.id.okButton);
            cancelButton = view.findViewById(R.id.cancelButton);

            cancelButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            okButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    editor.insertLink(linkText.getText().toString(), titleText.getText().toString());
                    dismiss();
                }
            });

            okButton.setEnabled(false);

            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    okButton.setEnabled(!(linkText.getText().toString().trim().isEmpty() ||
                            titleText.getText().toString().trim().isEmpty()));
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            };

            linkText.addTextChangedListener(textWatcher);
            titleText.addTextChangedListener(textWatcher);
        }
    }
}
