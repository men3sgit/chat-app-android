package com.example.chat_app.fragments;

import androidx.fragment.app.Fragment;

public final class FragmentFactory {
    public final static Fragment createFragment(FragmentType type) {
        switch (type) {
            case PROFILE:
                return new ProfileFragment();
            case NOTIFICATION:
                return new NotificationFragment();
            case SIGN_OUT:
                return new SignOutFragment();
            case SETTING:
                return new SettingFragment();
            case SHARE:
                return new ShareFragment();
        }
        return null;
    }
}
