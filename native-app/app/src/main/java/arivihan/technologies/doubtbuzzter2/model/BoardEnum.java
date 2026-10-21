package arivihan.technologies.doubtbuzzter2.model;

import android.util.Log;

import arivihan.technologies.doubtbuzzter2.R;
import lombok.Getter;

@Getter
public enum BoardEnum {
    CBSE("CBSE", R.drawable.cbse_board_logo),
    RBSE("RBSE", R.drawable.rbse_board_logo),
    MPBSE("MPBSE",R.drawable.mo_board_logo),
    DEFAULT("DEFAULT", R.drawable.mo_board_logo);

    private final String boardCode;
    private final int iconResId;

    BoardEnum(String boardCode, int iconResId) {
        this.boardCode = boardCode;
        this.iconResId = iconResId;
    }

    public String getDisplayName() {
        return boardCode;
    }

    public static BoardEnum fromLocalizedName(String key) {
        for (BoardEnum board : values()) {
            if (board.getDisplayName().equalsIgnoreCase(key.trim())) {
                return board;
            }
        }
        return DEFAULT;
    }
}
