package org.itstep.mariupol.afishamariupol;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * абстрактный класс, от которого может наследоваться класс активности, использующей один фрагмент
 */

public abstract class SingleFragmentActivity extends FragmentActivity {
    protected abstract Fragment createFragment();

    protected int getLayoutResId() {
    	return R.layout.activity_fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(android.R.id.tabhost);

        if (fragment == null) {
            fragment = createFragment();
            manager.beginTransaction()
                .add(android.R.id.tabhost, fragment)
                .commit();
        }
    }
}
