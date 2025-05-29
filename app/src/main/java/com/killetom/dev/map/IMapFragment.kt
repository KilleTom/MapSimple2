package com.killetom.dev.map

import androidx.fragment.app.Fragment
import com.killetom.dev.map.action.IMapAction

abstract class IMapFragment :Fragment(), IMapAction{


    var routerPlaneBottomPadding = 0
}