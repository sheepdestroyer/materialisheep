package io.github.sheepdestroyer.materialisheep;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.core.util.Pair;
import android.util.Log;

import io.github.sheepdestroyer.materialisheep.data.Item;
import io.github.sheepdestroyer.materialisheep.data.ItemManager;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

/**
 * A view model that provides a list of stories.
 */
public class StoryListViewModel extends ViewModel {
    private ItemManager mItemManager;
    private Scheduler mIoThreadScheduler;
    private MutableLiveData<Pair<Item[], Item[]>> mItems; // first = last updated, second = current

    private final io.reactivex.rxjava3.disposables.CompositeDisposable mDisposables = new io.reactivex.rxjava3.disposables.CompositeDisposable();

    /**
     * Injects the dependencies.
     *
     * @param itemManager       The item manager.
     * @param ioThreadScheduler The I/O thread scheduler.
     */
    public void inject(ItemManager itemManager, Scheduler ioThreadScheduler) {
        mItemManager = itemManager;
        mIoThreadScheduler = ioThreadScheduler;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mDisposables.clear();
    }

    /**
     * Gets the list of stories.
     *
     * @param filter    The filter to apply.
     * @param cacheMode The cache mode.
     * @return The list of stories.
     */
    public LiveData<Pair<Item[], Item[]>> getStories(String filter, @ItemManager.CacheMode int cacheMode) {
        if (mItems == null) {
            mItems = new MutableLiveData<>();
            mDisposables.add(Observable.fromCallable(() -> mItemManager.getStories(filter, cacheMode))
                    .subscribeOn(mIoThreadScheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(items -> setItems(items),
                            t -> Log.e("StoryListViewModel", "Error loading stories", t)));
        }
        return mItems;
    }

    /**
     * Refreshes the list of stories.
     *
     * @param filter    The filter to apply.
     * @param cacheMode The cache mode.
     */
    public void refreshStories(String filter, @ItemManager.CacheMode int cacheMode) {
        if (mItems == null || mItems.getValue() == null) {
            return;
        }
        mDisposables.add(Observable.fromCallable(() -> mItemManager.getStories(filter, cacheMode))
                .subscribeOn(mIoThreadScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> setItems(items),
                        t -> Log.e("StoryListViewModel", "Error refreshing stories", t)));

    }

    /**
     * Sets the list of items.
     *
     * @param items The list of items.
     */
    void setItems(Item[] items) {
        mItems.setValue(Pair.create(mItems.getValue() != null ? mItems.getValue().second : null, items));
    }
}
