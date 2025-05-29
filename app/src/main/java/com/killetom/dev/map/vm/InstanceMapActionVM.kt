package com.killetom.dev.map.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.killetom.dev.map.data.BaseLocation
import com.killetom.dev.map.data.NavInfor
import com.killetom.dev.map.data.TagLatLngData

class InstanceMapActionVM :ViewModel() {


    val tagPOI:MutableLiveData<TagLatLngData> = MutableLiveData<TagLatLngData>()

    val locationLatLng:MutableLiveData<BaseLocation> = MutableLiveData()

    val navReadyStatus:MutableLiveData<Boolean> = MutableLiveData(false)

    val navPlaneStatus:MutableLiveData<Boolean> = MutableLiveData(false)

    val navInfor:MutableLiveData<NavInfor> = MutableLiveData()

    val state :MutableLiveData<UIState> = MutableLiveData()

    sealed class UIState {
         class Normal() : UIState()
         class NAV() : UIState()
         class NAVDone():UIState()
    }
}