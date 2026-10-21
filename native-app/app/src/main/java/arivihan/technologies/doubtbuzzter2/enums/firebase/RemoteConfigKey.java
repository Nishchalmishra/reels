package arivihan.technologies.doubtbuzzter2.enums.firebase;

import lombok.Getter;

public enum RemoteConfigKey {

    INTERNAL_DEV_USERS_TO_BY_PASS_FEATURE("internal_dev_users_to_by_pass_all_features"),
    QR_CROP_LEFT("qr_crop_left"),
    QR_CROP_TOP("qr_crop_top"),
    QR_CROP_RIGHT("qr_crop_right"),
    QR_CROP_BOTTOM("qr_crop_bottom"),
    SHOW_I_WANT_TO_DISCUSS("show_i_want_to_discuss");

    @Getter
    private final String key;
    RemoteConfigKey(String key) {
        this.key = key;
    }
}
