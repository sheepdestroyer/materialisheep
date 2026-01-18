package io.github.sheepdestroyer.materialisheep;

import androidx.lifecycle.ViewModel;

public class LazyLoadViewModel extends ViewModel {
    private boolean mEagerLoad;
    private boolean mLoaded;

    public boolean isEagerLoad() {
        return mEagerLoad;
    }

    public void setEagerLoad(boolean eagerLoad) {
        mEagerLoad = eagerLoad;
    }

    public boolean isLoaded() {
        return mLoaded;
    }

    public void setLoaded(boolean loaded) {
        mLoaded = loaded;
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    public void setInitialized(boolean initialized) {
        mInitialized = initialized;
    }

    private boolean mInitialized;
}
