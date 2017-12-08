package org.itstep.mariupol.afishamariupol;

import android.support.v4.app.Fragment;

/**
 * Класс активности экрана создания ссылки на страницу события в "социальных сетях"
 */
public class ShareActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new ShareFragment();
    }
}
