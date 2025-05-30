package com.killetom.dev.map

import android.content.Context
import android.util.Log
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.amap.api.navi.AMapNavi
import com.amap.api.navi.AMapNaviListener
import com.amap.api.navi.NaviSetting
import com.amap.api.navi.ParallelRoadListener
import com.amap.api.navi.enums.AMapNaviParallelRoadStatus
import com.amap.api.navi.enums.NaviType
import com.amap.api.navi.model.*


class NavHelperAction : AMapNaviListener, ParallelRoadListener {



    private var naviService: AMapNavi? = null
    private var recordPoints: ArrayList<AMapNaviLocation> = arrayListOf()
    private var navStatus = false
    private var calculateStatus = -1
    private var navActionType = -1
    var requireLocation: (() -> AMapLocation?)? = null

     val navWalkAction = 0
     val navRiderAction = 1
     val navDriveAction = 2

    private var calculateReady = -1
    private val calculatingAction = 0
    private val calculateFaireAction = 1
    private val calculateDoneAction = 2

    var calculateStatusCall: ((Int) -> Unit)? = null
    var planeRouteCall: ((List<LatLng>) -> Unit)? = null
    var updateNavInfoCall: ((totalDistance: Long, currentDistance: Long, currentNavTime: Long, speed: Float) -> Unit)? =
        null
    var updateActualNavgationRouter: ((List<LatLng>) -> Unit)? = null
    var navStartCall: (() -> Unit)? = null
    var updateNavIconMarke: ((AMapNaviLocation)->Unit)?=null

    private var startNavTime = -1L

    fun getNavStatus(): Boolean = navStatus

    fun getNavActionType():Int = navActionType

    fun init(context: Context) {
        try {
            NaviSetting.updatePrivacyShow(context, true, true)
            NaviSetting.updatePrivacyAgree(context, true)
            naviService = AMapNavi.getInstance(context)
            naviService?.isNaviTravelView = true
            naviService?.addAMapNaviListener(this)
            naviService?.addParallelRoadListener(this)
            naviService?.setUseInnerVoice(true, true)
        } catch (_: Exception) {
        }
    }

    fun calculateWalkRoute(s: NaviLatLng, e: NaviLatLng) {
        calculateStatus = calculatingAction
        navActionType = navWalkAction
        calculateStatusCall?.invoke(calculateStatus)
        naviService?.calculateWalkRoute(s, e)
    }

    fun calculateRideRoute(s: NaviLatLng, e: NaviLatLng) {
        calculateStatus = calculatingAction
        navActionType = navRiderAction
        calculateStatusCall?.invoke(calculateStatus)
        naviService?.calculateRideRoute(s, e)
    }

    fun calculateDriveRoute(s: NaviLatLng, e: NaviLatLng) {
        calculateStatus = calculatingAction
        navActionType = navDriveAction
        calculateStatusCall?.invoke(calculateStatus)
        var strategy = 0
        try {
            //再次强调，最后一个参数为true时代表多路径，否则代表单路径
            naviService?.let {
                strategy = it.strategyConvert(true, false, false, false, false)
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        naviService?.calculateDriveRoute(listOf(s), listOf(e), emptyList(), strategy)
    }

    fun startNav() {
        if (calculateStatus == calculateDoneAction) naviService?.startNavi(NaviType.GPS)
    }

    fun onDestroy() {
        naviService?.stopNavi()
        AMapNavi.destroy()
    }


    override fun onInitNaviFailure() {

    }

    override fun onInitNaviSuccess() {
        calculateStatus = calculateReady
    }

    override fun onStartNavi(p0: Int) {
        navStatus = true
        startNavTime = System.currentTimeMillis()
        lastLocationTime = startNavTime - 1200L
        requireLocation?.invoke()?.let {
            recordPoints.clear()
        }
        navStartCall?.invoke()
    }

    override fun onTrafficStatusUpdate() {

    }

//    fun onLocationChange(location: AMapLocation?) {
//        if (navStatus) {
//            location?.let { locate ->
//
//                if (recordPoints.size == 1) {
//                    recordPoints.add(locate)
//                    return
//                }
//
//                val lastPoint = recordPoints.last()
//                val dis = AMapUtils.calculateLineDistance(
//                    LatLng(lastPoint.latitude, lastPoint.longitude),
//                    LatLng(locate.latitude, locate.longitude),
//                )
//                when (navWalkAction) {
//                    navRiderAction -> {
//                        if (dis >= 2) recordPoints.add(locate)
//                    }
//                    navDriveAction -> {
//                        if (dis >= 2.5) recordPoints.add(locate)
//                    }
//                    navWalkAction -> {
//                        if (dis >= 0.5) recordPoints.add(locate)
//                    }
//                }
//            }
//        }
//    }

    private var lastLocationTime = -1L
    override fun onLocationChange(location: AMapNaviLocation?) {
        if (navStatus && location != null) {

            if (recordPoints.isEmpty()) {
                lastLocationTime = System.currentTimeMillis()
                recordPoints.add(location)
                return
            }

            val currentTime = System.currentTimeMillis()
            val timeInvalid = currentTime - lastLocationTime

            if (timeInvalid <= 1200)
                return

            val lastPoint = recordPoints.last()

            Log.i("nav", "${location.toString()}")
            Log.i("nav", "${location.coord.toString()}")
            val dis = AMapUtils.calculateLineDistance(
                LatLng(lastPoint.coord.latitude, lastPoint.coord.latitude),
                LatLng(location.coord.latitude, location.coord.latitude)
            )
            when (navWalkAction) {
                navRiderAction -> {
                    if (dis >= 2) recordPoints.add(location)
                }
                navDriveAction -> {
                    if (dis >= 2.5) recordPoints.add(location)
                }
                navWalkAction -> {
                    if (dis >= 0.5) recordPoints.add(location)
                }
            }
            lastLocationTime = System.currentTimeMillis()

            callUpdateOverlay()

            updateNavIconMarke?.invoke(location)
        }

//        Log.i("${this@NavHelperAction::class.java.name}","${location?.toString()}")
    }


    private fun callUpdateOverlay() {


        if (recordPoints.isEmpty()) {
            return
        }


        val data = recordPoints.map { point ->
            LatLng(point.coord.latitude, point.coord.longitude)
        }


        updateActualNavgationRouter?.invoke(data)


    }

    override fun onGetNavigationText(p0: Int, p1: String?) {

    }

    @Deprecated("Deprecated in Java")
    override fun onGetNavigationText(p0: String?) {

    }

    override fun onEndEmulatorNavi() {
        navStatus = false
    }

    override fun onArriveDestination() {
        navStatus = false

    }

    @Deprecated("Deprecated in Java")
    override fun onCalculateRouteFailure(p0: Int) {
        calculateStatus = calculateFaireAction
        calculateStatusCall?.invoke(calculateStatus)
    }

    override fun onCalculateRouteFailure(p0: AMapCalcRouteResult?) {
        calculateStatus = calculateFaireAction
        calculateStatusCall?.invoke(calculateStatus)
    }

    override fun onReCalculateRouteForYaw() {

    }

    override fun onReCalculateRouteForTrafficJam() {

    }

    override fun onArrivedWayPoint(p0: Int) {

    }

    override fun onGpsOpenStatus(p0: Boolean) {

    }


    override fun onNaviInfoUpdate(info: NaviInfo?) {

        val length = naviService?.naviPath?.allLength ?: return
        val dis = info?.curStepRetainDistance ?: return
        val time = (System.currentTimeMillis() - startNavTime) / 1000

        val speed = if (recordPoints.isEmpty()) {
            0.0.toFloat()
        } else {
            recordPoints.last().speed
        }

        updateNavInfoCall?.invoke(length.toLong(), dis.toLong(), time, speed)
    }

    override fun updateCameraInfo(p0: Array<out AMapNaviCameraInfo>?) {

    }

    override fun updateIntervalCameraInfo(
        p0: AMapNaviCameraInfo?, p1: AMapNaviCameraInfo?, p2: Int
    ) {

    }

    override fun onServiceAreaUpdate(p0: Array<out AMapServiceAreaInfo>?) {

    }

    override fun showCross(p0: AMapNaviCross?) {

    }

    override fun hideCross() {

    }

    override fun showModeCross(p0: AMapModelCross?) {

    }

    override fun hideModeCross() {

    }

    @Deprecated("Deprecated in Java")
    override fun showLaneInfo(p0: Array<out AMapLaneInfo>?, p1: ByteArray?, p2: ByteArray?) {

    }

    override fun showLaneInfo(p0: AMapLaneInfo?) {

    }

    override fun hideLaneInfo() {

    }

    @Deprecated("Deprecated in Java")
    override fun onCalculateRouteSuccess(p0: IntArray?) {

    }

    override fun onCalculateRouteSuccess(result: AMapCalcRouteResult?) {

        calculateStatus = calculateDoneAction
        calculateStatusCall?.invoke(calculateStatus)

        val service = naviService ?: return
        if (result == null) return
        val paths = service.naviPaths.values
        if (paths.isEmpty()) return
        val path = paths.first()
        val routes = path.coordList.map { LatLng(it.latitude, it.longitude) }
        Log.i("xx", "sss")
        planeRouteCall?.invoke(routes)


    }

    @Deprecated("Deprecated in Java")
    override fun notifyParallelRoad(p0: Int) {

    }

    @Deprecated("Deprecated in Java")
    override fun OnUpdateTrafficFacility(p0: Array<out AMapNaviTrafficFacilityInfo>?) {

    }

    @Deprecated("Deprecated in Java")
    override fun OnUpdateTrafficFacility(p0: AMapNaviTrafficFacilityInfo?) {

    }

    @Deprecated("Deprecated in Java")
    override fun updateAimlessModeStatistics(p0: AimLessModeStat?) {

    }

    @Deprecated("Deprecated in Java")
    override fun updateAimlessModeCongestionInfo(p0: AimLessModeCongestionInfo?) {

    }

    override fun onPlayRing(p0: Int) {

    }

    override fun onNaviRouteNotify(p0: AMapNaviRouteNotifyData?) {

    }

    override fun onGpsSignalWeak(p0: Boolean) {

    }

    override fun notifyParallelRoad(p0: AMapNaviParallelRoadStatus?) {

    }

    fun startNavFormEmulator() {
        when (navActionType) {
            navWalkAction -> {
                naviService?.setEmulatorNaviSpeed(6)
            }
            navDriveAction -> {
                naviService?.setEmulatorNaviSpeed(55)
            }
            navRiderAction -> {
                naviService?.setEmulatorNaviSpeed(18)
            }
            else -> {
                naviService?.setEmulatorNaviSpeed(55)
            }
        }
        if (calculateStatus == calculateDoneAction) {
            startNavTime = System.currentTimeMillis()
            naviService?.startNavi(NaviType.EMULATOR)
        }
    }
}