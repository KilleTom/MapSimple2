package com.killetom.dev.map

import android.app.Application
import androidx.lifecycle.ViewModelStore
import com.amap.api.navi.AMapNavi


class MapSimpleApp :Application() {

    private val viewModelStore = ViewModelStore()

    fun getGlobalViewModelStore(): ViewModelStore {
        return viewModelStore
    }

    override fun onCreate() {
        super.onCreate()

//        AMapNavi.getInstance(this).init();
    }
}