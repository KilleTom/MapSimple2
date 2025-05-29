package com.killetom.dev.map

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.*
import com.amap.api.maps.model.*
import com.amap.api.navi.model.NaviLatLng
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItemV2
import com.amap.api.services.poisearch.PoiResultV2
import com.amap.api.services.poisearch.PoiSearchV2
import com.killetom.dev.map.data.BaseLocation
import com.killetom.dev.map.data.NavInfor
import com.killetom.dev.map.data.TagLatLngData
import com.killetom.dev.map.databinding.FragmentAmapBinding
import com.killetom.dev.map.vm.InstanceMapActionVM


class AMapFragment(private val tagPointVM: InstanceMapActionVM) : IMapFragment(), LocationSource,
    AMapLocationListener {

    private var aMap: AMap? = null

    private var lastLocation: AMapLocation? = null

    private var binding: FragmentAmapBinding? = null

    private val TAG = "AMapFragment"

    private var locationClient: AMapLocationClient? = null
    private var locationOption: AMapLocationClientOption? = null

    private val navHelperAction = NavHelperAction()

    private var mListener: LocationSource.OnLocationChangedListener? = null
    private var navagationPlanePolygonLine: Polyline? = null
    private var actualNavgationPolygonLine: Polyline? = null
    private var cardMarker: Marker? = null
    private var riderMarker: Marker? = null
    private var personMarker: Marker? = null

    private val carIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_card)
    private val bikeIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_bike)
    private val personWalkIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_person_walk)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navHelperAction.init(requireContext())
        try {
            Log.i(TAG, "onCreate-mapReadychecker")
            mapReadychecker()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        navHelperAction.calculateStatusCall = {

        }

        navHelperAction.requireLocation = { lastLocation }

        navHelperAction.planeRouteCall = { routes ->

            navagationPlanePolygonLine?.remove()

            navagationPlanePolygonLine = aMap?.addPolyline(
                PolylineOptions()
                    .addAll(routes)
                    .width(15f)
                    .color(Color.argb(150, 0, 200, 0)) // 半透明绿色
                    .setDottedLine(false)
            )

            navagationPlanePolygonLine?.let { line ->
                val boundsBuilder = LatLngBounds.Builder()
                val points = line.points
                for (point in points) {
                    boundsBuilder.include(point)
                }
                val bounds = boundsBuilder.build()

                // 步骤2：移动地图视角到边界区域中心

                // 步骤2：移动地图视角到边界区域中心
                aMap!!.animateCamera(
                    CameraUpdateFactory.newLatLngBoundsRect(
                        bounds,
                        100,  // 左右内边距
                        100,  // 上下内边距
                        100,  // 右边距（通常与左相同）
                        routerPlaneBottomPadding // 下边距（通常与上相同）
                    )
                )
            }


            tagPointVM.navReadyStatus.postValue(true)

        }

        navHelperAction.updateNavInfoCall =
            { totalDistance: Long, currentDistance: Long, currentNavTime: Long, speed: Float ->

                tagPointVM.navInfor.postValue(
                    NavInfor(
                        totalDistance.toDouble(),
                        speed.toDouble(),
                        currentDistance.toDouble(),
                        currentNavTime
                    )
                )

            }


        navHelperAction.updateActualNavgationRouter = { routes ->
            Log.i("update_router", "update${System.currentTimeMillis()}")
//            Log.i("update_router",Thread.currentThread().name)
            try {
                actualNavgationPolygonLine?.remove()

                actualNavgationPolygonLine = aMap?.addPolyline(
                    PolylineOptions()
                        .addAll(routes)
                        .width(15f)
                        .color(Color.argb(190, 66, 224, 221)) // 半透明绿色
                        .setDottedLine(false)
                )

            } catch (e: Exception) {
                Log.i("update_router", "update error$e")
                e.printStackTrace()
            }

        }

        navHelperAction.updateNavIconMarke = { location ->

            val navWalkAction = navHelperAction.navWalkAction
            val navRiderAction = navHelperAction.navRiderAction
            val navDriveAction = navHelperAction.navDriveAction

            val latlng = LatLng(location.coord.latitude, location.coord.longitude)

            when (navHelperAction.getNavActionType()) {

                navDriveAction -> {
                    var marker = cardMarker

                    if (marker == null) {

                        val markerView: View =
                            LayoutInflater.from(requireContext())
                                .inflate(R.layout.card_icon_layout, null)
                        val option = MarkerOptions()
                            .position(latlng)
                            .icon(BitmapDescriptorFactory.fromView(markerView))
                            .rotateAngle(location.roadBearing)
                            .anchor(0.5f, 0.5f)

                        marker = aMap?.addMarker(option)

                        cardMarker = marker

                    } else {
                        marker.position = latlng
                        marker.rotateAngle = location.roadBearing

                    }

                }

                navRiderAction -> {
                    var marker = riderMarker
                    val markerView: View =
                        LayoutInflater.from(requireContext())
                            .inflate(R.layout.bike_icon_layout, null)
                    if (marker == null) {

                        val option = MarkerOptions()
                            .position(latlng)
                            .icon(BitmapDescriptorFactory.fromView(markerView))
                            .rotateAngle(location.roadBearing)
                            .anchor(0.5f, 0.5f)


                        marker = aMap?.addMarker(option)

                        riderMarker = marker

                    } else {
                        marker.position = latlng
                        marker.rotateAngle = location.roadBearing
                    }
                }

                navWalkAction -> {
                    var marker = personMarker

                    if (marker == null) {

                        val markerView: View =
                            LayoutInflater.from(requireContext())
                                .inflate(R.layout.person_icon_layout, null)

                        val option = MarkerOptions()
                            .position(latlng)
                            .icon(BitmapDescriptorFactory.fromView(markerView))

                            .anchor(0.5f, 0.5f)


                        marker = aMap?.addMarker(option)

                        personMarker = marker

                    } else {
                        marker.position = latlng
                        marker.rotateAngle = location.roadBearing
                    }
                }
            }

        }


        navHelperAction.navStartCall = {
//            aMap?.clear()
            tagPointVM.state.postValue(InstanceMapActionVM.UIState.NAV())

            cardMarker?.remove()
            riderMarker?.remove()
            personMarker?.remove()


        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.i(TAG, "onCreateView")
        val current = FragmentAmapBinding.inflate(inflater, container, false)

        binding = current

        current.map.onCreate(savedInstanceState)

        return current.root
    }


    override fun onStart() {
        super.onStart()
//        Log.i(TAG,"onStart")
        var currentMap = aMap

        if (currentMap == null) {
//            Log.i(TAG,"onStart-initMap")
            currentMap = binding?.map?.map ?: return
//            Log.i(TAG,"onStart-initMap$aMap")
//            Log.i(TAG,"onStart-vis${binding?.map?.visibility}")

            currentMap.mapType = AMap.MAP_TYPE_NORMAL
            currentMap.showIndoorMap(true)
            currentMap.showBuildings(true)


            currentMap.setOnPOIClickListener(object : AMap.OnPOIClickListener {
                override fun onPOIClick(p: Poi?) {

                    if (navHelperAction.getNavStatus())
                        return

                    p?.let { showPOETICInfo(p) }

                }

            })

            //设置了定位的监听
            currentMap.setLocationSource(this)
            //初始化定位蓝点样式类

            // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位
            currentMap.isMyLocationEnabled = true

            // 设置定位图标样式
            currentMap.uiSettings?.isMyLocationButtonEnabled = true // 显示默认的定位按钮

            val myLocationStyle = MyLocationStyle()
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)//设置定位模式
            myLocationStyle.interval(2000) //只在连续定位模式下生效
            myLocationStyle.showMyLocation(true)//设置是否显示定位小蓝点
            // 将定位蓝点移动到屏幕中心
            myLocationStyle.anchor(0.5f, 0.5f).myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
            currentMap.myLocationStyle = myLocationStyle
            currentMap.isMyLocationEnabled = true// 是否启动显示定位蓝点,默认是false

            aMap = currentMap
            initLocation()
            setCenter(BaseLocation(21.28773, 110.367324))
//            aMap?.setOn
        }
//        Log.i(TAG,"onStart")

    }

    private fun initLocation() {
        // 初始化定位客户端
        try {
            locationClient = AMapLocationClient(requireActivity().application.applicationContext)

        } catch (e: java.lang.Exception) {
            Log.e(TAG, "loactionerror")
            e.printStackTrace()
        }

        // 设置定位回调监听
        locationClient?.setLocationListener(this)

        // 初始化定位参数
        locationOption = AMapLocationClientOption()

        // 设置定位模式为高精度模式
        locationOption?.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy

        // 设置是否返回地址信息（默认返回地址信息）
        locationOption?.isNeedAddress = true

        // 设置是否只定位一次
        locationOption?.isOnceLocation = false

        // 设置是否强制刷新WIFI，默认为true
//        locationOption?.setWifiActiveScan(true)

        // 设置是否允许模拟位置
        locationOption?.isMockEnable = true

        // 设置定位间隔(单位毫秒)
        locationOption?.interval = 2000

        // 给定位客户端对象设置定位参数
        locationClient?.setLocationOption(locationOption)

        // 启动定位
        locationClient?.startLocation()
    }

    private fun showPOETICInfo(p: Poi) {

        val latLng = p.coordinate

        // 移动相机到该位置
        aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        // 显示信息窗口

        // 显示信息窗口
//        aMap.showInfoWindow()

        val tag = TagLatLngData()
        tag.name = p.name
        tag.poiID = p.poiId
        tag.latLng = BaseLocation(latLng.latitude, latLng.longitude)
        tag.address = p.name
//        tag.city = p.cityName
//        tag.address = p.adName
        aMap?.let { map ->

            val location = map.myLocation ?: return@let
            val dis = AMapUtils.calculateLineDistance(
                LatLng(location.latitude, location.longitude),
                p.coordinate
            )
            tag.disdanceMessage = "距离 $dis 米"
        }
        Log.i(TAG, "${tag.poiID}")

        updateTagPoint(tag)
        getPOIDetail(p)
    }

    private fun updateTagPoint(tagLatLngData: TagLatLngData) {

        tagPointVM.tagPOI.postValue(tagLatLngData)

    }

    private fun getPOIDetail(p: Poi) {
        // 创建POI详情搜索对象
        val query = PoiSearchV2.Query(p.name, "", "")
        query.location = LatLonPoint(p.coordinate.latitude, p.coordinate.latitude)
        query.isDistanceSort = false
        query.pageNum = 1
        query.pageSize = 1
        val poiSearch = PoiSearchV2(requireContext(), query)

        poiSearch.setOnPoiSearchListener(object : PoiSearchV2.OnPoiSearchListener {


            override fun onPoiSearched(p0: PoiResultV2?, p1: Int) {

                if (p1 == 1000 && p0 != null) {

                    p0.pois.find {
                        it.poiId == p.poiId
                    }?.let { poiItemV2 ->

                        val tag = TagLatLngData()
                        tag.name = poiItemV2.provinceName
                        tag.poiID = poiItemV2.poiId
                        tag.latLng = BaseLocation(
                            poiItemV2.latLonPoint.latitude,
                            poiItemV2.latLonPoint.longitude
                        )
                        tag.city = poiItemV2.cityName
                        tag.address = poiItemV2.adName

                        aMap?.let { map ->
                            val location = map.myLocation
                            val dis = AMapUtils.calculateLineDistance(
                                LatLng(
                                    location.latitude,
                                    location.longitude
                                ), p.coordinate
                            )

                            tag.disdanceMessage = "距离 $dis 米"
                        }


                        updateTagPoint(tag)
                    }

                    return
                }

                Log.e(TAG, "onPoiSearched(${p0?.toString()},code:${p1})")

            }

            override fun onPoiItemSearched(p0: PoiItemV2?, p1: Int) {


            }
        })

        poiSearch.searchPOIId(p.poiId)


    }

    override fun onResume() {
        super.onResume()
        binding?.map?.onResume()
    }

    override fun onDestroyView() {

        aMap = null
        binding?.map?.onDestroy()

        binding = null

        navHelperAction.onDestroy()
        super.onDestroyView()

    }

    override fun onPause() {
        super.onPause()
        binding?.map?.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding?.map?.onSaveInstanceState(outState)
    }


    override fun mapReadychecker() {
//        TODO("Not yet implemented")

        initMap()
    }

    override fun getPresionLocation(): BaseLocation {

        aMap?.myLocation?.let {
            return BaseLocation(it.latitude, it.longitude)
        }

        lastLocation?.let {
            return BaseLocation(it.latitude, it.longitude)
        }

        return BaseLocation(0.0, 0.0)
    }

    override fun initMap() {
        MapsInitializer.updatePrivacyShow(requireContext(), true, true)  // 展示隐私合规对话框
        MapsInitializer.updatePrivacyAgree(requireContext(), true)      // 同意隐私合规政策
        MapsInitializer.initialize(requireContext())
        // 初始化高德地图
        // 初始化高德地图
        AMapLocationClient.updatePrivacyShow(requireContext(), true, true)
        AMapLocationClient.updatePrivacyAgree(requireContext(), true)
        // 搜索服务初始化
        // 搜索服务初始化


    }

    override fun setCenter(location: BaseLocation) {
        val latLng = LatLng(location.getLat(), location.getLng()) // 例如：北京天安门的位置
// 使用CameraUpdateFactory来改变地图的中心点和缩放级别。这里我们只改变中心点。
// 使用CameraUpdateFactory来改变地图的中心点和缩放级别。这里我们只改变中心点。
        Log.i(TAG, "move${latLng}")
        aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f)) // 第二个参数是缩放级别，可以根据需要调整。
    }

    override fun getMapCenter(): BaseLocation {

        val tageLocation = aMap?.cameraPosition?.target ?: return BaseLocation(0.0, 0.0)

        return BaseLocation(tageLocation.latitude, tageLocation.longitude)
    }

    override fun showCurrentLocation() {
        aMap?.let {
            val currentLocation = it.myLocation
            val tagLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
            it.moveCamera(CameraUpdateFactory.newLatLng(tagLatLng))
        }
    }

    override fun navDiverLocation(location: BaseLocation) {
        aMap?.clear()
        val sLocation = getPresionLocation()

        navHelperAction.calculateDriveRoute(
            NaviLatLng(sLocation.getLat(), sLocation.getLng()),
            NaviLatLng(location.getLat(), location.getLng())
        )
    }

    override fun navRiderLocation(location: BaseLocation) {
        aMap?.clear()
        val sLocation = getPresionLocation()

        navHelperAction.calculateRideRoute(
            NaviLatLng(sLocation.getLat(), sLocation.getLng()),
            NaviLatLng(location.getLat(), location.getLng())
        )
    }

    override fun navWalkLocation(location: BaseLocation) {
        aMap?.clear()
        val sLocation = getPresionLocation()

        navHelperAction.calculateWalkRoute(
            NaviLatLng(sLocation.getLat(), sLocation.getLng()),
            NaviLatLng(location.getLat(), location.getLng())
        )
    }

    override fun startSelectMapNavLocation() {
        navHelperAction.startNav()
    }

    override fun startSelectMapNavLocationFormEmulator() {
        navHelperAction.startNavFormEmulator()
    }

    override fun activate(p0: LocationSource.OnLocationChangedListener?) {

        mListener = p0
    }

    override fun deactivate() {

        mListener = null
    }

    private var lastLocationChangeTime = -1L

    override fun onLocationChanged(p0: AMapLocation?) {


        p0?.let {

            if ((System.currentTimeMillis() - lastLocationChangeTime) > 1500) {
                lastLocationChangeTime = System.currentTimeMillis()
                Log.i(TAG, it.toString())
                lastLocation = it
                mListener?.onLocationChanged(it)
            } else
                return

        }

    }


}