package org.itstep.mariupol.afishamariupol;

import androidx.fragment.app.Fragment;

/**
 * класс активности для отображения картинки постера на весь экран
 */
public class BigImageActivity extends SingleFragmentActivity {
	//наследуем класс активности от абстрактного класса,
	//прикрепляющего к активности один фрагмент
    @Override
    protected Fragment createFragment() {
        return new BigImageFragment();
    }
}
